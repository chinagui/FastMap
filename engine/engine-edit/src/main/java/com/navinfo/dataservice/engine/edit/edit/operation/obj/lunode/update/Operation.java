package com.navinfo.dataservice.engine.edit.edit.operation.obj.lunode.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;

public class Operation implements IOperation {

	private Command command;

	private LuNode lunode;

	public Operation(Command command, LuNode lunode) {
		this.command = command;
		this.lunode = lunode;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(lunode, ObjStatus.DELETE, lunode.pid());

				return null;
			} else {

				boolean isChanged = lunode.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(lunode, ObjStatus.UPDATE, lunode.pid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("lunode");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							meshJson.getString("objStatus"))) {

						LuNodeMesh mesh = lunode.meshMap.get(meshJson
								.getString("rowId"));

						if (mesh == null) {
							throw new Exception("rowId="
									+ meshJson.getString("rowId")
									+ "的LuNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								meshJson.getString("objStatus"))) {
							result.insertObject(mesh, ObjStatus.DELETE,
									lunode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								meshJson.getString("objStatus"))) {

							boolean isChanged = mesh.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(mesh, ObjStatus.UPDATE,
										lunode.pid());
							}
						}
					} else {
						LuNodeMesh mesh = new LuNodeMesh();

						mesh.Unserialize(meshJson);

						mesh.setNodePid(lunode.getPid());

						mesh.setMesh(lunode.mesh());

						result.insertObject(mesh, ObjStatus.INSERT,
								lunode.pid());
					}
				}
			}
		}

		return null;
	}

}
