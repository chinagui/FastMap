package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdbranchdetail.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdBranchDetail detail;

	private RdBranch branch;

	public Operation(Command command, RdBranchDetail detail, RdBranch branch) {
		this.command = command;

		this.branch = branch;

		this.detail = detail;

	}

	@Override
	public String run(Result result) throws Exception {

		if (branch.getDetails().size() == 1
				&& branch.getSignboards().size() == 0
				&& branch.getSignasreals().size() == 0
				&& branch.getSeriesbranches().size() == 0
				&& branch.getRealimages().size() == 0
				&& branch.getSchematics().size() == 0) {

			result.getDelObjects().add(branch);
		} else {
			result.getDelObjects().add(detail);
		}

		return null;
	}

}
