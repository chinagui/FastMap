package com.navinfo.dataservice.engine.edit.operation.obj.adlink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.AdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

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
			this.caleCatchModifyAdLink();
			map = AdLinkOperateUtils.splitLink(command.getGeometry(),
					command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);

		}
		// 如果创建行政区划线没有对应的挂接AD_NODE和ADFACE
		// 创建对应的ADNODE
		if (command.getCatchLinks().size() == 0 || map.size() == 0) {
			JSONObject se = new JSONObject();
			se = AdLinkOperateUtils.createAdNodeForLink(command.getGeometry(),
					command.getsNodePid(), command.geteNodePid(), result);
			map.put(command.getGeometry(), se);
		}
		// 创建行政区划线信息
		this.createAdLinks(map, result);
		// 挂接的线被打断的操作
		this.breakLine(result);

		return msg;
	}
	/***
	 * 当前台未开启挂接功能是，如果传入的点正好是link的端点 应按照挂接node来传参数
	 * 
	 * @throws Exception
	 */
	private void caleCatchModifyAdLink() throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject modifyJson = command.getCatchLinks().getJSONObject(i);
			if (modifyJson.containsKey("linkPid")) {
				AdLinkSelector linkSelector = new AdLinkSelector(conn);
				IRow row = linkSelector.loadById(modifyJson.getInt("linkPid"),
						false, true);
				AdLink link = (AdLink) row;
				Geometry geometry = GeoTranslator.transform(link.getGeometry(),
						0.00001, 5);
				if (geometry.getCoordinates()[0].x == modifyJson
						.getDouble("lon")

				&& geometry.getCoordinates()[0].y == modifyJson

				.getDouble("lat")) {
					modifyJson.remove("linkPid");
					modifyJson.put("nodePid", link.getsNodePid());

				}
				if (geometry.getCoordinates()[geometry.getCoordinates().length - 1].x == modifyJson
						.getDouble("lon")

						&& geometry.getCoordinates()[geometry.getCoordinates().length - 1].y == modifyJson

						.getDouble("lat")) {
					modifyJson.remove("linkPid");
					modifyJson.put("nodePid", link.geteNodePid());

				}
			}
		}

	}

	/*
	 * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	public void createAdLinks(Map<Geometry, JSONObject> map, Result result)
			throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
			// 不跨图幅
			if (meshes.size() == 1) {
				this.createAdLinkWithNoMesh(g, (int) map.get(g).get("s"),
						(int) map.get(g).get("e"), result);
			}
			// 跨图幅
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
					AdLinkOperateUtils.createAdLinkWithMesh(geomInter, maps,
							result);

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
			JSONObject node = AdLinkOperateUtils.createAdNodeForLink(g,
					sNodePid, eNodePid, result);
			AdLinkOperateUtils.addLink(g, (int) node.get("s"),
					(int) node.get("e"), result);
		}
	}

	/*
	 * AD_LINK打断具体操作 1.循环挂接的线 2.如果有被打断操作执行打断功能
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
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
						breakCommand, result, conn);
				breakProcess.innerRun();
			}
		}
	}

}
