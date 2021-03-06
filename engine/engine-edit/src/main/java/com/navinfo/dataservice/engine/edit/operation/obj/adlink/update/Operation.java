package com.navinfo.dataservice.engine.edit.operation.obj.adlink.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;

public class Operation implements IOperation {

	private Command command;

	private AdLink updateLink;

	public Operation(Command command, AdLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
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
				result.insertObject(updateLink, ObjStatus.DELETE,
						updateLink.pid());
			} else {

				boolean isChanged = updateLink.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray forms = content.getJSONArray("meshes");
			this.saveMeshes(result, forms);
		}

		return null;
	}

	/*
	 * 修改对应子表RD_LINK_MESH
	 */
	private void saveMeshes(Result result, JSONArray forms) throws Exception {
		for (int i = 0; i < forms.size(); i++) {

			JSONObject meshJson = forms.getJSONObject(i);

			if (meshJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						meshJson.getString("objStatus"))) {

					AdLinkMesh mesh = updateLink.meshMap.get(meshJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							meshJson.getString("objStatus"))) {
						result.insertObject(mesh, ObjStatus.DELETE,
								updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							meshJson.getString("objStatus"))) {

						boolean isChanged = mesh.fillChangeFields(meshJson);

						if (isChanged) {
							result.insertObject(mesh, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					AdLinkMesh mesh = new AdLinkMesh();

					mesh.Unserialize(meshJson);

					mesh.setLinkPid(this.updateLink.pid());

					mesh.setMesh(this.updateLink.mesh());

					result.insertObject(mesh, ObjStatus.INSERT, updateLink.pid());

				}
			}

		}

	}

}
