package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.comm.util.operate.AdminOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.type.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private RdLink updateLink;

	private RdNode enode;

	private RdNode snode;

	private Connection conn;

	public Operation(Connection conn, Command command, RdLink updateLink, RdNode snode, RdNode enode, Check check) {

		this.conn = conn;

		this.command = command;

		this.updateLink = updateLink;

		this.enode = enode;

		this.snode = snode;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = new JSONObject();

		result.setPrimaryPid(updateLink.getPid());

		content.put("geometry", command.getLinkGeom());

		// 判断端点有没有移动，如果移动，则需要分离节点

		JSONArray coords = command.getLinkGeom().getJSONArray("coordinates");

		JSONArray scoord = coords.getJSONArray(0);

		double slon = scoord.getDouble(0);
		double slat = scoord.getDouble(1);

		JSONArray ecoord = coords.getJSONArray(coords.size() - 1);

		double elon = ecoord.getDouble(0);
		double elat = ecoord.getDouble(1);

		Geometry geo = GeoTranslator.transform(updateLink.getGeometry(), 0.00001, 5);

		Coordinate[] oldCoords = geo.getCoordinates();

		boolean sNodeDepart = checkDepartSNode(oldCoords[0], slon, slat);

		boolean eNodeDepart = checkDepartENode(oldCoords[oldCoords.length - 1], elon, elat);

		com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Process departProcess = null;

		if (sNodeDepart || eNodeDepart) {

			JSONObject json = new JSONObject();

			json.put("projectId", command.getProjectId());

			JSONObject data = new JSONObject();

			data.put("linkPid", updateLink.getPid());

			if (sNodeDepart) {
				data.put("sNodePid", updateLink.getsNodePid());

				data.put("slon", slon);

				data.put("slat", slat);
			}

			if (eNodeDepart) {
				data.put("eNodePid", updateLink.geteNodePid());

				data.put("elon", elon);

				data.put("elat", elat);
			}

			json.put("data", data);

			com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Command departCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Command(
					json, json.toString());

			departProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Process(
					departCommand, conn);

			departProcess.preCheck();

			departProcess.prepareData();

			departProcess.processRefObj();

			departProcess.recordData();

		}

		if (command.getInterLines().size() == 0 && command.getInterNodes().size() == 0) {
			// 没有挂接到别的link或node上

			if (sNodeDepart) { // 需要新增节点

				RdNode node = NodeOperateUtils.createNode(slon, slat);

				content.put("sNodePid", node.getPid());

				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}

			if (eNodeDepart) {

				RdNode node = NodeOperateUtils.createNode(elon, elat);

				content.put("eNodePid", node.getPid());

				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}

		} else if ((command.getInterLines().size() == 1 || command.getInterLines().size() == 2)
				&& command.getInterNodes().size() == 0) {
			// 只挂接到link上

			int sNodePid = updateLink.getsNodePid();

			int eNodePid = updateLink.geteNodePid();

			if (sNodeDepart) { // 需要新增节点

				RdNode node = NodeOperateUtils.createNode(slon, slat);

				content.put("sNodePid", node.getPid());

				sNodePid = node.getPid();

				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}

			if (eNodeDepart) {

				RdNode node = NodeOperateUtils.createNode(elon, elat);

				content.put("eNodePid", node.getPid());

				eNodePid = node.getPid();

				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}

			this.breakLine(sNodePid, eNodePid);

		} else if (command.getInterLines().size() == 0
				&& (command.getInterNodes().size() == 1 || command.getInterNodes().size() == 2)) {
			// link的一个端点挂接到另外一组link的端点
			for (int i = 0; i < command.getInterNodes().size(); i++) {
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);

				int nodePid = mountNode.getInt("nodePid");

				int pid = mountNode.getInt("pid");

				if (nodePid == updateLink.getsNodePid()) {
					content.put("sNodePid", pid);

					if (!sNodeDepart) { // 如果是分离节点，需要保留该node，否则不保留
						result.insertObject(snode, ObjStatus.DELETE, snode.pid());
					}
				} else {
					content.put("eNodePid", pid);

					if (!eNodeDepart) { // 如果是分离节点，需要保留该node，否则不保留
						result.insertObject(enode, ObjStatus.DELETE, enode.pid());
					}
				}
			}

		} else if (command.getInterLines().size() == 1 && command.getInterNodes().size() == 1) {
			// link的一个端点打断另外一根link、link的一个端点挂接到另外一组link的端点

			for (int i = 0; i < command.getInterNodes().size(); i++) {
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);

				int nodePid = mountNode.getInt("nodePid");

				int pid = mountNode.getInt("pid");

				if (nodePid == updateLink.getsNodePid()) {
					content.put("sNodePid", pid);

					if (!sNodeDepart) { // 如果是分离节点，需要保留该node，否则不保留
						result.insertObject(snode, ObjStatus.DELETE, snode.pid());
					}
				} else {
					content.put("eNodePid", pid);

					if (!eNodeDepart) { // 如果是分离节点，需要保留该node，否则不保留
						result.insertObject(enode, ObjStatus.DELETE, enode.pid());
					}
				}
			}

			int sNodePid = updateLink.getsNodePid();

			int eNodePid = updateLink.geteNodePid();

			if (content.containsKey("eNodePid")) {
				if (sNodeDepart) { // 需要新增节点

					RdNode node = NodeOperateUtils.createNode(slon, slat);

					content.put("sNodePid", node.getPid());

					sNodePid = node.getPid();

					result.insertObject(node, ObjStatus.INSERT, node.pid());
				}
			} else {
				if (eNodeDepart) {

					RdNode node = NodeOperateUtils.createNode(elon, elat);

					content.put("eNodePid", node.getPid());

					eNodePid = node.getPid();

					result.insertObject(node, ObjStatus.INSERT, node.pid());
				}
			}

			this.breakLine(sNodePid, eNodePid);
		} else {
			// 错误请求
		}
		System.out.println();
		Geometry g = GeoTranslator.geojson2Jts(command.getLinkGeom());

		Set<String> meshes = MeshUtils.getInterMeshes(g);

		// 跨图幅
		if (meshes.size() > 1) {
			//在图幅处打断重新生成新的link
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			maps.put(g.getCoordinates()[0], updateLink.getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1], updateLink.geteNodePid());
			Iterator<String> it = meshes.iterator();
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(g, MeshUtils.mesh2Jts(meshIdStr));
				geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
				this.createRdLinkWithMesh(geomInter, maps, result);
			}
			//删掉原始link
			result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());
		}
		else
		{
			//没有跨图幅只是修改属性
			boolean isChanged = updateLink.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
			}

		}
		
		return null;
	}
	
	/*
	 * 创建RDLINK针对跨图幅有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 跨图幅需要生成和图廓线的交点
	 */
	private void createRdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result) throws Exception {
		if (g != null) {

			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				this.calRdLinkWithMesh(g, maps, result);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					this.calRdLinkWithMesh(g.getGeometryN(i), maps, result);
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
			maps.put(g.getCoordinates()[0], (int) node.get("e"));
		}
		// 创建线
		RdLink link = RdLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);
		
		link.setKind(updateLink.getKind());

		link.setLaneNum(updateLink.getLaneNum());
		
		AdminOperateUtils.SetAdminInfo4Link(link, conn);

		result.insertObject(link, ObjStatus.INSERT, link.pid());
	}
	
	public void breakLine(int sNodePid, int eNodePid) throws Exception {

		JSONArray coords = command.getLinkGeom().getJSONArray("coordinates");

		for (int i = 0; i < command.getInterLines().size(); i++) {
			// link的一个端点打断另外一根link
			JSONObject interLine = command.getInterLines().getJSONObject(i);
			JSONObject breakJson = new JSONObject();
			JSONObject data = new JSONObject();

			breakJson.put("objId", interLine.getInt("pid"));
			breakJson.put("projectId", command.getProjectId());

			int nodePid = interLine.getInt("nodePid");
			if (nodePid == updateLink.getsNodePid()) {
				data.put("breakNodePid", sNodePid);

				JSONArray coord = coords.getJSONArray(0);

				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);

				data.put("longitude", lon);
				data.put("latitude", lat);
			} else {
				data.put("breakNodePid", eNodePid);

				JSONArray coord = coords.getJSONArray(coords.size() - 1);

				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);

				data.put("longitude", lon);
				data.put("latitude", lat);
			}
			breakJson.put("data", data);
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command(
					breakJson, breakJson.toString());
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process(
					breakCommand, conn);
			breakProcess.runNotCommit();
		}
	}

	private boolean checkDepartSNode(Coordinate oldPoint, double lon, double lat) throws Exception {

		if (lon != oldPoint.x || lat != oldPoint.y) {
			// 移动了几何，如果挂接了多条link，需要分离节点

			RdNodeSelector selector = new RdNodeSelector(conn);

			int count = selector.loadRdLinkCountOnNode(updateLink.getsNodePid());

			if (count > 1) {
				return true;
			}
		}

		return false;
	}

	private boolean checkDepartENode(Coordinate oldPoint, double lon, double lat) throws Exception {

		if (lon != oldPoint.x || lat != oldPoint.y) {
			// 移动了几何，如果挂接了多条link，需要分离节点

			RdNodeSelector selector = new RdNodeSelector(conn);

			int count = selector.loadRdLinkCountOnNode(updateLink.geteNodePid());

			if (count > 1) {
				return true;
			}
		}

		return false;
	}

}
