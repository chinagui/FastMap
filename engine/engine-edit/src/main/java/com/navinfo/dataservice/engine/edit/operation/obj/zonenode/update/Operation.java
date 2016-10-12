package com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
/**
 * @author zhaokk
 * 修改行政区划点操作基础类 
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(this.command.getZoneNode(), ObjStatus.DELETE, this.command.getZoneNode().getPid());
				
				return null;
			} else {

				boolean isChanged = this.command.getZoneNode().fillChangeFields(content);

				if (isChanged) {
					result.insertObject(this.command.getZoneNode(), ObjStatus.UPDATE, this.command.getZoneNode().getPid());
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

						ZoneNodeMesh mesh =this.command.getZoneNode().meshMap
								.get(meshJson.getString("rowId"));
						
						if (mesh == null){
							throw new Exception("rowId="+meshJson.getString("rowId")+"的ZoneNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(meshJson
								.getString("objStatus"))) {
							result.insertObject(mesh, ObjStatus.DELETE, mesh.parentPKValue());
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(meshJson
								.getString("objStatus"))) {

							boolean isChanged = mesh
									.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(mesh, ObjStatus.UPDATE, mesh.parentPKValue());
							}
						}
					} else {
						ZoneNodeMesh mesh = new ZoneNodeMesh();
						
						mesh.Unserialize(meshJson);
						
						mesh.setNodePid(this.command.getPid());
		
						result.insertObject(mesh, ObjStatus.INSERT, mesh.parentPKValue());
					}
				}
			}
		}

		return null;
	}

}
