package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleterwnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		for (RdGsc row : command.getRdGscs()) {

			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}

		for (RwLink row : command.getLinks()) {

			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}

		for (RwNode row : command.getNodes()) {

			result.insertObject(row, ObjStatus.DELETE, row.pid());
		}

		return msg;
	}

}
