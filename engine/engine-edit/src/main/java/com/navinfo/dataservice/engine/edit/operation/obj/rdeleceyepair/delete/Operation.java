package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		deleteRdEleceyePair(result);
		return null;
	}

	public void deleteRdEleceyePair(Result result) {
		// 删除区间测速电子眼配对信息(同时删除子表信息)
		if (null != command.getPair()) {
			result.insertObject(command.getPair(), ObjStatus.DELETE, command.getPair().getPid());
		}
	}

}
