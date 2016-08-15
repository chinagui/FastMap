package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdVariableSpeed variableSpeed;

	private Connection conn;

	public Operation(Command command, RdVariableSpeed variableSpeed, Connection conn) {
		this.command = command;

		this.variableSpeed = variableSpeed;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			boolean isChanged = variableSpeed.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(variableSpeed, ObjStatus.UPDATE, variableSpeed.pid());
			}
		}

		// 接续线子表
		if (content.containsKey("vias")) {
			updateVias(result, content);
		}

		return null;

	}

	/**
	 * @param result
	 * @param content
	 */
	private void updateVias(Result result, JSONObject content) {
		JSONArray subObj = content.getJSONArray("vias");
		
		for (IRow row : variableSpeed.getVias()) {
			RdVariableSpeedVia via = (RdVariableSpeedVia) row;
			if (subObj == null) {
				result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
			} else if (!subObj.contains(via.getLinkPid())) {
				result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
			} else {
				subObj.remove((Integer) via.getLinkPid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdVariableSpeedVia via = new RdVariableSpeedVia();

			via.setLinkPid(subObj.getInt(i));

			via.setVspeedPid(variableSpeed.getPid());

			result.insertObject(via, ObjStatus.INSERT, via.getLinkPid());
		}
	}

}
