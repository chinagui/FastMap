package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkNameSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.geo.computation.CompPolylineUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class Operation implements IOperation {

	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	/*
	 * 执行
	 */
	@Override
	public String run(Result result) throws Exception {

		createSideRoad(result);

		return "";
	}

	/**
	 * 创建辅路
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void createSideRoad(Result result) throws Exception {

		// 生成的线按照顺序存放在List<LineString> 第1右线 ，第2是左线
		LineString[] lines = getSideLineGeo();

		// 获取与主路挂接的link
		List<RdLink> noTargetLinks = getNoTargetLinks();

		// Integer：已有nodepid；Geometry：已有node几何
		Map<Integer, Geometry> nodeGeoMap = new HashMap<Integer, Geometry>();

		// String 已有node坐标表标识, List<Integer>坐标标识对应nodepid
		Map<String, List<Integer>> geoNodesMap = new HashMap<String, List<Integer>>();

		// 获取已有node映射信息
		getNodeGeo(command.getLinks(), noTargetLinks, nodeGeoMap, geoNodesMap);

		for (int i = 0; i < 2; i++) {

			if (i == 0 && command.getSideType() == 3) {
				continue;
			}
			if (i == 1 && command.getSideType() == 2) {
				continue;
			}

			LineString line = lines[i];

			// Geometry交点几何, RdLink打断link
			Map<Geometry, RdLink> intersectionMap = getIntersectionPoint(
					noTargetLinks, line);

			List<Coordinate> allCoordinates = getAllCoordinate(line,
					intersectionMap.keySet());

			List<Coordinate> newLineCoordinates = getLineCoordinates(
					intersectionMap.keySet(), allCoordinates, nodeGeoMap);

			JSONObject requester = getCreateRdLinkJson(newLineCoordinates,
					nodeGeoMap, geoNodesMap, intersectionMap);

			List<RdLink> sideRoads = exeCreateSideRoad(requester, result);

			setSideRoad(sideRoads);
			
			Coordinate sPoint = newLineCoordinates.get(0);

			if (i == 1) {
				sPoint = newLineCoordinates.get(newLineCoordinates.size() - 1);
			}

			sideRoads = sortSideRoad(sideRoads, sPoint);

			//按顺序存入将被打断的挂接link和该link与辅路的交点
			LinkedHashMap<RdLink, Geometry> intersectionSortMap = sortIntersection(
					newLineCoordinates, intersectionMap);
			
			setRdName(sideRoads, intersectionSortMap);
		}
	}

	/**
	 * 按顺序存入将被打断的挂接link和该link与辅路的交点
	 * @param newLineCoordinates
	 * @param intersectionMap key：挂接link value：该link与辅路的交点
	 * @return
	 */
	private LinkedHashMap<RdLink, Geometry> sortIntersection(
			List<Coordinate> newLineCoordinates,
			Map<Geometry, RdLink> intersectionMap) {
		
		LinkedHashMap<RdLink, Geometry> linkMapTmp = new LinkedHashMap<RdLink, Geometry>();

		for (Coordinate coordinate : newLineCoordinates) {

			if (linkMapTmp.size() == intersectionMap.size()) {

				break;
			}
			for (Geometry geo : intersectionMap.keySet()) {

				RdLink link = intersectionMap.get(geo);

				if (linkMapTmp.containsKey(geo)) {
					continue;
				}

				if (GeoTranslator
						.isPointEquals(coordinate, geo.getCoordinate())) {

					linkMapTmp.put(link, geo);
				}
			}
		}

		return linkMapTmp;
	}

	/**
	 * 设置辅路名称
	 * @param sideRoads
	 * @param linkMapTmp
	 * @throws Exception
	 */
	private void setRdName(List<RdLink> sideRoads,
			LinkedHashMap<RdLink, Geometry> linkMapTmp) throws Exception {

		List<Integer> usedNodePids = new ArrayList<Integer>();

		usedNodePids.add(getStartAndEndNode(command.getLinks(), 0).pid());

		usedNodePids.add(getStartAndEndNode(command.getLinks(), 1).pid());

		List<RdLink> sideRoadsTmp = new ArrayList<RdLink>();

		sideRoadsTmp.addAll(sideRoads);

		ArrayList<RdLink> subMainLinks = new ArrayList<RdLink>();

		for (RdLink link : command.getLinks()) {

			subMainLinks.add(link);

			int nodePidFlag = 0;

			if (!usedNodePids.contains(link.getsNodePid())) {

				nodePidFlag = link.getsNodePid();

			} else if (!usedNodePids.contains(link.geteNodePid())) {

				nodePidFlag = link.geteNodePid();
			}

			if (nodePidFlag == 0) {
				continue;
			}

			usedNodePids.add(nodePidFlag);

			Point geoFlag = null;

			for (Map.Entry<RdLink, Geometry> entry : linkMapTmp.entrySet()) {

				if (entry.getKey().getsNodePid() == nodePidFlag
						|| entry.getKey().geteNodePid() == nodePidFlag) {

					geoFlag = (Point) entry.getValue();

					linkMapTmp.remove(entry.getKey());

					break;
				}
			}

			if (geoFlag == null) {
				continue;
			}

			ArrayList<RdLink> subSideLinks = new ArrayList<RdLink>();

			for (RdLink sideLink : sideRoadsTmp) {

				subSideLinks.add(sideLink);

				LineString linkGeo = (LineString) sideLink.getGeometry();

				Point sGeo = linkGeo.getStartPoint();

				Point eGeo = linkGeo.getEndPoint();

				if (GeoTranslator.isPointEquals(sGeo, geoFlag)
						|| GeoTranslator.isPointEquals(eGeo, geoFlag)) {
					break;
				}
			}
			
			handleRdLinkName(subMainLinks, subSideLinks);

			sideRoadsTmp.removeAll(subSideLinks);

			subMainLinks = new ArrayList<RdLink>();
		}

		handleRdLinkName(subMainLinks, sideRoadsTmp);
	}

	/**
	 * 对辅路排序
	 * @param sideRoads
	 * @param sPoint
	 */
	private List<RdLink> sortSideRoad(List<RdLink> sideRoads, Coordinate sPoint) {

		if (sideRoads.size() < 2) {
			
			return sideRoads;
		}
	
		List<RdLink> sortLinks = new ArrayList<RdLink>();

		List<RdLink> sideRoadTmp = new ArrayList<RdLink>();

		sideRoadTmp.addAll(sideRoads);

		Coordinate flagGeo = new Coordinate();

		flagGeo = sPoint;

		for (int i = 0; i < sideRoads.size(); i++) {

			for (RdLink link : sideRoadTmp) {

				Coordinate sGeo = link.getGeometry().getCoordinates()[0];

				Coordinate eGeo = link.getGeometry().getCoordinates()[link
						.getGeometry().getCoordinates().length - 1];

				if (GeoTranslator.isPointEquals(flagGeo, sGeo)) {

					flagGeo = eGeo;
					
				} else if (GeoTranslator.isPointEquals(flagGeo, eGeo)) {

					flagGeo = sGeo;

				} else {
					continue;
				}

				sortLinks.add(link);

				sideRoadTmp.remove(link);

				break;
			}
		}

		return sortLinks;
	}

	/**
	 * 获取辅路几何， 第1右线 ，第2是左线
	 * 
	 * @return
	 * @throws Exception
	 */
	private LineString[] getSideLineGeo() throws Exception {
		List<RdLink> targetLinks = command.getLinks();

		LineString[] lineStrings = new LineString[targetLinks.size()];
		// 组装LineString

		for (int i = 0; i < targetLinks.size(); i++) {

			lineStrings[i] = (JtsGeometryFactory.createLineString(GeoTranslator
					.transform(targetLinks.get(i).getGeometry(), 0.00001, 5)
					.getCoordinates()));
		}

		if (!checkLineConnect(lineStrings)) {
			return null;
		}

		// 传入起点和终点Point
		Point sPoint = (Point) GeoTranslator.transform(
				this.getStartAndEndNode(targetLinks, 0).getGeometry(), 0.00001,
				5);

		// 生成的线按照顺序存放在List<LineString> 第1右线 ，第2是左线
		LineString[] lines = CompPolylineUtil.separateSideRoad(sPoint,
				lineStrings, command.getDistance());

		return lines;
	}

	/**
	 * 获取与主路挂接的link
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<RdLink> getNoTargetLinks() throws Exception {

		List<Integer> targetNodePids = getNodePids(command.getLinks());

		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

		// 挂接非目标link
		List<RdLink> links = linkSelector.loadByNodePids(targetNodePids, false);

		List<RdLink> noTargetLinks = new ArrayList<RdLink>();

		for (RdLink link : links) {
			if (!command.getLinkPids().contains(link.getPid())) {
				noTargetLinks.add(link);
			}
		}

		return noTargetLinks;
	}

	/**
	 * 获取已有node映射信息
	 * 
	 * @param targetLinks
	 * @param noTargetLinks
	 * @param nodeGeoMap
	 * @param geoNodesMap
	 * @throws Exception
	 */
	private void getNodeGeo(List<RdLink> targetLinks,
			List<RdLink> noTargetLinks, Map<Integer, Geometry> nodeGeoMap,
			Map<String, List<Integer>> geoNodesMap) throws Exception {

		List<RdLink> allLinks = new ArrayList<RdLink>();

		allLinks.addAll(targetLinks);

		allLinks.addAll(noTargetLinks);

		List<Integer> nodePids = getNodePids(allLinks);

		RdNodeSelector nodeSelector = new RdNodeSelector(conn);

		List<IRow> nodeRows = nodeSelector.loadByIds(nodePids, true, false);

		for (IRow row : nodeRows) {

			RdNode node = (RdNode) row;

			Geometry nodeGeo = GeoTranslator.transform(node.getGeometry(),
					0.00001, 5);

			nodeGeoMap.put(node.pid(), nodeGeo);

			String flagGeo = nodeGeo.getCoordinate().toString();

			if (!geoNodesMap.containsKey(flagGeo)) {

				List<Integer> pids = new ArrayList<Integer>();

				geoNodesMap.put(flagGeo, pids);
			}

			geoNodesMap.get(flagGeo).add(node.pid());
		}
	}

	/**
	 * 获取辅路几何与非目标挂接link的交点
	 * 
	 * @param noTargetLinks
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private Map<Geometry, RdLink> getIntersectionPoint(
			List<RdLink> noTargetLinks, LineString line) throws Exception {

		Map<Geometry, RdLink> intersectionMap = new HashMap<Geometry, RdLink>();

		Map<Integer, RdLink> intersectionLinks = new HashMap<Integer, RdLink>();

		for (RdLink link : noTargetLinks) {

			Geometry linkGeo = GeoTranslator.transform(link.getGeometry(),
					0.00001, 5);

			Geometry intersection = GeoTranslator.transform(
					linkGeo.intersection(line), 0.00001, 5);

			if (intersection.getCoordinates().length == 1) {

				intersectionLinks.put(link.getPid(), link);

				intersectionMap.put(intersection, link);
			}
		}

		filterLink(intersectionMap, intersectionLinks);

		return intersectionMap;
	}

	/**
	 * 筛选有效打断link： 如果与主路、辅路都相交的link上有立交关系则生成的辅路上不做打断
	 * 
	 * @param intersectionLink
	 */
	private void filterLink(Map<Geometry, RdLink> intersectionMap,
			Map<Integer, RdLink> intersectionLinks) {
		if (intersectionMap.size() < 2) {
			return;
		}

		// 过滤后需要被打断的挂接linkPid
		List<Integer> filterLinkPids = new ArrayList<>();

		filterLinkPids.addAll(intersectionLinks.keySet());

		int filterCount = filterLinkPids.size();

		for (int index = 0; index < filterCount; index++) {

			RdLink currLink = intersectionLinks.get(filterLinkPids.get(index));

			Geometry currLinkGeo = GeoTranslator.transform(
					currLink.getGeometry(), 0.00001, 5);

			Set<Integer> delLink = new HashSet<>();

			for (Integer linkPid : filterLinkPids) {

				if (currLink.getPid() == linkPid) {

					continue;
				}

				RdLink link = intersectionLinks.get(linkPid);

				Geometry linkGeo = GeoTranslator.transform(link.getGeometry(),
						0.00001, 5);

				Geometry intersection = currLinkGeo.intersection(linkGeo);

				if ((intersection.getCoordinates().length == 1
						&& currLink.getsNodePid() != link.getsNodePid()
						&& currLink.getsNodePid() != link.geteNodePid()
						&& currLink.geteNodePid() != link.getsNodePid() && currLink
						.geteNodePid() != link.geteNodePid())
						|| intersection.getCoordinates().length > 1) {

					delLink.add(currLink.getPid());

					delLink.add(linkPid);
				}
			}

			if (delLink.size() > 0) {

				index -= 1;

				filterCount = filterCount - delLink.size();

				filterLinkPids.removeAll(delLink);
			}
		}

		// 与辅路相交但不进行打断的link几何
		Set<Geometry> noFilterGeo = new HashSet<Geometry>();

		for (Map.Entry<Geometry, RdLink> entry : intersectionMap.entrySet()) {

			int linkPid = entry.getValue().getPid();

			if (!filterLinkPids.contains(linkPid)) {
				noFilterGeo.add(entry.getKey());
			}
		}

		for (Geometry geo : noFilterGeo) {

			if (intersectionMap.containsKey(geo)) {
				intersectionMap.remove(geo);
			}
		}
	}

	/**
	 * 向LineString加入形状点
	 * 
	 * @param line
	 * @param intersectionGeo
	 * @return
	 * @throws Exception
	 */
	private List<Coordinate> getAllCoordinate(LineString line,
			Set<Geometry> intersectionGeo) throws Exception {

		List<Coordinate> sumCoordinates = new ArrayList<Coordinate>();

		for (Coordinate coordinate : line.getCoordinates()) {

			sumCoordinates.add(coordinate);
		}

		for (Geometry pointGeo : intersectionGeo) {

			Coordinate pointCoordinate = pointGeo.getCoordinate();

			int index = -1;

			for (int i = 0; i < sumCoordinates.size() - 1; i++) {

				Coordinate preCoordinate = sumCoordinates.get(i);

				Coordinate nextCoordinate = sumCoordinates.get(i + 1);

				if (preCoordinate.equals(pointCoordinate)) {
					break;
				}

				boolean isIntersection = GeoTranslator.isIntersection(
						preCoordinate, nextCoordinate, pointCoordinate);

				if (isIntersection) {

					if (GeoTranslator.isPointEquals(preCoordinate,
							pointCoordinate)) {

						sumCoordinates.set(i, pointCoordinate);
					} else if (GeoTranslator.isPointEquals(nextCoordinate,
							pointCoordinate)) {
						sumCoordinates.set(i + 1, pointCoordinate);
					} else {
						index = i + 1;
					}
					break;
				}
			}

			if (index > -1) {
				sumCoordinates.add(index, pointCoordinate);
			}
		}
		return sumCoordinates;
	}

	/**
	 * 交点与已有node几何相同时，用已经node几何替换交点
	 * 
	 * @param intersectionGeo
	 * @param allCoordinates
	 * @param nodeGeoMap
	 * @return
	 * @throws Exception
	 */
	private List<Coordinate> getLineCoordinates(Set<Geometry> intersectionGeo,
			List<Coordinate> allCoordinates, Map<Integer, Geometry> nodeGeoMap)
			throws Exception {

		if (allCoordinates.size() < 2) {
			return allCoordinates;
		}

		Set<String> flagPoints = new HashSet<String>();

		for (Geometry pointGeo : intersectionGeo) {

			Coordinate pointCoordinate = pointGeo.getCoordinate();

			flagPoints.add(pointCoordinate.toString());
		}

		for (int i = 1; i < allCoordinates.size(); i++) {

			if (!flagPoints.contains(allCoordinates.get(i).toString())) {
				continue;
			}

			for (Map.Entry<Integer, Geometry> entrySet : nodeGeoMap.entrySet()) {

				if (GeoTranslator.isPointEquals(allCoordinates.get(i), entrySet
						.getValue().getCoordinate())) {
					allCoordinates.set(i, entrySet.getValue().getCoordinate());

					break;
				}
			}
		}

		return allCoordinates;
	}

	/**
	 * 获取创建link的josn参数
	 * 
	 * @param lineCoordinates
	 * @param nodeGeoMap
	 * @param geoNodesMap
	 * @param intersectionMap
	 * @param flagIntersections
	 * @return
	 * @throws Exception
	 */
	private JSONObject getCreateRdLinkJson(List<Coordinate> lineCoordinates,
			Map<Integer, Geometry> nodeGeoMap,
			Map<String, List<Integer>> geoNodesMap,
			Map<Geometry, RdLink> intersectionMap) throws Exception {

		// String 交点几何标识,Geometry 交点几何
		Map<String, Geometry> flagIntersections = new HashMap<String, Geometry>();

		for (Geometry geo : intersectionMap.keySet()) {

			String keyCoordinate = geo.getCoordinate().toString();

			flagIntersections.put(keyCoordinate, geo);
		}

		JSONObject parameter = new JSONObject();

		parameter.put("command", "CREATE");

		parameter.put("dbId", this.command.getDbId());

		JSONObject data = new JSONObject();

		String sFlag = lineCoordinates.get(0).toString();

		String eFlag = lineCoordinates.get(lineCoordinates.size() - 1)
				.toString();

		if (geoNodesMap.containsKey(sFlag)) {
			int sNodePid = geoNodesMap.get(sFlag).get(0);

			data.put("sNodePid", sNodePid);
		} else {
			data.put("sNodePid", 0);
		}

		if (geoNodesMap.containsKey(eFlag)) {
			int eNodePid = geoNodesMap.get(eFlag).get(0);

			data.put("eNodePid", eNodePid);
		} else {
			data.put("eNodePid", 0);
		}
		
		data.put("kind", 9);
		
		data.put("laneNum", 1);

		Coordinate[] linkCoordinates = new Coordinate[lineCoordinates.size()];

		lineCoordinates.toArray(linkCoordinates);

		LineString lineGeo = JtsGeometryFactory
				.createLineString(linkCoordinates);

		JSONObject geometry = GeoTranslator.jts2Geojson(lineGeo);

		data.put("geometry", geometry);

		JSONArray catchLinks = new JSONArray();

		for (int i = 0; i < lineCoordinates.size(); i++) {

			Coordinate lineC = lineCoordinates.get(i);

			String lineCFlag = lineC.toString();

			if (geoNodesMap.containsKey(lineCFlag)) {

				JSONObject catchNode = new JSONObject();

				int nodePid = geoNodesMap.get(lineCFlag).get(0);

				catchNode.put("nodePid", nodePid);

				catchNode.put("seqNum", i);

				catchLinks.add(catchNode);
			} else if (flagIntersections.containsKey(lineCFlag)) {

				JSONObject catchLink = new JSONObject();

				RdLink link = intersectionMap.get(flagIntersections
						.get(lineCFlag));

				int linkPid = link.getPid();

				catchLink.put("linkPid", linkPid);

				catchLink.put("lon", lineC.x);

				catchLink.put("lat", lineC.y);

				catchLinks.add(catchLink);
			}
		}

		data.put("catchLinks", catchLinks);

		parameter.put("data", data);

		parameter.put("type", "RDLINK");

		JSONObject requester = new JSONObject();

		requester.put("parameter", parameter);

		return parameter;
	}

	/**
	 * 调用创建rdink接口执行创建辅路
	 * 
	 * @param requester
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private List<RdLink> exeCreateSideRoad(JSONObject requester, Result result)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Command(
				requester, requester.toString());

		com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Operation(
				command, this.conn);
		
		List<RdLink> sideRoads = operation.createSideRoad(result);

		return sideRoads;
	}

	/**
	 * 设置辅路属性
	 * 
	 * @param sideRoads
	 * @throws Exception
	 */
	private void setSideRoad(List<RdLink> sideRoads) throws Exception {
		for (RdLink link : sideRoads) {
			// 生成的link均为顺方向
			link.setDirect(2);
			// 车道数为：1
			link.setLaneNum(1);
			// 左右车道数：默认赋“0”
			link.setLaneLeft(0);
			// 左右车道数：默认赋“0”
			link.setLaneRight(0);
			// 上下分离：“否”
			link.setMultiDigitized(0);
			// 供用信息：默认值“可以通行
			link.setAppInfo(1);
			// 开发状态：详细
			link.setDevelopState(1);
			// IMI代码：其他道路
			link.setImiCode(0);
			// 功能等级为：5
			link.setFunctionClass(5);
			// 车道等级为1：一条车道
			link.setLaneClass(1);
			// 收费信息为：免费
			link.setTollInfo(2);
			// 道路种别为9：非引导道路
			link.setKind(9);
			// 道路幅宽为30
			link.setWidth(30);

			// 限制信息：自动增加一组限制信息：限制类型：穿行限制；限制方向：默认为未调查，不允许编辑;时间段：默认为空;车辆类型：默认为空;赋值方式：未验证
			RdLinkLimit limit = new RdLinkLimit();

			limit.setLinkPid(link.getPid());

			link.getLimits().add(limit);

			// 路径采纳：维护默认值：“作业中”
			link.setRouteAdopt(0);

			// 条件限速信息：默认为空
			link.getSpeedlimits().clear();

			// 普通限速信息
			RdLinkSpeedlimit speedlimit = new RdLinkSpeedlimit();

			speedlimit.setLinkPid(link.getPid());
			// 普通限速信息
			speedlimit.setSpeedType(0);
			// 速度限制等级：7（11-30）
			speedlimit.setSpeedClass(7);
			// 等级赋值标记：程序赋值
			speedlimit.setSpeedClassWork(1);

			// 速度限制值：15公里/小时（根据道路方向赋顺逆限速值）
			// 速度限制来源：如果顺向限速值非0，逆向限速值为0，则顺向限速来源为未调查，逆向限速值来源为无；如果顺向限速值为0，逆向限速值非0，则顺向限速来源为无，逆向限速值来源为未调查；
			if (link.getDirect() == 2) {
				speedlimit.setFromSpeedLimit(150);
				speedlimit.setFromLimitSrc(9);
				speedlimit.setToSpeedLimit(0);
				speedlimit.setToLimitSrc(0);
			}
			// if (link.getDirect() == 3) {
			// speedlimit.setToLimitSrc(150);
			// speedlimit.setToLimitSrc(9);
			// speedlimit.setFromSpeedLimit(0);
			// speedlimit.setFromLimitSrc(0);
			// }
			link.getSpeedlimits().add(speedlimit);

			// 道路形态：默认有一条记录，道路形态赋值为“辅路”
			link.getForms().clear();

			RdLinkForm form = new RdLinkForm();

			form.setLinkPid(link.getPid());
			form.setFormOfWay(34);
			link.getForms().add(form);

			// 铺设状态：维护默认值“铺设”
			link.setPaveStatus(0);

			// 线门牌信息

			// 除以上link属性外，新生成的辅路link属性字段均按照新增link的默认值维护

		}
	}

	/**
	 * 处理道路名
	 * 
	 * @param link
	 * @throws Exception
	 */
	private void handleRdLinkName(List<RdLink> mainLinks, List<RdLink> sideLinks)
			throws Exception {

		if (mainLinks.size() < 1 || sideLinks.size() < 1) {
			return;
		}

		List<RdLinkName> sameNames = getSameName(mainLinks);

		if (sameNames.size() < 1) {
			return;
		}

		// 官方名列表
		List<RdLinkName> lstClass1 = new ArrayList<RdLinkName>();
		// 别名列表
		List<RdLinkName> lstClass2 = new ArrayList<RdLinkName>();
		// 不以“桥”为结尾的别名按SEQ_NUM排序
		TreeMap<Integer, RdLinkName> mapClass2 = new TreeMap<Integer, RdLinkName>();
		// 曾用名列表
		List<RdLinkName> lstClass3 = new ArrayList<RdLinkName>();

		for (RdLinkName name : sameNames) {
			if (name.getNameClass() == 1) {
				lstClass1.add(name);
			} else if (name.getNameClass() == 2) {
				lstClass2.add(name);

				if (!name.getName().endsWith("桥")) {
					mapClass2.put(name.getSeqNum(), name);
				}
			} else if (name.getNameClass() == 3) {
				lstClass3.add(name);
			}
		}

		// 没有官方名。别名中取名称中不包含字母或者数字中的第一条不以“桥”为结尾（最后一个汉字）的道路名作为辅路的官方名称
		if (lstClass1.size() < 1 && mapClass2.size() > 0) {
			RdLinkName name = mapClass2.firstEntry().getValue();

			lstClass2.remove(name);

			name.setNameClass(1);

			lstClass1.add(name);
		}

		for (RdLink link : sideLinks) {

			List<IRow> allNames = new ArrayList<IRow>();

			List<RdLinkName> nameTmp = new ArrayList<RdLinkName>();

			nameTmp.addAll(lstClass1);

			nameTmp.addAll(lstClass2);

			nameTmp.addAll(lstClass3);

			for (RdLinkName name : nameTmp) {

				RdLinkName newName = new RdLinkName();

				newName.copy(name);

				newName.setLinkPid(link.getPid());

				allNames.add(newName);
			}

			link.setNames(allNames);
		}

		return;
	}

	

	/**
	 * 获取相同的道路名
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<RdLinkName> getSameName(List<RdLink> mainLinks)
			throws Exception {

		List<RdLinkName> sameNames = new ArrayList<RdLinkName>();

		if (mainLinks.size() == 0) {

			return sameNames;
		}

		List<Integer> linkPids = new ArrayList<Integer>();

		for (RdLink link : mainLinks) {
			if (!linkPids.contains(link.getPid())) {
				linkPids.add(link.getPid());
			}
		}

		Set<Integer> pids = new HashSet<Integer>();

		pids.addAll(linkPids);

		RdLinkNameSelector linkNameSelector = new RdLinkNameSelector(this.conn);

		Map<Integer, List<RdLinkName>> nameMap = linkNameSelector
				.loadNameByLinkPids(pids);

		if (nameMap.size() == 0) {

			return sameNames;
		}

		sameNames = nameMap.get(linkPids.get(0));

		if (linkPids.size() == 1) {

			return sameNames;
		}

		for (int i = 1; i < linkPids.size(); i++) {

			int sameSize = sameNames.size();

			if (sameSize < 1) {

				break;
			}

			List<RdLinkName> currNames = nameMap.get(linkPids.get(i));

			for (int j = sameSize - 1; j >= 0; j--) {

				boolean isSame = false;

				for (RdLinkName name : currNames) {

					if (IsEqualName(name, sameNames.get(j))) {

						isSame = true;

						break;
					}
				}
				if (!isSame) {

					sameNames.remove(j);
				}
			}
		}

		int nameSize = sameNames.size();

		for (int j = nameSize - 1; j >= 0; j--) {

			String name = sameNames.get(j).getName();

			if (isNuber(name) || isEng(name)) {
				sameNames.remove(j);
			}
		}

		return sameNames;
	}	

	/**
	 * @Description:是否包含数字
	 * @param name
	 * @return
	 * @author: y
	 * @time:2016-7-2 下午3:53:35
	 */
	private boolean isNuber(String value) {
		String regex = ".*[0-9].*";
		Pattern pattern = Pattern.compile(regex);
		// 需要转换成半角在匹配
		Matcher matcher = pattern.matcher(ExcelReader.f2h(value));
		return matcher.matches();
	}

	/**
	 * @Description:是否包含英文
	 * @param name
	 * @return
	 * @author: y
	 * @time:2016-7-2 下午3:53:35
	 */
	private boolean isEng(String value) {
		String regex = ".*[a-zA-Z].*";
		Pattern pattern = Pattern.compile(regex);
		// 需要转换成半角在匹配
		Matcher matcher = pattern.matcher(ExcelReader.f2h(value));
		return matcher.matches();
	}

	/**
	 * 根据规则判断RdLinkName是否相等
	 * 
	 * @param name1
	 * @param name2
	 * @return
	 */
	private boolean IsEqualName(RdLinkName name1, RdLinkName name2) {

		if (name1.getNameGroupid() != name2.getNameGroupid()) {
			return false;
		}

		if (name1.getNameClass() != name2.getNameClass()) {
			return false;
		}

		if (name1.getInputTime() != name2.getInputTime()) {
			return false;
		}

		if (name1.getNameType() != name2.getNameType()) {
			return false;
		}

		if (name1.getSrcFlag() != name2.getSrcFlag()) {
			return false;
		}

		if (name1.getRouteAtt() != name2.getRouteAtt()) {
			return false;
		}

		if (name1.getCode() != name2.getCode()) {
			return false;
		}

		return true;
	}

	/**
	 * 检查主路link串几何是否连通
	 * 
	 * @param lineStrings
	 * @return
	 * @throws Exception
	 */
	private boolean checkLineConnect(LineString[] lineStrings) throws Exception {
		if (lineStrings.length == 0) {
			return false;
		}

		if (lineStrings.length < 2) {
			return true;
		}

		for (int i = 0; i < lineStrings.length - 1; i++) {

			Coordinate[] preCoordinate = lineStrings[i].getCoordinates();

			Coordinate[] nextCoordinate = lineStrings[i + 1].getCoordinates();

			Coordinate preS = preCoordinate[0];

			Coordinate preN = preCoordinate[preCoordinate.length - 1];

			Coordinate nextS = nextCoordinate[0];

			Coordinate nextN = nextCoordinate[nextCoordinate.length - 1];

			if (!(preS.equals(nextS) || preS.equals(nextN)
					|| preN.equals(nextS) || preN.equals(nextN))) {

				String errInfo = "所选第 " + String.valueOf(i + 1) + "条link与第 "
						+ String.valueOf(i + 2)
						+ "条link在几何坐标上不连续，请先修复数据或联系技术人员";

				throw new Exception(errInfo);
			}
		}

		return true;
	}

	// 获取联通线的起点和终点
	// 0 起点 1 终点
	// 根据联通线的第一条link和第二条link算出起点Node
	// 根据联通线最后一条link和倒数第二条link算出终点Node
	private RdNode getStartAndEndNode(List<RdLink> links, int flag)
			throws Exception {
		RdNodeSelector nodeSelector = new RdNodeSelector(conn);
		RdLink fristLink = null;
		RdLink secondLink = null;
		RdNode node = null;
		if (links.size() == 1) {
			if (flag == 0) {
				IRow row = nodeSelector.loadById(links.get(0).getsNodePid(),
						true);
				return (RdNode) row;
			} else {
				IRow row = nodeSelector.loadById(links.get(0).geteNodePid(),
						true);
				return (RdNode) row;
			}
		}
		if (flag == 0) {
			fristLink = links.get(0);
			secondLink = links.get(1);
		}
		if (flag == 1) {
			fristLink = links.get(links.size() - 1);
			secondLink = links.get(links.size() - 2);
		}
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.add(secondLink.getsNodePid());
		nodes.add(secondLink.geteNodePid());
		if (nodes.contains(fristLink.getsNodePid())) {
			IRow row = nodeSelector.loadById(fristLink.geteNodePid(), true);
			node = (RdNode) row;
		}
		if (nodes.contains(fristLink.geteNodePid())) {
			IRow row = nodeSelector.loadById(fristLink.getsNodePid(), true);
			node = (RdNode) row;

		}
		return node;
	}

	/**
	 * 获取link的端点
	 * 
	 * @param links
	 * @return
	 */
	private List<Integer> getNodePids(List<RdLink> links) {

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdLink link : links) {

			if (!nodePids.contains(link.getsNodePid())) {

				nodePids.add(link.getsNodePid());
			}
			if (!nodePids.contains(link.geteNodePid())) {

				nodePids.add(link.geteNodePid());
			}
		}

		return nodePids;
	}

}
