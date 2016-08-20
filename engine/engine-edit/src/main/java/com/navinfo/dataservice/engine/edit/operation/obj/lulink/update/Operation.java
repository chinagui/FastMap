package com.navinfo.dataservice.engine.edit.operation.obj.lulink.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;

public class Operation implements IOperation {

	private Command command;

	private LuLink updateLink;

	public Operation(Command command, LuLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());
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
		if (content.containsKey("linkKinds")) {
			JSONArray kinds = content.getJSONArray("linkKinds");
			this.saveKinds(result, kinds);
		}

		return null;
	}

	/*
	 * 修改对应子表lu_link_mesh
	 */
	private void saveMeshes(Result result, JSONArray forms) throws Exception {
		for (int i = 0; i < forms.size(); i++) {
			JSONObject meshJson = forms.getJSONObject(i);
			if (meshJson.containsKey("objStatus")) {
				if (!ObjStatus.INSERT.toString().equals(meshJson.getString("objStatus"))) {
					LuLinkMesh mesh = updateLink.meshMap.get(meshJson.getString("rowId"));
					if (ObjStatus.DELETE.toString().equals(meshJson.getString("objStatus"))) {
						result.insertObject(mesh, ObjStatus.DELETE, updateLink.pid());
					} else if (ObjStatus.UPDATE.toString().equals(meshJson.getString("objStatus"))) {
						boolean isChanged = mesh.fillChangeFields(meshJson);
						if (isChanged) {
							result.insertObject(mesh, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					LuLinkMesh mesh = new LuLinkMesh();
					mesh.Unserialize(meshJson);
					mesh.setLinkPid(this.updateLink.pid());
					mesh.setMesh(this.updateLink.mesh());
					result.insertObject(mesh, ObjStatus.INSERT, updateLink.pid());
				}
			}
		}
	}

	/*
	 * 修改对应子表lu_link_kind
	 */
	private void saveKinds(Result result, JSONArray kinds) throws Exception {
		for (int i = 0; i < kinds.size(); i++) {
			JSONObject meshJson = kinds.getJSONObject(i);
			if (meshJson.containsKey("objStatus")) {
				if (!ObjStatus.INSERT.toString().equals(meshJson.getString("objStatus"))) {
					LuLinkKind kind = updateLink.linkKindMap.get(meshJson.getString("rowId"));
					if (ObjStatus.DELETE.toString().equals(meshJson.getString("objStatus"))) {
						result.insertObject(kind, ObjStatus.DELETE, updateLink.pid());
					} else if (ObjStatus.UPDATE.toString().equals(meshJson.getString("objStatus"))) {
						boolean isChanged = kind.fillChangeFields(meshJson);
						if (isChanged) {
							result.insertObject(kind, ObjStatus.UPDATE, kind.parentPKValue());
						}
					}
				} else {
					LuLinkKind kind = new LuLinkKind();
					kind.Unserialize(meshJson);
					kind.setLinkPid(this.updateLink.pid());
					kind.setMesh(this.updateLink.mesh());
					result.insertObject(kind, ObjStatus.INSERT, kind.parentPKValue());
				}
			}
		}
	}

}
