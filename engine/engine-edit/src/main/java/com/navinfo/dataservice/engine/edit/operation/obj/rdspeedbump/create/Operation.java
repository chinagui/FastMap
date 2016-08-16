package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:06:38
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		RdSpeedbump speedbump = new RdSpeedbump();
		speedbump.setPid(PidService.getInstance().applyRdSpeedbumpPid());
		speedbump.setLinkPid(this.command.getInLinkPid());
		speedbump.setNodePid(this.command.getInNodePid());
		result.insertObject(speedbump, ObjStatus.INSERT, speedbump.pid());
		return null;
	}

}
