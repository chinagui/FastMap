package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;

public class OpRefRelationObj {

	private Connection conn = null;

	public OpRefRelationObj(Connection conn) {

		this.conn = conn;
	}

	public String handleSameLink(LuLink breakLink, Command command,
			Result result) throws Exception {

		List<IObj> newLinks = new ArrayList<IObj>();

		newLinks.add(command.getsLuLink());

		newLinks.add(command.geteLuLink());

		// 打断link维护同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
				this.conn);
		operation.breakLink(breakLink, newLinks, command.getBreakNode(),
				command.getRequester(), result);

		return null;
	}
}
