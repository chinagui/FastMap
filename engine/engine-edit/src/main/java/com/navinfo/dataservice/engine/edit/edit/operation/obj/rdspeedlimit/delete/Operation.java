package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdspeedlimit.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class Operation implements IOperation {

	private Command command;

	private RdSpeedlimit speedlimit;

	public Operation(Command command, RdSpeedlimit speedlimit) {
		this.command = command;

		this.speedlimit = speedlimit;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(speedlimit);

		return null;
	}

}
