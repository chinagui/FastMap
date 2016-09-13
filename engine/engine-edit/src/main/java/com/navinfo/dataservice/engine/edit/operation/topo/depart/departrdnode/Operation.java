package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.departNode(result);

		return null;
	}

	private void departNode(Result result) throws Exception {
		if (this.command.getCatchNodePid() == 0) {
			if (this.command.getLinks().size() == 1) {
				this.removeNode(result);
			}
			if (this.command.getLinks().size() > 1) {
				RdNode node = NodeOperateUtils.createRdNode(this.command
						.getPoint().getX(), this.command.getPoint().getY());
				result.insertObject(node, ObjStatus.INSERT, node.pid());
				this.updateLinkGeomtry(result, node.getPid());
			}

		} else {
			if (this.command.getLinks().size() == 1) {
				result.insertObject(this.command.getNode(), ObjStatus.DELETE,
						this.command.getNodePid());
				this.updateLinkGeomtry(result, this.command.getCatchNodePid());
			}
		}
	}

	private void removeNode(Result result) throws Exception {
		JSONObject updateContent = new JSONObject();

		updateContent.put("dbId", command.getDbId());

		JSONObject data = new JSONObject();

		data.put("longitude", this.command.getPoint().getX());

		data.put("latitude", this.command.getPoint().getX());
		updateContent.put("objId", this.command.getNodePid());
		updateContent.put("data", data);
		com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(
				updateContent, this.command.getRdLink());
		com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(
				updatecommand, result, conn);
		process.innerRun();
	}

	private void updateLinkGeomtry(Result result, int nodePid) throws Exception {
		Set<String> meshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(this.command.getGeometry());
		// 修改线的几何属性
		// 如果没有跨图幅只是修改线的几何
		List<RdLink> links = new ArrayList<RdLink>();
		if (meshes.size() == 1) {
			JSONObject updateContent = new JSONObject();
			if (this.command.getGeometry().getCoordinates()[0]
					.equals(this.command.getPoint().getCoordinate())) {
				updateContent.put("sNodePid", nodePid);
			} else {
				updateContent.put("eNodePid", nodePid);
			}
			updateContent.put("geometry",
					GeoTranslator.jts2Geojson(this.command.getGeometry()));
			updateContent.put("length",
					GeometryUtils.getLinkLength(this.command.getGeometry()));
			this.command.getRdLink().fillChangeFields(updateContent);
			result.insertObject(this.command.getRdLink(), ObjStatus.UPDATE,
					this.command.getLinkPid());
			// 如果跨图幅就需要打断生成新的link
		} else {
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			if (this.command.getGeometry().getCoordinates()[0]
					.equals(this.command.getPoint().getCoordinate())) {
				maps.put(this.command.getGeometry().getCoordinates()[0],
						nodePid);

				maps.put(
						this.command.getGeometry().getCoordinates()[this.command
								.getGeometry().getCoordinates().length - 1],
						this.command.getRdLink().geteNodePid());
			} else {
				maps.put(this.command.getGeometry().getCoordinates()[0],
						this.command.getRdLink().geteNodePid());

				maps.put(
						this.command.getGeometry().getCoordinates()[this.command
								.getGeometry().getCoordinates().length - 1],
						nodePid);
			}

			Iterator<String> it = meshes.iterator();
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(
						this.command.getGeometry(),
						MeshUtils.mesh2Jts(meshIdStr));
				geomInter = GeoTranslator.geojson2Jts(
						GeoTranslator.jts2Geojson(geomInter), 1, 5);
				RdLinkOperateUtils.createRdLinkWithMesh(geomInter, maps,
						this.command.getRdLink(), result, links);

			}

			result.insertObject(this.command.getRdLink(), ObjStatus.DELETE,
					this.command.getRdLink().pid());

		}

	}

}
