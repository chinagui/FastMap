package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdDirectroute {
	private Connection conn = null;

	public OpRefRdDirectroute(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

}
