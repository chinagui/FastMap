package com.navinfo.dataservice.engine.edit.operation.obj.adnode.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
/**
 * @author zhaokk
 * 修改行政区划点操作基础类 
 */
public class Operation implements IOperation {

	private Command command;

	private AdNode adnode;

	public Operation(Command command, AdNode adnode) {
		this.command = command;

		this.adnode = adnode;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(adnode, ObjStatus.DELETE, adnode.pid());
				
				return null;
			} else {

				boolean isChanged = adnode.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(adnode, ObjStatus.UPDATE, adnode.pid());
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("adnode");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(meshJson.getString("objStatus"))) {

						AdNodeMesh mesh =adnode.meshMap
								.get(meshJson.getString("rowId"));
						
						if (mesh == null){
							throw new Exception("rowId="+meshJson.getString("rowId")+"的RdNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(meshJson
								.getString("objStatus"))) {
							result.insertObject(mesh, ObjStatus.DELETE, adnode.pid());
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(meshJson
								.getString("objStatus"))) {

							boolean isChanged = mesh
									.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(mesh, ObjStatus.UPDATE, adnode.pid());
							}
						}
					} else {
						AdNodeMesh mesh = new AdNodeMesh();
						
						mesh.Unserialize(meshJson);
						
						mesh.setNodePid(adnode.getPid());
						
						mesh.setMesh(adnode.mesh());
						
						result.insertObject(mesh, ObjStatus.INSERT, adnode.pid());
					}
				}
			}
		}

		return null;
	}

}
