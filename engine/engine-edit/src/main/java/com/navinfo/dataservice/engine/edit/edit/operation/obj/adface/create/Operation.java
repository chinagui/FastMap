package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.IObj;
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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.comm.util.operate.AdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.NodeOperateUtils;
import com.navinfo.navicommons.exception.GeoComputationException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * 
 * @author zhaokk 行政区划面有关操作
 */
public class Operation implements IOperation {

	private Command command;
	private Check check;
	private Connection conn;
	private Result result;
	private AdFace face;
	private boolean updateFlag = true;

	public Operation(Result result) {
		this.result = result;
	}

	public Operation(Result result, AdFace face) {
		this.face = face;
		this.result = result;
	}

	public Operation(Command command, Check check, Connection conn,
			Result result) {
		this.command = command;

		this.check = check;

		this.conn = conn;
		this.result = result;
		this.updateFlag = false;
	}

	@Override
	public String run(Result result) throws Exception {

		// 既有线构成面
		if (command.getLinkPids() != null) {
			// ADLINK
			if (command.getLinkType().equals(ObjType.ADLINK.toString())) {
				this.createFaceByAdLink(command.getLinks());
			}
			// RDLINK
			if (command.getLinkType().equals(ObjType.RDLINK.toString())) {
				// 根据RDLINK生成ADLINK
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				List<IObj> adLinks = new ArrayList<IObj>();
				for (IObj obj : command.getLinks()) {
					RdLink link = (RdLink) obj;
					adLinks.add(this.createLinkOfFace(GeoTranslator.transform(
							link.getGeometry(), 0.00001, 5), maps));
				}
				this.createFaceByAdLink(adLinks);
			}
		}
		// 创建
		if (command.getGeometry() != null) {
			this.createFaceByGeometry(result);
		}
		return null;
	}

	/**
	 * 根据既有线创建面
	 * 
	 * @param objList
	 *            传入要创建面的几何
	 * @throws Exception
	 */
	public  void createFaceByAdLink(List<IObj> objList) throws Exception {
		List<Geometry> list = new ArrayList<Geometry>();
		Set<String> meshes = new HashSet<String>();
		List<AdLink> adLinks = new ArrayList<AdLink>();
		for (IObj obj : command.getLinks()) {
			AdLink link = (AdLink) obj;
			adLinks.add(link);
			if (link.getMeshes().size() == 1) {
				for (IRow iRow : link.getMeshes()) {
					AdLinkMesh adlinkmesh = (AdLinkMesh) iRow;
					meshes.add(String.valueOf(adlinkmesh.getMeshId()));
				}
			}
			list.add(GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
		}
		if (meshes.size() == 1) {
			int meshId = Integer.parseInt(meshes.iterator().next());
			this.createFace();
			this.face.setMeshId(meshId);
			this.face.setMesh(meshId);
			this.reCaleFaceGeometry(adLinks);
		} else {
			Geometry geom = GeoTranslator.getCalLineToPython(list);
			this.createFaceWithMesh(meshes, geom, 0);
		}
	}

	/**
	 * @param meshes
	 *            跨域图幅
	 * @param geom
	 *            初始画面几何
	 * @param flag
	 *            创建面表示 0 根据几何，1 根据既有线
	 * @throws Exception
	 */
	private void createFaceWithMesh(Set<String> meshes, Geometry geom, int flag)
			throws Exception {
		Iterator<String> it = meshes.iterator();
		Map<Coordinate, Integer> mapNode = new HashMap<Coordinate, Integer>();
		Map<Geometry, AdLink> mapLink = new HashMap<Geometry, AdLink>();
		if (flag == 1) {
			for (IObj obj : command.getLinks()) {
				AdLink adLink = (AdLink) obj;
				mapLink.put(adLink.getGeometry(), adLink);
			}
		}
		while (it.hasNext()) {
			String meshIdStr = it.next();
			// 获取每个图幅中闭合线的数组
			Set<LineString[]> set = CompGeometryUtil.cut(
					JtsGeometryFactory.createPolygon(geom.getCoordinates()),
					meshIdStr);
			Iterator<LineString[]> itLine = set.iterator();
			while (itLine.hasNext()) {
				LineString[] lineStrings = itLine.next();
				List<AdLink> links = new ArrayList<AdLink>();
				// 创建线
				for (LineString lineString : lineStrings) {
					AdLink adLink = null;
					if (mapLink.containsKey(lineString)) {
						adLink = mapLink.get(lineString);
					} else {
						if (MeshUtils.isMeshLine(lineString)) {
							if (mapLink.containsKey(lineString.reverse())) {
								adLink = mapLink.get(lineString.reverse());
							}else{
								adLink = this.createLinkOfFace(lineString, mapNode);
								mapLink.put(lineString, adLink);
							}
						} else {
							adLink = this.createLinkOfFace(lineString, mapNode);
							mapLink.put(lineString, adLink);
						}
					}
					links.add(adLink);
				}
				this.createFace();
				this.reCaleFaceGeometry(links);
			}

		}

	}

	/**
	 * 按照几何形状生成面
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void createFaceByGeometry(Result result) throws Exception {
		Geometry geom = GeoTranslator.geojson2Jts(command.getGeometry(), 1, 5);
		Coordinate sPoint = geom.getCoordinates()[0];
		// 获取几何形状跨越图幅号
		Set<String> meshes = new HashSet<String>();
		meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geom);
		// 如果不跨图幅
		if (meshes.size() == 1) {
			// 生成起始node
			AdNode Node = NodeOperateUtils.createAdNode(sPoint.x, sPoint.y);
			result.insertObject(Node, ObjStatus.INSERT, Node.pid());
			this.createFace();
			List<AdLink> links = new ArrayList<AdLink>();
			links.add(AdLinkOperateUtils.getAddLink(geom, Node.getPid(),
					Node.getPid(), result));
			this.reCaleFaceGeometry(links);
		}// 如果跨图幅
		else {
			this.createFaceWithMesh(meshes, geom, 0);
		}

	}

	private AdLink createLinkOfFace(Geometry g, Map<Coordinate, Integer> maps)
			throws Exception {
		int sNodePid = 0;
		int eNodePid = 0;
		if (maps.containsKey(g.getCoordinates()[0])) {
			sNodePid = maps.get(g.getCoordinates()[0]);
		}
		if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
			eNodePid = maps
					.get(g.getCoordinates()[g.getCoordinates().length - 1]);
		}
		JSONObject node = AdLinkOperateUtils.createAdNodeForLink(g, sNodePid,
				eNodePid, result);
		if (!maps.containsValue(node.get("s"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("s"));
		}
		if (!maps.containsValue(node.get("e"))) {
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1],
					(int) node.get("e"));
		}
		return AdLinkOperateUtils.getAddLink(g, (int) node.get("s"),
				(int) node.get("e"), result);
	}

	/*
	 * 添加Link和FaceTopo关系
	 */
	public void addLink(Map<AdLink, Integer> map) {
		List<IRow> adFaceTopos = new ArrayList<IRow>();
		for (AdLink link : map.keySet()) {
			AdFaceTopo faceTopo = new AdFaceTopo();
			faceTopo.setLinkPid(link.getPid());
			faceTopo.setFacePid(face.getPid());
			faceTopo.setSeqNum(map.get(link));
			adFaceTopos.add(faceTopo);
		}
		this.face.setFaceTopos(adFaceTopos);
	}

	/*
	 * 添加Link和FaceTopo关系
	 */
	public void createFaceTop(Map<AdLink, Integer> map) {
		List<IRow> adFaceTopos = new ArrayList<IRow>();
		for (AdLink link : map.keySet()) {
			AdFaceTopo faceTopo = new AdFaceTopo();
			faceTopo.setLinkPid(link.getPid());
			faceTopo.setFacePid(face.getPid());
			faceTopo.setSeqNum(map.get(link));
			adFaceTopos.add(faceTopo);
			result.insertObject(faceTopo, ObjStatus.INSERT, face.getPid());
		}

	}

	/*
	 * @param List 按照ADFACE的形状重新维护ADFACE
	 */
	@SuppressWarnings("null")
	public void reCaleFaceGeometry(List<AdLink> links) throws Exception {
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
		Map<Integer, AdLink> currLinkAndPidMap = new HashMap<Integer, AdLink>();
		currLinkAndPidMap.put(currNodePid, currLink);
		// 获取下一条联通的LINK
		while (AdLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
			if (currLinkAndPidMap.keySet().iterator().next() == startNodePid) {
				break;
			}
			index++;
			map.put(currLinkAndPidMap.get(currLinkAndPidMap.keySet().iterator()
					.next()), index);
			list.add(currLinkAndPidMap.get(
					currLinkAndPidMap.keySet().iterator().next()).getGeometry());

		}
		// 线几何组成面的几何
		if (this.updateFlag) {
			this.createFaceTop(map);
		} else {
			this.addLink(map);
		}
		Geometry g = GeoTranslator.getCalLineToPython(list);
		Coordinate[] c1 = new Coordinate[g.getCoordinates().length];
		// 判断线组成面是否可逆
		if (!GeometryUtils.IsCCW(g.getCoordinates())) {
			for (int i = g.getCoordinates().length - 1; i >= 0; i--) {
				c1[c1.length - i - 1] = g.getCoordinates()[i];
			}
			this.reverseFaceTopo();

		} else {
			c1 = g.getCoordinates();
		}
		// 更新面的几何属性
		if (this.updateFlag) {
			this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), this.face);
			
		} else {
			this.createFaceGeometry(GeoTranslator.getPolygonToPoints(c1),
					this.face);
		}

	}

	/*
	 * 更新面的几何属性
	 */
	private void createFaceGeometry(Geometry g, AdFace face) throws Exception {
		face.setGeometry(g);
		// 缩放计算面积和周长
		g = GeoTranslator.transform(g, 0.00001, 5);
		String meshId =  CompGeometryUtil.geoToMeshesWithoutBreak(g).iterator().next();
		if (!StringUtils.isEmpty(meshId)) {
			face.setMeshId(Integer.parseInt(meshId));
		}
		face.setArea(GeometryUtils.getCalculateArea(g));
		face.setPerimeter(GeometryUtils.getLinkLength(g));
		result.insertObject(face, ObjStatus.INSERT, face.getPid());
	}

	/*
	 * 更新面的几何属性
	 */
	private void updateGeometry(Geometry g, AdFace face) throws Exception {

		JSONObject updateContent = new JSONObject();
		g = GeoTranslator.transform(g, 0.00001, 5);

		String meshId =  CompGeometryUtil.geoToMeshesWithoutBreak(g).iterator().next();
		if (!StringUtils.isEmpty(meshId)) {
			updateContent.put("mesh", Integer.parseInt(meshId));
		}
		updateContent.put("geometry", GeoTranslator.jts2Geojson(g));
		updateContent.put("area", GeometryUtils.getCalculateArea(g));
		updateContent.put("perimeter", GeometryUtils.getLinkLength(g));
		face.fillChangeFields(updateContent);
		result.insertObject(face, ObjStatus.UPDATE, face.getPid());
	}

	/*
	 * 更新面的几何属性
	 */
	private void createFace() throws Exception {
		AdFace face = new AdFace();
		face.setPid(PidService.getInstance().applyAdFacePid());
		result.setPrimaryPid(face.getPid());
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
