package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
