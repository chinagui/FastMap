package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;

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
				result.insertObject(adAdmin, ObjStatus.DELETE, adAdmin.pid());

				return null;
			} else {

				boolean isChanged = adAdmin.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(adAdmin, ObjStatus.UPDATE, adAdmin.pid());
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
							result.insertObject(row, ObjStatus.DELETE, adAdmin.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = row.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(row, ObjStatus.UPDATE, adAdmin.pid());
							}
						}
					} else {
						AdAdminName row = new AdAdminName();

						row.Unserialize(json);

						row.setPid(PidUtil.getInstance().applyAdAdminNamePid());

						row.setRegionId(adAdmin.getPid());

						row.setMesh(adAdmin.mesh());

						result.insertObject(row, ObjStatus.INSERT, adAdmin.pid());
					}
				}
			}
		}

		return null;
	}

}
