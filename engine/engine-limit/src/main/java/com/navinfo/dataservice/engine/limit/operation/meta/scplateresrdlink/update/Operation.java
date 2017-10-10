package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update.Command;

import net.sf.json.JSONObject;

public class Operation implements IOperation{
	private Command command = null;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception{
		updateRdLink(result);
		return null;
	}

	private void updateRdLink(Result result) throws Exception {
		JSONObject content = command.getContent();

		ScPlateresRdLink rdlink = command.getRdLink();

		if (content.containsKey("objStatus") && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

			boolean isChanged = rdlink.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(rdlink, ObjStatus.UPDATE, String.valueOf(rdlink.getLinkPid()));
			}

		}
		result.setPrimaryId(String.valueOf(String.valueOf(rdlink.getLinkPid())));
	}
}
