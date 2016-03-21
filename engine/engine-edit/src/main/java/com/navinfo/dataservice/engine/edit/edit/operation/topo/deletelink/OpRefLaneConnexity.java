package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpRefLaneConnexity implements IOperation {
	
	private Command command;

	public OpRefLaneConnexity(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdLaneConnexity lane : command.getLanes()){
			
			result.insertObject(lane, ObjStatus.DELETE);
		}
		
		return null;
	}

}
