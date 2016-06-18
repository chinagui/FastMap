package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RwLink updateLink;

	public Operation(Command command, RwLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());

				return null;
			} else {

				boolean isChanged = updateLink.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
				}
			}
		}
		//TODO
		return null;
	}
}