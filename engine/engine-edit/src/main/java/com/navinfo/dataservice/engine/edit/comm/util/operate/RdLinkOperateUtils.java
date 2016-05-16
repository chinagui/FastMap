package com.navinfo.dataservice.engine.edit.comm.util.operate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.comm.util.type.GeometryTypeName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

/**
 * @author zhaokk LINK 公共方法
 */
public class RdLinkOperateUtils {
	
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
			Set<String> meshes = MeshUtils.getInterMeshes(link.getGeometry());
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
				calAdLinkWithMesh(g, maps,sourceLink,result,links);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					calAdLinkWithMesh(g.getGeometryN(i), maps,sourceLink,result,links);
				}

			}
		}
	}
	/*
	 * 创建行政区划线 针对跨图幅创建图廓点不能重复
	 */
	private static void calAdLinkWithMesh(Geometry g,Map<Coordinate, Integer> maps,RdLink sourceLink,
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


}
