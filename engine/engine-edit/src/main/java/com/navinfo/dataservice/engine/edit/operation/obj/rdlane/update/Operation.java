package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.druid.sql.visitor.functions.Char;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.LineSegment;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhaokk 修改车道信息
 */
public class Operation implements IOperation {

	private Command command;
	
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}
	
	public Operation(Command command, Connection conn) {
		
		this.command = command;
		
		this.conn = conn;		
	}

	public Operation() {
	}

	@Override
	public String run(Result result) throws Exception {
		this.updateRdLane(result);
		return null;
	}

	/***
	 * 修改车道信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateRdLane(Result result) throws Exception {

		this.updateRdLane(result, command.getContent(),
				this.command.getRdLane());
	}

	public void updateRdLane(Result result, JSONObject content, RdLane rdLane)
			throws Exception {

			boolean isChanged = rdLane.fillChangeFields(
					content);

			if (isChanged) {
				result.insertObject(rdLane, ObjStatus.UPDATE, rdLane.getPid());
			}

			if (content.containsKey("conditions")) {
				this.updateCondition(result, rdLane,
						content.getJSONArray("conditions"));
		}
	}

	/***
	 * 字表详细车道的时间段和车辆限制表修改
	 * 
	 * @param result
	 * @param jsonArray
	 * @throws Exception
	 */
	private void updateCondition(Result result, RdLane rdLane,
			JSONArray jsonArray) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = jsonArray.iterator();
		JSONObject jsonCondition = null;
		RdLaneCondition condition = null;
		while (iterator.hasNext()) {
			jsonCondition = iterator.next();
			if (jsonCondition.containsKey("objStatus")) {
				String objStatus = jsonCondition.getString("objStatus");
				condition = rdLane.conditionMap.get(jsonCondition
						.getString("rowId"));
				if (condition == null) {
					throw new Exception("rowId="
							+ jsonCondition.getString("rowId")
							+ "的RdLaneCondition不存在");
				}
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = condition
							.fillChangeFields(jsonCondition);
					if (isChange) {
						result.insertObject(condition, ObjStatus.UPDATE,
								rdLane.getPid());
					}
				}
			}
		}

	}
	
	public Map<ObjStatus, List<RdLane>> autoHandleByRdBranch(RdBranch branch,String patternCode,
			Result result) throws Exception {

		Map<ObjStatus, List<RdLane>> rdLaneInfo = new HashMap<ObjStatus, List<RdLane>>();

		rdLaneInfo = handleRdLane(branch, patternCode, result);

		return rdLaneInfo;
	}

	private Map<ObjStatus, List<RdLane>> handleRdLane(RdBranch targetBranch, String patternCode, Result result) throws Exception {

		Map<ObjStatus, List<RdLane>> rdLaneInfo = new HashMap<ObjStatus, List<RdLane>>();
		
		RdBranchSelector rdBranchSelector = new RdBranchSelector(conn);

		List<RdBranch> rdBranchs = rdBranchSelector.loadRdBranchByLinkNode(
				targetBranch.getInLinkPid(), targetBranch.getNodePid(), true);
		
		if(patternCode.length()<8)
		{
			return rdLaneInfo;
		}

		String position78 = patternCode.substring(6, 8);

		List<Integer> linkPids = new ArrayList<Integer>();

		for (RdBranch branch : rdBranchs) {

			if (!linkPids.contains(branch.getOutLinkPid())) {
				
				linkPids.add(branch.getOutLinkPid());
			}
		}
		
		List<Integer> outLaneCount = getOutLaneCount(position78);

		if (outLaneCount.size() != linkPids.size()) {
			
			return rdLaneInfo;
		}
		
		RdLinkSelector RdLinkSelector = new RdLinkSelector(conn);
		
		RdLink inLink = (RdLink) RdLinkSelector.loadById(
				targetBranch.getInLinkPid(), true, true);
		
		int inCount = Integer.parseInt(patternCode.substring(4,5));

		updateLane(inLink, targetBranch.getNodePid(), inCount, result);

		List<IRow> linkRows = RdLinkSelector.loadByIds(linkPids, true, false);
		
		LineSegment inLineSegment = getLineSegment(inLink,
				targetBranch.getNodePid());

		TreeMap<Double, RdLink> outMap = new TreeMap<Double, RdLink>();

		for (IRow row : linkRows) {

			RdLink link = (RdLink) row;

			LineSegment outLineSegment = getLineSegment(link,
					targetBranch.getNodePid());

			double angle = AngleCalculator.getConnectLinksAngle(inLineSegment,
					outLineSegment);

			outMap.put(angle, link);
		}

		int index = 0;

		for (Map.Entry<Double, RdLink> entry : outMap.entrySet()) {

			RdLink link = entry.getValue();
			
			int directNodePid = link.getsNodePid() == link.getsNodePid() ? link
					.geteNodePid() : link.getsNodePid();
					
			Map<ObjStatus, List<RdLane>> info = updateLane(link,
					directNodePid, outLaneCount.get(index++), result);

			for (Map.Entry<ObjStatus, List<RdLane>> infoEntry : info.entrySet()) {

				if (rdLaneInfo.containsKey(infoEntry.getKey())) {

					rdLaneInfo.get(infoEntry.getKey()).addAll(
							infoEntry.getValue());
				} else {
					rdLaneInfo.put(infoEntry.getKey(), infoEntry.getValue());
				}
			}
		}
		
		return rdLaneInfo;
	}
	
	private Map<ObjStatus, List<RdLane>> updateLane(RdLink link,
			int directNodePid, int laneNum, Result result) throws Exception {
		
		Map<ObjStatus, List<RdLane>> info = new HashMap<ObjStatus, List<RdLane>>();

		if (link == null || laneNum == 0) {

			return info;
		}

		int direct = link.getDirect();

		if (link.getDirect() == 1 || link.getDirect() == 0) {
			
			direct = link.getsNodePid() == directNodePid ? 3 : 2;
		}

		RdLaneSelector rdLaneSelector = new RdLaneSelector(this.conn);

		List<RdLane> rdLanes = rdLaneSelector.loadByLink(link.getPid(), direct,
				true);

		for (RdLane lane : rdLanes) {

			if (lane.getSeqNum() > laneNum) {

				result.insertObject(lane, ObjStatus.DELETE, lane.getPid());
				
				if (!info.containsKey(ObjStatus.DELETE)) {
					info.put(ObjStatus.DELETE, new ArrayList<RdLane>());
				}
				info.get(ObjStatus.DELETE).add(lane);

				continue;
			}

			lane.changedFields().put("laneNum", laneNum);

			result.insertObject(lane, ObjStatus.UPDATE, lane.getPid());
			
			if (!info.containsKey(ObjStatus.UPDATE)) {
				info.put(ObjStatus.UPDATE, new ArrayList<RdLane>());
			}
			
			info.get(ObjStatus.UPDATE).add(lane);
		}

		int srcFlag = rdLanes.size() > 0 ? rdLanes.get(0).getSrcFlag() : 1;

		for (int i = 0; i < laneNum - rdLanes.size(); i++) {

			RdLane lane = new RdLane();

			// 申请PId
			int lanePid = PidUtil.getInstance().applyRdLanePid();
			lane.setPid(lanePid);
			lane.setLinkPid(link.getPid());
			// 车道总数
			lane.setLaneNum(laneNum);
			// 车道方向
			lane.setLaneDir(1);
			if (link.getDirect() == 1 || link.getDirect() == 0) {
				lane.setLaneDir(direct);
			}
			// 车道序号
			lane.setSeqNum(rdLanes.size() + 1 + i);
			// 车道来源
			lane.setSrcFlag(srcFlag);

			result.insertObject(lane, ObjStatus.INSERT, lane.getPid());
			
			if (!info.containsKey(ObjStatus.INSERT)) {
				info.put(ObjStatus.INSERT, new ArrayList<RdLane>());
			}
			info.get(ObjStatus.INSERT).add(lane);
		}

		return info;
	}

	private List<Integer> getOutLaneCount(String position78) {

		List<Integer> outLaneCount = new ArrayList<Integer>();

		switch (position78) {

		case "09":
			outLaneCount.add(1);
			outLaneCount.add(1);
			break;
		case "0a":
			outLaneCount.add(1);
			outLaneCount.add(2);
			break;
		case "0b":
			outLaneCount.add(1);
			outLaneCount.add(3);
			break;
		case "12":
			outLaneCount.add(2);
			outLaneCount.add(2);
			break;
		case "0c":
			outLaneCount.add(1);
			outLaneCount.add(4);
			break;
		case "11":
			outLaneCount.add(2);
			outLaneCount.add(1);
			break;
		case "19":
			outLaneCount.add(3);
			outLaneCount.add(1);
			break;
		case "21":
			outLaneCount.add(4);
			outLaneCount.add(1);
			break;
		case "06":
			outLaneCount.add(2);
			outLaneCount.add(0);
			break;
		case "01":
			outLaneCount.add(1);
			outLaneCount.add(1);
			outLaneCount.add(2);
			break;
		case "00":
			outLaneCount.add(4);
			outLaneCount.add(0);
			break;
		case "02":
			outLaneCount.add(3);
			outLaneCount.add(2);
			break;
		case "22":
			outLaneCount.add(4);
			outLaneCount.add(2);
			break;
		case "03":
			outLaneCount.add(5);
			outLaneCount.add(0);
			break;
		case "04":
			outLaneCount.add(3);
			outLaneCount.add(0);
			break;
		case "05":
			outLaneCount.add(2);
			outLaneCount.add(3);
			break;
		case "24":
			outLaneCount.add(2);
			outLaneCount.add(4);
			break;
		case "33":
			outLaneCount.add(3);
			outLaneCount.add(3);
			break;
		case "52":
			outLaneCount.add(5);
			outLaneCount.add(2);
			break;
		case "51":
			outLaneCount.add(5);
			outLaneCount.add(1);
			break;
		case "43":
			outLaneCount.add(4);
			outLaneCount.add(3);
			break;
		case "61":
			outLaneCount.add(6);
			outLaneCount.add(1);
			break;
		case "14":
			outLaneCount.add(3);
			outLaneCount.add(2);
			outLaneCount.add(1);
			break;
		default:
			break;
		}

		return outLaneCount;
	}

	/**
	 * 获取link指定端点处的直线几何
	 * 
	 * @param link
	 *            线
	 * @param nodePidDir
	 *            指定端点
	 * @return 以指定端点为起点的直线几何
	 */
	private LineSegment getLineSegment(RdLink link, int nodePidDir) {
		LineSegment lineSegment = null;
		if (link.getsNodePid() == nodePidDir) {
			lineSegment = new LineSegment(
					link.getGeometry().getCoordinates()[0], link.getGeometry()
							.getCoordinates()[1]);
		}
		if (link.geteNodePid() == nodePidDir) {
			lineSegment = new LineSegment(
					link.getGeometry().getCoordinates()[link.getGeometry()
							.getCoordinates().length - 2], link.getGeometry()
							.getCoordinates()[link.getGeometry()
							.getCoordinates().length - 1]);
		}
		return lineSegment;
	}

}
