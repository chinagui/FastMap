package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletecross;

import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpRefRdBranch implements IOperation {
	
	private Command command;

	public OpRefRdBranch(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( IRow row : command.getBranches()){
			
			result.insertObject(row, ObjStatus.DELETE);
		}
		
		return null;
	}

}
