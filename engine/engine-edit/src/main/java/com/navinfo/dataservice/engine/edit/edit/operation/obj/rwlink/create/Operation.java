package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.edit.comm.util.EditUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RwLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Check check;

	private Connection conn;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		if (command.getGeometry().getCoordinates().length < 2) {
			throw new Exception("线至少包含两个点");
		}

		Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();
		
		//有挂接的线
		if (command.getCatchLinks().size() > 0) {
			map = RwLinkOperateUtils.splitRwLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);

		}
		
		//没有挂接的线
		if (command.getCatchLinks().size() == 0 || map.size() == 0) {
			JSONObject se = new JSONObject();

			se = RwLinkOperateUtils.createRwNodeForLink(command.getGeometry(), command.getsNodePid(),
					command.geteNodePid(), result);

			map.put(command.getGeometry(), se);
		}

		// 创建线信息
		this.createRwLinks(map, result);
		// 挂接的线被打断的操作
		this.breakLine(result);
		
		//往result中设置新增的pid（setPrimaryPid）
		EditUtils.handleResult(RwLink.class, result);
		
		return msg;
	}

	/*
	 * 创建RwLink 不跨图幅生成线
	 */
	private void createRwLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
		if (g != null) {
			JSONObject node = RwLinkOperateUtils.createRwNodeForLink(g, sNodePid, eNodePid, result);
			RwLink link = RwLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);

			link.setKind(command.getKind());

			link.setForm(command.getForm());

			result.insertObject(link, ObjStatus.INSERT, link.pid());
		}
	}

	/*
	 * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	private void createRwLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
			// 不跨图幅
			if (meshes.size() == 1) {
				createRwLinkWithNoMesh(g, (int) map.get(g).get("s"), (int) map.get(g).get("e"), result);
			}
			// 跨图幅
			else {
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(g.getCoordinates()[0], (int) map.get(g).get("s"));
				maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) map.get(g).get("e"));
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(g, MeshUtils.mesh2Jts(meshIdStr));
					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
					RwLinkOperateUtils.getCreateRwLinksWithMesh(geomInter, maps, result);
				}
			}

		}

	}

	/**
	 * RWLINK打断操作
	 * @param result 结果集
	 * @throws Exception
	 */
	private void breakLine(Result result) throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject json = command.getCatchLinks().getJSONObject(i);
			if (json.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				breakJson.put("objId", json.getInt("linkPid"));
				breakJson.put("dbId", command.getDbId());
				JSONObject data = new JSONObject();
				data.put("breakNodePid", json.getInt("breakNode"));
				data.put("longitude", json.getDouble("lon"));
				data.put("latitude", json.getDouble("lat"));
				breakJson.put("data", data);
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint.Process(
						breakCommand,result,conn);
				breakProcess.innerRun();
			}
		}
	}

}
