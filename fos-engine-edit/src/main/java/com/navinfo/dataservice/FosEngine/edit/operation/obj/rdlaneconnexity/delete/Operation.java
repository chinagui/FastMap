package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlaneconnexity.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdLaneConnexity lane;

	public Operation(Command command, RdLaneConnexity lane) {
		this.command = command;

		this.lane = lane;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(lane);
				
		return null;
	}

}
