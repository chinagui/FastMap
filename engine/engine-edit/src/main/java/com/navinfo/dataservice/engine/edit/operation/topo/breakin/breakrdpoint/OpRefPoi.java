package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefPoi {

	private Connection conn;

	public OpRefPoi(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, RdLink oldLink, List<RdLink> newLinks) throws Exception {

		// poi被动维护（引导link，方位）
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation poiUpdateOption = new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation(
				this.conn);
		poiUpdateOption.breakLinkForPoi(oldLink, newLinks, result);

		return null;
	}
}
