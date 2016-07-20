package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;

public class Operation implements IOperation {

	private Command command;

	private RdLaneConnexity lane;

	public Operation(Command command, RdLaneConnexity lane) {
		this.command = command;

		this.lane = lane;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(lane, ObjStatus.DELETE, lane.pid());
				
		return null;
	}

}
