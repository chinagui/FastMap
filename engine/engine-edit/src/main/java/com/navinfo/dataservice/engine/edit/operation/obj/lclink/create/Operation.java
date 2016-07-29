package com.navinfo.dataservice.engine.edit.operation.obj.lclink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;
		Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();
		// 如果创建土地覆盖线有对应的挂接LCNODE和LCFACE
		// 执行挂接线处理逻辑
		if (command.getCatchLinks().size() > 0) {
			map = LcLinkOperateUtils.splitLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);
		}
		// 如果创建土地覆盖线没有对应的挂接LCNODE和LCFACE
		// 创建对应的LCNODE
		if (command.getCatchLinks().size() == 0 || map.size() == 0) {
			JSONObject se = new JSONObject();
			se = LcLinkOperateUtils.createLcNodeForLink(command.getGeometry(), command.getsNodePid(),
					command.geteNodePid(), result);
			map.put(command.getGeometry(), se);
		}
		// 创建土地覆盖线信息
		this.createLcLinks(map, result);
		// 挂接的线被打断的操作
		this.breakLine(result);

		return msg;
	}
	/*
	 * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	public void createLcLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
			// 不跨图幅
			if (meshes.size() == 1) {
				this.createLcLinkWithNoMesh(g, (int) map.get(g).get("s"), (int) map.get(g).get("e"), result);
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
					LcLinkOperateUtils.createLcLinkWithMesh(geomInter, maps, result);
				}
			}
		}
	}

	/*
	 * 创建土地覆盖线 不跨图幅生成线
	 */
	private void createLcLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
		if (g != null) {
			JSONObject node = LcLinkOperateUtils.createLcNodeForLink(g, sNodePid, eNodePid, result);
			LcLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);
		}
	}

	/*
	 * LC_LINK打断具体操作 1.循环挂接的线 2.如果有被打断操作执行打断功能
	 */
	public void breakLine(Result result) throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject json = command.getCatchLinks().getJSONObject(i);
			if (json.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				// 要打断线的pid
				breakJson.put("objId", json.getInt("linkPid"));
				// 要打断线的project_id
				breakJson.put("dbId", command.getDbId());
				JSONObject data = new JSONObject();
				// 要打断点的pid和经纬度
				data.put("breakNodePid", json.getInt("breakNode"));
				data.put("longitude", json.getDouble("lon"));
				data.put("latitude", json.getDouble("lat"));
				breakJson.put("data", data);
				// 组装打断线的参数
				// 保证是同一个连接
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(
						breakCommand, result, conn);
				breakProcess.innerRun();
			}
		}
	}

}
