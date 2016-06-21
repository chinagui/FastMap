package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deletecross;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;

public class OpRefRdLaneConnexity implements IOperation {
	
	private Command command;

	public OpRefRdLaneConnexity(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdLaneConnexity row : command.getLanes()){
			
			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}
		
		return null;
	}

}
