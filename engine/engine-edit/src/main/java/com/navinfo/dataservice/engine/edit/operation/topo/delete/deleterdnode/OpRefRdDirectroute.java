package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdDirectroute {
	private Connection conn = null;

	public OpRefRdDirectroute(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result,  Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);

		
		for (RdLink link : command.getLinks()) {
			operation.deleteByLink(link, result);
		}

		return null;
	}

}