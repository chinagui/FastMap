package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.move;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private AdAdmin moveAdmin;

	public Operation(Command command, AdAdmin moveAdmin) {
		this.command = command;

		this.moveAdmin = moveAdmin;
	}

	@Override
	public String run(Result result) throws Exception {

		result.setPrimaryPid(moveAdmin.getPid());
		
		this.updateAdminGeometry(result);
		
		return null;
	}

	private void updateAdminGeometry(Result result) throws Exception {
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });

		JSONObject updateContent = new JSONObject();

		updateContent.put("geometry", geojson);
		
		updateContent.put("linkPid", moveAdmin.getLinkPid());
		
		moveAdmin.setLinkPid(command.getLinkPid());

		moveAdmin.fillChangeFields(updateContent);
		
		result.insertObject(moveAdmin, ObjStatus.UPDATE, moveAdmin.pid());
	}
}
