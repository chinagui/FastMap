package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdspeedlimit.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class Operation implements IOperation {

	private Command command;

	private RdSpeedlimit limit;

	public Operation(Command command, RdSpeedlimit limit) {
		this.command = command;

		this.limit = limit;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.getDelObjects().add(limit);
				
				return null;
			} else {

				boolean isChanged = limit.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(limit);
				}
			}
		}

		return null;
	}

}
