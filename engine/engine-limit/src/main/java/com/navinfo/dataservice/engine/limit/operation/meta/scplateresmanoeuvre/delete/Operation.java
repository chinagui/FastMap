package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation{

	private Command command = null;
	private Connection conn = null;

	public Operation(Connection conn) {
		this.conn = conn;
	}
	
	public Operation(Command command,Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		for (int i = 0; i < command.getManoeuvre().size(); i++) {
			result.insertObject(command.getManoeuvre().get(i), ObjStatus.DELETE,
					command.getGroupId() + command.getManoeuvre().get(i).getManoeuvreId());
		}
		return null;
	}


	public void delByGroupId(String groupId, Result result) throws Exception {

		if ( conn == null)
		{
			return;
		}

		ScPlateresManoeuvreSearch search = new ScPlateresManoeuvreSearch(this.conn);

		List<ScPlateresManoeuvre> manoeuvres = search.loadByGroupId(groupId);

		for (ScPlateresManoeuvre manoeuvre : manoeuvres) {

			result.insertObject(manoeuvre, ObjStatus.DELETE, manoeuvre.getGroupId());
		}
	}


}
