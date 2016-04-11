package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.update;

import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminName;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private AdAdmin adAdmin;

	public Operation(Command command, AdAdmin adAdmin) {
		this.command = command;

		this.adAdmin = adAdmin;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.getDelObjects().add(adAdmin);

				return null;
			} else {

				boolean isChanged = adAdmin.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(adAdmin);
				}
			}
		}

		if (content.containsKey("names")) {
			JSONArray names = content.getJSONArray("names");

			for (int i = 0; i < names.size(); i++) {

				JSONObject json = names.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

						AdAdminName row = adAdmin.adAdminNameMap.get(json.getString("rowId"));

						if (row == null) {
							throw new Exception("rowId=" + json.getString("rowId") + "的AdAdminName不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
							result.getDelObjects().add(row);

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = row.fillChangeFields(json);

							if (isChanged) {
								result.getUpdateObjects().add(row);
							}
						}
					} else {
						AdAdminName row = new AdAdminName();

						row.Unserialize(json);

						row.setPid(PidService.getInstance().applyAdAdminNamePid());

						row.setRegionId(adAdmin.getPid());

						row.setMesh(adAdmin.mesh());

						result.getAddObjects().add(row);
					}
				}
			}
		}

		return null;
	}

}
