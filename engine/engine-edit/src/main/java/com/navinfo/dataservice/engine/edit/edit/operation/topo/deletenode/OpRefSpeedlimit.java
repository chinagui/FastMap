package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class OpRefSpeedlimit implements IOperation {
	
	private Command command;

	public OpRefSpeedlimit(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdSpeedlimit limit : command.getLimits()){
			
			result.insertObject(limit, ObjStatus.DELETE, limit.pid());
		}
		
		return null;
	}

}
