package com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.ZoneLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @author zhaokk 移动ZONE点操作类 移动ZONE点 点不会打断其它的ZONE线
 */
public class Operation implements IOperation {

	private Command command;
	private Map<Integer, List<ZoneLink>> map;
	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		result.setPrimaryPid(this.command.getNodePid());
		this.updateNodeGeometry(result);
		this.updateLinkGeomtry(result);
		this.updateFaceGeomtry(result);
		return null;
	}

	/*
	 * 移动行政区划点修改对应的线的信息
	 */
	private void updateLinkGeomtry(Result result) throws Exception {
		Map<Integer, List<ZoneLink>> map = new HashMap<Integer, List<ZoneLink>>();
		for (ZoneLink link : command.getLinks()) {
			int nodePid = this.command.getNodePid();

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
			List<ZoneLink> links = new ArrayList<ZoneLink>();
			if (meshes.size() == 1) {
				JSONObject updateContent = new JSONObject();
				updateContent.put("geometry", geojson);
				updateContent.put("length", GeometryUtils.getLinkLength(geo));
				link.fillChangeFields(updateContent);
				ZoneLink zoneLink = new ZoneLink();
				zoneLink.copy(link);
				zoneLink.setPid(link.getPid());
				zoneLink.setGeometry(GeoTranslator.geojson2Jts(geojson, 100000, 5));
				links.add(zoneLink);
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
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo, GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),1,5));
					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
					links.addAll(ZoneLinkOperateUtils.getCreateZoneLinksWithMesh(geomInter, maps, result,link));

				}
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.DELETE, link.pid());
			}
			updataRelationObj(link, links, result);
		}
		this.map = map;
	}

	/**
	 * 维护关联要素
	 * 
	 * @param link
	 * @param links
	 * @param result
	 * @throws Exception
	 */
	private void updataRelationObj(ZoneLink link, List<ZoneLink> links, Result result) throws Exception {
		// 同一点关系
		JSONObject updateJson = this.command.getJson();

		if (updateJson.containsKey("mainType")) {
			String mainType = updateJson.getString("mainType");
			if (mainType.equals(ObjType.ZONENODE.toString())) {
				com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
						null, this.conn);
				sameNodeOperation.moveMainNodeForTopo(this.command.getJson(), ObjType.ZONENODE, result);
			}
		} else {
			com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
					null, this.conn);
			sameNodeOperation.moveMainNodeForTopo(this.command.getJson(), ObjType.ZONENODE, result);
		}

	}

	/*
	 * 移动行政区划点修改对应的点的信息
	 */
	private void updateNodeGeometry(Result result) throws Exception {
		String meshes[] = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

		Set<String> meshSet = new HashSet<String>(Arrays.asList(meshes));

		boolean isChangeMesh = false;
		
		ZoneNode updateNode = this.command.getZoneNode();

		for (IRow row : updateNode.getMeshes()) {
			ZoneNodeMesh nodeMesh = (ZoneNodeMesh) row;
		

			if (!meshSet.contains(String.valueOf(nodeMesh.getMeshId()))) {
				isChangeMesh = true;
				break;
			}
		}
		//图幅号发生改变后更新图幅号：先删除，后新增
		if (isChangeMesh) {
			for (IRow row : updateNode.getMeshes()) {
				ZoneNodeMesh nodeMesh = (ZoneNodeMesh) row;

				result.insertObject(nodeMesh, ObjStatus.DELETE, updateNode.getPid());
			}

			for (String mesh : meshes) {

				ZoneNodeMesh nodeMesh = new ZoneNodeMesh();
				nodeMesh.setNodePid(updateNode.getPid());
				nodeMesh.setMeshId(Integer.parseInt(mesh));

				result.insertObject(nodeMesh, ObjStatus.INSERT, updateNode.getPid());
			}
		}
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
		data.put("pid", this.command.getNodePid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateNodeJson.put("data", data);

		// 组装打断线的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Command(
				updateNodeJson, command.getRequester());
		com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Process(
				updatecommand, result, conn);
		process.innerRun();
	}

	/**
	 * 移动Adnode 修改行政区划面信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateFaceGeomtry(Result result) throws Exception {
		if (command.getFaces() != null && command.getFaces().size() > 0) {

			for (ZoneFace face : command.getFaces()) {
				boolean flag = false;
				List<ZoneLink> links = new ArrayList<ZoneLink>();
				for (IRow iRow : face.getFaceTopos()) {
					ZoneFaceTopo obj = (ZoneFaceTopo) iRow;
					if (this.map.containsKey(obj.getLinkPid())) {
						if (this.map.get(obj.getLinkPid()).size() > 1) {
							flag = true;
						}
						links.addAll(this.map.get(obj.getLinkPid()));
					} else {
						links.add((ZoneLink) new ZoneLinkSelector(conn).loadById(obj.getLinkPid(), true));
					}

					result.insertObject(obj, ObjStatus.DELETE, face.getPid());

				}
				if (flag) {
					// 如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(
							result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByZoneLink(objs);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				} else {
					// 如果不跨图幅只需要维护面的行政几何
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(
							result, face);
					opFace.reCaleFaceGeometry(links);
				}

			}
		}

	}
}