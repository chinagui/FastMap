package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class Operation implements IOperation {

	private Check check;

	private Command command;

	private Connection conn;

	private LineSegment inLinkSegment;

	/**
	 * key为退出线pid，value为退出线线段
	 */
	private Map<Integer, LineSegment> outLinkSegmentMap = new HashMap<Integer, LineSegment>();;

	/**
	 * key为退出线pid， value为经过线pid列表
	 */
	private Map<Integer, List<Integer>> viaLinkPidMap = new HashMap<Integer, List<Integer>>();;

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

		int meshId = new RdLinkSelector(conn).loadById(command.getInLinkPid(),
				true).mesh();

		RdRestriction restrict = new RdRestriction();

		restrict.setMesh(meshId);

		restrict.setPid(PidUtil.getInstance().applyRestrictionPid());

		result.setPrimaryPid(restrict.getPid());

		int inNodePid = command.getNodePid();

		int inLinkPid = command.getInLinkPid();

		restrict.setInLinkPid(inLinkPid);

		restrict.setNodePid(inNodePid);

		List<Integer> outLinkPids = command.getOutLinkPids();

		boolean hasSelectOutLink = true;

		if (CollectionUtils.isEmpty(outLinkPids)) {

			hasSelectOutLink = false;

			// 计算退出线，只算直接和进入点联通的线，不算经过线
			outLinkPids = calOutLinkPids();
		}
		// 如果和进入点没有联通的线，提示未计算出退出线，暂时不计算经过线
		if (CollectionUtils.isEmpty(outLinkPids)) {
			throw new Exception("未计算出退出线，请手动指定退出线");
		}
		
		for(Integer outLinkPid : outLinkPids)
		{
			List<Integer> outLinkIdList = new ArrayList<>();
			
			outLinkIdList.add(outLinkPid);
			
			// 计算经过线
			this.calViaLinks(inLinkPid, inNodePid, outLinkIdList);
		}
		
		// 检查
		Set<Integer> pids = new HashSet<Integer>();
		pids.add(command.getInLinkPid());
		for (Integer pid : outLinkPids) {
			pids.add(pid);

			List<Integer> viaLinkPids = viaLinkPidMap.get(pid);

			for (Integer viapid : viaLinkPids) {
				pids.add(viapid);
			}
		}
		check.checkGLM01017(conn, pids);

		// 生成Detial对象信息
		List<IRow> details = new ArrayList<IRow>();

		String[] infArray = command.getRestricInfos().split(",");

		List<Integer> infoList = new ArrayList<>();

		for (String info : infArray) {
			if (info.contains("[")) {
				// 理论值带[]
				infoList.add(Integer.parseInt(info.substring(1, 2)));
			} else {
				// 实际值不带
				infoList.add(Integer.parseInt(info));
			}
		}
		// 删除某一交限方向的多个退出link，选取正北或者正南方向夹角最小的
		if (CollectionUtils.isNotEmpty(outLinkPids) && !hasSelectOutLink) {
			deleteMultLinkOnSameDir(outLinkPids, infoList);
		}
		
		check.checkSameInAndOutLink(inLinkPid, outLinkPids);
		// 根据方向确定完真实的退出线，没有提示手动指定
		if (CollectionUtils.isEmpty(outLinkPids)) {
			throw new Exception("未计算出退出线，请手动指定退出线");
		}

		details.addAll(createDetail(restrict, outLinkPids, infoList, infArray,
				hasSelectOutLink));

		restrict.setDetails(details);

		restrict.setRestricInfo(command.getRestricInfos());

		result.insertObject(restrict, ObjStatus.INSERT, restrict.pid());

		return null;
	}

	/**
	 * 删除同一交限方向的重复线，只留下正北或者正南夹角最小的线
	 * 
	 * @param outLinkPids
	 *            退出线
	 * @param infoList
	 *            交限信息
	 */
	private void deleteMultLinkOnSameDir(List<Integer> outLinkPids,
			List<Integer> infoList) {

		// map结构：外层key：restricInfo 内层Map：内层key：angle，内存value outLinkPid
		Map<Integer, Map<Double, Integer>> resAngleLinkMap = new HashMap<>();

		List<Integer> resultOutLinkPids = new ArrayList<>();

		resultOutLinkPids.addAll(outLinkPids);

		for (Integer outPid : resultOutLinkPids) {
			LineSegment outLinkSegment = outLinkSegmentMap.get(outPid);

			if (outLinkSegment != null) {
				// 获取线的夹角
				double angle = AngleCalculator.getAngle(inLinkSegment,
						outLinkSegment);
				// 计算交限信息
				int restricInfo = this.calRestricInfo(angle);

				if (infoList.contains(restricInfo)) {
					// map中只保存夹角最小的交和对应的退出线
					Map<Double, Integer> angleMap = resAngleLinkMap
							.get(restricInfo);
					if (angleMap == null) {
						Map<Double, Integer> angleLinkMap = new HashMap<>();

						angleLinkMap.put(angle, outPid);

						resAngleLinkMap.put(restricInfo, angleLinkMap);
					} else {
						double tmpAngle = angleMap.keySet().iterator().next();

						if (angle >= tmpAngle) {
							outLinkPids.remove(outLinkPids.indexOf(outPid));
						} else if (angle < tmpAngle) {
							angleMap.put(angle, angleMap.values().iterator()
									.next());
						}

					}

				} else {
					// 线不在交限的方向内的删除
					outLinkPids.remove(outLinkPids.indexOf(outPid));
				}
			}

		}
	}

	/**
	 * 计算进入点联通的线
	 * 
	 * @return
	 */
	private List<Integer> calOutLinkPids() {
		RdLinkSelector selector = new RdLinkSelector(conn);

		List<Integer> outLinkList = new ArrayList<>();
		try {
			List<RdLink> iRows = selector.loadByNodePid(command.getNodePid(),
					false);

			if (CollectionUtils.isNotEmpty(iRows)) {
				for (RdLink link : iRows) {
					outLinkList.add(link.getPid());
				}
				// 剔除进入线，防止进入线和退出线是一条线
				if (outLinkList.contains(command.getInLinkPid())) {
					outLinkList.remove(outLinkList.indexOf(command
							.getInLinkPid()));
				}
			}
		} catch (Exception e) {
		}
		return outLinkList;
	}

	/**
	 * 计算经过线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPids
	 * @throws Exception
	 */
	private void calViaLinks(int inLinkPid, int nodePid,
			List<Integer> outLinkPids) throws Exception {

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

					Coordinate p1 = new Coordinate(Double.valueOf(splits[0]),
							Double.valueOf(splits[1]));

					splits = inNode2.split(",");

					Coordinate p2 = new Coordinate(Double.valueOf(splits[0]),
							Double.valueOf(splits[1]));

					inLinkSegment = new LineSegment(p1, p2);
				}

				int outLinkPid = resultSet.getInt("link_pid");

				int relationType = resultSet.getInt("relation_type");

				relationTypeMap.put(outLinkPid, relationType);

				String outNode1 = resultSet.getString("out_node1");

				String outNode2 = resultSet.getString("out_node2");

				String[] splits = outNode1.split(",");

				Coordinate p1 = new Coordinate(Double.valueOf(splits[0]),
						Double.valueOf(splits[1]));

				splits = outNode2.split(",");

				Coordinate p2 = new Coordinate(Double.valueOf(splits[0]),
						Double.valueOf(splits[1]));

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
	 * @param outLinkPids
	 * @param infoList
	 * @param infArray
	 * @param hasSelectOutLink
	 * @return
	 * @throws Exception
	 */
	public List<RdRestrictionDetail> createDetail(RdRestriction restrict,
			List<Integer> outLinkPids, List<Integer> infoList,
			String[] infArray, boolean hasSelectOutLink) throws Exception {

		List<RdRestrictionDetail> details = new ArrayList<>();

		for (int i = 0; i < outLinkPids.size(); i++) {

			int outLinkPid = outLinkPids.get(i);

			RdRestrictionDetail detail = new RdRestrictionDetail();

			detail.setOutLinkPid(outLinkPid);

			LineSegment outLinkSegment = outLinkSegmentMap.get(detail
					.getOutLinkPid());

			double angle = AngleCalculator.getAngle(inLinkSegment,
					outLinkSegment);

			int restricInfo = this.calRestricInfo(angle);

			if (hasSelectOutLink) {
				detail.setMesh(restrict.mesh());

				detail.setPid(PidUtil.getInstance().applyRestrictionDetailPid());

				detail.setRestricPid(restrict.getPid());

				if (!infArray[i].contains("[")) {
					detail.setFlag(1);
				}

				detail.setRestricInfo(infoList.get(i));

				detail.setRelationshipType(relationTypeMap.get(detail
						.getOutLinkPid()));

				if (detail.getRelationshipType() == 1) {
					check.checkGLM26017(conn, command.getNodePid());

					check.checkGLM08033(conn, command.getInLinkPid(),
							outLinkPid);
				}

				List<Integer> viaLinkPids = viaLinkPidMap.get(detail
						.getOutLinkPid());

				int seqNum = 1;

				List<IRow> vias = new ArrayList<IRow>();

				if (CollectionUtils.isNotEmpty(viaLinkPids) && detail.getRelationshipType() !=1) {
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
			} else {
				if (infoList.contains(restricInfo)) {
					detail.setMesh(restrict.mesh());

					detail.setPid(PidUtil.getInstance()
							.applyRestrictionDetailPid());

					detail.setRestricPid(restrict.getPid());

					detail.setRestricInfo(restricInfo);
					
					if (!infArray[i].contains("[")) {
						detail.setFlag(1);
					}

					detail.setRelationshipType(relationTypeMap.get(detail
							.getOutLinkPid()));

					if (detail.getRelationshipType() == 1) {
						check.checkGLM26017(conn, command.getNodePid());

						check.checkGLM08033(conn, command.getInLinkPid(),
								outLinkPid);
					}

					List<Integer> viaLinkPids = viaLinkPidMap.get(detail
							.getOutLinkPid());

					int seqNum = 1;

					List<IRow> vias = new ArrayList<IRow>();

					if (CollectionUtils.isNotEmpty(viaLinkPids) && detail.getRelationshipType() !=1) {
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

					infoList.remove(infoList.indexOf(restricInfo));
				}
			}
		}

		return details;
	}
}
