package com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.utils.RwLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

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

		return null;
	}

	private void updateLinkGeomtry(Result result) throws Exception {

		for (RwLink link : command.getLinks()) {

			JSONObject geojson = getGeojson(link);

			Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);

			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);

			if (meshes.size() == 1) {

				JSONObject updateContent = new JSONObject();

				updateContent.put("geometry", geojson);

				updateContent.put("length", GeometryUtils.getLinkLength(geo));

				link.fillChangeFields(updateContent);

				result.insertObject(link, ObjStatus.UPDATE, link.pid());

			} else {
				// 如果跨图幅就需要打断生成新的link
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();

				maps.put(geo.getCoordinates()[0], link.getsNodePid());

				maps.put(geo.getCoordinates()[geo.getCoordinates().length - 1], link.geteNodePid());

				Iterator<String> it = meshes.iterator();

				while (it.hasNext()) {

					String meshIdStr = it.next();

					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo, GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),1,5));

					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

					RwLinkOperateUtils.createRwLinkWithMesh(geomInter, maps, result,link);
				}

				handleRdGsc(link, result);

				result.insertObject(link, ObjStatus.DELETE, link.pid());
			}

			updataRelationObj(result);
		}
	}

	/**
	 * 维护关联要素
	 * 
	 * @param link
	 * @param links
	 * @param result
	 * @throws Exception
	 */
	private void updataRelationObj(Result result) throws Exception {
	}

	/*
	 * 修改对应的点的信息
	 */
	private void updateNodeGeometry(Result result) throws Exception {

		// 计算点的几何形状
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });

		RwNode rwNode = this.command.getUpdateNode();

		String meshes[] = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

		Set<String> meshSet = new HashSet<String>(Arrays.asList(meshes));

		boolean isChangeMesh = false;

		for (IRow row : rwNode.getMeshes()) {
			RwNodeMesh nodeMesh = (RwNodeMesh) row;

			if (!meshSet.contains(String.valueOf(nodeMesh.getMeshId()))) {
				isChangeMesh = true;
				break;
			}
		}
		//图幅号发生改变后更新图幅号：先删除，后新增
		if (isChangeMesh) {
			for (IRow row : rwNode.getMeshes()) {
				RwNodeMesh nodeMesh = (RwNodeMesh) row;

				result.insertObject(nodeMesh, ObjStatus.DELETE, rwNode.getPid());
			}

			for (String mesh : meshes) {

				RwNodeMesh nodeMesh = new RwNodeMesh();
				nodeMesh.setNodePid(rwNode.getPid());
				nodeMesh.setMeshId(Integer.parseInt(mesh));

				result.insertObject(nodeMesh, ObjStatus.INSERT, rwNode.getPid());
			}
		}

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
		com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command(
				updateNodeJson, command.getRequester());

		com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process(
				updatecommand, result, conn);
		// 保证是同一个连接
		process.innerRun();
	}

	/**
	 * 处理立交关系
	 * 
	 * @param link
	 * @throws Exception
	 */
	private void handleRdGsc(RwLink deleteLink, Result result) throws Exception {

		int newLinkPid = 0;

		// 获取 使用未移动的node做端点的新生成的link，该link继承原link的所有立交关系
		for (IRow row : result.getAddObjects()) {

			if (row.objType() != ObjType.RWLINK) {
				continue;
			}

			RwLink rwLink = (RwLink) row;

			if (rwLink.geteNodePid() == this.command.getNodePid()
					|| rwLink.getsNodePid() == this.command.getNodePid()) {

				newLinkPid = rwLink.pid();

				break;
			}
		}

		if (newLinkPid == 0) {
			return;
		}

		RdGscSelector selector = new RdGscSelector(this.conn);

		List<RdGsc> rdGscs = selector.loadRdGscLinkByLinkPid(deleteLink.getPid(), "RW_LINK", true);

		// 将降立交关系link的pid更新为新生成link的pid
		for (RdGsc gsc : rdGscs) {

			for (RdGscLink gscLink : gsc.rdGscLinkMap.values()) {

				if (gscLink.getLinkPid() == deleteLink.pid() && gscLink.getTableName().equals("RW_LINK")) {

					JSONObject updateContent = new JSONObject();

					updateContent.put("linkPid", newLinkPid);

					gscLink.fillChangeFields(updateContent);

					result.insertObject(gscLink, ObjStatus.UPDATE, gsc.pid());
				}
			}
		}
	}

	/**
	 * 获取link更新后的 几何josn对象
	 * 
	 * @param link
	 * @return
	 */
	private JSONObject getGeojson(RwLink link) {
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
		} else {
			ps[ps.length - 1][0] = lon;

			ps[ps.length - 1][1] = lat;
		}
		JSONObject geojson = new JSONObject();

		geojson.put("type", "LineString");

		geojson.put("coordinates", ps);

		return geojson;
	}

}
