package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;

public class Operation implements IOperation {

	private Command command;

	private RdWarninginfo  rdWarninginfo;

	public Operation(Command command) {
		this.command = command;

		this.rdWarninginfo = command.getRdWarninginfo();
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(rdWarninginfo, ObjStatus.DELETE, command.getPid());

		return null;
	}
}
