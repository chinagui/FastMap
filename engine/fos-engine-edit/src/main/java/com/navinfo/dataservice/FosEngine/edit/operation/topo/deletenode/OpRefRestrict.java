package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletenode;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
