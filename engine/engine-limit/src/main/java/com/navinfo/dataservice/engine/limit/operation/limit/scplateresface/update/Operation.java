package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;

import net.sf.json.JSONObject;

public class Operation implements IOperation {
	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		//批量写入boundaryLink
		if (command.getFaces() != null) {

			for (ScPlateresFace face : command.getFaces()) {

				if (face.getBoundaryLink().equals(command.getBoundaryLink())) {
					continue;
				}
				face.changedFields().put("boundaryLink", command.getBoundaryLink());

				result.insertObject(face, ObjStatus.UPDATE, face.getGeometryId());

				result.setPrimaryId(face.getGeometryId());

			}
		} else {

			JSONObject content = this.command.getContent();

			ScPlateresFace face = command.getFace();

			if (content.containsKey("objStatus") && content.getString("objStatus").equals(ObjStatus.UPDATE.toString())) {
				boolean isChange = face.fillChangeFields(content);

				if (isChange) {
					result.insertObject(face, ObjStatus.UPDATE, this.command.getGemetryId());
				}
			}

			result.setPrimaryId(face.getGeometryId());
		}

		return null;

	}
}
