package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deletelunode;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;

/**
 * 删除土地利用点对应信息
 */
public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	public OpTopo(Command command) {

		this.command = command;
	}

	@Override
	public String run(Result result) {

		String msg = null;
		// 删除土地利用点
		result.insertObject(command.getNode(), ObjStatus.DELETE, command
				.getNode().pid());
		for (LuNode node : command.getNodes()) {

			result.insertObject(node, ObjStatus.DELETE, node.pid());

			result.setPrimaryPid(node.getPid());
		}
		for (LuLink link : command.getLinks()) {

			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}

		return msg;
	}

}
