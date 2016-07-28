package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

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
			this.result.insertObject(command.getRdGate(), ObjStatus.UPDATE, command.getRdGate().getPid());
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

}
