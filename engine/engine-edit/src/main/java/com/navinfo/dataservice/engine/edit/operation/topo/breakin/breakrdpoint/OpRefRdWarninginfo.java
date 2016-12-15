package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdWarninginfo {

	private Connection conn = null;

	public OpRefRdWarninginfo(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, int linkPid, List<RdLink> newLinks)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation(
				conn);

		warninginfoOperation.breakRdLink(null, linkPid, newLinks, result);

		return null;
	}

}
