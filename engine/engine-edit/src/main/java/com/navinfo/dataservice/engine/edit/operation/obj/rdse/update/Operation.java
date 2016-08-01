package com.navinfo.dataservice.engine.edit.operation.obj.rdse.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:51:14
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		boolean isChange = command.getRdSe().fillChangeFields(command.getContent());
		if (isChange) {
			result.insertObject(command.getRdSe(), ObjStatus.UPDATE, command.getRdSe().pid());
		}
		return null;
	}

}
