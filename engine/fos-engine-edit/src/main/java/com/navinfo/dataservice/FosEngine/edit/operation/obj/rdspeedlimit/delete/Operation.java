package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdSpeedlimit speedlimit;

	public Operation(Command command, RdSpeedlimit speedlimit) {
		this.command = command;

		this.speedlimit = speedlimit;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(speedlimit);

		return null;
	}

}
