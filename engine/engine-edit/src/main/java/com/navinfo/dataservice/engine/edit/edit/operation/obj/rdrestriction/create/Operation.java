package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
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
	private Map<Integer, LineSegment> outLinkSegmentMap;

	/**
	 * key为退出线pid， value为经过线pid列表
	 */
	private Map<Integer, List<Integer>> viaLinkPidMap;

	/**
	 * key为退出线pid，value为交限类型
	 */
	private Map<Integer, Integer> relationTypeMap;

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

		restrict.setPid(PidService.getInstance().applyRestrictionPid());

		result.setPrimaryPid(restrict.getPid());

		restrict.setInLinkPid(command.getInLinkPid());

		restrict.setNodePid(command.getNodePid());

		List<Integer> outLinkPids = command.getOutLinkPids();

		this.calViaLinks(command.getInLinkPid(), command.getNodePid(), outLinkPids);

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

		for (int outLinkPid : outLinkPids) {

			RdRestrictionDetail detail = new RdRestrictionDetail();

			detail.setOutLinkPid(outLinkPid);

			LineSegment outLinkSegment = outLinkSegmentMap.get(detail.getOutLinkPid());

			double angle = AngleCalculator.getAngle(inLinkSegment, outLinkSegment);

			int restricInfo = this.calRestricInfo(angle);

			if (infoList.contains(restricInfo)) {
				detail.setMesh(meshId);

				detail.setPid(PidService.getInstance().applyRestrictionDetailPid());

				detail.setRestricPid(restrict.getPid());

				detail.setRestricInfo(restricInfo);

				detail.setRelationshipType(relationTypeMap.get(detail.getOutLinkPid()));

				if (detail.getRelationshipType() == 1) {
					check.checkGLM26017(conn, command.getNodePid());

					check.checkGLM08033(conn, command.getInLinkPid(), outLinkPid);
				}

				List<Integer> viaLinkPids = viaLinkPidMap.get(detail.getOutLinkPid());

				int seqNum = 1;

				List<IRow> vias = new ArrayList<IRow>();

				for (Integer viaLinkPid : viaLinkPids) {

					RdRestrictionVia via = new RdRestrictionVia();

					via.setMesh(meshId);

					via.setDetailId(detail.getPid());

					via.setSeqNum(seqNum);

					via.setLinkPid(viaLinkPid);

					vias.add(via);

					seqNum++;
				}

				detail.setVias(vias);

				details.add(detail);

				infoList.remove(infoList.indexOf(restricInfo));
			}

		}

		for (Integer info : infoList) {
			RdRestrictionDetail detail = new RdRestrictionDetail();

			detail.setOutLinkPid(0);

			detail.setMesh(meshId);

			detail.setPid(PidService.getInstance().applyRestrictionDetailPid());

			detail.setRestricPid(restrict.getPid());

			detail.setRestricInfo(info);
			
			if(command.getOutLinkPids().size()>0)
			{
				detail.setRelationshipType(relationTypeMap.get(command.getOutLinkPids().get(0)));
			}

			details.add(detail);
		}
		restrict.setDetails(details);

		restrict.setRestricInfo(command.getRestricInfos());

		result.insertObject(restrict, ObjStatus.INSERT, restrict.pid());

		return null;
	}

	/**
	 * 计算经过线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPids
	 * @throws Exception
	 */
	private void calViaLinks(int inLinkPid, int nodePid, List<Integer> outLinkPids)
			throws Exception {

		outLinkSegmentMap = new HashMap<Integer, LineSegment>();

		viaLinkPidMap = new HashMap<Integer, List<Integer>>();

		relationTypeMap = new HashMap<Integer, Integer>();

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
}
