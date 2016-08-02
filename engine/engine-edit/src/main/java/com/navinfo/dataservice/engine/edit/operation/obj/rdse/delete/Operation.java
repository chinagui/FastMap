package com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:43:54
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		result.insertObject(command.getRdSe(), ObjStatus.DELETE, command.getPid());
		return null;
	}

}
