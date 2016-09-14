package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;

public class OpRefRestrict implements IOperation {
	
	private Command command;

	public OpRefRestrict(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdRestriction restrict : command.getRestrictions()){
			
			result.insertObject(restrict, ObjStatus.DELETE, restrict.pid());
		}
		
		return null;
	}
}
