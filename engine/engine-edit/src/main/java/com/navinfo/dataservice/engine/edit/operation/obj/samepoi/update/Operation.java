package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:38:13
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		boolean isChange = this.command.getSamepoi().fillChangeFields(this.command.getContent());
		if (isChange) {
			result.insertObject(this.command.getSamepoi(), ObjStatus.UPDATE, this.command.getSamepoi().pid());
		}
		return null;
	}

}
