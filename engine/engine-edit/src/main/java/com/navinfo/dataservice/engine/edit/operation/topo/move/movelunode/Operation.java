package com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.LuLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 移动土地利用点具体操作类
 */
public class Operation implements IOperation {

	private Command command;

	private LuNode updateNode;
	private Map<Integer, List<LuLink>> map;
	private Connection conn;

	public Operation(Command command, LuNode updateNode, Connection conn) {
		this.command = command;
		this.updateNode = updateNode;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		result.setPrimaryPid(updateNode.getPid());
		this.updateNodeGeometry(result);
		this.updateLinkGeomtry(result);
		this.updateFaceGeomtry(result);
		return null;
	}

	/*
	 * 更新土地利用点对应土地利用线信息
	 */
	private void updateLinkGeomtry(Result result) throws Exception {
		Map<Integer, List<LuLink>> map = new HashMap<Integer, List<LuLink>>();
		for (LuLink link : command.getLinks()) {
			int nodePid = updateNode.pid();

			double lon = command.getLongitude();

			double lat = command.getLatitude();

			Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
			Coordinate[] cs = geom.getCoordinates();
			double[][] ps = new double[cs.length][2];

			for (int i = 0; i < cs.length; i++) {
				ps[i][0] = cs[i].x;

				ps[i][1] = cs[i].y;
			}

			if (link.getsNodePid() == nodePid) {
				ps[0][0] = lon;

				ps[0][1] = lat;
			}
			if (link.geteNodePid() == nodePid) {
				ps[ps.length - 1][0] = lon;

				ps[ps.length - 1][1] = lat;
			}
			JSONObject geojson = new JSONObject();
			geojson.put("type", "LineString");
			geojson.put("coordinates", ps);
			Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
			// 修改线的几何属性
			// 如果没有跨图幅只是修改线的几何
			List<LuLink> links = new ArrayList<LuLink>();
			if (meshes.size() == 1) {
				JSONObject updateContent = new JSONObject();
				updateContent.put("geometry", geojson);
				updateContent.put("length", GeometryUtils.getLinkLength(geo));
				link.fillChangeFields(updateContent);
				link.setGeometry(geo);
				LuLink luLink = new LuLink();
				luLink.copy(link);
				luLink.setGeometry(GeoTranslator.geojson2Jts(geojson, 100000, 5));
				links.add(luLink);
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.UPDATE, link.pid());
				// 如果跨图幅就需要打断生成新的link
			} else {
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(geo.getCoordinates()[0], link.getsNodePid());
				maps.put(geo.getCoordinates()[link.getGeometry().getCoordinates().length - 1], link.geteNodePid());
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo, MeshUtils.mesh2Jts(meshIdStr));
					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
					links.addAll(LuLinkOperateUtils.getCreateLuLinksWithMesh(geomInter, maps, result));

				}
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.DELETE, link.pid());
			}

		}
		this.map = map;
	}

	/*
	 * 更新土地利用点信息
	 */
	private void updateNodeGeometry(Result result) throws Exception {

		// 计算点的几何形状
		JSONObject geojson = new JSONObject();
		geojson.put("type", "Point");
		geojson.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });
		JSONObject updateNodeJson = new JSONObject();
		// 要移动点的project_id
		updateNodeJson.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 移动点的新几何
		data.put("geometry", geojson);
		data.put("pid", updateNode.pid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateNodeJson.put("data", data);

		// 组装打断线的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Command(
				updateNodeJson, command.getRequester());
		com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Process(
				updatecommand, result, conn);
		process.innerRun();
	}

	/*
	 * 更新土地利用面的信息
	 * 
	 */
	private void updateFaceGeomtry(Result result) throws Exception {
		if (command.getFaces() != null && command.getFaces().size() > 0) {

			for (LuFace face : command.getFaces()) {
				boolean flag = false;
				List<LuLink> links = new ArrayList<LuLink>();
				for (IRow iRow : face.getFaceTopos()) {
					LuFaceTopo obj = (LuFaceTopo) iRow;
					if (this.map.containsKey(obj.getLinkPid())) {
						if (this.map.get(obj.getLinkPid()).size() > 1) {
							flag = true;
						}
						links.addAll(this.map.get(obj.getLinkPid()));
					} else {
						links.add((LuLink) new LuLinkSelector(conn).loadById(obj.getLinkPid(), true));
					}

					result.insertObject(obj, ObjStatus.DELETE, face.getPid());

				}
				if (flag) {
					// 如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation(
							result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByLuLink(objs);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				} else {
					// 如果不跨图幅只需要维护面的行政几何
					com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation(
							result, face);
					opFace.reCaleFaceGeometry(links);
				}

			}
		}

	}
}