package com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Operation implements IOperation {

    private Command command;

    private RdNode rdnode;

    private Connection conn;

    public Operation(Command command, RdNode rdnode) {
        this.command = command;
        this.rdnode = rdnode;
    }

    public Operation(Command command, RdNode rdnode, Connection conn) {
        this.command = command;
        this.rdnode = rdnode;
        this.conn = conn;
    }

	@Override
	public String run(Result result) throws Exception {

		if (null != command.getContent()) {
			updateNode(result, command.getContent());
		}
		// 批量修改
		else if (null != command.getUpdateContents()) {

			for (int i = 0; i < command.getUpdateContents().size(); i++) {

				JSONObject content = command.getUpdateContents().getJSONObject(
						i);

				int nodePid = content.getInt("pid");

				rdnode = command.getNodeMap().get(nodePid);

				updateNode(result, content);
			}
		}

		return null;
	}

	public String updateNode(Result result, JSONObject content)
			throws Exception {

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(rdnode, ObjStatus.DELETE, rdnode.pid());
				return null;
			} else {
				boolean isChanged = rdnode.fillChangeFields(content);
				if (content.containsKey("geometry")) {
					Geometry geo = GeoTranslator.geojson2Jts(content
							.getJSONObject("geometry"));
					List<String> meshes = new ArrayList<>();
					meshes.addAll(Arrays.asList(MeshUtils.point2Meshes(
							geo.getCoordinate().x, geo.getCoordinate().y)));

					List<IRow> nodeMeshes = new AbstractSelector(
							RdNodeMesh.class, conn).loadRowsByParentId(
							rdnode.pid(), false);
					rdnode.getMeshes().clear();
					int count = 0;
					for (IRow row : nodeMeshes) {
						RdNodeMesh mesh = (RdNodeMesh) row;
						String meshId = String.valueOf(mesh.getMeshId());
						if (meshes.contains(String.valueOf(meshId))) {
							meshes.remove(meshId);
							count++;
						} else {
							result.insertObject(mesh, ObjStatus.DELETE,
									mesh.parentPKValue());
						}
					}
					for (String meshId : meshes) {
						RdNodeMesh mesh = new RdNodeMesh();
						mesh.setNodePid(rdnode.pid());
						mesh.setMeshId(Integer.valueOf(meshId));
						result.insertObject(mesh, ObjStatus.INSERT,
								mesh.parentPKValue());
						count++;
					}

					rdnode.getForms().clear();
					List<IRow> forms = new AbstractSelector(RdNodeForm.class,
							conn).loadRowsByParentId(rdnode.pid(), false);
					if (count >= 2) {
						boolean flag = true;
						for (IRow f : forms) {
							RdNodeForm form = (RdNodeForm) f;
							if (form.getFormOfWay() == 1) {
								result.insertObject(form, ObjStatus.DELETE,
										form.parentPKValue());
							}
							if (form.getFormOfWay() == 2) {
								flag = false;
							}
						}
						if (flag) {
							RdNodeForm form = new RdNodeForm();
							form.setNodePid(rdnode.pid());
							form.setFormOfWay(2);
							result.insertObject(form, ObjStatus.INSERT,
									form.parentPKValue());
						}
					} else {
						for (IRow f : forms) {
							RdNodeForm form = (RdNodeForm) f;
							if (form.getFormOfWay() == 2) {
								result.insertObject(form, ObjStatus.DELETE,
										form.parentPKValue());
								if (forms.size() == 1) {
									RdNodeForm ff = new RdNodeForm();
									ff.setFormOfWay(1);
									ff.setNodePid(rdnode.pid());
									result.insertObject(ff, ObjStatus.INSERT,
											ff.parentPKValue());
								}
							}
						}
					}
				}

				if (isChanged) {
					result.insertObject(rdnode, ObjStatus.UPDATE, rdnode.pid());
				}
			}
		}

		if (content.containsKey("forms")) {
			JSONArray forms = content.getJSONArray("forms");

			for (int i = 0; i < forms.size(); i++) {

				JSONObject formJson = forms.getJSONObject(i);

				if (formJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							formJson.getString("objStatus"))) {

						RdNodeForm form = rdnode.formMap.get(formJson
								.getString("rowId"));

						if (form == null) {
							throw new Exception("rowId="
									+ formJson.getString("rowId")
									+ "的RdNodeForm不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								formJson.getString("objStatus"))) {
							result.insertObject(form, ObjStatus.DELETE,
									rdnode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								formJson.getString("objStatus"))) {

							boolean isChanged = form.fillChangeFields(formJson);

							if (isChanged) {
								result.insertObject(form, ObjStatus.UPDATE,
										rdnode.pid());
							}
						}
					} else {
						RdNodeForm row = new RdNodeForm();

						row.Unserialize(formJson);

						row.setNodePid(rdnode.getPid());

						row.setMesh(rdnode.mesh());

						result.insertObject(row, ObjStatus.INSERT, rdnode.pid());
					}
				}
			}
		}

		if (content.containsKey("meshes")) {
			JSONArray meshes = content.getJSONArray("meshes");

			for (int i = 0; i < meshes.size(); i++) {

				JSONObject meshJson = meshes.getJSONObject(i);

				if (meshJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							meshJson.getString("objStatus"))) {

						RdNodeMesh row = rdnode.meshMap.get(meshJson
								.getString("rowId"));

						if (row == null) {
							throw new Exception("rowId="
									+ meshJson.getString("rowId")
									+ "的RdNodeMesh不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								meshJson.getString("objStatus"))) {
							result.insertObject(row, ObjStatus.DELETE,
									rdnode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								meshJson.getString("objStatus"))) {

							boolean isChanged = row.fillChangeFields(meshJson);

							if (isChanged) {
								result.insertObject(row, ObjStatus.UPDATE,
										rdnode.pid());
							}
						}
					} else {
						RdNodeMesh row = new RdNodeMesh();

						row.Unserialize(meshJson);

						row.setNodePid(rdnode.getPid());

						row.setMesh(rdnode.mesh());

						result.insertObject(row, ObjStatus.INSERT, rdnode.pid());
					}
				}
			}
		}

		if (content.containsKey("names")) {
			JSONArray names = content.getJSONArray("names");

			for (int i = 0; i < names.size(); i++) {

				JSONObject json = names.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							json.getString("objStatus"))) {

						RdNodeName row = rdnode.nameMap.get(json
								.getString("rowId"));

						if (row == null) {
							throw new Exception("rowId="
									+ json.getString("rowId")
									+ "的RdNodeName不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								json.getString("objStatus"))) {
							result.insertObject(row, ObjStatus.DELETE,
									rdnode.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								json.getString("objStatus"))) {

							boolean isChanged = row.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(row, ObjStatus.UPDATE,
										rdnode.pid());
							}
						}
					} else {
						RdNodeName row = new RdNodeName();

						row.Unserialize(json);

						row.setPid(PidUtil.getInstance().applyNodeNameId());

						row.setNodePid(rdnode.getPid());

						row.setMesh(rdnode.mesh());

						result.insertObject(row, ObjStatus.INSERT, rdnode.pid());
					}
				}
			}
		}

		return null;
	}

}
