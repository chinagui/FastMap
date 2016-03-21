package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletecross;

import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpRefRdLaneConnexity implements IOperation {
	
	private Command command;

	public OpRefRdLaneConnexity(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( IRow row : command.getLanes()){
			
			result.insertObject(row, ObjStatus.DELETE);
		}
		
		return null;
	}

}
