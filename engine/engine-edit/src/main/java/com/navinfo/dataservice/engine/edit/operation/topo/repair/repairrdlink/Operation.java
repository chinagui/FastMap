package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Connection conn, Command command) {

		this.conn = conn;

		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		// 处理修行挂接功能
		this.caleCatchs(result);
		// 创建修行后的Link
		this.repariRdlink(result);
		return null;
	}

	/***
	 * 修行挂接点和线
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void caleCatchs(Result result) throws Exception {
		if (this.command.getCatchInfos() != null
				&& this.command.getCatchInfos().size() > 0) {
			RdNodeSelector nodeSelector = new RdNodeSelector(conn);
			RdLinkSelector linkSelector = new RdLinkSelector(conn);
			for (int i = 0; i < this.command.getCatchInfos().size(); i++) {
				JSONObject obj = this.command.getCatchInfos().getJSONObject(i);
				// 分离移动的node
				int nodePid = obj.getInt("nodePid");
				Point point = null;
				double lon = 0;
				double lat = 0;
				if (!obj.containsKey("catchNodePid")) {
					point = (Point) GeoTranslator.transform(GeoTranslator
							.point2Jts(obj.getDouble("longitude"),
									obj.getDouble("latitude")), 1, 5);
					// 分离移动后的经纬度
					lon = point.getX();
					lat = point.getY();
				}

				RdNode preNode = (RdNode) nodeSelector.loadById(nodePid, true,
						true);
				// 分离node挂接的link
				List<RdLink> links = linkSelector.loadByNodePidOnlyRdLink(
						nodePid, true);

				if (obj.containsKey("catchNodePid")
						&& obj.getInt("catchNodePid") != 0) {
					// 分离节点挂接功能
					this.departCatchtNode(result, nodePid,
							obj.getInt("catchNodePid"), preNode, links);

				} else if (obj.containsKey("catchLinkPid")
						&& obj.getInt("catchLinkPid") != 0) {
					// 分离节点挂接打断功能
					this.departCatchBreakLink(lon, lat, preNode,
							obj.getInt("catchLinkPid"), links, result);
				} else {
					// 移动功能
					if (links.size() == 1) {
						this.moveNodeGeo(preNode, lon, lat, result);
					} else {
						this.departNode(result, nodePid, lon, lat);
					}
				}

			}

		}

	}

	/***
	 * 
	 * @param result
	 * @param nodePid
	 * @param lon
	 * @param lat
	 * @throws Exception
	 */
	private void departNode(Result result, int nodePid, double lon, double lat)
			throws Exception {

		// 分离功能
		RdNode node = NodeOperateUtils.createRdNode(lon, lat);
		result.insertObject(node, ObjStatus.INSERT, node.pid());
		this.updateNodeForLink(nodePid, node.getPid());

	}

	/***
	 * 重新赋值link的起始点的pid
	 * 
	 * @param nodePid
	 *            原始link的端点pid
	 * @param pid
	 *            修行后新的端点pid
	 * @throws Exception 
	 */
	private void updateNodeForLink(int nodePid, int pid) throws Exception {
		JSONObject content= new JSONObject();
		if (this.command.getUpdateLink().getsNodePid() == nodePid) {
			content.put("sNodePid", pid);
			this.command.getUpdateLink().fillChangeFields(content);

		} else {
			content.put("eNodePid", pid);
			this.command.getUpdateLink().fillChangeFields(content);
		}
	}

	/***
	 * 分离节点 修行挂接Node操作
	 * 
	 * @param result
	 * @param preNode
	 * @throws Exception
	 */
	private void departCatchtNode(Result result, int nodePid, int catchNodePid,
			RdNode preNode, List<RdLink> links) throws Exception {
		RdNodeSelector nodeSelector = new RdNodeSelector(conn);
		// 用分离挂接的Node替换修行Link对应的几何,以保持精度
		RdNode catchNode = (RdNode) nodeSelector.loadById(catchNodePid, true,
				true);
		// 获取挂接Node的几乎额
		Geometry geom = GeoTranslator.transform(catchNode.getGeometry(),
				0.00001, 5);
		Point point = (((Point) GeoTranslator.point2Jts(geom.getCoordinate().x,
				geom.getCoordinate().y)));
		// 如果原有node挂接的LINK<=1 原来的node需要删除更新link的几何为新的node
		if (links.size() <= 1) {
			result.insertObject(preNode, ObjStatus.DELETE, preNode.getPid());
		}
		// 更新link的几何为新的node点
		this.updateNodeForLink(nodePid, catchNodePid);
		// 更新link的几何用挂接的点的几何代替link的起始形状点
		if (this.command.getUpdateLink().getsNodePid() == nodePid) {

			this.command.getLinkGeom().getCoordinates()[0] = point
					.getCoordinate();
		} else {
			this.command.getLinkGeom().getCoordinates()[this.command
					.getLinkGeom().getCoordinates().length - 1] = point
					.getCoordinate();
		}
	}

	/***
	 * 分离节点 挂接Link打断功能能
	 * 
	 * @param lon
	 *            打断点经度
	 * @param lat
	 *            打断点的维度
	 * @param preNode
	 *            分离的node
	 * @param linkPid
	 *            挂节点的linkPid
	 * @param links
	 *            分离node挂接的node
	 * @param result
	 * @throws Exception
	 */
	private void departCatchBreakLink(double lon, double lat, RdNode preNode,
			int linkPid, List<RdLink> links, Result result) throws Exception {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 如果没有挂接的link node需要继承 如果有node需要新生成
		int breakNodePid = preNode.getPid();
		if (links.size() > 1) {
			RdNode node = NodeOperateUtils.createRdNode(lon, lat);
			result.insertObject(node, ObjStatus.INSERT, node.getPid());
			breakNodePid = node.getPid();
			this.updateNodeForLink(preNode.getPid(), breakNodePid);

		}
		// node继承需要修改node的几何
		else {
			this.moveNodeGeo(preNode, lon, lat, result);

		}

		// 组装打断的参数
		data.put("longitude", lon);
		data.put("latitude", lat);
		data.put("breakNodePid", breakNodePid);
		breakJson.put("data", data);
		// 调用打断的API
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
				breakCommand, conn, result);
		breakProcess.innerRun();

	}

	/***
	 * 
	 * 
	 * @param node
	 *            移动点的对象
	 * @param lon
	 *            移动后的经度
	 * @param lat
	 *            移动后的纬度
	 * @param result
	 * @throws Exception
	 */
	private void moveNodeGeo(RdNode node, double lon, double lat, Result result)
			throws Exception {
		JSONObject geojson = new JSONObject();
		geojson.put("type", "Point");
		geojson.put("coordinates", new double[] { lon, lat });
		JSONObject updateContent = new JSONObject();
		// 要移动点的dbId
		updateContent.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 移动点的新几何
		data.put("geometry", geojson);
		data.put("pid", node.getPid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateContent.put("data", data);
		// 组装更新node的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(
				updateContent, command.getRequester(), node);
		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(
				updatecommand, result, conn);
		process.innerRun();

	}

	/***
	 * 创建修行后的RDLINK
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void repariRdlink(Result result) throws Exception {
		List<RdLink> links = new ArrayList<RdLink>();
		Set<String> meshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(this.command.getLinkGeom());
		// 不跨图幅
		if (meshes.size() == 1) {
			this.caleNoMeshForLink(links, result);
		}// 跨图幅
		else {
			this.caleMeshForLinks(links, meshes, result);
		}
		// 属性关联维护
		updataRelationObj(this.command.getUpdateLink(), links, result);

	}

	/***
	 *
	 * 处理不跨图幅功能
	 * 
	 * @param links
	 * @param result
	 * @throws Exception
	 */
	private void caleNoMeshForLink(List<RdLink> links, Result result)
			throws Exception {

		JSONObject content = new JSONObject();
		result.setPrimaryPid(this.command.getUpdateLink().getPid());

		// 获取修行后link长度的变化
		content.put("geometry",
				GeoTranslator.jts2Geojson(this.command.getLinkGeom()));
		content.put("length",
				GeometryUtils.getLinkLength(this.command.getLinkGeom()));
		// 差分获取变化的值
		boolean isChanged = this.command.getUpdateLink().fillChangeFields(
				content);
		// 修改link的值
		if (isChanged) {
			result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE,
					this.command.getUpdateLink().getPid());
		}
		// 拷贝原link，set属性
		RdLink link = new RdLink();
		link.setPid(this.command.getUpdateLink().pid());
		link.copy(this.command.getUpdateLink());

		link.setGeometry(GeoTranslator.transform(this.command.getLinkGeom(),
				100000, 0));
		links.add(link);
		// 设置Link的urban属性
		UrbanBatchUtils.updateUrban(this.command.getUpdateLink(),
				link.getGeometry(), conn, result);
		this.caleBatchForLink(link, result);

	}

	/***
	 * 处理跨图幅修行
	 * 
	 * @param links
	 * @param meshes
	 * @param result
	 * @throws Exception
	 */
	private void caleMeshForLinks(List<RdLink> links, Set<String> meshes,
			Result result) throws Exception {
		// 计算Link图幅范围
		Iterator<String> it = meshes.iterator();
		Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
		Geometry g = GeoTranslator.transform(this.command.getUpdateLink()
				.getGeometry(), 0.00001, 5);
		// 容器中加入原有link的起始点信息
		maps.put(g.getCoordinates()[0], this.command.getUpdateLink()
				.getsNodePid());
		maps.put(g.getCoordinates()[g.getCoordinates().length - 1],
				this.command.getUpdateLink().geteNodePid());
		while (it.hasNext()) {
			String meshIdStr = it.next();
			Geometry geomInter = MeshUtils.linkInterMeshPolygon(this.command
					.getLinkGeom(), GeoTranslator.transform(
					MeshUtils.mesh2Jts(meshIdStr), 1, 5));
			// 判断link和图幅相交线的形状
			if (geomInter instanceof GeometryCollection) {
				int geoNum = geomInter.getNumGeometries();
				for (int i = 0; i < geoNum; i++) {
					Geometry subGeo = geomInter.getGeometryN(i);
					if (subGeo instanceof LineString) {
						subGeo = GeoTranslator.geojson2Jts(
								GeoTranslator.jts2Geojson(subGeo), 1, 5);

						List<RdLink> rdLinkds = RdLinkOperateUtils
								.getCreateRdLinksWithMesh(subGeo, maps,
										result, this.command.getUpdateLink());
						links.addAll(rdLinkds);
					}
				}
			} else {
				geomInter = GeoTranslator.geojson2Jts(
						GeoTranslator.jts2Geojson(geomInter), 1, 5);

				List<RdLink> rdLinkds = RdLinkOperateUtils
						.getCreateRdLinksWithMesh(geomInter, maps, result,
								this.command.getUpdateLink());

				links.addAll(rdLinkds);
			}
			// 重新批处理设置Link的urban属性 设置link的区划号码 设置link的ZoneId
			for (RdLink link : links) {
				UrbanBatchUtils.updateUrban(link, null, conn, result);
				this.caleBatchForLink(link, result);
			}
		}
		// 删除原有的link
		deleteRdLink(result);

	}

	/***
	 * 批处理 设置link的区划号码 设置link的ZoneId
	 * 
	 * @param link
	 * @param result
	 * @throws Exception
	 */
	private void caleBatchForLink(RdLink link, Result result) throws Exception {

		// 设置link的区划号码
		AdminIDBatchUtils.updateAdminID(this.command.getUpdateLink(),
				link.getGeometry(), conn);
		// 设置link的ZoneId
		ZoneIDBatchUtils.updateZoneID(this.command.getUpdateLink(),
				link.getGeometry(), conn, result);
	}

	/**
	 * @param result
	 * @throws Exception
	 */
	private void deleteRdLink(Result result) throws Exception {
		result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE,
				this.command.getUpdateLink().getPid());
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj(RdLink oldLink, List<RdLink> newLinks,
			Result result) throws Exception {

		CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

		List<RdLink> sortLinks = calLinkOperateUtils.sortLink(newLinks);

		if (newLinks.size() == 1) {
			if (!this.command.getOperationType().equals("innerRun")) {
				// 维护同一线
				com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation samelinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
						this.conn);

				samelinkOperation.repairLink(newLinks.get(0),
						this.command.getRequester(), result);
			}
		}

		/*
		 * 任何情况均需要处理的元素
		 */
		// 电子眼
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation eleceyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation(
				this.conn);
		eleceyeOperation.moveEleceye(oldLink, newLinks, result);

		// poi被动维护（引导link，方位）
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation poiUpdateOption = new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation(
				this.conn);
		poiUpdateOption.updateLinkSideForPoi(oldLink, newLinks, result);

		// 维护点限速坐标
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation speedlimitOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation(
				this.conn);
		speedlimitOperation.moveSpeedlimit(oldLink, newLinks, result);

		// 立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

		Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

		for (RdLink link : newLinks) {
			newLinkMap.put(link.getPid(), link.getGeometry());
		}

		gscOperation.repairLink(this.command.getGscList(), newLinkMap, oldLink,
				result);

		// 维护限高限重
		com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation hgwgOperation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation(conn);
		hgwgOperation.moveHgwgLimit(oldLink, newLinks, result);

		//维护里程桩
		com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Operation maileageOperation = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Operation(conn);
		maileageOperation.moveMileagepile(oldLink, newLinks, result);

		/*
		 * 条件以下为仅打断情况下需要处理的元素 (size < 2说明没有进行打断操作)
		 */
		if (newLinks.size() < 2) {
			return;
		}
		// 警示信息
		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation warninginOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation(
				this.conn);
		warninginOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		// 维护信号灯
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(
				this.conn);
		trafficSignalOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		// 分岔路提示
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation rdSeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
				this.conn);
		rdSeOperation.breakRdSe(result, oldLink.pid(), newLinks);

		// 减速带
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation rdSpeedbumpOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation(
				this.conn);
		rdSpeedbumpOpeartion.breakSpeedbump(result, oldLink.getPid(), newLinks);
		// 坡度
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation rdSlopeOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation(
				this.conn);
		rdSlopeOpeartion.breakRdLink(oldLink.getPid(), newLinks, result);
		// 顺行
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);
		operation.breakRdLink(oldLink, sortLinks, result);
		// 维护CRF交叉点
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation(
				this.conn);
		// 维护CRF道路
		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation(
				this.conn);
		rdRoadOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		rdinterOperation.breakRdLink(oldLink, newLinks, result);
		// 维护CRF对象
		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
				this.conn);
		rdObjectOperation.breakRdObjectLink(oldLink, newLinks, result);
		// 收费站
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation rdTollgateOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
				this.conn);
		rdTollgateOpeartion.breakRdTollgate(result, oldLink.getPid(), newLinks);

		// 语音引导
		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation voiceguideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
				conn);
		voiceguideOperation.breakRdLink(oldLink, sortLinks, result);

		// 维护可变限速关系
		com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation variableSpeedOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation(
				this.conn);
		variableSpeedOperation.breakLine(oldLink, newLinks, result);
		// 详细车道维护
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation rdlaneOperation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(this.conn);
        rdlaneOperation.breakRdLink(oldLink.getPid(), newLinks, result);


	}

}
