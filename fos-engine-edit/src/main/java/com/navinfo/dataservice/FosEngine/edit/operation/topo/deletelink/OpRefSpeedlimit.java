package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class OpRefSpeedlimit implements IOperation {
	
	private Command command;

	public OpRefSpeedlimit(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdSpeedlimit limit : command.getLimits()){
			
			result.insertObject(limit, ObjStatus.DELETE);
		}
		
		return null;
	}

}
