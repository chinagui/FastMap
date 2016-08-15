package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;

public class Operation implements IOperation {

	private Command command;

	private RdVariableSpeed variableSpeed;

	public Operation(Command command, RdVariableSpeed variableSpeed) {
		this.command = command;

		this.variableSpeed = variableSpeed;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());
				
		return null;
	}

}
