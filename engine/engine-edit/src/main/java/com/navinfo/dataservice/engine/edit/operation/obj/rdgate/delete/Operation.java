package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class Operation implements IOperation {
	
	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			delRdGate(result);
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void delRdGate(Result result) {
		result.insertObject(this.command.getRdGate(), ObjStatus.DELETE, this.command.getRdGate().parentPKValue());
		
		for (IRow condition : this.command.getRdGate().getCondition()) {
			result.insertObject(condition, ObjStatus.DELETE, condition.parentPKValue());
		}
	}

}
