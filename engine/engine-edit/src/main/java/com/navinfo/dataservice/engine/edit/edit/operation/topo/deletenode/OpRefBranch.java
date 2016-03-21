package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

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
