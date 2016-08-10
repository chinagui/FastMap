package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:17:21
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		result.insertObject(this.command.getRdTollgate(), ObjStatus.DELETE, this.command.getPid());
		return null;
	}

}
