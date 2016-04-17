package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;

public class Operation implements IOperation {

	private Command command;

	private RdNode rdnode;

	public Operation(Command command, RdNode rdnode) {
		this.command = command;

		this.rdnode = rdnode;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.getDelObjects().add(rdnode);
				
				return null;
			} else {

				boolean isChanged = rdnode.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(rdnode);
				}
			}
		}

		if (content.containsKey("forms")) {
			JSONArray forms = content.getJSONArray("forms");

			for (int i = 0; i < forms.size(); i++) {

				JSONObject formJson = forms.getJSONObject(i);

				if (formJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(formJson.getString("objStatus"))) {

						RdNodeForm form = rdnode.formMap
								.get(formJson.getString("rowId"));
						
						if (form == null){
							throw new Exception("rowId="+formJson.getString("rowId")+"的RdNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(formJson
								.getString("objStatus"))) {
							result.getDelObjects().add(form);
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(formJson
								.getString("objStatus"))) {

							boolean isChanged = form
									.fillChangeFields(formJson);

							if (isChanged) {
								result.getUpdateObjects().add(form);
							}
						}
					} else {
						RdNodeForm row = new RdNodeForm();
						
						row.Unserialize(formJson);
						
						row.setNodePid(rdnode.getPid());
						
						row.setMesh(rdnode.mesh());
						
						result.getAddObjects().add(row);
					}
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("meshes");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(meshJson.getString("objStatus"))) {

						RdNodeMesh row = rdnode.meshMap
								.get(meshJson.getString("rowId"));
						
						if (row == null){
							throw new Exception("rowId="+meshJson.getString("rowId")+"的RdNodeMesh不存在");
						}

						if (ObjStatus.DELETE.toString().equals(meshJson
								.getString("objStatus"))) {
							result.getDelObjects().add(row);
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(meshJson
								.getString("objStatus"))) {

							boolean isChanged = row
									.fillChangeFields(meshJson);

							if (isChanged) {
								result.getUpdateObjects().add(row);
							}
						}
					} else {
						RdNodeMesh row = new RdNodeMesh();
						
						row.Unserialize(meshJson);
						
						row.setNodePid(rdnode.getPid());
						
						row.setMesh(rdnode.mesh());
						
						result.getAddObjects().add(row);
					}
				}
			}
		}
		
		if (content.containsKey("names")) {
			JSONArray names = content.getJSONArray("names");

			for (int i = 0; i < names.size(); i++) {

				JSONObject json = names.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(json.getString("objStatus"))) {

						RdNodeName row = rdnode.nameMap
								.get(json.getString("rowId"));
						
						if (row == null){
							throw new Exception("rowId="+json.getString("rowId")+"的RdNodeName不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json
								.getString("objStatus"))) {
							result.getDelObjects().add(row);
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json
								.getString("objStatus"))) {

							boolean isChanged = row
									.fillChangeFields(json);

							if (isChanged) {
								result.getUpdateObjects().add(row);
							}
						}
					} else {
						RdNodeName row = new RdNodeName();
						
						row.Unserialize(json);
						
						row.setPid(PidService.getInstance().applyNodeNameId());
						
						row.setNodePid(rdnode.getPid());
						
						row.setMesh(rdnode.mesh());
						
						result.getAddObjects().add(row);
					}
				}
			}
		}
		
		return null;
	}

}
