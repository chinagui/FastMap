package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdRoad {
	private Connection conn = null;

	public OpRefRdRoad(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation(
				conn);

		operation.breakRdLink(oldLink.getPid(), newLinks, result);

		return null;
	}

}
