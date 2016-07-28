package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;

public class Operation implements IOperation {
	
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	private Result result;
	
	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			this.result = result;
			RdGate rdGate = new RdGate();
			this.result.insertObject(rdGate, ObjStatus.DELETE, command.getPid());
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

}
