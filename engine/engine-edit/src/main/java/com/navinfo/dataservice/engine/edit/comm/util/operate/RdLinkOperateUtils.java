package com.navinfo.dataservice.engine.edit.comm.util.operate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhaokk LINK 公共方法
 */
public class RdLinkOperateUtils {
	
	/*
	 * 分割线
	 * 
	 * @param geometry 要分割线的几何 sNodePid 起点pid eNodePid 终点pid catchLinks
	 * 挂接的线和点的集合 1.生成所有不存在的RDNODE 2.标记挂接的link被打断的点 3.返回线被分割的几何属性和起点和终点的List集合
	 */
	public static Map<Geometry, JSONObject> splitRdLink(Geometry geometry, int sNodePid,
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

			RdNode node = NodeOperateUtils.createNode(x, y);
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
		//如果挂接只有RDNODE则不需要生成新的RDNODE
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

					RdNode node = NodeOperateUtils.createNode(x, y);

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

				RdNode node = NodeOperateUtils.createNode(x, y);

				result.insertObject(node, ObjStatus.INSERT, node.pid());

				se.put("e", node.getPid());
			}
			maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
		}
		return maps;

	}
	
	/*
	 * 创建生成一条RDLINK
	 * 继承原有LINK的属性
	 * */
	public static IRow addLinkBySourceLink(RdNode sNode,RdNode eNode,RdLink link,RdLink sourceLink,Result
			result) throws Exception{
		//继承原有link信息
		link.copy(sourceLink);
		//获取pid
		link.setPid(PidService.getInstance().applyLinkPid());
		//计算Geometry
		link.setGeometry(GeoTranslator.transform(link.getGeometry(), 100000, 0));
		result.setPrimaryPid(link.getPid());
		double linkLength = GeometryUtils.getLinkLength(link.getGeometry());
		link.setLength(linkLength);
		link.setOriginLinkPid(link.getPid());
		link.setsNodePid(sNode.getPid());
		link.seteNodePid(eNode.getPid());
		result.insertObject(link, ObjStatus.INSERT, link.pid());
		return link;
	}
	
	/*
	 * 创建生成一条RDLINK
	 * 继承原有LINK的属性
	 * */
	public static IRow addLinkByNoResult(RdNode sNode,RdNode eNode,RdLink link,RdLink sourceLink) throws Exception{
		//继承原有link信息
		Geometry geometry = link.getGeometry();
		link.setPid(PidService.getInstance().applyLinkPid());
		link.copy(sourceLink);
		//计算Geometry
		link.setGeometry(GeoTranslator.transform(geometry, 100000, 0));
		double linkLength = GeometryUtils.getLinkLength(link.getGeometry());
		link.setLength(linkLength);
		link.setOriginLinkPid(link.getPid());
		link.setsNodePid(sNode.getPid());
		link.seteNodePid(eNode.getPid());
		return link;
	}
	/*
	 * 创建生成一条RDLINK
	 * */
	public static List<RdLink> addRdLink(RdNode sNode,RdNode eNode,RdLink link,RdLink sourceLink, Result result)
			throws Exception {
			List<RdLink> links = new ArrayList<RdLink>();
			Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(link.getGeometry());
			//不跨图幅
			if (meshes.size() == 1) {
				if(sourceLink != null && sourceLink.getPid() != 0){
					links.add(((RdLink)addLinkByNoResult(sNode, eNode,link, sourceLink)));
				}
			} 
			//跨图幅
			else {
				Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
				maps.put(sNode.getGeometry().getCoordinate(), sNode.getPid());
				maps.put(eNode.getGeometry().getCoordinate(), eNode.getPid());
				Iterator<String> it = meshes.iterator();
				while (it.hasNext()) {
					String meshIdStr = it.next();
					Geometry geomInter = MeshUtils.linkInterMeshPolygon(link.getGeometry(),
							MeshUtils.mesh2Jts(meshIdStr));
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);
					createRdLinkWithMesh(geomInter, maps,sourceLink,result,links);

				}
			}
			return links;
		}
	private static void createRdLinkWithMesh(Geometry g,
			Map<Coordinate, Integer> maps, RdLink sourceLink,Result result,List<RdLink> links) throws Exception {
		if (g != null) {
			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				calRdLinkWithMesh(g, maps,sourceLink,result,links);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					calRdLinkWithMesh(g.getGeometryN(i), maps,sourceLink,result,links);
				}

			}
		}
	}
	/*
	 * 创建行政区划线 针对跨图幅创建图廓点不能重复
	 */
	private static void calRdLinkWithMesh(Geometry g,Map<Coordinate, Integer> maps,RdLink sourceLink,
			Result result,List<RdLink> links) throws Exception {
		//定义创建行政区划线的起始Pid 默认为0
		int sNodePid = 0;
		int eNodePid = 0;
		//判断新创建的线起点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[0])) {
			sNodePid = maps.get(g.getCoordinates()[0]);
		}
		//判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
		if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
			eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
		}
		//创建线对应的点
		JSONObject node = createRdNodeForLink(
				g, sNodePid, eNodePid, result);
		if (!maps.containsValue(node.get("s"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("s"));
		}
		if (!maps.containsValue(node.get("e"))) {
			maps.put(g.getCoordinates()[0], (int) node.get("e"));
		}
		RdNode sNode  =new RdNode();
		sNode.setPid((int) node.get("s"));
		RdNode eNode = new RdNode();
		eNode.setPid((int) node.get("e"));
		RdLink link = new RdLink();
		link.setGeometry(g);
		//创建线
		if(sourceLink != null && sourceLink.getPid() != 0){
			links.add((RdLink)addLinkByNoResult(sNode, eNode, link, sourceLink));
		}
	}

	
	/*
	 * 创建一条RDLINK对应的端点
	 */
	public static JSONObject createRdNodeForLink(Geometry g, int sNodePid, int eNodePid,Result result)
			throws Exception {
		JSONObject node = new JSONObject();
		if (0 == sNodePid) {
			Coordinate point = g.getCoordinates()[0];
			RdNode rdNode =NodeOperateUtils.createNode(point.x, point.y);
			result.insertObject(rdNode, ObjStatus.INSERT, rdNode.pid());
			node.put("s", rdNode.getPid());
		}else{
			node.put("s", sNodePid);
		}
		//创建终止点信息
		if (0 == eNodePid) {
			Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
			RdNode rdNode =NodeOperateUtils.createNode(point.x, point.y);
			result.insertObject(rdNode, ObjStatus.INSERT, rdNode.pid());
			node.put("e", rdNode.getPid());
		}else{
			node.put("e", eNodePid);
		}
		return node;
		
	}
	
	/*
	 * 创建生成一条RDLINK,未赋值图幅号
	 * */
	public static RdLink addLink(Geometry geo,int sNodePid, int eNodePid,Result result) throws Exception{
		RdLink link = new RdLink();
		
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		
		if(meshes.size()>1)
		{
			throw new Exception("创建RDLINK失败：对应多个图幅");
		}
		else
		{
			link.setMesh(Integer.parseInt(meshes.iterator().next()));
		}
		
		link.setPid(PidService.getInstance().applyLinkPid());

		result.setPrimaryPid(link.getPid());

		double linkLength = GeometryUtils.getLinkLength(geo);

		if(linkLength<=2){
			throw new Exception("道路link长度应大于2米");
		}

		link.setLength(linkLength);

		link.setGeometry(GeoTranslator.transform(geo, 100000, 0));

		link.setOriginLinkPid(link.getPid());

		link.setWidth(55);

		link.setsNodePid(sNodePid);
		
		link.seteNodePid(eNodePid);

		setLinkChildren(link);
		
		return link;
	}
	
	/**
	 * 维护link的子表
	 * 
	 * @param link
	 */
	private static void setLinkChildren(RdLink link) {

		RdLinkForm form = new RdLinkForm();

		form.setLinkPid(link.getPid());

		form.setMesh(link.mesh());

		List<IRow> forms = new ArrayList<IRow>();

		forms.add(form);

		link.setForms(forms);

		RdLinkSpeedlimit speedlimit = new RdLinkSpeedlimit();

		speedlimit.setMesh(link.mesh());

		speedlimit.setLinkPid(link.getPid());

		List<IRow> speedlimits = new ArrayList<IRow>();

		speedlimits.add(speedlimit);

		link.setSpeedlimits(speedlimits);

	}
}
