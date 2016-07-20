package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

public class OpRefRwGsc implements IOperation {
	
	private Command command;

	public OpRefRwGsc(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdGsc rdGsc : command.getRdGscs()){
			
			result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());
		}
		
		return null;
	}

}
