package com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;

import com.navinfo.dataservice.engine.edit.utils.AdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;
import org.json.JSONException;

import java.sql.Connection;
import java.util.*;

/***
 * 节点分离具体实现类
 * 
 * @author zhaokk
 * 
 * 
 */
public class Operation implements IOperation {

	private Command command;
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.departNode(result);
		return null;
	}

	/***
	 * 分离节点
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void departNode(Result result) throws Exception {

		if (this.command.getCatchNodePid() == 0
				&& this.command.getCatchLinkPid() == 0) {
			// 如果分离节点没有挂接link和node 并且分离的node没有挂接其它的link按照node移动功能处理
			if (this.command.getLinks().size() == 1) {
				this.removeNode(result);
			}
			// 如果分离节点没有挂接link和node 并且分离的node有挂接其它的link按照原则此node要新增
			if (this.command.getLinks().size() > 1) {
				AdNode node = NodeOperateUtils.createAdNode(this.command
						.getPoint().getX(), this.command.getPoint().getY());
				result.insertObject(node, ObjStatus.INSERT, node.pid());
				this.updateLinkGeomtry(result, node.getPid());
			}

		} else {
			// 节点挂接功能
			if (this.command.getCatchNodePid() != 0) {
				this.caleCatchNode(result);
			}
			// 打断功能

			if (this.command.getCatchLinkPid() != 0) {

				this.caleCatchBreakLink(result);
			}

		}
	}

	/***
	 * 挂接打断功能
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void caleCatchBreakLink(Result result) throws Exception {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", this.command.getCatchLinkPid());
		breakJson.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 如果没有挂接的link node需要继承 如果有node需要新生成
		int breakNodePid = this.command.getNodePid();
		if (this.command.getLinks().size() > 1) {
			AdNode node = NodeOperateUtils.createAdNode(this.command.getPoint()
					.getX(), this.command.getPoint().getY());
			result.insertObject(node, ObjStatus.INSERT, node.getPid());
			breakNodePid = node.getPid();

		}
		// node继承需要修改node的几何
		else {
			this.updateNodeGeo(result);

		}

		data.put("longitude", this.command.getPoint().getX());
		data.put("latitude", this.command.getPoint().getY());
		data.put("breakNodePid", breakNodePid);
		if (this.command.getLinks().size() == 1) {
			data.put("breakNodePid", this.command.getNodePid());
		}
		breakJson.put("data", data);
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
				breakCommand, result, conn);
		breakProcess.innerRun();

		// 维护修行link的几何
		this.updateLinkGeomtry(result, breakNodePid);

	}

	/**
	 * 修改node的几何
	 */

	private void updateNodeGeo(Result result) throws Exception {
		JSONObject geojson = new JSONObject();
		geojson.put("type", "Point");

		geojson.put("coordinates",
				new double[] { this.command.getPoint().getX(),
						this.command.getPoint().getY() });

		JSONObject updateContent = new JSONObject();

		// 要移动点的dbId
		updateContent.put("dbId", command.getDbId());
		JSONObject data = new JSONObject();
		// 移动点的新几何
		data.put("geometry", geojson);
		data.put("pid", this.command.getNodePid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateContent.put("data", data);

		// 组装更新线的参数
		// 保证是同一个连接
		com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(
				updateContent, command.getRequester(), this.command.getNode());
		com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(
				updatecommand, result, conn);
		process.innerRun();

	}

	/***
	 * 节点挂接功能
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void caleCatchNode(Result result) throws Exception {
		// 加载挂接的点求几何
		AdNodeSelector nodeSelector = new AdNodeSelector(conn);
		IRow row = nodeSelector.loadById(this.command.getCatchNodePid(), true,
				true);
		AdNode node = (AdNode) row;
		Geometry geom = GeoTranslator.transform(node.getGeometry(), 0.00001, 5);
		this.command.setPoint(((Point) GeoTranslator.point2Jts(
				geom.getCoordinate().x, geom.getCoordinate().y)));
		// 如果原有node挂接的LINK<=1 原来的node需要删除更新link的几何为新的node
		if (this.command.getLinks().size() <= 1) {
			result.insertObject(this.command.getNode(), ObjStatus.DELETE,
					this.command.getNodePid());
		}
		// 更新link的几何为新的node点
		this.updateLinkGeomtry(result, this.command.getCatchNodePid());

	}

	/***
	 * 调用点的移动功能
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void removeNode(Result result) throws Exception {
		JSONObject updateContent = new JSONObject();
		// 组装移动的参数
		// dbID
		updateContent.put("dbId", command.getDbId());

		JSONObject data = new JSONObject();
		// 移动的经纬度
		data.put("longitude", this.command.getPoint().getX());
		data.put("latitude", this.command.getPoint().getY());
		updateContent.put("objId", this.command.getNodePid());
		updateContent.put("data", data);
		// 调用移动的API
		com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(
				updateContent, this.command.getAdLink(), this.command.getNode());
		com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(
				updatecommand, result, conn);
		process.innerRun();
	}

	/***
	 * 修改分离后线的几何 如果分离后线跨图幅需要有打断功能
	 * 
	 * @param result
	 * @param nodePid
	 * @throws Exception
	 */
	private void updateLinkGeomtry(Result result, int nodePid) throws Exception {
		Geometry geom = GeoTranslator.transform(this.command.getAdLink()
				.getGeometry(), 0.00001, 5);

		Coordinate[] cs = geom.getCoordinates();

		double[][] ps = new double[cs.length][2];

		for (int i = 0; i < cs.length; i++) {
			ps[i][0] = cs[i].x;

			ps[i][1] = cs[i].y;
		}

		if (this.command.getAdLink().getsNodePid() == command.getNodePid()) {
			ps[0][0] = this.command.getPoint().getX();

			ps[0][1] = this.command.getPoint().getY();
		} else {
			ps[ps.length - 1][0] = this.command.getPoint().getX();

			ps[ps.length - 1][1] = this.command.getPoint().getY();
		}

		JSONObject geojson = new JSONObject();

		geojson.put("type", "LineString");

		geojson.put("coordinates", ps);
		Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);

		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		// 修改线的几何属性
		// 如果没有跨图幅只是修改线的几何
		List<AdLink> links = new ArrayList<AdLink>();
		if (meshes.size() == 1) {
			JSONObject updateContent = new JSONObject();
			if (this.command.getAdLink().geteNodePid() == this.command
					.getNodePid()) {
				updateContent.put("eNodePid", nodePid);
			} else {
				updateContent.put("sNodePid", nodePid);
			}
			updateContent.put("geometry", geojson);
			updateContent.put("length", GeometryUtils.getLinkLength(geo));
			this.command.getAdLink().fillChangeFields(updateContent);
			result.insertObject(this.command.getAdLink(), ObjStatus.UPDATE,
					this.command.getLinkPid());
			// 如果跨图幅就需要打断生成新的link
		} else {
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			if (geo.getCoordinates()[0].equals(this.command.getPoint()
					.getCoordinate())) {
				maps.put(geo.getCoordinates()[0], nodePid);

				maps.put(geo.getCoordinates()[geo.getCoordinates().length - 1],
						this.command.getAdLink().geteNodePid());
			} else {
				maps.put(geo.getCoordinates()[0], this.command.getAdLink()
						.geteNodePid());

				maps.put(geo.getCoordinates()[geo.getCoordinates().length - 1],
						nodePid);
			}

			Iterator<String> it = meshes.iterator();
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
						GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),
								1, 5));
				geomInter = GeoTranslator.geojson2Jts(
						GeoTranslator.jts2Geojson(geomInter), 1, 5);
				List<AdLink> adLinks = AdLinkOperateUtils
						.getCreateAdLinksWithMesh(geomInter, maps, result,
								this.command.getAdLink());
				for (AdLink link : adLinks) {
					result.insertObject(link, ObjStatus.INSERT, link.getPid());
				}
				links.addAll(adLinks);

			}
			result.insertObject(this.command.getAdLink(), ObjStatus.DELETE,
					this.command.getAdLink().pid());

		}
		// 分离节点属性关系维护
		this.updateRelation(links, result);
	}

	private void updateRelation(List<AdLink> newLinks, Result result)
			throws Exception {
		// 构造修改后的link几何
		assemblyLinks(newLinks);

	}

	private List<AdLink> assemblyLinks(List<AdLink> newLinks)
			throws JSONException {
		// 如果newLinkl
		if (newLinks.isEmpty()) {
			AdLink adLink = command.getAdLink();
			AdLink newLink = new AdLink();
			newLink.copy(adLink);
			newLink.setGeometry(GeoTranslator.geojson2Jts((JSONObject) adLink
					.changedFields().get("geometry")));
			newLink.setLength(GeometryUtils.getLinkLength(newLink.getGeometry()));
			newLinks.add(newLink);
		}
		return newLinks;
	}

}
