package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

public class OpRefRelationObj {

	private Connection conn;

	public OpRefRelationObj(Connection conn) {
		this.conn = conn;
	}

	public String handleSameLink(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);

		operation.deleteByLink(command.getLink(), result);

		return null;
	}

}
