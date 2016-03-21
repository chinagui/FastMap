package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletenode;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class OpRefBranch implements IOperation {
	
	private Command command;

	public OpRefBranch(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdBranch branch : command.getBranches()){
			
			result.insertObject(branch, ObjStatus.DELETE);
		}
		
		return null;
	}

}
