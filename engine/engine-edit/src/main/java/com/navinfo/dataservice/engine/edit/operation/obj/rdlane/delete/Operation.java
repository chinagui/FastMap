package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;

/***
 * 删除车道信息
 * 
 * @author zhaokk
 * 
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

	@Override
	public String run(Result result) throws Exception {
		this.deleteRdLane(result);
		return null;
	}

	private void deleteRdLane(Result result) throws Exception {
		// 加载删除详细车道上rdlink其它车道信息
		if (this.command.getLanes().size() > 0) {
			for (RdLane lane : this.command.getLanes()) {
				if (lane.getPid() == this.command.getRdLane().getPid()) {
					this.deleteRdLane(result, this.command.getRdLane());
					continue;
				}
				if (lane.getSeqNum() > this.command.getRdLane().getSeqNum()) {
					lane.changedFields().put("seqNum", lane.getSeqNum() - 1);
				}
				lane.changedFields().put("laneNum", lane.getLaneNum() - 1);
				result.insertObject(lane, ObjStatus.UPDATE, lane.getPid());
			}

		}

	}
	/***
	 * 删除车道不需要维护对应link上其它车道信息
	 * @param result
	 * @param lanePid
	 * @throws Exception
	 */
	public void deleteRdLane(Result result, RdLane lane) throws Exception {
		this.deleteRdLaneTopoDetail(result, lane.getPid());
		result.insertObject(lane, ObjStatus.DELETE, lane.getPid());
	}

	/***
	 * 删除车道信息维护车道联通信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void deleteRdLaneTopoDetail(Result result, int lanePid)
			throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete.Operation(
				conn);
		operation.deleteTopoForRdLane(result, lanePid);
	}

	/***
	 * 删除link维护车道信息
	 * 
	 * @param linkPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteRdLaneforRdLink(int linkPid, Result result)
			throws Exception {
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(linkPid, 1,
				true);
		for (RdLane lane : lanes) {
			this.deleteRdLaneTopoDetail(result, lane.getPid());
			result.insertObject(lane, ObjStatus.DELETE, lane.getPid());
		}
	}
	
	/**
	 * 删除link对详细车道的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdLaneInfectData(int linkPid,Connection conn) throws Exception {
		
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(linkPid, 1,
				true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdLane lane : lanes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(lane.objType());

			alertObj.setPid(lane.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
