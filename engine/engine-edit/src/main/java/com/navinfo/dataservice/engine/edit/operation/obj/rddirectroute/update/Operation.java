package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		RdDirectroute directroute = command.getDirectroute();

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(directroute, ObjStatus.DELETE,
						directroute.pid());

				return null;
			} else if (ObjStatus.UPDATE.toString().equals(
					content.getString("objStatus"))) {
				boolean isChanged = directroute.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(directroute, ObjStatus.UPDATE,
							directroute.pid());
				}
			}
		}

		return null;

	}

}