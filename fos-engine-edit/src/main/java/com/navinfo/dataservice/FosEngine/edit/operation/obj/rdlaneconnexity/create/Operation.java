package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlaneconnexity.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.operation.Helper;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.service.PidService;
import com.vividsolutions.jts.geom.LineSegment;

public class Operation implements IOperation {

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

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdLaneConnexity lane = new RdLaneConnexity();

		lane.setPid(PidService.getInstance().applyLaneConnexityPid());

		result.setPrimaryPid(lane.getPid());

		lane.setInLinkPid(command.getInLinkPid());

		lane.setNodePid(command.getNodePid());

		lane.setLaneInfo(command.getLaneInfo());

		List<Integer> outLinkPids = command.getOutLinkPids();

		Helper.calViaLinks(conn, command.getInLinkPid(), command.getNodePid(),
				outLinkPids, inLinkSegment, outLinkSegmentMap, viaLinkPidMap,
				relationTypeMap);

		List<IRow> topos = new ArrayList<IRow>();

		for (int outLinkPid : outLinkPids) {

			RdLaneTopology topo = new RdLaneTopology();

			topo.setPid(PidService.getInstance().applyLaneTopologyPid());

			topo.setConnexityPid(lane.getPid());

			topo.setOutLinkPid(outLinkPid);

			LineSegment outLinkSegment = outLinkSegmentMap.get(topo
					.getOutLinkPid());

			double angle = AngleCalculator.getAngle(inLinkSegment,
					outLinkSegment);

			int reachDir = Helper.calRestricInfo(angle);

			topo.setReachDir(reachDir);

			topo.setRelationshipType(relationTypeMap.get(topo.getOutLinkPid()));

			List<Integer> viaLinkPids = viaLinkPidMap.get(topo.getOutLinkPid());

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();

			for (Integer viaLinkPid : viaLinkPids) {

				RdLaneVia via = new RdLaneVia();

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

		result.insertObject(lane, ObjStatus.INSERT);

		return null;
	}

	/**
	 * 
	 * @param laneInfo
	 * @return
	 */
	public void splitLaneInfo(String laneInfo, List<Integer> dirs,
			List<Integer> index) {

		String[] splits = laneInfo.split(",");

		for (int i = 0; i < splits.length; i++) {

			String split = splits[i];

			if (split.startsWith("[")) {
				split = split.substring(1, split.length() - 1);
			}

			if (split.contains("<")) {

				String first = split.substring(0, 1);
				String second = split.substring(2, 3);

				List<Integer> result = splitDir(first);

				for (Integer dir : result) {
					dirs.add(dir);
					index.add(i);
				}

				result = splitDir(second);

				for (Integer dir : result) {
					dirs.add(dir);
					index.add(i);
				}
			} else {
				List<Integer> result = splitDir(split);

				for (Integer dir : result) {
					dirs.add(dir);
					index.add(i);
				}
			}
		}

	}

	public List<Integer> splitDir(String lane) {

		List<Integer> result = new ArrayList<Integer>();
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

	public static int calDir(double angle) {
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
