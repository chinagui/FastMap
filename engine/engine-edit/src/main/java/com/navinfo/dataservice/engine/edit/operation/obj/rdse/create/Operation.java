package com.navinfo.dataservice.engine.edit.operation.obj.rdse.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:34:09
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		RdSe rdSe = new RdSe();
		rdSe.setPid(PidUtil.getInstance().applyRdSePid());
		rdSe.setInLinkPid(this.command.getContent().getInt("inLinkPid"));
		rdSe.setNodePid(this.command.getContent().getInt("nodePid"));
		rdSe.setOutLinkPid(this.command.getContent().getInt("outLinkPid"));

		result.insertObject(rdSe, ObjStatus.INSERT, rdSe.pid());
		return null;
	}

}
