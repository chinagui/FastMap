package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;

public class Operation implements IOperation {
	
	private Command command;
	
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			delRdGate(result);
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void delRdGate(Result result) {
		result.insertObject(this.command.getRdGate(), ObjStatus.DELETE, this.command.getRdGate().parentPKValue());
	}
	
	
	/**
	 * 删除link维护大门
	 * @param linkPid
	 * @param conn
	 * @param result
	 * @throws Exception
	 */
	public void delByLink(int linkPid, Result result) throws Exception {
		if (conn == null) {
			return;
		}
		RdGateSelector rdSelector = new RdGateSelector(conn);
		try {
			List<RdGate> rdGateList = rdSelector.loadByLink(linkPid,true);
			for (RdGate rdGate:rdGateList) {
				result.insertObject(rdGate, ObjStatus.DELETE, rdGate.parentPKValue());
			}
		} catch (Exception e) {
			throw e;
		}
	}
		
	/**
	 * 删除link对大门的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdGateInfectData(int linkPid,Connection conn) throws Exception {
		
		RdGateSelector rdGateSelector = new RdGateSelector(conn);
		
		List<RdGate> rdGateList = rdGateSelector.loadByLink(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdGate rdGate : rdGateList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGate.objType());

			alertObj.setPid(rdGate.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
