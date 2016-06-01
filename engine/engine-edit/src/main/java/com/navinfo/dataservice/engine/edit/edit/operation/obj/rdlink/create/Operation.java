package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.comm.util.operate.AdminOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
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
		
		if(command.getGeometry().getCoordinates().length<2)
		{
			throw new Exception("线至少包含两个点");
		}
		
		Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();

		if (command.getCatchLinks().size() > 0) {
			map = RdLinkOperateUtils.splitRdLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);

		}
		if (command.getCatchLinks().size() == 0 || map.size() == 0) {
			JSONObject se = new JSONObject();

			se = RdLinkOperateUtils.createRdNodeForLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
					result);

			map.put(command.getGeometry(), se);
		}

		// 创建线信息
		this.createRdLinks(map, result);
		// 挂接的线被打断的操作
		this.breakLine(result);

		return msg;
	}

	/*
	 * 创建RDLINK 不跨图幅生成线
	 */
	private RdLink createRdLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
		if (g != null) {
			JSONObject node = RdLinkOperateUtils.createRdNodeForLink(g, sNodePid, eNodePid, result);
			RdLink link = RdLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);
			
			link.setKind(command.getKind());

			link.setLaneNum(command.getLaneNum());
			
			AdminOperateUtils.SetAdminInfo4Link(link, conn);
			
			return link;
		}
		
		return null;
	}

	/*
	 * 创建RDLINK针对跨图幅有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 跨图幅需要生成和图廓线的交点
	 */

	private void createRdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result, String meshId) throws Exception {
		if (g != null) {

			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				RdLink link = this.calRdLinkWithMesh(g, maps, result);
				
				link.setMeshId(Integer.parseInt(meshId));
				
				result.insertObject(link, ObjStatus.INSERT, link.pid());
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					RdLink link = this.calRdLinkWithMesh(g.getGeometryN(i), maps, result);
					
					link.setMeshId(Integer.parseInt(meshId));
					
					result.insertObject(link, ObjStatus.INSERT, link.pid());
				}

			}
		}
	}

	/*
	 * 创建RDLINK 针对跨图幅创建图廓点不能重复
	 */
	private RdLink calRdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result) throws Exception {
		// 定义创建RDLINK的起始Pid 默认为0
		int sNodePid = 0;
		int eNodePid = 0;
		// 判断新创建的线起点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[0])) {
			sNodePid = maps.get(g.getCoordinates()[0]);
		}
		// 判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
			eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
		}
		// 创建线对应的点
		JSONObject node = RdLinkOperateUtils.createRdNodeForLink(g, sNodePid, eNodePid, result);
		if (!maps.containsValue(node.get("s"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("s"));
		}
		if (!maps.containsValue(node.get("e"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("e"));
		}
		// 创建线
		RdLink link = RdLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);
		
		link.setKind(command.getKind());

		link.setLaneNum(command.getLaneNum());
		
		AdminOperateUtils.SetAdminInfo4Link(link, conn);

		return link;
	}

	/*
	 * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	public void createRdLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = MeshUtils.getInterMeshes(g);
			// 不跨图幅
			if (meshes.size() == 1) {
				RdLink link = this.createRdLinkWithNoMesh(g, (int) map.get(g).get("s"), (int) map.get(g).get("e"), result);
				
				link.setMeshId(Integer.parseInt(meshes.iterator().next()));
				
				result.insertObject(link, ObjStatus.INSERT, link.pid());
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
					this.createRdLinkWithMesh(geomInter, maps, result,meshIdStr);
				}
			}

		}

	}

	public void breakLine(Result result) throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject json = command.getCatchLinks().getJSONObject(i);
			if (json.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				breakJson.put("objId", json.getInt("linkPid"));
				breakJson.put("projectId", command.getProjectId());
				JSONObject data = new JSONObject();
				data.put("breakNodePid", json.getInt("breakNode"));
				data.put("longitude", json.getDouble("lon"));
				data.put("latitude", json.getDouble("lat"));
				breakJson.put("data", data);
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process(
						breakCommand, conn);
				breakProcess.run();
			}
		}
	}

}
