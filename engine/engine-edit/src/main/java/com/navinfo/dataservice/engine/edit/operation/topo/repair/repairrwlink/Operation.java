package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink;

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
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RwLinkOperateUtils;
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

	private Map<Integer, List<RwLink>> map;

	public Map<Integer, List<RwLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<RwLink>> map) {
		this.map = map;
	}

	public Operation(Command command) {

		this.command = command;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		// 处理修行挂接功能
		this.caleCatchs(result);
		// 修行修改线信息
		this.updateLink(result);

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
			RwNodeSelector nodeSelector = new RwNodeSelector(conn);
			RwLinkSelector linkSelector = new RwLinkSelector(conn);
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

				RwNode preNode = (RwNode) nodeSelector.loadById(nodePid, true,
						true);
				// 分离node挂接的link
				List<RwLink> links = linkSelector.loadByNodePid(nodePid, true);

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
		RwNode node = NodeOperateUtils.createRwNode(lon, lat);
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
			RwNode preNode, List<RwLink> links) throws Exception {
		RwNodeSelector nodeSelector = new RwNodeSelector(conn);
		// 用分离挂接的Node替换修行Link对应的几何,以保持精度
		RwNode catchNode = (RwNode) nodeSelector.loadById(catchNodePid, true,
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
	private void departCatchBreakLink(double lon, double lat, RwNode preNode,
			int linkPid, List<RwLink> links, Result result) throws Exception {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 如果没有挂接的link node需要继承 如果有node需要新生成
		int breakNodePid = preNode.getPid();
		if (links.size() > 1) {
			RwNode node = NodeOperateUtils.createRwNode(lon, lat);
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
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(
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
	private void moveNodeGeo(RwNode node, double lon, double lat, Result result)
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
		com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command(
				updateContent, command.getRequester(), node);
		com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process(
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
		Map<Integer, List<RwLink>> map = new HashMap<Integer, List<RwLink>>();
		List<RwLink> links = new ArrayList<RwLink>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(command
				.getLinkGeom());
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", command.getLinkGeom());
			Geometry geo = command.getLinkGeom();
			double length = 0;
			if (null != geo)
				length = GeometryUtils.getLinkLength(geo);
			content.put("length", length);
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(
					content);
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(),
						ObjStatus.UPDATE, this.command.getUpdateLink().getPid());
			}
			// 拷贝原link，set属性
			RwLink link = new RwLink();
			link.setPid(this.command.getUpdateLink().getPid());
			link.copy(this.command.getUpdateLink());
			link.setGeometry(GeoTranslator.transform(
					this.command.getLinkGeom(), 100000, 0));
			links.add(link);
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

							links.addAll(RwLinkOperateUtils
									.getCreateRwLinksWithMesh(subGeo, maps,
											result,
											this.command.getUpdateLink()));
						}
					}
				} else {
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);

					links.addAll(RwLinkOperateUtils.getCreateRwLinksWithMesh(
							geomInter, maps, result,
							this.command.getUpdateLink()));
				}

			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE,
					this.command.getUpdateLink().getPid());
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
	private void updataRelationObj(RwLink oldLink, List<RwLink> newLinks,
			Result result) throws Exception {
		// 立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

		Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

		for (RwLink link : newLinks) {
			newLinkMap.put(link.getPid(), link.getGeometry());
		}

		gscOperation.repairLink(this.command.getGscList(), newLinkMap, oldLink,
				result);
	}

}
