package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;

public class Operation implements IOperation{

	private Command command = null;
	
	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception{
		result.insertObject(command.getManoeuvre(), ObjStatus.DELETE, String.valueOf(command.getManoeuvreId()));
		return null;
	}

}
