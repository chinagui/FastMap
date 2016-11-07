package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class Operation implements IOperation {

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

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		int meshId = new RdLinkSelector(conn).loadById(command.getInLinkPid(),
				true).mesh();

		RdLaneConnexity lane = new RdLaneConnexity();

		lane.setMesh(meshId);

		lane.setPid(PidUtil.getInstance().applyLaneConnexityPid());

		result.setPrimaryPid(lane.getPid());

		lane.setInLinkPid(command.getInLinkPid());

		lane.setNodePid(command.getNodePid());

		lane.setLaneInfo(command.getLaneInfo());

		List<Integer> outLinkPids = command.getOutLinkPids();
		
		for(Integer outLink : outLinkPids)
		{
			List<Integer> outLinkList = new ArrayList<>();
			
			outLinkList.add(outLink);
			
			this.calViaLinks(conn, command.getInLinkPid(), command.getNodePid(),
					outLinkList);
		}

		List<Set<Integer>> dirs = new ArrayList<Set<Integer>>();

		Map<Integer, Set<Integer>> busdirs = new HashMap<Integer, Set<Integer>>();

		//1:车道总数；2：左附加车道数；3：有附加车道数
		int[] laneNunInfo = new int[3];

		this.splitLaneInfo(command.getLaneInfo(), dirs, busdirs, laneNunInfo);
		
		lane.setLaneNum(laneNunInfo[0]);
		
		lane.setLeftExtend(laneNunInfo[1]);
		
		lane.setRightExtend(laneNunInfo[2]);

		List<IRow> topos = new ArrayList<IRow>();

		for (int outLinkPid : outLinkPids) {

			RdLaneTopology topo = new RdLaneTopology();

			topo.setMesh(meshId);

			topo.setPid(PidUtil.getInstance().applyLaneTopologyPid());

			topo.setConnexityPid(lane.getPid());

			topo.setOutLinkPid(outLinkPid);

			LineSegment outLinkSegment = outLinkSegmentMap.get(topo
					.getOutLinkPid());

			double angle = AngleCalculator.getAngle(inLinkSegment,
					outLinkSegment);

			int reachDir = this.calDir(angle);

			topo.setReachDir(reachDir);

			int inLaneInfo = this.calInLaneInfo(reachDir, dirs);

			topo.setInLaneInfo(inLaneInfo);

			int busLaneInfo = this.calBusLaneInfo(reachDir, busdirs,
					dirs.size());

			topo.setBusLaneInfo(busLaneInfo);

			topo.setRelationshipType(relationTypeMap.get(topo.getOutLinkPid()));

			List<Integer> viaLinkPids = viaLinkPidMap.get(topo.getOutLinkPid());

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();

			for (Integer viaLinkPid : viaLinkPids) {

				RdLaneVia via = new RdLaneVia();

				via.setMesh(meshId);

				via.setTopologyId(topo.getPid());

				via.setSeqNum(seqNum);

				via.setLinkPid(viaLinkPid);

				vias.add(via);

				seqNum++;
			}

			topo.setVias(vias);

			topos.add(topo);

		}

		lane.setTopos(topos);

		result.insertObject(lane, ObjStatus.INSERT, lane.pid());

		return null;
	}

	private int calInLaneInfo(int reachDir, List<Set<Integer>> dirs) {

		int inLaneInfo = 0;

		int size = dirs.size();
		
		for (int i = 0; i < size; i++) {
			
			Set<Integer> set = dirs.get(i);

			if (set.contains(reachDir)) {

				inLaneInfo += Math.pow(2, size - 1 - i);
			}
		}
		inLaneInfo = inLaneInfo << (16 - size); // 向 左补位至16位
		return inLaneInfo;
	}

	private int calBusLaneInfo(int reachDir,
			Map<Integer, Set<Integer>> busdirs, int size) {

		int busLaneInfo = 0;

		Iterator iter = busdirs.entrySet().iterator();

		while (iter.hasNext()) {

			Map.Entry entry = (Map.Entry) iter.next();

			int index = (int) entry.getKey();

			Set<Integer> val = (Set<Integer>) entry.getValue();

			busLaneInfo += Math.pow(2, size - 1 - index);

		}
		busLaneInfo = busLaneInfo << (16 - size); // 向 左补位至16位
		
		return busLaneInfo;
	}

	private int calDir(double angle) {
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
	 * 
	 * @param laneInfo
	 * @return
	 */
	private void splitLaneInfo(String laneInfo, List<Set<Integer>> dirs,
			Map<Integer, Set<Integer>> busdirs, int[] laneNunInfo) {

		int leftAddCount = 0;

		int rightAddCount = 0;
		
		String[] splits = laneInfo.split(",");		
	
		//计算左附加车道数
		for (int i = 0; i < splits.length; i++) {

			String split = splits[i];

			if (split.startsWith("[")) {

				leftAddCount += 1;

			} else {
				//不是附加车道
				break;
			}
		}

		//计算右附加车道数
		for (int i = splits.length - 1; i >= 0; i--) {

			String split = splits[i];

			if (split.startsWith("[")) {

				rightAddCount += 1;

			} else {
				//不是附加车道
				break;
			}
		}

		if (leftAddCount == splits.length && leftAddCount != 0) {

			if (splits.length % 2 == 1) {

				leftAddCount = (splits.length - 1) / 2 + 1;

				rightAddCount = (splits.length - 1) / 2;

			} else {

				leftAddCount = splits.length / 2;

				rightAddCount = splits.length / 2;
			}
		}

		for (int i = 0; i < splits.length; i++) {

			String split = splits[i];	

			if (split.contains("<")) {

				String first = split.substring(0, 1);
				
				String second = split.substring(2, 3);

				Set<Integer> set = splitDir(first);

				dirs.add(set);

				Set<Integer> busset = splitDir(second);

				busdirs.put(i, busset);

			} else {
				Set<Integer> set = splitDir(split);

				dirs.add(set);
			}
		}
		
		laneNunInfo[0] = splits.length;
		
		laneNunInfo[1] = leftAddCount;
		
		laneNunInfo[2] = rightAddCount;
	}

	private Set<Integer> splitDir(String lane) {

		Set<Integer> result = new HashSet<Integer>();
		switch (lane) {
		case "a":
			result.add(1);
			break;
		case "b":
			result.add(2);
			break;
		case "c":
			result.add(3);
			break;
		case "d":
			result.add(4);
			break;
		case "e":
			result.add(1);
			result.add(4);
			break;
		case "f":
			result.add(1);
			result.add(3);
			break;
		case "g":
			result.add(1);
			result.add(2);
			break;
		case "h":
			result.add(1);
			result.add(2);
			result.add(3);
			break;
		case "i":
			result.add(1);
			result.add(3);
			result.add(4);
			break;
		case "j":
			result.add(1);
			result.add(2);
			result.add(4);
			break;
		case "k":
			result.add(2);
			result.add(3);
			break;
		case "l":
			result.add(2);
			result.add(4);
			break;
		case "m":
			result.add(2);
			result.add(3);
			result.add(4);
			break;
		case "n":
			result.add(3);
			result.add(4);
			break;
		case "o":
			result.add(0);
			break;
		case "p":
			result.add(1);
			result.add(2);
			result.add(3);
			result.add(4);
			break;
		case "r":
			result.add(5);
			break;
		case "s":
			result.add(6);
			break;
		case "t":
			result.add(1);
			result.add(5);
			break;
		case "u":
			result.add(2);
			result.add(5);
			break;
		case "v":
			result.add(3);
			result.add(5);
			break;
		case "w":
			result.add(4);
			result.add(5);
			break;
		case "x":
			result.add(1);
			result.add(6);
			break;
		case "y":
			result.add(2);
			result.add(6);
			break;
		case "z":
			result.add(3);
			result.add(6);
			break;
		case "0":
			result.add(4);
			result.add(6);
			break;
		case "1":
			result.add(5);
			result.add(6);
			break;
		case "2":
			result.add(1);
			result.add(2);
			result.add(5);
			break;
		case "3":
			result.add(1);
			result.add(2);
			result.add(6);
			break;
		case "4":
			result.add(1);
			result.add(3);
			result.add(5);
			break;
		case "5":
			result.add(1);
			result.add(3);
			result.add(5);
			break;
		}

		return result;
	}

	/**
	 * 计算经过线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPids
	 * @throws Exception
	 */
	private void calViaLinks(Connection conn, int inLinkPid, int nodePid,
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
							
							if(viaPid == inLinkPid || viaPid == outLinkPid){
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
}
