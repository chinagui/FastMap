package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

import net.sf.json.JSONObject;

public class OpRefRdGsc implements IOperation {

	private Command command;
	
	private Connection conn;

	public OpRefRdGsc(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		// 删除link对立交的影响
		for (RdGsc rdGsc : command.getRdGscs()) {
			JSONObject data = new JSONObject();
			// 立交的pid
			data.put("objId", rdGsc.pid());

			ICommand updatecommand = new com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete.Command(
					data, command.getRequester());
			com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete.Process process = new com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete.Process(
					updatecommand, result, conn);
			process.innerRun();
		}

		return null;
	}

}
