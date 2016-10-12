package com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	/*
	 * 修改RDLINK 操作
	 */
	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(this.command.getZoneLink(), ObjStatus.DELETE,
						this.command.getZoneLink().getPid());
			} else {

				boolean isChanged = this.command.getZoneLink().fillChangeFields(content);

				if (isChanged) {
					result.insertObject(this.command.getZoneLink(), ObjStatus.UPDATE, this.command.getZoneLink().getPid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray forms = content.getJSONArray("meshes");
			this.saveMeshes(result, forms);
		}
		if(content.containsKey("kinds")){
			JSONArray kinds = content.getJSONArray("kinds");
			this.saveKinds(result,kinds);
		}
		return null;
	}
	/*
	 * 修改对应子表ZONE_LINK_KIND
	 */

	private void saveKinds(Result result, JSONArray kinds) throws Exception {
		for (int i = 0; i < kinds.size(); i++) {

			JSONObject kindJson = kinds.getJSONObject(i);

			if (kindJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						kindJson.getString("objStatus"))) {

					ZoneLinkKind kind = this.command.getZoneLink().kindMap.get(kindJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							kindJson.getString("objStatus"))) {
						result.insertObject(kind, ObjStatus.DELETE,
								kind.parentPKValue());

					} else if (ObjStatus.UPDATE.toString().equals(
							kindJson.getString("objStatus"))) {

						boolean isChanged = kind.fillChangeFields(kindJson);

						if (isChanged) {
							result.insertObject(kind, ObjStatus.UPDATE,  kind.parentPKValue());
						}
					}
				} else {
					ZoneLinkKind kind = new ZoneLinkKind();

					kind.Unserialize(kindJson);

					kind.setLinkPid(this.command.getLinkPid());
					result.insertObject(kind, ObjStatus.INSERT, kind.parentPKValue());

				}
			}

		}
		
	}

	/*
	 * 修改对应子表ZONE_LINK_MESH
	 */
	private void saveMeshes(Result result, JSONArray forms) throws Exception {
		for (int i = 0; i < forms.size(); i++) {

			JSONObject meshJson = forms.getJSONObject(i);

			if (meshJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						meshJson.getString("objStatus"))) {

					ZoneLinkMesh mesh = this.command.getZoneLink().meshMap.get(meshJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							meshJson.getString("objStatus"))) {
						result.insertObject(mesh, ObjStatus.DELETE,
								mesh.parentPKValue());

					} else if (ObjStatus.UPDATE.toString().equals(
							meshJson.getString("objStatus"))) {

						boolean isChanged = mesh.fillChangeFields(meshJson);

						if (isChanged) {
							result.insertObject(mesh, ObjStatus.UPDATE,  mesh.parentPKValue());
						}
					}
				} else {
					ZoneLinkMesh mesh = new ZoneLinkMesh();

					mesh.Unserialize(meshJson);

					mesh.setLinkPid(this.command.getLinkPid());

					result.insertObject(mesh, ObjStatus.INSERT, mesh.parentPKValue());
				}
			}

		}

	}

}
