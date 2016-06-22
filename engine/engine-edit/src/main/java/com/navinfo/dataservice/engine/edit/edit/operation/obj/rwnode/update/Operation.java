package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwnode.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
public class Operation implements IOperation {

	private Command command;

	private RwNode rwnode;

	public Operation(Command command) {
		this.command = command;

		this.rwnode = command.getRwNode();
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(rwnode, ObjStatus.DELETE, rwnode.pid());

				return null;
			} else {

				boolean isChanged = rwnode.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rwnode, ObjStatus.UPDATE, rwnode.pid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("rwnode");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							meshJson.getString("objStatus"))) {

						RwNodeMesh mesh = rwnode.meshMap.get(meshJson
								.getString("rowId"));

						if (mesh == null) {
							throw new Exception("rowId="
									+ meshJson.getString("rowId")
									+ "的RwNodeMesh不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								meshJson.getString("objStatus"))) {
							result.insertObject(mesh, ObjStatus.DELETE,
									rwnode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								meshJson.getString("objStatus"))) {

							boolean isChanged = mesh.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(mesh, ObjStatus.UPDATE,
										rwnode.pid());
							}
						}
					} else {
						RwNodeMesh mesh = new RwNodeMesh();

						mesh.Unserialize(meshJson);

						mesh.setNodePid(rwnode.getPid());

						mesh.setMesh(rwnode.mesh());

						result.insertObject(mesh, ObjStatus.INSERT,
								rwnode.pid());
					}
				}
			}
		}

		return null;
	}

}
