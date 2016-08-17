package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

public class OpRefRdGate {
	private Connection conn = null;

	public OpRefRdGate(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, Command command) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation rdOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
				conn);
		for (int pid : command.getLinkPids()) {
			rdOperation.delByLink(pid, result);
		}

		return null;
	}
}
