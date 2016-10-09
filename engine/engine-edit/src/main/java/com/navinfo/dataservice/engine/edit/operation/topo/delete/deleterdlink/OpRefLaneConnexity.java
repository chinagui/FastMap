package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class OpRefLaneConnexity implements IOperation {

	private Command command;

	private Connection conn;
	
	public OpRefLaneConnexity(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		List<Integer> linkPidList = new ArrayList<>();

		linkPidList.add(this.command.getLinkPid());

		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation rdLaneDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation(
				this.conn);

		rdLaneDelOption.deleteRdLaneByLink(linkPidList, result);

		return null;
	}
}
