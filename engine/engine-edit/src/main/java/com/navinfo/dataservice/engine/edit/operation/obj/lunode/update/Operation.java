package com.navinfo.dataservice.engine.edit.operation.obj.lunode.update;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(lunode, ObjStatus.DELETE, lunode.pid());
				return null;
			} else {
				boolean isChanged = lunode.fillChangeFields(content);
				if (isChanged) {
                    this.updateNodeFormAndMesh(lunode, result);
					result.insertObject(lunode, ObjStatus.UPDATE, lunode.pid());
				}
			}
		}

		return null;
	}

    private void updateNodeFormAndMesh(LuNode node, Result result) throws JSONException {
        if (!node.changedFields().containsKey("geometry")) {
            return;
        }

        node.changedFields().put("form", NodeOperateUtils.calcFormOfChangedFields(node));
        Geometry geometry = GeoTranslator.geojson2Jts((JSONObject) node.changedFields().get("geometry"));
        String[] meshes = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
        List<IRow> delMeshes = new ArrayList<>();
        List<String> addMeshes = new ArrayList<>(Arrays.asList(meshes));
        Iterator<String> addIterator = addMeshes.iterator();
        for (IRow row : node.getMeshes()) {
            LuNodeMesh mesh = (LuNodeMesh) row;
            if (!addMeshes.contains(String.valueOf(mesh.mesh()))) {
                delMeshes.add(mesh);
                continue;
            } else {
                while (addIterator.hasNext()) {
                    if (addIterator.next().equals(String.valueOf(mesh.mesh()))) {
                        addIterator.remove();
                    }
                }
            }
        }
        for (IRow row : delMeshes) {
            result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
        }
        for (String meshId : addMeshes) {
            LuNodeMesh mesh = new LuNodeMesh();
            mesh.setNodePid(node.pid());
            mesh.setMeshId(Integer.parseInt(meshId));
            result.insertObject(mesh, ObjStatus.INSERT, mesh.getNodePid());
        }
    }
}
