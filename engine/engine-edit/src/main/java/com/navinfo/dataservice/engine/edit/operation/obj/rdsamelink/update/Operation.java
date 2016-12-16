package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Connection conn;

	public Operation(Connection conn) {

		this.conn = conn;
	}
	
	private Geometry repairLinkGeo = null;

	@Override
	public String run(Result result) throws Exception {

		return null;
	}

	/**
	 * 获取打断信息
	 * 
	 * @param oldLink
	 * @return info[] 1:oldLinkPid,2 打断Link对应同一线等级
	 * @throws Exception
	 */
	private int[] getOperationInfo(IRow oldLink) throws Exception {

		ObjType linkType = oldLink.objType();

		int oldLinkPid = 0;
		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞
		// 5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
		int currLevel = 0;

		if (linkType == ObjType.RDLINK) {

			oldLinkPid = ((RdLink) oldLink).getPid();
			currLevel = 1;

		} else if (linkType == ObjType.ADLINK) {

			oldLinkPid = ((AdLink) oldLink).getPid();

			currLevel = 2;

		} else if (linkType == ObjType.ZONELINK) {

			oldLinkPid = ((ZoneLink) oldLink).getPid();

			currLevel = 3;

		} else if (linkType == ObjType.LULINK) {

			LuLink luLink = (LuLink) oldLink;

			currLevel = 5;

			for (IRow row : luLink.getLinkKinds()) {

				LuLinkKind linkKind = (LuLinkKind) row;

				if (linkKind.getKind() == 21) {

					currLevel = 4;
				}
			}

			oldLinkPid = ((LuLink) oldLink).getPid();
		}

		int[] info = new int[2];

		info[0] = oldLinkPid;

		info[1] = currLevel;

		return info;
	}
	
	
	/**
	 * 平滑修行打断link维护rdsamelink。
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param newNodes
	 *            新生成的node组
	 * @param result
	 * @throws Exception
	 */
	public String breakLinkForRepair(IRow breakLink,
			Map<IRow, Geometry> breakNodeMap,
			LinkedHashMap<IRow, Geometry> linkMap, Geometry repairLinkGeo,
			Result result) throws Exception {

		this.repairLinkGeo = repairLinkGeo;

		return breakLink(breakLink, breakNodeMap, linkMap, result);
	}

	/**
	 * 打断link维护rdsamelink。
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param newNodes
	 *            新生成的node组
	 * @param result
	 * @throws Exception
	 */
	public String breakLink(IRow breakLink, Map<IRow, Geometry> breakNodeMap,
			LinkedHashMap<IRow, Geometry> linkMap, Result result)
			throws Exception {

		int[] info = getOperationInfo(breakLink);

		int breakLinkPid = info[0];

		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
		int currLevel = info[1];

		String linkTableName = breakLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart originalPart = sameLinkSelector.loadLinkPartByLink(
				breakLinkPid, linkTableName, true);

		// 被打断link不存在同一关系，不处理
		if (originalPart == null) {

			return null;
		}

		if (currLevel == 5) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		RdSameLink originalSameLink = (RdSameLink) sameLinkSelector.loadById(
				originalPart.getGroupId(), true);

		// 同一线中其他的组成link
		List<RdSameLinkPart> linkParts = new ArrayList<RdSameLinkPart>();

		int highLevel = getLinkPartsInfo(currLevel, originalSameLink, linkParts);

		if (currLevel > highLevel) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		Map<String, List<IRow>> sameNodeMap = new HashMap<String, List<IRow>>();

		for (Map.Entry<IRow, Geometry> entry : breakNodeMap.entrySet()) {

			String strKey = entry.getValue().getCoordinate().toString();

			List<IRow> nodeRows = new ArrayList<IRow>();

			nodeRows.add(entry.getKey());

			sameNodeMap.put(strKey, nodeRows);
		}

		Map<Geometry, List<IRow>> sameLinkMap = new HashMap<Geometry, List<IRow>>();

		for (Map.Entry<IRow, Geometry> entry : linkMap.entrySet()) {

			List<IRow> linkRows = new ArrayList<IRow>();

			linkRows.add(entry.getKey());

			sameLinkMap.put(entry.getValue(), linkRows);
		}

		for (RdSameLinkPart part : linkParts) {

			JSONObject breakJson = new JSONObject();
			if (linkTableName.equals(part.getTableName())
					&& breakLinkPid == part.getLinkPid()) {
				continue;
			}

			ObjType type = ReflectionAttrUtils.getObjTypeByTableName(part
					.getTableName());

			breakJson.element("objId", part.getLinkPid());

			if (type == ObjType.ADLINK) {

				breakAdLink(breakJson, breakNodeMap, sameNodeMap, sameLinkMap,
						result);
			}
			if (type == ObjType.LULINK) {
				breakLuLink(breakJson, breakNodeMap, sameNodeMap, sameLinkMap,
						result);
			}
			if (type == ObjType.ZONELINK) {
				breakZoneLink(breakJson, breakNodeMap, sameNodeMap,
						sameLinkMap, result);
			}
		}

		handleSameObj(sameNodeMap, sameLinkMap, originalSameLink, result);

		return null;
	}

	private void handleSameObj(Map<String, List<IRow>> sameNodeMap,
			Map<Geometry, List<IRow>> sameLinkMap, RdSameLink originalSameLink,
			Result result) throws Exception {

		// 新建同一点
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation samenodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
				null, null);

		for (List<IRow> newNodes : sameNodeMap.values()) {
			samenodeOperation.breakSameLink(result, newNodes);
		}

		// 新建同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Operation sameLinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Operation(
				null, null);

		for (List<IRow> newlinks : sameLinkMap.values()) {
			sameLinkOperation.breakSameLink(result, newlinks);
		}

		// 删除原始同一线
		result.insertObject(originalSameLink, ObjStatus.DELETE,
				originalSameLink.getPid());
	}

	/**
	 * 调用adlink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void breakAdLink(JSONObject breakJson,
			Map<IRow, Geometry> breakNodeMap,
			Map<String, List<IRow>> sameNodeMap,
			Map<Geometry, List<IRow>> sameLinkMap, Result result)
			throws Exception {

		TreeMap<Integer, Coordinate> nodeMap = new TreeMap<Integer, Coordinate>();

		for (Geometry nodeGeo : breakNodeMap.values()) {

			Coordinate coordinate = GeoTranslator
					.transform(nodeGeo, 0.00001, 5).getCoordinate();

			AdNode node = NodeOperateUtils.createAdNode(coordinate.x,
					coordinate.y);

			String strKey = nodeGeo.getCoordinate().toString();

			sameNodeMap.get(strKey).add(node);

			nodeMap.put(node.getPid(), coordinate);

			result.insertObject(node, ObjStatus.INSERT, node.getPid());
		}

		breakJson.element("type", "ADLINK");

		breakJson = getBreakLinkJson(breakJson, nodeMap);

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command command = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
				breakJson, null);
		
		if (repairLinkGeo != null) {
			command.setRepairLinkGeo(repairLinkGeo);
		}

		command.setOperationType("sameLinkBreak");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
				command, result, conn);

		process.innerRun();

		for (AdLink link : command.getNewLinks()) {

			//防止double精度丢失
			Geometry linkGeo =GeoTranslator.transform(link.getGeometry(), GeoTranslator.geoUpgrade, 0) ;

			if (sameLinkMap.containsKey(linkGeo)) {

				sameLinkMap.get(linkGeo).add(link);

			} else if (sameLinkMap.containsKey(linkGeo.reverse())) {

				sameLinkMap.get(linkGeo.reverse()).add(link);

			} else {
				throw new Exception("打断后生成的link几何不相等，不能组成同一线");
			}
		}
	}

	/**
	 * 调用adlink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void breakLuLink(JSONObject breakJson,
			Map<IRow, Geometry> breakNodeMap,
			Map<String, List<IRow>> sameNodeMap,
			Map<Geometry, List<IRow>> sameLinkMap, Result result)
			throws Exception {

		TreeMap<Integer, Coordinate> nodeMap = new TreeMap<Integer, Coordinate>();

		for (Geometry nodeGeo : breakNodeMap.values()) {

			Coordinate coordinate = GeoTranslator
					.transform(nodeGeo, 0.00001, 5).getCoordinate();

			LuNode node = NodeOperateUtils.createLuNode(coordinate.x,
					coordinate.y);

			String strKey = nodeGeo.getCoordinate().toString();

			sameNodeMap.get(strKey).add(node);

			nodeMap.put(node.getPid(), coordinate);

			result.insertObject(node, ObjStatus.INSERT, node.getPid());
		}

		breakJson.element("type", "LULINK");

		breakJson = getBreakLinkJson(breakJson, nodeMap);

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command command = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(
				breakJson, null);
		
		if (repairLinkGeo != null) {
			command.setRepairLinkGeo(repairLinkGeo);
		}

		command.setOperationType("sameLinkBreak");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(
				command, result, conn);

		process.innerRun();

		for (LuLink link : command.getNewLinks()) {

			//防止double精度丢失
			Geometry linkGeo =GeoTranslator.transform(link.getGeometry(), GeoTranslator.geoUpgrade, 0) ;

			if (sameLinkMap.containsKey(linkGeo)) {

				sameLinkMap.get(linkGeo).add(link);

			} else if (sameLinkMap.containsKey(linkGeo.reverse())) {

				sameLinkMap.get(linkGeo.reverse()).add(link);

			} else {
				throw new Exception("打断后生成的link几何不相等，不能组成同一线");
			}
		}

	}

	/**
	 * 调用adlink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void breakZoneLink(JSONObject breakJson,
			Map<IRow, Geometry> breakNodeMap,
			Map<String, List<IRow>> sameNodeMap,
			Map<Geometry, List<IRow>> sameLinkMap, Result result)
			throws Exception {

		TreeMap<Integer, Coordinate> nodeMap = new TreeMap<Integer, Coordinate>();

		for (Geometry nodeGeo : breakNodeMap.values()) {

			Coordinate coordinate = GeoTranslator
					.transform(nodeGeo, 0.00001, 5).getCoordinate();

			ZoneNode node = NodeOperateUtils.createZoneNode(coordinate.x,
					coordinate.y);

			String strKey = nodeGeo.getCoordinate().toString();

			sameNodeMap.get(strKey).add(node);

			nodeMap.put(node.getPid(), coordinate);

			result.insertObject(node, ObjStatus.INSERT, node.getPid());
		}

		breakJson.element("type", "ZONELINK");

		breakJson = getBreakLinkJson(breakJson, nodeMap);

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command command = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(
				breakJson, null);
		
		if (repairLinkGeo != null) {
			command.setRepairLinkGeo(repairLinkGeo);
		}

		command.setOperationType("sameLinkBreak");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(
				command, result, conn);

		process.innerRun();

		for (ZoneLink link : command.getNewLinks()) {

			//防止double精度丢失
			Geometry linkGeo =GeoTranslator.transform(link.getGeometry(), GeoTranslator.geoUpgrade, 0) ;

			if (sameLinkMap.containsKey(linkGeo)) {

				sameLinkMap.get(linkGeo).add(link);

			} else if (sameLinkMap.containsKey(linkGeo.reverse())) {

				sameLinkMap.get(linkGeo.reverse()).add(link);

			} else {
				throw new Exception("打断后生成的link几何不相等，不能组成同一线");
			}
		}
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
	private JSONObject getBreakLinkJson(JSONObject breakJson,
			TreeMap<Integer, Coordinate> nodeMap) throws Exception {

		breakJson.put("command", "BREAK");

		breakJson.put("dbId", 0);

		JSONObject data = new JSONObject();

		if (nodeMap.size() == 1) {

			data.put("breakNodePid", nodeMap.firstEntry().getKey());

			data.put("longitude", nodeMap.firstEntry().getValue().x);

			data.put("latitude", nodeMap.firstEntry().getValue().y);

		} else if (nodeMap.size() > 1) {

			JSONArray array = new JSONArray();

			for (Map.Entry<Integer, Coordinate> entry : nodeMap.entrySet()) {

				JSONObject breakObj = new JSONObject();

				breakObj.put("breakNodePid", entry.getKey());

				breakObj.put("longitude", entry.getValue().x);

				breakObj.put("latitude", entry.getValue().y);

				array.add(breakObj);
			}

			data.put("breakNodes", array);
		}

		breakJson.put("data", data);

		return breakJson;
	}

	/**
	 * 获取同一线的part信息
	 * 
	 * @param currLevel
	 *            被打断link的对应等级
	 * @param originalSameLink
	 *            被打断同一线
	 * @param linkParts
	 *            同一线的part组
	 * @return 同一线组成link的对应的最高等级
	 */
	private int getLinkPartsInfo(int currLevel, RdSameLink originalSameLink,
			List<RdSameLinkPart> linkParts) {

		int highLevel = currLevel;

		for (IRow row : originalSameLink.getParts()) {

			RdSameLinkPart part = (RdSameLinkPart) row;

			if (part.getTableName().equals("RD_LINK") && highLevel > 1) {

				highLevel = 1;

			} else if (part.getTableName().equals("AD_LINK") && highLevel > 2) {

				highLevel = 2;

			} else if (part.getTableName().equals("ZONE_LINK") && highLevel > 3) {

				highLevel = 3;
			}

			linkParts.add(part);
		}

		return highLevel;
	}

	public String repairLink(IObj repairLink, String requester, Result result)
			throws Exception {

		int[] info = getOperationInfo(repairLink);

		int repairLinkPid = info[0];

		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
		int currLevel = info[1];

		String linkTableName = repairLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart originalPart = sameLinkSelector.loadLinkPartByLink(
				repairLinkPid, linkTableName, true);

		// 被打断link不存在同一关系，不处理
		if (originalPart == null) {

			return null;
		}

		if (currLevel == 5) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		RdSameLink originalSameLink = (RdSameLink) sameLinkSelector.loadById(
				originalPart.getGroupId(), true);

		// 同一线中其他的组成link
		List<RdSameLinkPart> linkParts = new ArrayList<RdSameLinkPart>();

		int highLevel = getLinkPartsInfo(currLevel, originalSameLink, linkParts);

		if (currLevel > highLevel) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		// link的修形requester可用信息相同，可共用。
		JSONObject repairJson = JSONObject.fromObject(requester);

		for (RdSameLinkPart part : linkParts) {

			if (linkTableName.equals(part.getTableName())
					&& repairLinkPid == part.getLinkPid()) {
				continue;
			}

			repairJson.element("objId", part.getLinkPid());

			repairLink(part, repairJson, result);
		}

		return null;

	}

	/**
	 * 对统一关系的其他组成link调用修形功能
	 * 
	 * @param part
	 * @param repairJson
	 * @param result
	 * @throws Exception
	 */
	private void repairLink(RdSameLinkPart part, JSONObject repairJson,
			Result result) throws Exception {
		ObjType type = ReflectionAttrUtils.getObjTypeByTableName(part
				.getTableName());

		switch (type) {

		case ADLINK:
			repairJson.element("type", "ADLINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command adCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(
					repairJson, null);

			adCommand.setOperationType("sameLinkRepair");

			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process adProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(
					adCommand, result, conn);
			adProcess.innerRun();
			break;
		case ZONELINK:
			repairJson.element("type", "ZONELINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command zoneCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(
					repairJson, null);
			zoneCommand.setOperationType("sameLinkRepair");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process zoneProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(
					zoneCommand, result, conn);
			zoneProcess.innerRun();
			break;
		case LULINK:
			repairJson.element("type", "LULINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command luCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(
					repairJson, null);
			luCommand.setOperationType("sameLinkRepair");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process luProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(
					luCommand, result, conn);
			luProcess.innerRun();
			break;
		default:
			break;
		}
	}

}
