package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

public class OpRefRdWarninginfo {

	private Connection conn = null;

	public OpRefRdWarninginfo(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);
		for (int pid : command.getLinkPids()) {

			warninginfoOperation.deleteByLink(pid, result);
		}

		return null;
	}

}
