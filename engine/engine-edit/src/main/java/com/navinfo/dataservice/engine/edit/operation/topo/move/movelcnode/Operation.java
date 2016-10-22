package com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode;

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
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private LcNode updateNode;
	private Map<Integer, List<LcLink>> map;
	private Connection conn;

	public Operation(Command command, LcNode updateNode, Connection conn) {
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

	private void updateLinkGeomtry(Result result) throws Exception {
		Map<Integer, List<LcLink>> map = new HashMap<Integer, List<LcLink>>();
		for (LcLink link : command.getLinks()) {
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
			List<LcLink> links = new ArrayList<LcLink>();
			if (meshes.size() == 1) {
				JSONObject updateContent = new JSONObject();
				updateContent.put("geometry", geojson);
				updateContent.put("length", GeometryUtils.getLinkLength(geo));
				link.fillChangeFields(updateContent);
				LcLink adLink = new LcLink();
				adLink.copy(link);
				adLink.setGeometry(GeoTranslator.geojson2Jts(geojson, 100000, 5));
				links.add(adLink);
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
					links.addAll(LcLinkOperateUtils.getCreateLcLinksWithMesh(geomInter, maps, result,link));

				}
				handleRdGsc(link, result);
				map.put(link.getPid(), links);
				result.insertObject(link, ObjStatus.DELETE, link.pid());
			}

		}
		this.map = map;
	}

	
	/**
	 * 处理立交关系
	 * 
	 * @param link
	 * @throws Exception
	 */
	private void handleRdGsc(LcLink deleteLink, Result result) throws Exception {

		int newLinkPid = 0;

		// 获取 使用未移动的node做端点的新生成的link，该link继承原link的所有立交关系
		for (IRow row : result.getAddObjects()) {

			if (row.objType() != ObjType.LCLINK) {
				continue;
			}

			LcLink lcLink = (LcLink) row;

			if (lcLink.geteNodePid() == this.command.getNodePid()
					|| lcLink.getsNodePid() == this.command.getNodePid()) {

				newLinkPid = lcLink.pid();

				break;
			}
		}

		if (newLinkPid == 0) {
			return;
		}

		RdGscSelector selector = new RdGscSelector(this.conn);

		List<RdGsc> rdGscs = selector.loadRdGscLinkByLinkPid(deleteLink.getPid(), "LC_LINK", true);

		// 将降立交关系link的pid更新为新生成link的pid
		for (RdGsc gsc : rdGscs) {

			for (RdGscLink gscLink : gsc.rdGscLinkMap.values()) {

				if (gscLink.getLinkPid() == deleteLink.pid() && gscLink.getTableName().equals("LC_LINK")) {

					JSONObject updateContent = new JSONObject();

					updateContent.put("linkPid", newLinkPid);

					gscLink.fillChangeFields(updateContent);

					result.insertObject(gscLink, ObjStatus.UPDATE, gsc.pid());
				}
			}
		}
	}
	
	private void updateNodeGeometry(Result result) throws Exception {
		String meshes[] = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

		Set<String> meshSet = new HashSet<String>(Arrays.asList(meshes));

		boolean isChangeMesh = false;

		for (IRow row : updateNode.getMeshes()) {
			LcNodeMesh nodeMesh = (LcNodeMesh) row;
		

			if (!meshSet.contains(String.valueOf(nodeMesh.getMeshId()))) {
				isChangeMesh = true;
				break;
			}
		}
		//图幅号发生改变后更新图幅号：先删除，后新增
		if (isChangeMesh) {
			for (IRow row : updateNode.getMeshes()) {
				LcNodeMesh nodeMesh = (LcNodeMesh) row;

				result.insertObject(nodeMesh, ObjStatus.DELETE, updateNode.getPid());
			}

			for (String mesh : meshes) {

				LcNodeMesh nodeMesh = new LcNodeMesh();
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
		data.put("pid", updateNode.pid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateNodeJson.put("data", data);
		// 组装打断线的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command(
				updateNodeJson, command.getRequester());
		com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process(
				updatecommand, result, conn);
		process.innerRun();
	}

	private void updateFaceGeomtry(Result result) throws Exception {
		if (command.getFaces() != null && command.getFaces().size() > 0) {
			for (LcFace face : command.getFaces()) {
				boolean flag = false;
				List<LcLink> links = new ArrayList<LcLink>();
				for (IRow iRow : face.getTopos()) {
					LcFaceTopo obj = (LcFaceTopo) iRow;
					if (this.map.containsKey(obj.getLinkPid())) {
						if (this.map.get(obj.getLinkPid()).size() > 1) {
							flag = true;
						}
						links.addAll(this.map.get(obj.getLinkPid()));
					} else {
						links.add((LcLink) new LcLinkSelector(conn).loadById(obj.getLinkPid(), true));
					}
					result.insertObject(obj, ObjStatus.DELETE, face.getPid());
				}
				if (flag) {
					// 如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
							result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByLcLink(objs);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				} else {
					// 如果不跨图幅只需要维护面的行政几何
					com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
							result, face);
					opFace.reCaleFaceGeometry(links);
				}

			}
		}

	}
}