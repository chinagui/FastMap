package com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;
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
                    this.updateNodeFormAndMesh(lcnode, result);
					result.insertObject(lcnode, ObjStatus.UPDATE, lcnode.pid());
				}
			}
		}
		return null;
	}

    private void updateNodeFormAndMesh(LcNode node, Result result) throws JSONException {
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
            LcNodeMesh mesh = (LcNodeMesh) row;
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
            LcNodeMesh mesh = new LcNodeMesh();
            mesh.setNodePid(node.pid());
            mesh.setMeshId(Integer.parseInt(meshId));
            result.insertObject(mesh, ObjStatus.INSERT, mesh.getNodePid());
        }
    }
}
