package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;

public class OpTopo implements IOperation {

	private Command command;

	public OpTopo(Command command) {

		this.command = command;
	}

	@Override
	public String run(Result result) {

		String msg = null;

		LcLink link = command.getLink();

		result.setPrimaryPid(link.getPid());

		result.insertObject(link, ObjStatus.DELETE, link.pid());

		for (LcNode node : command.getNodes()) {

			result.insertObject(node, ObjStatus.DELETE, node.pid());
		}

		return msg;
	}

}
