package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdGate {

	private Connection conn = null;

	public OpRefRdGate(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result, int linkPid, List<RdLink> newLinks) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation rdOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation(
				conn);
		
		rdOperation.breakRdLink(linkPid, newLinks, result);
		
		return null;
	}
	
}
