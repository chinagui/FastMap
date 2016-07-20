package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		result.insertObject(this.command.getEleceye(), ObjStatus.DELETE, this.command.getEleceye().parentPKValue());

		return null;
	}

}
