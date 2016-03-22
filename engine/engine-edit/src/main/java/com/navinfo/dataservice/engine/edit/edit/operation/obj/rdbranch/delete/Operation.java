package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	public Operation(Command command, RdBranch branch) {
		this.command = command;

		this.branch = branch;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(branch);
				
		return null;
	}

}
