package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

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
			limit.changedFields().put("linkPid", 0);
			result.insertObject(limit, ObjStatus.UPDATE, limit.pid());
		}
		
		return null;
	}

}
