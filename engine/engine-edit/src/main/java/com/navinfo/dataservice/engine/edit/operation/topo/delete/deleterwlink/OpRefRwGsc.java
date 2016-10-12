package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;

public class OpRefRwGsc implements IOperation {

	private Command command;

	private Connection conn;

	public OpRefRwGsc(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation gscDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				this.conn);

		RwLink link = this.command.getLink();

		List<RwLink> linkList = new ArrayList<>();

		linkList.add(link);

		gscDelOption.deleteByLinkPid(linkList, result);

		return null;
	}

}
