package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.AdminOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

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

		if (command.getGeometry().getCoordinates().length < 2) {
			throw new Exception("线至少包含两个点");
		}

		Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();

		if (command.getCatchLinks().size() > 0) {

			this.caleCatchModifyRdLink();

			map = RdLinkOperateUtils.splitRdLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
					command.getCatchLinks(), result);

		}
		if (command.getCatchLinks().size() == 0 || map.size() == 0) {
			JSONObject se = new JSONObject();

			se = RdLinkOperateUtils.createRdNodeForLink(command.getGeometry(), command.getsNodePid(),
					command.geteNodePid(), result);

			map.put(command.getGeometry(), se);
		}

		// 创建线信息
		this.createRdLinks(map, result);
		List<RdLink> links = getLinksFromResult(result);
		// 挂接的线被打断的操作
		this.breakLine(result);
		// 处理挂机交叉口内link的形态
		this.handleCrossLink(result, links);

		return msg;
	}

	/**
	 * @param result
	 * @return
	 */
	private List<RdLink> getLinksFromResult(Result result) {

		List<RdLink> links = new ArrayList<>();

		for (IRow row : result.getAddObjects()) {
			if (row instanceof RdLink) {
				RdLink link = (RdLink) row;

				links.add(link);
			}
		}

		return links;
	}

	/**
	 * @param result
	 * @param links
	 * @throws Exception
	 */
	private void handleCrossLink(Result result, List<RdLink> links) throws Exception {
		List<IRow> addRows = result.getAddObjects();

		Map<Integer, List<Integer>> crossNodeMap = new HashMap<>();
		
		RdCrossSelector selector = new RdCrossSelector(conn);
		
		//针对挂接两个node点为都是路口点位的要维护link形态为交叉口内link
		List<Integer> hasHandledLinks = handleLinksForCorss(links);

		for (IRow row : addRows) {
			if (row instanceof RdCrossNode) {
				RdCrossNode crossNode = (RdCrossNode) row;

				if (crossNodeMap.containsKey(crossNode.getPid())) {
					crossNodeMap.get(crossNode.getPid()).add(crossNode.getNodePid());
				} else {
					List<Integer> crossNodeList = new ArrayList<>();

					crossNodeList.add(crossNode.getNodePid());

					crossNodeMap.put(crossNode.getPid(), crossNodeList);
				}
			}
		}

		List<RdTrafficsignal> insertTraffsignals = new ArrayList<>();
		
		for (Map.Entry<Integer, List<Integer>> entry : crossNodeMap.entrySet()) {
			int crossPid = entry.getKey();

			RdCross cross = (RdCross) selector.loadById(crossPid, true);

			List<Integer> crossNodePidList = entry.getValue();

			List<Integer> allCrossNodePidList = new ArrayList<>();

			allCrossNodePidList.addAll(crossNodePidList);

			for (IRow row : cross.getNodes()) {
				RdCrossNode crossNode = (RdCrossNode) row;

				allCrossNodePidList.add(crossNode.getNodePid());
			}

			for (RdLink link : links) {
				// 交叉口内link。已经处理过的不再处理
				if (!hasHandledLinks.contains(link.getPid()) && allCrossNodePidList.contains(link.getsNodePid())
						&& allCrossNodePidList.contains(link.geteNodePid())) {
					RdLinkForm form = (RdLinkForm) link.getForms().get(0);

					form.setFormOfWay(50);
				} else if (cross.getSignal() == 1) {
					// 有红绿灯信号维护红绿灯
					for (Integer crossNodePid : crossNodePidList) {
						// link的起点活终点都需要建立红绿灯（交叉口内link除外）
						if (link.getsNodePid() == crossNodePid || link.geteNodePid() == crossNodePid) {
							RdTrafficsignal signal = new RdTrafficsignal();

							signal.setPid(PidUtil.getInstance().applyRdTrafficsignalPid());

							signal.setLinkPid(link.getPid());

							// 默认为受控制
							signal.setFlag(1);

							signal.setNodePid(crossNodePid);

							insertTraffsignals.add(signal);
						}
					}
				}
			}
		}

		for (RdTrafficsignal signal : insertTraffsignals) {
			result.insertObject(signal, ObjStatus.INSERT, signal.getPid());
		}
	}

	/**
	 * 针对新增的link处理挂接两个node点都是同一个路口点的形态问题
	 * @param links
	 * @param selector 
	 * @throws Exception 
	 */
	private List<Integer> handleLinksForCorss(List<RdLink> links) throws Exception {
		List<Integer> hasHandledLink = new ArrayList<>();
		
		RdCrossNodeSelector nodeSelector = new RdCrossNodeSelector(conn);
		
		for(RdLink link : links)
		{
			int sNodePid = link.getsNodePid();
			
			RdCrossNode sCrossNode = (RdCrossNode) nodeSelector.loadByNodeId(sNodePid, true);
			
			int eNodePid = link.geteNodePid();
			
			RdCrossNode eCrossNode = (RdCrossNode) nodeSelector.loadByNodeId(eNodePid, true);
			
			//如果起点和终点是同一路口点，则link形态为交叉口内link
			if(!hasHandledLink.contains(link.getPid()) && sCrossNode != null && eCrossNode != null && sCrossNode.getPid() == eCrossNode.getPid())
			{
				RdLinkForm form = (RdLinkForm) link.getForms().get(0);

				form.setFormOfWay(50);
				
				hasHandledLink.add(link.getPid());
			}
		}
		
		return hasHandledLink;
	}

	/*
	 * 创建RDLINK 不跨图幅生成线
	 */
	private void createRdLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
		if (g != null) {
			JSONObject node = RdLinkOperateUtils.createRdNodeForLink(g, sNodePid, eNodePid, result);
			RdLink link = RdLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result, null);

			link.setKind(command.getKind());

			link.setLaneNum(command.getLaneNum());

			AdminOperateUtils.SetAdminInfo4Link(link, conn);

			// 设置Link的urban属性
			UrbanBatchUtils.updateUrban(link, null, this.conn, result);
			// 设置link的adminId属性
			AdminIDBatchUtils.updateAdminID(link, null, conn);
			// 设置link的zoneId属性
			ZoneIDBatchUtils.updateZoneID(link, null, conn, result);

			result.insertObject(link, ObjStatus.INSERT, link.pid());
		}
	}

	/*
	 * 创建RDLINK针对跨图幅有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 跨图幅需要生成和图廓线的交点
	 */

	private void createRdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result, String meshId)
			throws Exception {
		if (g != null) {

			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				this.calRdLinkWithMesh(g, maps, result);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					calRdLinkWithMesh(g.getGeometryN(i), maps, result);
				}

			}
		}
	}

	/*
	 * 创建RDLINK 针对跨图幅创建图廓点不能重复
	 */
	private void calRdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result) throws Exception {
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
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
		}
		// 创建线
		RdLink link = RdLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result, null);

		link.setKind(command.getKind());

		link.setLaneNum(command.getLaneNum());

		AdminOperateUtils.SetAdminInfo4Link(link, conn);

		// 设置Link的urban属性
		UrbanBatchUtils.updateUrban(link, null, this.conn, result);
		// 设置link的adminId属性
		AdminIDBatchUtils.updateAdminID(link, null, conn);
		// 设置link的zoneId属性
		ZoneIDBatchUtils.updateZoneID(link, null, conn, result);

		result.insertObject(link, ObjStatus.INSERT, link.pid());
	}

	/*
	 * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

	public void createRdLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {

		for (Geometry g : map.keySet()) {
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
			// 不跨图幅
			if (meshes.size() == 1) {
				createRdLinkWithNoMesh(g, (int) map.get(g).get("s"), (int) map.get(g).get("e"), result);
			}
			// 跨图幅
			else {
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(g.getCoordinates()[0], (int) map.get(g).get("s"));
				maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) map.get(g).get("e"));
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(g,
							GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr), 1, 5));
					if (geomInter instanceof GeometryCollection) {
						int geoNum = geomInter.getNumGeometries();
						for (int i = 0; i < geoNum; i++) {
							Geometry subGeo = geomInter.getGeometryN(i);
							if (subGeo instanceof LineString) {
								subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);

								this.createRdLinkWithMesh(subGeo, maps, result, meshIdStr);
							}
						}
					} else {
						geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

						this.createRdLinkWithMesh(geomInter, maps, result, meshIdStr);
					}
				}
			}

		}

	}

	public void breakLine(Result result) throws Exception {
		for (int i = 0; i < command.getMapListJson().size(); i++) {
			Map<String, Object> json = command.getMapListJson().get(i);

			JSONObject modifyJson = command.getCatchLinks().getJSONObject(i);

			if (modifyJson.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				breakJson.put("objId", json.get("linkPid"));
				breakJson.put("dbId", command.getDbId());
				JSONObject data = new JSONObject();
				data.put("breakNodePid", modifyJson.getInt("breakNode"));
				data.put("longitude", json.get("lon"));
				data.put("latitude", json.get("lat"));
				breakJson.put("data", data);
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
						breakCommand, conn, result);
				breakProcess.innerRun();
			}
		}
	}

	/***
	 * 当前台未开启挂接功能是，如果传入的点正好是link的端点 应按照挂接node来传参数
	 * 
	 * @throws Exception
	 */
	private void caleCatchModifyRdLink() throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject modifyJson = command.getCatchLinks().getJSONObject(i);
			if (modifyJson.containsKey("linkPid")) {
				RdLinkSelector linkSelector = new RdLinkSelector(conn);
				IRow row = linkSelector.loadByIdOnlyRdLink(modifyJson.getInt("linkPid"), false);
				RdLink link = (RdLink) row;
				Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
				if (geometry.getCoordinates()[0].x == modifyJson.getDouble("lon")

						&& geometry.getCoordinates()[0].y == modifyJson

								.getDouble("lat")) {
					modifyJson.remove("linkPid");
					modifyJson.put("nodePid", link.getsNodePid());

				}
				if (geometry.getCoordinates()[geometry.getCoordinates().length - 1].x == modifyJson.getDouble("lon")

						&& geometry.getCoordinates()[geometry.getCoordinates().length - 1].y == modifyJson

								.getDouble("lat")) {
					modifyJson.remove("linkPid");
					modifyJson.put("nodePid", link.geteNodePid());

				}
			}
		}
	}
}
