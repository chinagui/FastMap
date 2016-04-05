package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletecross;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;

public class OpRefRdBranch implements IOperation {
	
	private Command command;

	public OpRefRdBranch(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdBranch row : command.getBranches()){
			
			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}
		
		return null;
	}

}
