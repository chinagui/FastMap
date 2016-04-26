package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

public class OpRefRdGsc implements IOperation {
	
	private Command command;

	public OpRefRdGsc(Command command) {
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
