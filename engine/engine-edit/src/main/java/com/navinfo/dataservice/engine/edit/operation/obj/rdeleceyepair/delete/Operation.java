package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		// 删除区间测速电子眼
		for (RdEleceyePart part : command.getParts()) {
			result.insertObject(part, ObjStatus.DELETE, part.getEleceyePid());
		}
		// 删除区间测速电子眼配对信息
		if (null != command.getPair()) {
			result.insertObject(command.getPair(), ObjStatus.DELETE, command.getPair().getPid());
		}
		return null;
	}

}
