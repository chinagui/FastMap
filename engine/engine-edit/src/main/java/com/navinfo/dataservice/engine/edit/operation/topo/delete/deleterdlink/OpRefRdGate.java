package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;

public class OpRefRdGate {

	private Connection conn = null;
	
	private Command command;
	
	public OpRefRdGate(Command command) {
		this.command = command;
	}
	
	public OpRefRdGate(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, int linkPid) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation rdOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
				conn);
		rdOperation.delByLink(linkPid, result);

		return null;
	}
	
	/**
	 * 删除link对大门的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteRdGateInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdGate rdGate : command.getRdGates()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGate.objType());

			alertObj.setPid(rdGate.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
