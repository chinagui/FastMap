package com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private LcNode lcnode;

	public Operation(Command command, LcNode lcnode) {
		this.command = command;

		this.lcnode = lcnode;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(lcnode, ObjStatus.DELETE, lcnode.pid());
				return null;
			} else {
				boolean isChanged = lcnode.fillChangeFields(content);
				if (isChanged) {
					result.insertObject(lcnode, ObjStatus.UPDATE, lcnode.pid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("adnode");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(meshJson.getString("objStatus"))) {

						LcNodeMesh mesh = lcnode.lcNodeMeshMap.get(meshJson.getString("rowId"));

						if (mesh == null) {
							throw new Exception("rowId=" + meshJson.getString("rowId") + "的lcNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(meshJson.getString("objStatus"))) {
							result.insertObject(mesh, ObjStatus.DELETE, lcnode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(meshJson.getString("objStatus"))) {

							boolean isChanged = mesh.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(mesh, ObjStatus.UPDATE, lcnode.pid());
							}
						}
					} else {
						LcNodeMesh mesh = new LcNodeMesh();

						mesh.Unserialize(meshJson);

						mesh.setNodePid(lcnode.getPid());

						mesh.setMesh(lcnode.mesh());

						result.insertObject(mesh, ObjStatus.INSERT, lcnode.pid());
					}
				}
			}
		}

		return null;
	}

}
