package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn = null;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = delete(result, command.getRoad());
		
		List<RdRoad> roadList = new ArrayList<>();
		
		List<Integer> deleteRoadPidList = new ArrayList<>();
		
		roadList.add(command.getRoad());
		
		deleteRoadPidList.add(command.getPid());
		
		delete(result,roadList,deleteRoadPidList);

		return msg;
	}

	private String delete(Result result, List<RdRoad> roadList, List<Integer> deleteRoadPidList) throws Exception {

		for (RdRoad road : roadList) {
			delete(result, road);
		}

		// 维护CRFO:如果删除的CRFI属于某个CRFO，要从CRFO组成信息中去掉
		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
				conn);

		operation.deleteByType(deleteRoadPidList, ObjType.RDROAD, result);

		return null;
	}

	private String delete(Result result, RdRoad road) {

		result.insertObject(road, ObjStatus.DELETE, road.pid());
		
		return null;
	}

	public String deleteByLinks(List<Integer> linkPids, Result result) throws Exception {

		RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(this.conn);

		List<RdRoadLink> rdRoadLinks = rdRoadLinkSelector.loadByLinks(linkPids, true);

		if (rdRoadLinks.size() < 1) {

			return null;
		}

		List<Integer> rdRoadPids = new ArrayList<Integer>();

		for (RdRoadLink roadLink : rdRoadLinks) {

			if (!rdRoadPids.contains(roadLink.getPid())) {

				rdRoadPids.add(roadLink.getPid());
			}
		}

		AbstractSelector roadSelector = new AbstractSelector(RdRoad.class, this.conn);

		List<IRow> rows = roadSelector.loadByIds(rdRoadPids, true, true);

		List<Integer> deleteRoadPidList = new ArrayList<Integer>();

		List<RdRoad> deRoadList = new ArrayList<>();

		for (IRow roadRow : rows) {

			RdRoad road = (RdRoad) roadRow;

			int deleteCount = 0;

			for (IRow linkRow : road.getLinks()) {

				RdRoadLink roadLink = (RdRoadLink) linkRow;

				if (linkPids.contains(roadLink.getLinkPid())) {

					deleteCount++;
				}
			}

			if ((road.getLinks().size() - deleteCount) < 2) {

				// 需要删除主表
				if (!deleteRoadPidList.contains(road.getPid())) {
					deleteRoadPidList.add(road.getPid());
					deRoadList.add(road);
				}
			}
		}

		for (RdRoadLink roadLink : rdRoadLinks) {

			if (!deleteRoadPidList.contains(roadLink.getPid())) {

				result.insertObject(roadLink, ObjStatus.DELETE, roadLink.getPid());
			}
		}

		// 调用删除主表对象方法
		delete(result, deRoadList, deleteRoadPidList);
		return null;
	}

	/**
	 * 删除link对CRF Road的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateRdRoadInfectData(int linkPid, Connection conn) throws Exception {

		List<Integer> linkPidList = new ArrayList<>();

		linkPidList.add(linkPid);

		RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(this.conn);

		List<RdRoadLink> rdRoadLinks = rdRoadLinkSelector.loadByLinks(linkPidList, true);

		List<AlertObject> alertList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(rdRoadLinks)) {
			RdRoadLink roadLink = rdRoadLinks.get(0);

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(ObjType.RDROAD);

			alertObj.setPid(roadLink.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if (!alertList.contains(alertObj)) {
				alertList.add(alertObj);
			}
		}
		return alertList;
	}
}
