package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;

import net.sf.json.JSONObject;

public class Operation implements IOperation{

	private Command command;
	
	public Operation(Command command){
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {

		//批量写入boundaryLink
		if (command.getLinks() != null) {

			for (ScPlateresLink link : command.getLinks()) {

				if (link.getBoundaryLink().equals(command.getBoundaryLink())) {
					continue;
				}
				link.changedFields().put("boundaryLink", command.getBoundaryLink());

				result.insertObject(link, ObjStatus.UPDATE, link.getGeometryId());

				result.setPrimaryId(link.getGeometryId());

			}
		} else {

			ScPlateresLink link = command.getLink();

			JSONObject content = this.command.getContent();

			if (content.containsKey("objStatus") && content.getString("objStatus").equals(ObjStatus.UPDATE.toString())) {
				boolean isChange = link.fillChangeFields(content);

				if (isChange) {
					result.insertObject(link, ObjStatus.UPDATE, this.command.getGemetryId());
				}
			}

			result.setPrimaryId(link.getGeometryId());
		}
		return null;
	}

}
