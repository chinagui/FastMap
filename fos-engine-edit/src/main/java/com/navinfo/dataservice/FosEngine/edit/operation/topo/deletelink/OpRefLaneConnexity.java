package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
