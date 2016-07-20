package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;

public class Operation implements IOperation {

	private Command command;

	private RdCross cross;

	public Operation(Command command, RdCross cross) {
		this.command = command;

		this.cross = cross;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(cross, ObjStatus.DELETE, cross.getPid());
				
		return null;
	}

}
