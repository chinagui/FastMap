package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;

public class Operation implements IOperation {

	private RdLaneConnexity lane;

	private Connection conn;

	public Operation() {
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, RdLaneConnexity lane) {
		this.lane = lane;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(lane, ObjStatus.DELETE, lane.pid());

		return null;
	}

	/**
	 * 删除link维护车信
	 * 
	 * @param linkPidList
	 * @param result
	 * @throws Exception
	 */
	public void deleteRdLaneByLink(List<Integer> linkPidList, Result result) throws Exception {
		Map<Integer, RdLaneConnexity> deleteLanesMap = new HashMap<>();

		List<RdLaneTopology> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除车信本身
		getDeleteInLinkRdLane(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);

		for (RdLaneConnexity rdLane : deleteLanesMap.values()) {
			result.insertObject(rdLane, ObjStatus.DELETE, rdLane.getPid());
		}

		for (RdLaneTopology topo : deleteDetailLanesList) {
			//可能通过经过线算出来某车信已经需要删除，该车信的组不需要单独处理
			if(!deleteLanesMap.containsKey(topo.getConnexityPid()))
			{
				result.insertObject(topo, ObjStatus.DELETE, topo.getConnexityPid());
			}
		}
	}

	private void getDeleteInLinkRdLane(List<Integer> linkPidList, Map<Integer, RdLaneConnexity> deleteLanesMap) throws Exception {
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdLaneConnexity> inLinkLanes = selector.loadByLink(linkPid, 1, true);

			for (RdLaneConnexity lane : inLinkLanes) {
				deleteLanesMap.put(lane.getPid(), lane);
			}
		}
	}

	private void getDeleteOutLinkLane(List<Integer> linkPidList,
			List<RdLaneTopology> deleteDetailLanesList, Map<Integer, RdLaneConnexity> deleteInLinkLanesMap) throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdLaneConnexity> outLinkLanes = selector.loadByLink(linkPid, 2, true);

			for (RdLaneConnexity lane : outLinkLanes) {
				if (!deleteInLinkLanesMap.containsKey(lane.getPid())) {
					List<Integer> allTopoLinks = new ArrayList<>();
					
					List<Integer> delTopoLinks = new ArrayList<>();
					
					List<IRow> rows = lane.getTopos();

					RdLaneTopology delTopogy = null;

					for (IRow row : rows) {
						RdLaneTopology topo = (RdLaneTopology) row;
						
						allTopoLinks.add(topo.getOutLinkPid());
						
						if (linkPidList.contains(topo.getOutLinkPid())) {
							delTopogy = topo;
							delTopoLinks.add(topo.getOutLinkPid());
						}
					}
					if (delTopoLinks.containsAll(allTopoLinks)) {
						deleteInLinkLanesMap.put(lane.getPid(), lane);
					} else if (delTopogy != null) {
						deleteDetailLanesList.add(delTopogy);
					}
				}
			}

		}
	}

	private void getDeleteViaLinkLane(List<Integer> linkPidList,
			List<RdLaneTopology> deleteDetailLanesList, Map<Integer, RdLaneConnexity> deleteInLinkLanesMap) throws Exception {
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdLaneConnexity> viaLinkLanes = selector.loadByLink(linkPid, 3, true);

			for (RdLaneConnexity lane : viaLinkLanes) {
				if (!deleteInLinkLanesMap.containsKey(lane.getPid())) {
					List<Integer> allTopoLinks = new ArrayList<>();

					List<Integer> delTopoLinks = new ArrayList<>();

					List<IRow> rows = lane.getTopos();

					if (rows.size() == 1) {
						deleteInLinkLanesMap.put(lane.getPid(), lane);
					} else {
						List<RdLaneTopology> updateLaneTopoList = new ArrayList<>();

						for (IRow row : rows) {

							RdLaneTopology topo = (RdLaneTopology) row;
							
							allTopoLinks.add(topo.getOutLinkPid());
							
							List<IRow> vias = topo.getVias();

							for (IRow viaRow : vias) {
								RdLaneVia via = (RdLaneVia) viaRow;

								if (via.getLinkPid() == linkPid) {
									delTopoLinks.add(topo.getOutLinkPid());

									updateLaneTopoList.add(topo);
								}
							}
						}
						if (delTopoLinks.containsAll(allTopoLinks)) {
							deleteInLinkLanesMap.put(lane.getPid(), lane);
						} else {
							deleteDetailLanesList.addAll(updateLaneTopoList);
						}
					}
				}
			}
		}
	}

	/**
	 * 删除link对车信的更新影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateResInfectData(List<Integer> linkPidList) throws Exception {
		
		Map<Integer, RdLaneConnexity> deleteLanesMap = new HashMap<>();
		
		List<RdLaneTopology> deleteDetailLanesList = new ArrayList<>();
		//1.进入线的车信
		getDeleteInLinkRdLane(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneTopology topo : deleteDetailLanesList) {
			
			if(!deleteLanesMap.containsKey(topo.getConnexityPid()))
			{
				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(ObjType.RDLANECONNEXITY);

				alertObj.setPid(topo.getConnexityPid());

				alertObj.setStatus(ObjStatus.UPDATE);

				if(!alertList.contains(alertObj))
				{
					alertList.add(alertObj);
				}
			}
		}

		return alertList;
	}

	/**
	 * 删除link对车信的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdLaneConnexityInfectData(List<Integer> linkPidList) throws Exception {

		Map<Integer, RdLaneConnexity> deleteLanesMap = new HashMap<>();

		List<RdLaneTopology> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除车信本身
		getDeleteInLinkRdLane(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkLane(linkPidList, deleteDetailLanesList,deleteLanesMap);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : deleteLanesMap.values()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除路口对车信的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossRdlaneConInfectData(List<RdLaneConnexity> rdLaneConnexities)
			throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : rdLaneConnexities) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 根据link数组 维护RdLaneConnexity
	 * @param linkPids
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLinks(List<Integer> linkPids,Result result) throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		List<RdLaneConnexity> storageTmp = selector.loadByLinks(linkPids, 1, true);

		storageTmp.addAll(selector.loadByLinks(linkPids, 2, true));

		storageTmp.addAll(selector.loadByLinks(linkPids, 3, true));

		Map<Integer, RdLaneConnexity> storage = new HashMap<>();

		for (RdLaneConnexity laneConnexity : storageTmp) {

			storage.put(laneConnexity.getPid(), laneConnexity);
		}

		for (RdLaneConnexity laneConnexity : storage.values()) {

			//被删link作为进入线，删除RdLaneConnexity
			if (linkPids.contains(laneConnexity.getInLinkPid())) {

				result.insertObject(laneConnexity, ObjStatus.DELETE, laneConnexity.getPid());

				continue;
			}

			List<RdLaneTopology> delTopo = new ArrayList<>();

			for (IRow rowTopo : laneConnexity.getTopos()) {

				RdLaneTopology topo = (RdLaneTopology) rowTopo;

				//被删link作为退出线，删除该Link会对应删除此组关系
				if (linkPids.contains(topo.getOutLinkPid())) {

					delTopo.add(topo);

					continue;
				}

				for (IRow rowVia : topo.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					//被删link作为经过线，删除该link会对应删除次组关系
					if (linkPids.contains(via.getLinkPid())) {

						delTopo.add(topo);

						break;
					}
				}
			}

			//退出线全被删除，删除RdLaneConnexity
			if (delTopo.size() == laneConnexity.getTopos().size()) {

				result.insertObject(laneConnexity, ObjStatus.DELETE, laneConnexity.getPid());

				break;
			}

			for (RdLaneTopology topo : delTopo) {

				result.insertObject(topo, ObjStatus.DELETE, topo.getConnexityPid());
			}
		}
	}
}
