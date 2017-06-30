package com.navinfo.dataservice.engine.edit.operation.topo.topobreakin;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.operation.topo.topobreakin.Command;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;
	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception{
		this.breakLine(result);
		return null;
	}

	private void breakLine(Result result) throws Exception {
		for (int linkPid : this.command.getLinkPids()) {
			JSONObject breakLinkJson = getBreaksPara(linkPid);

			com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
					breakLinkJson, breakLinkJson.toString());
			com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
					breakCommand, conn, result);
			breakProcess.innerRun();
		}
	}

	/**
	 * 组装每一条link打断需要的数据
	 * 
	 * @param linkPid
	 * @return
	 */
	private JSONObject getBreaksPara(int linkPid) {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", this.command.getDbId());
		
		JSONObject data = new JSONObject();
		
		if (this.command.getBreakNodePid() != 0) {
			data.put("breakNodePid", this.command.getBreakNodePid());
			data.put("longitude", this.command.getBreakPoint().getX());
			data.put("latitude", this.command.getBreakPoint().getY());
		} else {
			data.put("longitude", this.command.getBreakPoint().getX());
			data.put("latitude", this.command.getBreakPoint().getY());
		}

		breakJson.put("data", data);
		return breakJson;
	}
}
