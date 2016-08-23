package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdObject {

	private Connection conn;

	public OpRefRdObject(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks) throws Exception {

		// 维护CRF对象
		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
				this.conn);
		rdObjectOperation.breakRdObjectLink(oldLink, newLinks, result);

		return null;
	}
}
