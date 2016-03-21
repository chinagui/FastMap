package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpRefRestrict implements IOperation {
	
	private Command command;

	public OpRefRestrict(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdRestriction restrict : command.getRestrictions()){
			
			result.insertObject(restrict, ObjStatus.DELETE);
		}
		
		return null;
	}

}
