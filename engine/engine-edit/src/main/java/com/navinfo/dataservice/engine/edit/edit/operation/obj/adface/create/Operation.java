package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.comm.util.operate.AdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.NodeOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
/**
 * 
 * @author zhaokk
 * 行政区划面有关操作
 */
public class Operation implements IOperation {

	private Command command;
	private Check check;
	private Connection conn;
	private Result result;
	private AdFace face;

	public Operation(Result result) {
		this.result = result;
	}

	public Operation(Command command, Check check, Connection conn,Result result) {
		this.command = command;

		this.check = check;

		this.conn = conn;
		this.result = result;
	}

	@Override
	public String run(Result result) throws Exception {

		// 既有线构成面
		if (command.getLinkPids() != null) {
			// ADLINK
			if (command.getLinkType().equals(ObjType.ADLINK.toString())) {
				this.createFace();
				this.reCaleFaceGeometry(command.getLinks(), face);

			}
			// RDLINK
			if (command.getLinkType().equals(ObjType.RDLINK.toString())) {
				// 需求待定
			}
		}
		// 创建
		if (command.getGeometry() != null) {
			this.createFaceByGeometry(result);
		}
		return null;
	}

	private Set<String> getLinkInterMesh(Geometry linkGeom) throws Exception {
		Set<String> set = new HashSet<String>();

		Coordinate[] cs = linkGeom.getCoordinates();

		for (Coordinate c : cs) {
			set.add(MeshUtils.lonlat2Mesh(c.x, c.y));
		}

		return set;
	}
	

	/*
	 * 创建行政区划面根据几何属性来创建面
	 */
	public void createFaceByGeometry(Result result) throws Exception {
		Geometry geom = GeoTranslator.geojson2Jts(command.getGeometry(), 1, 5);
		Coordinate sPoint = geom.getCoordinates()[0];
		AdNode Node = NodeOperateUtils.createAdNode(sPoint.x, sPoint.y);
		result.insertObject(Node, ObjStatus.INSERT, Node.pid());
		Set<String> meshes = new HashSet<String>();
		meshes = this.getLinkInterMesh(geom);
		if (meshes.size() == 1) {
			AdLink link = new AdLink();
			int meshId = Integer.parseInt(meshes.iterator().next());
			link.setPid(PidService.getInstance().applyAdLinkPid());
			link.setMesh(meshId);
			double linkLength = GeometryUtils.getLinkLength(geom);
			link.setLength(linkLength);
			link.setGeometry(GeoTranslator.transform(geom, 100000, 0));
			link.setsNodePid(Node.getPid());
			link.seteNodePid(Node.getPid());
			AdLinkMesh adLinkMesh = new AdLinkMesh();
			adLinkMesh.setLinkPid(link.getPid());
			adLinkMesh.setMeshId(meshId);
			List<IRow> adLinkMeshs = new ArrayList<IRow>();
			adLinkMeshs.add(adLinkMesh);
			link.setMeshes(adLinkMeshs);
			result.insertObject(link, ObjStatus.INSERT, link.getPid());
			this.createFace();
			this.face.setMeshId(meshId);
			this.face.setMesh(meshId);
			List<AdLink> links = new ArrayList<AdLink>();
			links.add(link);
			this.reCaleFaceGeometry(links, face);
		}
	}

	/*
	 * 添加Link和FaceTopo关系
	 */
	public void addLink( Map<AdLink,Integer> map) {
		List<IRow> adFaceTopos = new ArrayList<IRow>();
		for (AdLink  link :map.keySet()){
			AdFaceTopo faceTopo = new AdFaceTopo();
			faceTopo.setLinkPid(link.getPid());
			faceTopo.setFacePid(face.getPid());
			faceTopo.setSeqNum(map.get(link));
			adFaceTopos.add(faceTopo);
		}
		this.face.setFaceTopos(adFaceTopos);
	}

	/*
	 * @param List 按照ADFACE的形状重新维护ADFACE
	 */
	@SuppressWarnings("null")
	public void reCaleFaceGeometry(List<AdLink> links, AdFace face)
			throws Exception {
		if (links == null && links.size() < 1) {
			throw new Exception("重新维护面的形状:发现面没有组成link");
		}
		AdLink currLink = null;
		for (AdLink adLink : links) {
			currLink = adLink;
			break;
		}
		if (currLink == null) {
			return;
		}
		// 获取当前LINK和NODE
		int startNodePid = currLink.getsNodePid();
		int currNodePid = startNodePid;
		Map<AdLink, Integer> map = new HashMap<AdLink, Integer>();
		map.put(currLink, 1);
		int index = 1;
		List<Geometry> list = new ArrayList<Geometry>();
		list.add(currLink.getGeometry());
		// 获取下一条联通的LINK
		while (AdLinkOperateUtils.getNextLink(links, currNodePid, currLink)) {
			if (currNodePid == startNodePid) {
				break;
			}
			index++;
			map.put(currLink, index);
			list.add(currLink.getGeometry());
		}
		// 线几何组成面的几何
		this.addLink(map);
		Geometry g = GeoTranslator.getCalLineToPython(list);
		Coordinate[] c1 = new Coordinate[g.getCoordinates().length];
		// 判断线组成面是否可逆
		if (!GeometryUtils.IsCCW(g.getCoordinates())) {
			for (int i = g.getCoordinates().length - 1; i >= 0; i--) {
				c1[c1.length - i - 1] = g.getCoordinates()[i];
			}
			this.reverseFaceTopo();

		}else{
			c1 = g.getCoordinates();
		}
		// 更新面的几何属性
		this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), face);

	}

	/*
	 * 更新面的几何属性
	 */
	private void updateGeometry(Geometry g, AdFace face) throws Exception {
		face.setGeometry(g);
		face.setArea(GeometryUtils.getCalculateArea(g));
		face.setPerimeter(GeometryUtils.getLinkLength(g));
		result.insertObject(face, ObjStatus.INSERT, face.getPid());
	}

	/*
	 * 更新面的几何属性
	 */
	private void createFace() throws Exception {
		AdFace face = new AdFace();
		face.setPid(PidService.getInstance().applyAdFacePid());
		this.face = face;
	}

	/*
	 * 重新维护faceTopo的顺序关系
	 */
	private void reverseFaceTopo() {
		int newIndex = 0;
		for (int i = result.getAddObjects().size() - 1; i >= 0; i--) {
			if (result.getAddObjects().get(i) instanceof AdFaceTopo) {
				newIndex++;
				((AdFaceTopo) result.getAddObjects().get(i))
						.setSeqNum(newIndex);

			}
		}
	}

}
