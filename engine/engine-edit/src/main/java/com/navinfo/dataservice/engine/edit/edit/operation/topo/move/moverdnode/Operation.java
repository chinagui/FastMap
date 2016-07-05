package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.moverdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdNode updateNode;
	
	private Connection conn;
	
	private Map<Integer, List<RdLink>> map;

	public Operation(Command command, RdNode updateNode,Connection conn) {
		this.command = command;

		this.updateNode = updateNode;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.setPrimaryPid(updateNode.getPid());

		this.updateNodeGeometry(result);

		this.updateLinkGeomtry(result);

		return null;
	}

	private void updateLinkGeomtry(Result result) throws Exception {
		Map<Integer, List<RdLink>> map = new HashMap<Integer, List<RdLink>>();
		for (RdLink link : command.getLinks()) {

			Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);

			Coordinate[] cs = geom.getCoordinates();

			double[][] ps = new double[cs.length][2];

			for (int i = 0; i < cs.length; i++) {
				ps[i][0] = cs[i].x;

				ps[i][1] = cs[i].y;
			}

			if (link.getsNodePid() == command.getNodePid()) {
				ps[0][0] = command.getLongitude();

				ps[0][1] = command.getLatitude();
			} else {
				ps[ps.length - 1][0] = command.getLongitude();

				ps[ps.length - 1][1] = command.getLatitude();
			}

			JSONObject geojson = new JSONObject();

			geojson.put("type", "LineString");

			geojson.put("coordinates", ps);

			Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);
			Set<String> meshes =  CompGeometryUtil.geoToMeshesWithoutBreak(geo);
			// 修改线的几何属性
			// 如果没有跨图幅只是修改线的几何
			link.setGeometry(geo);
			List<RdLink> links = new ArrayList<RdLink>();
			if (meshes.size() == 1) {
				JSONObject updateContent = new JSONObject();
				updateContent.put("geometry", geojson);
				updateContent.put("length", GeometryUtils.getLinkLength(geo));
				link.fillChangeFields(updateContent);
				
				links.add(link);
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.UPDATE, link.pid());
			//如果跨图幅就需要打断生成新的link
			}else{
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(link.getGeometry().getCoordinates()[0], link.getsNodePid());
				maps.put(link.getGeometry().getCoordinates()[link.getGeometry().getCoordinates().length-1], link.geteNodePid());
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
							MeshUtils.mesh2Jts(meshIdStr));
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);
					RdLinkOperateUtils.createRdLinkWithMesh(geomInter, maps,link,result,links);

				}
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.DELETE, link.pid());
			}
		}
		this.map = map;
	}

	private void updateNodeGeometry(Result result) throws Exception {
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });

		JSONObject updateContent = new JSONObject();

		// 要移动点的dbId
		updateContent.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 移动点的新几何
		data.put("geometry", geojson);
		data.put("pid", updateNode.pid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateContent.put("data", data);

		// 组装更新线的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update.Command(
				updateContent, command.getRequester());
		com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update.Process process = new com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update.Process(
				updatecommand, result, conn);
		process.innerRun();
	}
}
