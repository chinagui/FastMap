package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
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
				RdNode node = NodeOperateUtils.createRdNode(this.command
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
			RdNode node = NodeOperateUtils.createRdNode(this.command.getPoint()
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
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
				breakCommand, conn, result);
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
		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(
				updateContent, command.getRequester(), this.command.getNode());
		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(
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
		RdNodeSelector nodeSelector = new RdNodeSelector(conn);
		IRow row = nodeSelector.loadById(this.command.getCatchNodePid(), true,
				true);
		RdNode node = (RdNode) row;
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
		com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(
				updateContent, this.command.getRdLink(), this.command.getNode());
		com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(
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
		Geometry geom = GeoTranslator.transform(this.command.getRdLink()
				.getGeometry(), 0.00001, 5);

		Coordinate[] cs = geom.getCoordinates();

		double[][] ps = new double[cs.length][2];

		for (int i = 0; i < cs.length; i++) {
			ps[i][0] = cs[i].x;

			ps[i][1] = cs[i].y;
		}

		if (this.command.getRdLink().getsNodePid() == command.getNodePid()) {
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
		List<RdLink> links = new ArrayList<RdLink>();
		if (meshes.size() == 1) {
			JSONObject updateContent = new JSONObject();
			if (this.command.getRdLink().geteNodePid() == this.command
					.getNodePid()) {
				updateContent.put("eNodePid", nodePid);
			} else {
				updateContent.put("sNodePid", nodePid);
			}
			updateContent.put("geometry", geojson);
			updateContent.put("length", GeometryUtils.getLinkLength(geo));
			this.command.getRdLink().fillChangeFields(updateContent);
			result.insertObject(this.command.getRdLink(), ObjStatus.UPDATE,
					this.command.getLinkPid());
			// 如果跨图幅就需要打断生成新的link
		} else {
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			if (geo.getCoordinates()[0].equals(this.command.getPoint()
					.getCoordinate())) {
				maps.put(geo.getCoordinates()[0], nodePid);

				maps.put(geo.getCoordinates()[geo.getCoordinates().length - 1],
						this.command.getRdLink().geteNodePid());
			} else {
				maps.put(geo.getCoordinates()[0], this.command.getRdLink()
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
				RdLinkOperateUtils.createRdLinkWithMesh(geomInter, maps,
						this.command.getRdLink(), result, links);

			}

			result.insertObject(this.command.getRdLink(), ObjStatus.DELETE,
					this.command.getRdLink().pid());

		}
		// 分离节点属性关系维护
		this.updateRelation(links, result);
	}

	private void updateRelation(List<RdLink> newLinks, Result result)
			throws Exception {
		// 构造修改后的link几何
		assemblyLinks(newLinks);

		// 维护电子眼
		OpRefElectroniceye opRefElectroniceye = new OpRefElectroniceye(
				this.conn);
		opRefElectroniceye.updateRelation(command, newLinks, result);
		// 维护IxPoi
		OpRefIxPoi opRefIxPoi = new OpRefIxPoi(this.conn);
		opRefIxPoi.updateRelation(command, newLinks, result);
		// 维护RdGsc
		OpRefRdgsc opRefRdgsc = new OpRefRdgsc(this.conn);
		opRefRdgsc.updateRelation(command, newLinks, result);
		// 维护CRF对象
		OpRefRdObject opRefRdObject = new OpRefRdObject(this.conn);
		opRefRdObject.updateRelation(command, newLinks, result);
		// 维护同一关系
		OpRefRdSamelink opRefRdSamelink = new OpRefRdSamelink(this.conn);
		opRefRdSamelink.updateRelation(command, newLinks, result);
		// 维护点限速
		OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(this.conn);
		opRefSpeedlimit.updateRelation(command, newLinks, result);

		// 维护点限速
		OpRefRdlane opRefRdlane = new OpRefRdlane(this.conn);
		opRefRdlane.updateRelation(command, newLinks, result);

		OpRefRelationObj opRefRelationObj = new OpRefRelationObj(this.conn);
		// 维护分歧
		opRefRelationObj.handleRdBranch(command, newLinks, result);
		// 维护顺行
		opRefRelationObj.handleRdDirectroute(command, newLinks, result);
		// 维护大门
		opRefRelationObj.handleRdGate(command, newLinks, result);
		// 维护车信
		opRefRelationObj.handleRdLaneconnexity(command, newLinks, result);
		// 维护交限
		opRefRelationObj.handleRdRestriction(command, newLinks, result);
		// 维护分岔提示
		opRefRelationObj.handleRdSe(command, newLinks, result);
		// 维护坡度
		opRefRelationObj.handleRdSlope(command, newLinks, result);
		// 维护减速带
		opRefRelationObj.handleRdSpeedbump(command, newLinks, result);
		// 维护收费站
		opRefRelationObj.handleRdTollgate(command, newLinks, result);
		// 维护可变限速
		opRefRelationObj.handleRdVariableSpeed(command, newLinks, result);
		// 维护语音引导
		opRefRelationObj.handleRdVoiceguide(command, newLinks, result);
		// 维护警示信息
		opRefRelationObj.handleRdWarninginfo(command, newLinks, result);
		// 维护限高限重
		opRefRelationObj.handleRdHgwgLimit(command, newLinks, result);
	}

	private List<RdLink> assemblyLinks(List<RdLink> newLinks)
			throws JSONException {
		// 如果newLinkl
		if (newLinks.isEmpty()) {
			RdLink rdLink = command.getRdLink();
			RdLink newLink = new RdLink();
			newLink.copy(rdLink);
			newLink.setGeometry(GeoTranslator.geojson2Jts((JSONObject) rdLink
					.changedFields().get("geometry")));
			newLink.setLength(GeometryUtils.getLinkLength(newLink.getGeometry()));
			newLinks.add(newLink);
		}
		return newLinks;
	}

}
