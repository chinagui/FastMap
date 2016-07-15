package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;

public class OpTopo implements IOperation {

	private Command command;

	public OpTopo(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		LuLink link = command.getLink();

		result.setPrimaryPid(link.getPid());

		result.insertObject(link, ObjStatus.DELETE, link.pid());

		for (LuNode node : command.getNodes()) {

			result.insertObject(node, ObjStatus.DELETE, node.pid());
		}

		return msg;
	}

}
