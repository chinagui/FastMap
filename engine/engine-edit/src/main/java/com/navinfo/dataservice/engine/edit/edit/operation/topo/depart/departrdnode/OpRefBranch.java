package com.navinfo.dataservice.engine.edit.edit.operation.topo.depart.departrdnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;

public class OpRefBranch implements IOperation {
	
	private Command command;

	public OpRefBranch(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdBranch branch : command.getBranches()){
			
			result.insertObject(branch, ObjStatus.DELETE, branch.pid());
		}
		
		return null;
	}

}
