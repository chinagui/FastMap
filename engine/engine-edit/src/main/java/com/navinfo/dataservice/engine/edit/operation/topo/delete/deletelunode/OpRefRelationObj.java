package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;

public class OpRefRelationObj {

	private Connection conn;

	public OpRefRelationObj(Connection conn) {
		this.conn = conn;
	}

	public String handleSameLink(Result result, Command command)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);

		for (LuLink link : command.getLinks()) {
			operation.deleteByLink(link, result);
		}

		return null;
	}

}