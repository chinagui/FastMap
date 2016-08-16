package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdDirectroute {
	private Connection conn = null;

	public OpRefRdDirectroute(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, RdLink oldLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);

		operation.deleteByLink(oldLink, result);

		return null;
	}

}
