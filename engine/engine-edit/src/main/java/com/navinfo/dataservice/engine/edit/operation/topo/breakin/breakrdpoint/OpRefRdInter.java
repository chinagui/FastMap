package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdInter {

	private Connection conn;

	public OpRefRdInter(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks) throws Exception {

		// 维护CRF交叉点
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation(
				this.conn);
		rdinterOperation.breakRdLink(oldLink, newLinks, result);

		return null;
	}
}
