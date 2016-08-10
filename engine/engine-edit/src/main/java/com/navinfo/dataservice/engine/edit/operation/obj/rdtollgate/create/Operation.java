package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:05:45
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		RdTollgate tollgate = new RdTollgate();
		tollgate.setPid(PidService.getInstance().applyRdTollgatePid());
		tollgate.setInLinkPid(this.command.getInLinkPid());
		tollgate.setNodePid(this.command.getNodePid());
		tollgate.setOutLinkPid(this.command.getOutLinkPid());
		result.insertObject(tollgate, ObjStatus.INSERT, tollgate.pid());
		return null;
	}

}
