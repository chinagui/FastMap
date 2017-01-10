package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Check check;

	private Command command;

	private Connection conn;

	private LineSegment inLinkSegment;

	/**
	 * key为退出线pid，value为退出线线段
	 */
	private Map<Integer, LineSegment> outLinkSegmentMap = new HashMap<Integer, LineSegment>();

	/**
	 * key为退出线pid， value为经过线pid列表
	 */
	private Map<Integer, List<Integer>> viaLinkPidMap = new HashMap<Integer, List<Integer>>();

	/**
	 * key为退出线pid，value为交限类型
	 */
	private Map<Integer, Integer> relationTypeMap = new HashMap<Integer, Integer>();

	public Operation(Command command, Connection conn, Check check) {
		this.command = command;

		this.conn = conn;

		this.check = check;
	}

	@Override
	public String run(Result result) throws Exception {

		int meshId = new RdLinkSelector(conn).loadById(command.getInLinkPid(), true).mesh();

		RdRestriction restrict = new RdRestriction();

		restrict.setMesh(meshId);

		restrict.setPid(PidUtil.getInstance().applyRestrictionPid());

		result.setPrimaryPid(restrict.getPid());

		int inNodePid = command.getNodePid();

		int inLinkPid = command.getInLinkPid();

		restrict.setInLinkPid(inLinkPid);

		restrict.setNodePid(inNodePid);

		// 处理需呀自动计算退出线的交限
		JSONArray calOutLinkObjs = command.getCalOutLinkObjs();

		List<IRow> details = new ArrayList<>();

		if (calOutLinkObjs.size() > 0) {
			List<Integer> outLinkPids = getInNodeLinkPids();

			if (CollectionUtils.isEmpty(outLinkPids)) {
				throw new Exception("进入点挂接的link没有合适的退出线");
			} else {
				this.calViaLinks(inLinkPid, inNodePid, outLinkPids);

				Map<Integer, String> infoMap = new HashMap<>();

				for (int i = 0; i < calOutLinkObjs.size(); i++) {
					JSONObject calObj = calOutLinkObjs.getJSONObject(i);

					String arrow = calObj.getString("arrow");

					// 进入点挂接link删除已经作为限制方向的退出线，防止重复计算
					outLinkPids.removeAll(infoMap.keySet());

					// 选取正北或者正南方向夹角最小的退出线
					int outLinkPid = getMinAngleOutLinkPidOnArrowDir(outLinkPids, calIntInfo(arrow), infoMap);

					if (outLinkPid == 0) {
						throw new Exception("交限限制信息为:" + arrow + "未自动计算出退出线，创建交限失败，请手动指定退出线");
					} else {
						infoMap.put(outLinkPid, arrow);
					}
				}

				check.checkSameInAndOutLink(inLinkPid, infoMap);
				// 限制方向数量和退出线数量不一致则判定为未计算出退出线
				if (infoMap.size() != calOutLinkObjs.size()) {
					throw new Exception("未计算出退出线，请手动指定退出线");
				}
				// 计算交限详细信息
				details.addAll(createDetail(restrict, infoMap));
			}
		}

		// 处理不需要自动计算退出线的交限
		JSONArray outLinkObjs = command.getOutLinkObjs();

		if (outLinkObjs.size() > 0) {

			this.calViaLinks(inLinkPid, inNodePid, this.command.getOutLinkPidList());

			Map<Integer, String> infoMap = new HashMap<>();

			for (int i = 0; i < outLinkObjs.size(); i++) {
				JSONObject calObj = outLinkObjs.getJSONObject(i);

				int linkPid = calObj.getInt("outLinkPid");

				String arrow = calObj.getString("arrow");

				infoMap.put(linkPid, arrow);
			}

			check.checkSameInAndOutLink(inLinkPid, infoMap);

			// 计算交限详细信息
			for (int i = 0; i < calOutLinkObjs.size(); i++) {
				details.addAll(createDetail(restrict, infoMap));
			}
		}

		restrict.setDetails(details);

		restrict.setRestricInfo(command.getRestricInfos());

		result.insertObject(restrict, ObjStatus.INSERT, restrict.pid());

		return null;
	}

	/**
	 * 获取最小夹角的退出线
	 *
	 * @param outLinkPids
	 *            退出线
	 * @param infoMap
	 * @param infoList
	 *            交限信息
	 */
	private int getMinAngleOutLinkPidOnArrowDir(List<Integer> outLinkPids, int arrow, Map<Integer, String> infoMap) {
		// 最小夹角对应的退出线
		int minAngleOutLinkPid = 0;

		// 最小夹角
		double temAngle = 361;

		List<Integer> resultOutLinkPids = new ArrayList<>();

		resultOutLinkPids.addAll(outLinkPids);

		for (Integer outPid : resultOutLinkPids) {
			LineSegment outLinkSegment = outLinkSegmentMap.get(outPid);

			if (outLinkSegment != null) {
				// 获取线的夹角
				double angle = AngleCalculator.getAngle(inLinkSegment, outLinkSegment);
				// 计算交限信息
				int restricInfo = this.calRestricInfo(angle);

				if (arrow == restricInfo) {
					// link计算的夹角比上个link的夹角小的替换最小夹角和对应的linkPid
					if (angle < temAngle) {

						temAngle = angle;

						minAngleOutLinkPid = outPid;
					}
				}
			}

		}

		return minAngleOutLinkPid;
	}

	/**
	 * 计算进入点联通的线(排除进入线和已经选择该link作为退出线的)
	 *
	 * @return
	 */
	private List<Integer> getInNodeLinkPids() {
		RdLinkSelector selector = new RdLinkSelector(conn);

		List<Integer> linkPids = new ArrayList<>();

		try {
			linkPids = selector.loadLinkPidByNodePid(command.getNodePid(), true);

			if (CollectionUtils.isNotEmpty(linkPids)) {
				// 剔除进入线，防止进入线和退出线是一条线
				if (linkPids.contains(command.getInLinkPid())) {
					linkPids.remove(linkPids.indexOf(command.getInLinkPid()));
				}

				// 删除已经作为指定方向的退出线
				linkPids.removeAll(command.getOutLinkPidList());
			}
		} catch (Exception e) {
		}
		return linkPids;
	}

	/**
	 * 计算经过线
	 *
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPids
	 * @throws Exception
	 */
	private void calViaLinks(int inLinkPid, int nodePid, List<Integer> outLinkPids) throws Exception {

		String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			StringBuilder sb = new StringBuilder();

			for (int pid : outLinkPids) {

				sb.append(pid);

				sb.append(",");
			}

			sb.deleteCharAt(sb.length() - 1);

			pstmt.setString(3, sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				if (inLinkSegment == null) {
					String inNode1 = resultSet.getString("in_node1");

					String inNode2 = resultSet.getString("in_node2");

					String[] splits = inNode1.split(",");

					Coordinate p1 = new Coordinate(Double.valueOf(splits[0]), Double.valueOf(splits[1]));

					splits = inNode2.split(",");

					Coordinate p2 = new Coordinate(Double.valueOf(splits[0]), Double.valueOf(splits[1]));

					inLinkSegment = new LineSegment(p1, p2);
				}

				int outLinkPid = resultSet.getInt("link_pid");

				int relationType = resultSet.getInt("relation_type");

				relationTypeMap.put(outLinkPid, relationType);

				String outNode1 = resultSet.getString("out_node1");

				String outNode2 = resultSet.getString("out_node2");

				String[] splits = outNode1.split(",");

				Coordinate p1 = new Coordinate(Double.valueOf(splits[0]), Double.valueOf(splits[1]));

				splits = outNode2.split(",");

				Coordinate p2 = new Coordinate(Double.valueOf(splits[0]), Double.valueOf(splits[1]));

				LineSegment line = new LineSegment(p1, p2);

				outLinkSegmentMap.put(outLinkPid, line);

				String viaPath = resultSet.getString("via_path");

				List<Integer> viaLinks = new ArrayList<Integer>();

				if (viaPath != null) {

					splits = viaPath.split(",");

					for (String s : splits) {
						if (!s.equals("")) {

							int viaPid = Integer.valueOf(s);

							if (viaPid == outLinkPid || viaPid == inLinkPid) {
								continue;
							}

							viaLinks.add(viaPid);
						}
					}

				}

				viaLinkPidMap.put(outLinkPid, viaLinks);
			}

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
			}

		}

	}

	/**
	 * 计算限制信息
	 *
	 * @param angle
	 * @return
	 */
	private int calRestricInfo(double angle) {
		if (angle > 45 && angle <= 135) {
			return 3;
		} else if (angle > 135 && angle <= 225) {
			return 4;
		} else if (angle > 225 && angle <= 315) {
			return 2;
		} else {
			return 1;
		}

	}

	/**
	 * 创建Detial对象
	 *
	 * @param restrict
	 * @param infoMap
	 * @return
	 * @throws Exception
	 */
	public List<RdRestrictionDetail> createDetail(RdRestriction restrict, Map<Integer, String> infoMap)
			throws Exception {

		List<RdRestrictionDetail> details = new ArrayList<>();

		for (Map.Entry<Integer, String> entry : infoMap.entrySet()) {

			int outLinkPid = entry.getKey();

			String info = entry.getValue();

			RdRestrictionDetail detail = new RdRestrictionDetail();

			detail.setOutLinkPid(outLinkPid);

			// LineSegment outLinkSegment =
			// outLinkSegmentMap.get(detail.getOutLinkPid());
			//
			// double angle = AngleCalculator.getAngle(inLinkSegment,
			// outLinkSegment);
			//
			// int restricInfo = this.calRestricInfo(angle);

			if (info.contains("[")) {
				detail.setFlag(1);
			} else {
				detail.setFlag(2);
			}

			detail.setMesh(restrict.mesh());

			detail.setPid(PidUtil.getInstance().applyRestrictionDetailPid());

			detail.setRestricPid(restrict.getPid());

			detail.setRestricInfo(calIntInfo(info));

			detail.setRelationshipType(relationTypeMap.get(detail.getOutLinkPid()));

			if (detail.getRelationshipType() == 1) {
				check.checkGLM26017(conn, command.getNodePid());

				check.checkGLM08033(conn, command.getInLinkPid(), outLinkPid);
			}

			List<Integer> viaLinkPids = viaLinkPidMap.get(detail.getOutLinkPid());

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();

			if (CollectionUtils.isNotEmpty(viaLinkPids) && detail.getRelationshipType() != 1) {
				for (Integer viaLinkPid : viaLinkPids) {

					RdRestrictionVia via = new RdRestrictionVia();

					via.setMesh(restrict.mesh());

					via.setDetailId(detail.getPid());

					via.setSeqNum(seqNum);

					via.setLinkPid(viaLinkPid);

					vias.add(via);

					seqNum++;
				}
			}

			detail.setVias(vias);

			details.add(detail);

			// 创建交限时间段和车辆限制信息
			if (command.getRestricType() == 1) {
				detail.setConditions(createRdRestrictionConditions(detail));
			}
		}
		return details;
	}

	private List<IRow> createRdRestrictionConditions(RdRestrictionDetail detail) {
		List<IRow> conditions = new ArrayList<>();
		RdRestrictionCondition condition = new RdRestrictionCondition();
		condition.setDetailId(detail.pid());
		condition.setVehicle(4);
		conditions.add(condition);
		return conditions;
	}

	/**
	 * 计算箭头的限制信息
	 * 
	 * @param arrow
	 * @param infoMap
	 */
	private int calIntInfo(String arrow) {
		if (arrow.contains("[")) {
			// 理论值带[]
			return Integer.parseInt(arrow.substring(1, 2));
		} else {
			// 实际值不带
			return Integer.parseInt(arrow);
		}
	}
}
