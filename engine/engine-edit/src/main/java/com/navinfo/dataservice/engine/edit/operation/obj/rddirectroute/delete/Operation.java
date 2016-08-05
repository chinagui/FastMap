package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		msg = deleteRdDirectroute(result, command.getDirectroute());
		
		return msg;
	}

	public String deleteRdDirectroute(Result result, RdDirectroute directroute) {
		
		result.insertObject(directroute, ObjStatus.DELETE, directroute.pid());

		return null;
	}

}
