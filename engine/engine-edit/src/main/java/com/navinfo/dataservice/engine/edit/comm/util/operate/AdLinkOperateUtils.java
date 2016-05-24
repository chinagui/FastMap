package com.navinfo.dataservice.engine.edit.comm.util.operate;

import io.netty.util.internal.MpscLinkedQueueNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhaokk LINK 公共方法
 */
public class AdLinkOperateUtils {

	/*
	 * 添加link获取下一条连接的link
	 */
	public static boolean getNextLink(List<AdLink> links, Map<Integer, AdLink> map) throws Exception {
		int nextNodePid = 0;
		int currNodePid = map.keySet().iterator().next();
		AdLink currLink = map.get(map.keySet().iterator().next());
		if (currNodePid == currLink.getsNodePid()) {
			nextNodePid = currLink.geteNodePid();
		} else {
			nextNodePid = currLink.getsNodePid();
		}
		for (AdLink link : links) {
			if (link.getPid() == currLink.getPid()) {
				continue;
			}
			if (link.getsNodePid() == nextNodePid
					|| link.geteNodePid() == nextNodePid) {
				map.clear();
				map.put(nextNodePid, link);
				return true;
			}
		}
		return false;
	}
	/*
	 * 创建生成一条ADLINK
	 * */
	public static void addLink(Geometry g,int sNodePid, int eNodePid,Result result) throws Exception{
		AdLink link = new AdLink();
		Set<String> meshes = MeshUtils.getInterMeshes(g);
		int meshId = Integer.parseInt(meshes.iterator().next());
		link.setMesh(meshId);
		link.setPid(PidService.getInstance().applyAdLinkPid());
		double linkLength = GeometryUtils.getLinkLength(g);
		link.setLength(linkLength);
		link.setGeometry(GeoTranslator.transform(g, 100000, 0));
		link.setsNodePid(sNodePid);
		link.seteNodePid(eNodePid);
		setLinkChildren(link);
		result.setPrimaryPid(link.pid());
		result.insertObject(link, ObjStatus.INSERT, link.pid());
	}
	/*
	 * 创建生成一条ADLINK
	 * 继承原有LINK的属性
	 * */
	public static IRow addLinkBySourceLink(Geometry g,int sNodePid, int eNodePid,AdLink sourcelink,Result result) throws Exception{
		AdLink link = new AdLink();
		link.copy(sourcelink);
		Set<String> meshes = MeshUtils.getInterMeshes(g);
		int meshId = Integer.parseInt(meshes.iterator().next());
		link.setMesh(meshId);
		link.setPid(PidService.getInstance().applyAdLinkPid());
		double linkLength = GeometryUtils.getLinkLength(g);
		link.setLength(linkLength);
		link.setGeometry(GeoTranslator.transform(g, 100000, 0));
		link.setsNodePid(sNodePid);
		link.seteNodePid(eNodePid);
		setLinkChildren(link);
		result.insertObject(link, ObjStatus.INSERT, link.pid());
		return link;
	}
	
	
	
	
	/*
	 * 维护link的子表 AD_LINK_MESH
	 * 
	 * @param link
	 */
	private static void setLinkChildren(AdLink link) {

		AdLinkMesh mesh = new AdLinkMesh();

		mesh.setLinkPid(link.getPid());

		mesh.setMesh(link.mesh());

		List<IRow> meshes = new ArrayList<IRow>();

		meshes.add(mesh);

		link.setMeshes(meshes);
	}

	/*
	 * 创建一条行政区划线对应的端点
	 */
	public static JSONObject createAdNodeForLink(Geometry g, int sNodePid, int eNodePid,Result result)
			throws Exception {
		JSONObject node = new JSONObject();
		if (0 == sNodePid) {
			Coordinate point = g.getCoordinates()[0];
			AdNode adNode =NodeOperateUtils.createAdNode(point.x, point.y);
			result.insertObject(adNode, ObjStatus.INSERT, adNode.pid());
			node.put("s", adNode.getPid());
		}else{
			node.put("s", sNodePid);
		}
		//创建终止点信息
		if (0 == eNodePid) {
			Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
			AdNode adNode=NodeOperateUtils.createAdNode(point.x, point.y);
			result.insertObject(adNode, ObjStatus.INSERT, adNode.pid());
			node.put("e", adNode.getPid());
		}else{
			node.put("e", eNodePid);
		}
		return node;
		
	}

	/*
	 * 分割行政区划线
	 * 
	 * @param geometry 要分割线的几何 sNodePid 起点pid eNodePid 终点pid catchLinks
	 * 挂接的线和点的集合 1.生成所有不存在的RDNODE 2.标记挂接的link被打断的点 3.返回线被分割的几何属性和起点和终点的List集合
	 */
	public static Map<Geometry, JSONObject> splitLink(Geometry geometry, int sNodePid,
			int eNodePid, JSONArray catchLinks, Result result) throws Exception {
		Map<Geometry, JSONObject> maps = new HashMap<Geometry, JSONObject>();
		JSONArray coordinates = GeoTranslator.jts2Geojson(geometry)
				.getJSONArray("coordinates");
		JSONObject tmpGeom = new JSONObject();
		// 组装要生成的link
		tmpGeom.put("type", "LineString");
		JSONArray tmpCs = new JSONArray();
		// 添加第一个点几何
		tmpCs.add(coordinates.get(0));

		int p = 0;

		int pc = 1;
		// 挂接的第一个点是LINK的几何属性第一个点
		if (tmpCs.getJSONArray(0).getDouble(0) == catchLinks.getJSONObject(0)
				.getDouble("lon")
				&& tmpCs.getJSONArray(0).getDouble(1) == catchLinks
						.getJSONObject(0).getDouble("lat")) {
			p = 1;
		}
		JSONObject se = new JSONObject();
        // 生成起点ADNODE
		if (0 == sNodePid) {
			double x = coordinates.getJSONArray(0).getDouble(0);

			double y = coordinates.getJSONArray(0).getDouble(1);

			AdNode node = NodeOperateUtils.createAdNode(x, y);
			result.insertObject(node, ObjStatus.INSERT, node.pid());
			se.put("s", node.getPid());

			if (p == 1 && catchLinks.getJSONObject(0).containsKey("linkPid")) {
				catchLinks.getJSONObject(0).put("breakNode", node.getPid());
			}
		} else {
			se.put("s", sNodePid);
		}
        //循环当前要分割LINK的几何 循环挂接的集合
		// 当挂接几何和link的集合有相同的点 生成新的link
		//如果挂接的存在linkPid 则被打断，且生成新的点
		//如果挂接只有ADNODE则不需要生成新的ADNODE
		while (p < catchLinks.size() && pc < coordinates.size()) {
			tmpCs.add(coordinates.getJSONArray(pc));

			if (coordinates.getJSONArray(pc).getDouble(0) == catchLinks
					.getJSONObject(p).getDouble("lon")
					&& coordinates.getJSONArray(pc).getDouble(1) == catchLinks
							.getJSONObject(p).getDouble("lat")) {

				tmpGeom.put("coordinates", tmpCs);
				if (catchLinks.getJSONObject(p).containsKey("nodePid")) {
					se.put("e", catchLinks.getJSONObject(p).getInt("nodePid"));
					maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
					se = new JSONObject();

					se.put("s", catchLinks.getJSONObject(p).getInt("nodePid"));
				} else {
					double x = catchLinks.getJSONObject(p).getDouble("lon");

					double y = catchLinks.getJSONObject(p).getDouble("lat");

					AdNode node = NodeOperateUtils.createAdNode(x, y);

					result.insertObject(node, ObjStatus.INSERT, node.pid());

					se.put("e", node.getPid());
					maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
					se = new JSONObject();

					se.put("s", node.getPid());

					catchLinks.getJSONObject(p).put("breakNode", node.getPid());
				}

				tmpGeom = new JSONObject();

				tmpGeom.put("type", "LineString");

				tmpCs = new JSONArray();

				tmpCs.add(coordinates.getJSONArray(pc));

				p++;
			}

			pc++;
		}
        //循环挂接的线是否完毕 如果>1 则表示完毕
		if (tmpCs.size() > 1) {
			tmpGeom.put("coordinates", tmpCs);
			if (eNodePid != 0) {
				se.put("e", eNodePid);

			} else {
				double x = tmpCs.getJSONArray(tmpCs.size() - 1).getDouble(0);

				double y = tmpCs.getJSONArray(tmpCs.size() - 1).getDouble(1);

				AdNode node = NodeOperateUtils.createAdNode(x, y);

				result.insertObject(node, ObjStatus.INSERT, node.pid());

				se.put("e", node.getPid());
			}
			maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
		}
		return maps;

	}
	/*
	 * 根据移动link端点重新生成link的几何
	 */
	public static  Geometry caleLinkGeomertyForMvNode(AdLink link,int nodePid,double lon,double lat) throws JSONException{
		Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
		Coordinate[] cs = geom.getCoordinates();
		double[][] ps = new double[cs.length][2];

		for (int i = 0; i < cs.length; i++) {
			ps[i][0] = cs[i].x;

			ps[i][1] = cs[i].y;
		}

		if (link.getsNodePid() == nodePid) {
			ps[0][0] = lon;

			ps[0][1] = lat;
		} else {
			ps[ps.length - 1][0] = lon;

			ps[ps.length - 1][1] = lat;
		}
		JSONObject geojson = new JSONObject();

		geojson.put("type", "LineString");

		geojson.put("coordinates", ps);
		return (GeoTranslator.geojson2Jts(geojson, 1, 5));
	}

}
