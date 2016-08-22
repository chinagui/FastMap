package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
/***
 * 
 * @author zhaokk
 *
 */
public class OpRefRdLane {

	private Connection conn = null;

	public OpRefRdLane(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, int linkPid) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
				conn);
		operation.deleteRdLaneforRdLink(linkPid, result);
		return null;
	}
}
