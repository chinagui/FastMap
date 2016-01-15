package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdbranchdetail.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdBranchDetail branch;

	public Operation(Command command, RdBranchDetail branch) {
		this.command = command;

		this.branch = branch;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(branch);
				
		return null;
	}

}
