package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletecross;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;

public class OpRefRdRestriction implements IOperation {
	
	private Command command;

	public OpRefRdRestriction(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdRestriction row : command.getRestricts()){
			
			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}
		
		return null;
	}

}
