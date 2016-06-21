package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;

public class OpRefLaneConnexity implements IOperation {
	
	private Command command;

	public OpRefLaneConnexity(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdLaneConnexity lane : command.getLanes()){
			
			result.insertObject(lane, ObjStatus.DELETE, lane.pid());
		}
		
		return null;
	}

}
