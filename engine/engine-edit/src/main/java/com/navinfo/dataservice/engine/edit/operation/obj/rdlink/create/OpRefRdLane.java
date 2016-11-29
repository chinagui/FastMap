package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/***
 * 新建link对详细车道的维护
 * 
 * @author zhaokk
 * 
 */
public class OpRefRdLane {

	private Connection conn;

	public OpRefRdLane(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, List<RdLink> links) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
				conn);
		operation.caleLanesforCreateRdLinks(links, result);
		return null;
	}
}
