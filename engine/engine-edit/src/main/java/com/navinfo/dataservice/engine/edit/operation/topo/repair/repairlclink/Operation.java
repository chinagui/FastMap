package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
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
	private Map<Integer, List<LcLink>> map;

	public Map<Integer, List<LcLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<LcLink>> map) {
		this.map = map;
	}

	public Operation(Connection conn, Command command) {
		this.conn = conn;
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		// 处理修行挂接功能
		this.caleCatchs(result);
		// 修行修改线信息
		this.updateLink(result);
		// 修行修改面信息
		this.updateFace(result);
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
			LcNodeSelector nodeSelector = new LcNodeSelector(conn);
			LcLinkSelector linkSelector = new LcLinkSelector(conn);
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

				LcNode preNode = (LcNode) nodeSelector.loadById(nodePid, true,
						true);
				// 分离node挂接的link
				List<LcLink> links = linkSelector.loadByNodePid(nodePid, true);

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
		LcNode node = NodeOperateUtils.createLcNode(lon, lat);
		result.insertObject(node, ObjStatus.INSERT, node.pid());
		this.updateNodeForLink(nodePid, node.getPid());

	}

	/***
	 * 分离节点 修行挂接Node操作
	 * 
	 * @param result
	 * @param preNode
	 * @throws Exception
	 */
	private void departCatchtNode(Result result, int nodePid, int catchNodePid,
			LcNode preNode, List<LcLink> links) throws Exception {
		LcNodeSelector nodeSelector = new LcNodeSelector(conn);
		// 用分离挂接的Node替换修行Link对应的几何,以保持精度
		LcNode catchNode = (LcNode) nodeSelector.loadById(catchNodePid, true,
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
	private void departCatchBreakLink(double lon, double lat, LcNode preNode,
			int linkPid, List<LcLink> links, Result result) throws Exception {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 如果没有挂接的link node需要继承 如果有node需要新生成
		int breakNodePid = preNode.getPid();
		if (links.size() > 1) {
			LcNode node = NodeOperateUtils.createLcNode(lon, lat);
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
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(
				breakCommand, result, conn);
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
	private void moveNodeGeo(LcNode node, double lon, double lat, Result result)
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
		com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command(
				updateContent, command.getRequester(), node);
		com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process(
				updatecommand, result, conn);
		process.innerRun();

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
		JSONObject content = new JSONObject();
		if (this.command.getUpdateLink().getsNodePid() == nodePid) {
			content.put("sNodePid", pid);
			this.command.getUpdateLink().fillChangeFields(content);

		} else {
			content.put("eNodePid", pid);
			this.command.getUpdateLink().fillChangeFields(content);
		}
	}

	/**
	 * 修改线的信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateLink(Result result) throws Exception {
		Map<Integer, List<LcLink>> map = new HashMap<Integer, List<LcLink>>();
		List<LcLink> links = new ArrayList<LcLink>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(command
				.getLinkGeom());
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", GeoTranslator.jts2Geojson(command.getLinkGeom()));
			Geometry geo = command.getLinkGeom();
			double length = 0;
			if (null != geo)
				length = GeometryUtils.getLinkLength(geo);
			content.put("length", length);
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(
					content);
			LcLink adLink = new LcLink();
			adLink.setPid(this.command.getUpdateLink().getPid());
			adLink.copy(this.command.getUpdateLink());
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(),
						ObjStatus.UPDATE, this.command.getLinkPid());
				adLink.setGeometry(GeoTranslator.transform(
						command.getLinkGeom(), 100000, 0));
			}
			links.add(adLink);
		} else {
			Iterator<String> it = meshes.iterator();
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			Geometry g = GeoTranslator.transform(this.command.getUpdateLink()
					.getGeometry(), 0.00001, 5);
			maps.put(g.getCoordinates()[0], this.command.getUpdateLink()
					.getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1],
					this.command.getUpdateLink().geteNodePid());
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(command
						.getLinkGeom(), GeoTranslator.transform(
						MeshUtils.mesh2Jts(meshIdStr), 1, 5));

				if (geomInter instanceof GeometryCollection) {
					int geoNum = geomInter.getNumGeometries();
					for (int i = 0; i < geoNum; i++) {
						Geometry subGeo = geomInter.getGeometryN(i);
						if (subGeo instanceof LineString) {
							subGeo = GeoTranslator.geojson2Jts(
									GeoTranslator.jts2Geojson(subGeo), 1, 5);

							links.addAll(LcLinkOperateUtils
									.getCreateLcLinksWithMesh(subGeo, maps,
											result,
											this.command.getUpdateLink()));
						}
					}
				} else {
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);

					links.addAll(LcLinkOperateUtils.getCreateLcLinksWithMesh(
							geomInter, maps, result,
							this.command.getUpdateLink()));
				}
			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE,
					this.command.getLinkPid());
		}
		updataRelationObj(this.command.getUpdateLink(), links, result);
		map.put(this.command.getLinkPid(), links);
		this.map = map;
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj(LcLink oldLink, List<LcLink> newLinks,
			Result result) throws Exception {
		// 立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

		Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

		for (LcLink link : newLinks) {
			newLinkMap.put(link.getPid(), link.getGeometry());
		}

		gscOperation.repairLink(this.command.getGscList(), newLinkMap, oldLink,
				result);
	}

	/**
	 * 修改面的信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateFace(Result result) throws Exception {
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
						links.add((LcLink) new LcLinkSelector(conn).loadById(
								obj.getLinkPid(), true));
					}
					result.insertObject(obj, ObjStatus.DELETE, face.getPid());
				}
				if (flag) {
					// 如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
							result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByLcLink(objs, face);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				} else {
					// 如果不跨图幅只需要维护面的几何
					com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
							result, face);
					opFace.reCaleFaceGeometry(links);
				}
			}

		}
	}

}
