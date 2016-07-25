package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		// 删除电子眼
		result.insertObject(this.command.getEleceye(), ObjStatus.DELETE, this.command.getEleceye().parentPKValue());
		// 删除电子眼组成关系表
		for (IRow pair : this.command.getEleceye().getPairs()) {
			result.insertObject((RdEleceyePair) pair, ObjStatus.DELETE, pair.parentPKValue());
		}
		return null;
	}

}
