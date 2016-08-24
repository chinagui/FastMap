package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdRoad {
	private Connection conn = null;

	public OpRefRdRoad(Connection conn) {

		this.conn = conn;

	}

	public String run(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation(
				conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		for (RdLink link : command.getLinks()) {

			linkPids.add(link.getPid());
		}

		operation.deleteByLinks(linkPids, result);

		return null;
	}

}