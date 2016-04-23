package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.comm.util.GeometryTypeName;
import com.navinfo.dataservice.engine.edit.comm.util.LinkOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhaokk 创建行政区划线参数基础类
 */
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
		Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();
		// 如果创建行政区划线有对应的挂接AD_NODE和ADFACE
		// 执行挂接线处理逻辑
		if (command.getCatchLinks().size() > 0) {
			map = LinkOperateUtils.splitLink(command.getGeometry(),
					command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);

		}
		// 如果创建行政区划线没有对应的挂接AD_NODE和ADFACE
		// 创建对应的ADNODE
		if (command.getCatchLinks().size() == 0) {
			JSONObject se = new JSONObject();
			se = LinkOperateUtils.createAdNodeForLink(command.getGeometry(),
					command.getsNodePid(), command.geteNodePid(), result);
			map.put(command.getGeometry(), se);
		}
		// 创建行政区划线信息
		this.createAdLinks(map,result);
		//挂接的线被打断的操作
		this.breakLine();
        
		return msg;
	}
	/*
	 * 创建多条被分割的线
	 *  1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	public void createAdLinks(Map<Geometry, JSONObject> map, Result result)
			throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = MeshUtils.getInterMeshes(g);
			//不跨图幅
			if (meshes.size() == 1) {
				this.createAdLinkWithNoMesh(g, (int) map.get(g).get("s"),
						(int) map.get(g).get("e"), result);
			} 
			//跨图幅
			else {
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(g.getCoordinates()[0], (int) map.get(g).get("s"));
				maps.put(g.getCoordinates()[g.getCoordinates().length - 1],
						(int) map.get(g).get("e"));
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(g,
							MeshUtils.mesh2Jts(meshIdStr));
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);
					this.createAdLinkWithMesh(geomInter, maps,result);

				}
			}

		}

	}

	/*
	 * 创建行政区划线 不跨图幅生成线
	 */

	private void createAdLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid,
			Result result) throws Exception {
		if (g != null) {
			JSONObject node = LinkOperateUtils.createAdNodeForLink(g, sNodePid,
					eNodePid, result);
			LinkOperateUtils.addLink(g, (int) node.get("s"),
					(int) node.get("e"), result);
		}
	}

	/*
	 * 创建行政区划线 针对跨图幅有两种情况 
	 * 1.跨图幅和图幅交集是LineString 
	 * 2.跨图幅和图幅交集是MultineString
	 * 3.跨图幅需要生成和图廓线的交点
	 */

	private void createAdLinkWithMesh(Geometry g,
			Map<Coordinate, Integer> maps, Result result) throws Exception {
		if (g != null) {
			
			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				this.calAdLinkWithMesh(g, maps,result);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					this.calAdLinkWithMesh(g.getGeometryN(i), maps,result);
				}

			}
		}
	}
	/*
	 * 创建行政区划线 针对跨图幅创建图廓点不能重复
	 */
	private void calAdLinkWithMesh(Geometry g,Map<Coordinate, Integer> maps,
			Result result) throws Exception {
		//定义创建行政区划线的起始Pid 默认为0
		int sNodePid = 0;
		int eNodePid = 0;
		//判断新创建的线起点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[0])) {
			sNodePid = maps.get(g.getCoordinates()[0]);
		}
		//判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
			eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
		}
		//创建线对应的点
		JSONObject node = LinkOperateUtils.createAdNodeForLink(
				g, sNodePid, eNodePid, result);
		if (!maps.containsValue(node.get("s"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("s"));
		}
		if (!maps.containsValue(node.get("e"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("e"));
		}
		//创建线
		LinkOperateUtils.addLink(g, (int) node.get("s"),
				(int) node.get("e"), result);
	}

	/*
	 * AD_LINK打断具体操作
	 * 1.循环挂接的线
	 * 2.如果有被打断操作执行打断功能
	 */
	public void breakLine() throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject json = command.getCatchLinks().getJSONObject(i);
			
			if (json.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				//要打断线的pid
				breakJson.put("objId", json.getInt("linkPid"));
				//要打断线的project_id
				breakJson.put("projectId", command.getProjectId());
				JSONObject data = new JSONObject();
				//要打断点的pid和经纬度
				data.put("breakNodePid", json.getInt("breakNode"));
				data.put("longitude", json.getDouble("lon"));
				data.put("latitude", json.getDouble("lat"));
				breakJson.put("data", data);
				//组装打断线的参数
				//保证是同一个连接
				ICommand breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process(
						breakCommand, conn);
				breakProcess.run();
			}
		}
	}

}
