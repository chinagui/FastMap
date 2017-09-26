package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.update.Command;

import net.sf.json.JSONObject;

public class Operation implements IOperation {
	private Command command = null;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception{
		updateManoeuvre(result);
		return null;
	}

	private void updateManoeuvre(Result result) throws Exception {
		JSONObject content = command.getContent();

		ScPlateresManoeuvre manoeuvre = command.getManoeuvre();

		if (content.containsKey("objStatus") && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

			boolean isChanged = manoeuvre.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(manoeuvre, ObjStatus.UPDATE, this.command.getGroupId() + this.command.getManoeuvreId());
			}

		}
		result.setPrimaryId(String.valueOf(manoeuvre.getManoeuvreId()));
	}
}
