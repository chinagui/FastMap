package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdTmcLocationLink {

	private Connection conn;

	public OpRefRdTmcLocationLink(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks) throws Exception {

		// 维护TMC匹配信息
		com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation(
				this.conn);
		rdObjectOperation.breakLinkUpdateTmc(result, oldLink, newLinks);

		return null;
	}
}
