package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete;

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
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;

public class Operation implements IOperation {

	private RdRestriction restrict;
	
	private Connection conn;

	public Operation()
	{
	}
	
	public Operation(Connection conn)
	{
		this.conn = conn;
	}
	
	public Operation(Command command, RdRestriction restrict) {
		this.restrict = restrict;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(restrict, ObjStatus.DELETE, restrict.pid());

		return null;
	}
	
	/**
	 * 删除link维护车信
	 * 
	 * @param linkPidList
	 * @param result
	 * @throws Exception
	 */
	public void deleteRdRestrictionByLink(List<Integer> linkPidList, Result result) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		List<RdRestrictionDetail> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除车信本身
		deleteLanesMap.putAll(getDeleteInLinkRdRest(linkPidList));
		// 2.link作为退出线，删除该Link会对应删除此组关系
		deleteLanesMap.putAll(getDeleteOutLinkRest(linkPidList, deleteDetailLanesList));
		// 3.link作为经过线，删除该link会对应删除次组关系
		deleteLanesMap.putAll(getDeleteViaLinkRest(linkPidList, deleteDetailLanesList));
		
		for(RdRestriction restriction : deleteLanesMap.values())
		{
			result.insertObject(restriction, ObjStatus.DELETE, restriction.getPid());
		}
		
		for(RdRestrictionDetail detial : deleteDetailLanesList)
		{
			result.insertObject(detial, ObjStatus.DELETE, detial.getRestricPid());
		}
	}
	
	private Map<Integer, RdRestriction> getDeleteInLinkRdRest(List<Integer> linkPidList) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();
		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdRestriction> inLinkLanes = selector.loadByLink(linkPid, 1, true);

			for (RdRestriction restriction : inLinkLanes) {
				deleteLanesMap.put(restriction.getPid(), restriction);
			}
		}
		return deleteLanesMap;
	}

	private Map<Integer, RdRestriction> getDeleteOutLinkRest(List<Integer> linkPidList,
			List<RdRestrictionDetail> deleteDetailList) throws Exception {

		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		for (Integer linkPid : linkPidList) {
			List<RdRestriction> outLinkLanes = selector.loadByLink(linkPid, 2, true);

			for (RdRestriction restriction : outLinkLanes) {
				if (!deleteLanesMap.containsKey(restriction.getPid())) {
					List<Integer> allTopoLinks = new ArrayList<>();

					List<IRow> rows = restriction.getDetails();

					RdRestrictionDetail detail = null;

					for (IRow row : rows) {
						RdRestrictionDetail topo = (RdRestrictionDetail) row;

						if (topo.getOutLinkPid() == linkPid) {
							detail = topo;
						}

						allTopoLinks.add(topo.getOutLinkPid());
					}
					if (allTopoLinks.containsAll(linkPidList)) {
						deleteLanesMap.put(restriction.getPid(), restriction);
					} else if(detail != null){
						deleteDetailList.add(detail);
					}
				}
			}

		}

		return deleteLanesMap;
	}

	private Map<Integer, RdRestriction> getDeleteViaLinkRest(List<Integer> linkPidList,
			List<RdRestrictionDetail> deleteDetailList) throws Exception {
		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		for (Integer linkPid : linkPidList) {
			List<RdRestriction> viaLinkLanes = selector.loadByLink(linkPid, 3, true);

			for (RdRestriction restriction : viaLinkLanes) {
				if (!deleteLanesMap.containsKey(restriction.getPid())) {
					List<Integer> allTopoLinks = new ArrayList<>();
					
					List<Integer> delTopoLinks = new ArrayList<>();

					List<IRow> rows = restriction.getDetails();

					if (rows.size() == 1) {
						deleteLanesMap.put(restriction.getPid(), restriction);
					} else {
						List<RdRestrictionDetail> updateDetailList = new ArrayList<>();
						
						for (IRow row : rows) {
							RdRestrictionDetail topo = (RdRestrictionDetail) row;
							
							allTopoLinks.add(topo.getOutLinkPid());
							
							List<IRow> vias = topo.getVias();

							for (IRow viaRow : vias) {
								RdRestrictionVia via = (RdRestrictionVia) viaRow;

								if (via.getLinkPid() == linkPid) {
									delTopoLinks.add(topo.getOutLinkPid());
									
									updateDetailList.add(topo);
								}
							}
						}
						if (delTopoLinks.containsAll(allTopoLinks)) {
							deleteLanesMap.put(restriction.getPid(), restriction);
						} else{
							deleteDetailList.addAll(updateDetailList);
						}
					}
				}
			}
		}
		
		return deleteLanesMap;
	}
	
	/**
	 * 删除link对交限的更新影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateResInfectData(List<Integer> linkPidList) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		List<RdRestrictionDetail> deleteDetailLanesList = new ArrayList<>();

		// 2.link作为退出线，删除该Link会对应删除此组关系
		deleteLanesMap.putAll(getDeleteOutLinkRest(linkPidList, deleteDetailLanesList));
		// 3.link作为经过线，删除该link会对应删除次组关系
		deleteLanesMap.putAll(getDeleteViaLinkRest(linkPidList, deleteDetailLanesList));

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestrictionDetail detail : deleteDetailLanesList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(ObjType.RDRESTRICTION);

			alertObj.setPid(detail.getRestricPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除link对交限的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteLinkResInfectData(List<Integer> linkPidList) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		List<RdRestrictionDetail> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除车信本身
		deleteLanesMap.putAll(getDeleteInLinkRdRest(linkPidList));
		// 2.link作为退出线，删除该Link会对应删除此组关系
		deleteLanesMap.putAll(getDeleteOutLinkRest(linkPidList, deleteDetailLanesList));
		// 3.link作为经过线，删除该link会对应删除次组关系
		deleteLanesMap.putAll(getDeleteViaLinkRest(linkPidList, deleteDetailLanesList));

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : deleteLanesMap.values()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除路口对交限的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossRestrictInfectData(List<RdRestriction> restrictions) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction restriction : restrictions) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(restriction.objType());

			alertObj.setPid(restriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
