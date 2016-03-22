package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;

public class Operation implements IOperation {

	private Command command;

	private RdRestriction restrict;

	public Operation(Command command, RdRestriction restrict) {
		this.command = command;

		this.restrict = restrict;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(restrict);

		return null;
	}

}
