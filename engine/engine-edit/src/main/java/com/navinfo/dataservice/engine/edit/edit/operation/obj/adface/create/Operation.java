package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.comm.util.AdminUtils;
import com.navinfo.dataservice.engine.edit.comm.util.LinkOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.OperateUtils;
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

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;

		this.conn = conn;
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
		AdNode sNode = OperateUtils.createAdNode(sPoint.x, sPoint.y);
		result.insertObject(sNode, ObjStatus.INSERT, sNode.pid());
		Coordinate ePoint = geom.getCoordinates()[geom.getCoordinates().length - 1];
		AdNode eNode = OperateUtils.createAdNode(ePoint.x, ePoint.y);
		result.insertObject(eNode, ObjStatus.INSERT, eNode.pid());
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
			link.setStartNodePid(sNode.getPid());
			link.setEndNodePid(eNode.getPid());
			AdLinkMesh adLinkMesh = new AdLinkMesh();
			adLinkMesh.setLinkPid(link.getPid());
			adLinkMesh.setMeshId(meshId);
			List<IRow> adLinkMeshs = new ArrayList<IRow>();
			adLinkMeshs.add(adLinkMesh);
			link.setMeshes(adLinkMeshs);
			AdFace adFace = new AdFace();
			adFace.setPid(PidService.getInstance().applyAdFacePid());
			adFace.setArea(GeometryUtils.getCalculateArea(geom));
			adFace.setMeshId(meshId);
			adFace.setGeometry(GeoTranslator.transform(geom, 100000, 0));
			adFace.setPerimeter(GeometryUtils.getLinkLength(geom));
			AdFaceTopo adFaceTopo = new AdFaceTopo();
			adFaceTopo.setFacePid(adFace.getPid());
			adFaceTopo.setMesh(meshId);
			adFaceTopo.setSeqNum(1);
			adFaceTopo.setLinkPid(link.getPid());
			List<IRow> adFaceTopos = new ArrayList<IRow>();
			adFaceTopos.add(adFaceTopo);
			adFace.setFaceTopos(adFaceTopos);
			result.setPrimaryPid(adFace.getPid());
			result.insertObject(link, ObjStatus.INSERT, link.getPid());
			result.insertObject(adFace, ObjStatus.INSERT, adFace.getPid());
		}
	}

	/*
	 * 添加Link和FaceTopo关系
	 */
	public void addLink(AdFace face, AdLink link, int seqNum) {
		AdFaceTopo faceTopo = new AdFaceTopo();
		faceTopo.setLinkPid(link.getPid());
		faceTopo.setFacePid(face.getPid());
		faceTopo.setSeqNum(seqNum);
		result.insertObject(faceTopo, ObjStatus.INSERT, face.getPid());
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
		int startNodePid = currLink.getStartNodePid();
		int currNodePid = startNodePid;
		this.addLink(face, currLink, 1);
		int index = 1;
		List<Geometry> list = new ArrayList<Geometry>();
		list.add(currLink.getGeometry());
		// 获取下一条联通的LINK
		while (LinkOperateUtils.getNextLink(links, currNodePid, currLink)) {
			if (currNodePid == startNodePid) {
				break;
			}
			index++;
			this.addLink(face, currLink, index);
			list.add(currLink.getGeometry());
		}
		// 线几何组成面的几何
		Geometry g = GeoTranslator.getCalLineToPython(list);
		Coordinate[] c1 = new Coordinate[g.getCoordinates().length];
		// 判断线组成面是否可逆
		if (!GeometryUtils.IsCCW(g.getCoordinates())) {
			for (int i = g.getCoordinates().length - 1; i >= 0; i--) {
				c1[c1.length - i - 1] = c1[0];
			}
			this.reverseFaceTopo();

		}
		// 更新面的几何属性
		this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), face);

	}

	/*
	 * 更新面的几何属性
	 */
	private void updateGeometry(Geometry g, AdFace face) {
		face.setGeometry(g);
		face.setArea(GeometryUtils.getCalculateArea(g));
		face.setPerimeter(GeometryUtils.getLinkLength(g));
		result.insertObject(face, ObjStatus.UPDATE, face.getPid());
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
