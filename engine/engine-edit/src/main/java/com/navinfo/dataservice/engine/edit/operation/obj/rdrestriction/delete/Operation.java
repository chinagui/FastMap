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
	 * 删除link维护交限
	 * 
	 * @param linkPidList
	 * @param result
	 * @throws Exception
	 */
	public void deleteRdRestrictionByLink(List<Integer> linkPidList, Result result) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		List<RdRestrictionDetail> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除交限本身
		getDeleteInLinkRdRest(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);
		
		for(RdRestriction restriction : deleteLanesMap.values())
		{
			result.insertObject(restriction, ObjStatus.DELETE, restriction.getPid());
		}
		
		for(RdRestrictionDetail detail : deleteDetailLanesList)
		{
			//可能通过经过线算出来某交限已经需要删除，该交限的组不需要单独处理
			if(!deleteLanesMap.containsKey(detail.getRestricPid()))
			{
				result.insertObject(detail, ObjStatus.DELETE, detail.getRestricPid());
			}
		}
		
		setRestricInfo(deleteDetailLanesList,  result);
	}
	
	private void getDeleteInLinkRdRest(List<Integer> linkPidList,Map<Integer, RdRestriction> deleteLanesMap) throws Exception {
		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdRestriction> inLinkLanes = selector.loadByLink(linkPid, 1, true);

			for (RdRestriction restriction : inLinkLanes) {
				deleteLanesMap.put(restriction.getPid(), restriction);
			}
		}
	}

	private void getDeleteOutLinkRest(List<Integer> linkPidList,
			List<RdRestrictionDetail> deleteDetailList,Map<Integer, RdRestriction> deleteLanesMap) throws Exception {

		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		for (Integer linkPid : linkPidList) {
			List<RdRestriction> outLinkLanes = selector.loadByLink(linkPid, 2, true);

			for (RdRestriction restriction : outLinkLanes) {
				if (!deleteLanesMap.containsKey(restriction.getPid())) {
					List<Integer> allTopoLinks = new ArrayList<>();
					
					List<Integer> delTopoLinks = new ArrayList<>();
					
					List<IRow> rows = restriction.getDetails();

					RdRestrictionDetail detail = null;

					for (IRow row : rows) {
						RdRestrictionDetail topo = (RdRestrictionDetail) row;
						
						allTopoLinks.add(topo.getOutLinkPid());
						
						if (linkPidList.contains(topo.getOutLinkPid())) {
							detail = topo;
							delTopoLinks.add(topo.getOutLinkPid());
						}
					}
					if (delTopoLinks.containsAll(allTopoLinks)) {
						deleteLanesMap.put(restriction.getPid(), restriction);
					} else if(detail != null){
						deleteDetailList.add(detail);
					}
				}
			}

		}
	}
	
	/**
	 * 维护删除退出线后的交限RestricInfo字段
	 * 
	 * @param restriction
	 *            交限
	 * @param detail
	 *            被删详细信息
	 */
	private void setRestricInfo(
			List<RdRestrictionDetail> deleteDetailLanesList, Result result)
			throws Exception {

		if (deleteDetailLanesList == null || deleteDetailLanesList.size() == 0) {

			return;
		}

		Map<Integer, RdRestrictionDetail> delDetailMap = new HashMap<Integer, RdRestrictionDetail>();

		//被维护的交限pid组
		List<Integer> pids = new ArrayList<Integer>();

		for (RdRestrictionDetail detail : deleteDetailLanesList) {

			if (!pids.contains((Integer) detail.getRestricPid())) {

				pids.add(detail.getRestricPid());
			}

			delDetailMap.put(detail.getPid(), detail);
		}

		RdRestrictionSelector selector = new RdRestrictionSelector(conn);	

		for (Integer restricPid : pids) {

			IRow restrictionRow= selector.loadById(restricPid, true);
			
			RdRestriction restriction = (RdRestriction) restrictionRow;

			String restricInfo = "";

			for (IRow row : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) row;

				if (delDetailMap.containsKey(detail.getPid())) {

					continue;
				}

				String info = String.valueOf(detail.getRestricInfo());

				if (detail.getFlag() == 0 || detail.getFlag() == 2) {

					info = "[" + info + "]";
				}

				if (restricInfo.isEmpty()) {

					restricInfo += info;

				} else {

					restricInfo += "," + info;
				}
			}

			restriction.changedFields().put("restricInfo", restricInfo);

			result.insertObject(restriction, ObjStatus.UPDATE, restriction.pid());
		}
	}

	private void getDeleteViaLinkRest(List<Integer> linkPidList,
			List<RdRestrictionDetail> deleteDetailList,Map<Integer, RdRestriction> deleteLanesMap) throws Exception {
		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

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
		// 1.link作为进入线，删除link删除交限本身
		getDeleteInLinkRdRest(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestrictionDetail detail : deleteDetailLanesList) {

			if(!deleteLanesMap.containsKey(detail.getRestricPid()))
			{
				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(ObjType.RDRESTRICTION);

				alertObj.setPid(detail.getRestricPid());

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
	 * 删除link对交限的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteLinkResInfectData(List<Integer> linkPidList) throws Exception {
		Map<Integer, RdRestriction> deleteLanesMap = new HashMap<>();

		List<RdRestrictionDetail> deleteDetailLanesList = new ArrayList<>();

		// 1.link作为进入线，删除link删除交限本身
		getDeleteInLinkRdRest(linkPidList,deleteLanesMap);
		// 2.link作为退出线，删除该Link会对应删除此组关系
		getDeleteOutLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);
		// 3.link作为经过线，删除该link会对应删除次组关系
		getDeleteViaLinkRest(linkPidList, deleteDetailLanesList,deleteLanesMap);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : deleteLanesMap.values()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
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

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 根据link数组 维护RdRestriction
	 * @param linkPids
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLinks(List<Integer> linkPids,Result result) throws Exception {

		RdRestrictionSelector selector = new RdRestrictionSelector(conn);

		List<RdRestriction> storageTmp = selector.loadByLinks(linkPids, 1, true);

		storageTmp.addAll(selector.loadByLinks(linkPids, 2, true));

		storageTmp.addAll(selector.loadByLinks(linkPids, 3, true));

		Map<Integer,RdRestriction> storage=new HashMap<>();

		for (RdRestriction restriction: storageTmp)
		{
			storage.put(restriction.getPid(),restriction);
		}

		for (RdRestriction restriction: storage.values())
		{
			//被删link作为进入线，删除交限
			if (linkPids.contains(restriction.getInLinkPid())) {

				result.insertObject(restriction, ObjStatus.DELETE, restriction.getPid());

				continue;
			}

			List<RdRestrictionDetail> delDetail = new ArrayList<>();

			for (IRow rowDetail : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

				//被删link作为退出线，删除该Link会对应删除此组关系
				if (linkPids.contains(detail.getOutLinkPid())) {

					delDetail.add(detail);

					continue;
				}

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

					//被删link作为经过线，删除该link会对应删除次组关系
					if (linkPids.contains(via.getLinkPid())) {

						delDetail.add(detail);

						break;
					}
				}
			}

			//退出线全被删除，删除交限
			if (delDetail.size()==restriction.getDetails().size())
			{
				result.insertObject(restriction, ObjStatus.DELETE, restriction.getPid());

				break;
			}

			for (RdRestrictionDetail detail:  delDetail) {

				result.insertObject(detail, ObjStatus.DELETE, detail.getRestricPid());
			}
		}
	}
}
