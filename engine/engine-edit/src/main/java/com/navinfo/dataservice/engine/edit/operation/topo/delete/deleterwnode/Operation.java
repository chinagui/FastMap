package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;

public class Operation implements IOperation {

	private Command command;
	
	private Connection conn;

	public Operation(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation gscDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				this.conn);
		
		List<RwLink>  linkList = this.command.getLinks();
		
		gscDelOption.deleteByLinkPid(linkList, result);
		
		for (RwLink row : command.getLinks()) {

			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}

		for (RwNode row : command.getNodes()) {

			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}

		return msg;
	}

}
