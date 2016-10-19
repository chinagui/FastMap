package com.navinfo.dataservice.engine.edit.operation.batch.poi;

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
		try {
			exeBatch(result);
			return null;
		} catch (Exception e) {
			throw e;
		}
		
	}

	private void exeBatch(Result result) throws Exception {
		try {
			// 数据加锁
			
			result.insertObject(command.getPoi(), ObjStatus.UPDATE, command.getPid());
		} catch (Exception e) {
			throw e;
		}
	}

}
