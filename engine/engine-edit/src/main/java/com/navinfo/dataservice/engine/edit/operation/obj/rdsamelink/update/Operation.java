package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;

public class Operation implements IOperation {

	private Connection conn;

	public Operation(Connection conn) {

		this.conn = conn;
	}

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
	private int[] getOperationInfo(IObj oldLink) throws Exception {

		ObjType linkType = oldLink.objType();

		int oldLinkPid = 0;
		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
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

	public String repairLink(IObj repairLink, String requester, Result result) throws Exception {

		int[] info = getOperationInfo(repairLink);

		int repairLinkPid = info[0];

		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
		int currLevel = info[1];

		String linkTableName = repairLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart originalPart = sameLinkSelector.loadLinkPartByLink(repairLinkPid, linkTableName, true);

		// 被打断link不存在同一关系，不处理
		if (originalPart == null) {

			return null;
		}

		if (currLevel == 5) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		RdSameLink originalSameLink = (RdSameLink) sameLinkSelector.loadById(originalPart.getGroupId(), true);

		// 同一线中其他的组成link
		List<RdSameLinkPart> linkParts = new ArrayList<RdSameLinkPart>();

		int highLevel = getLinkPartsInfo(currLevel, originalSameLink, linkParts);

		if (currLevel > highLevel) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		// link的修形requester可用信息相同，可共用。
		JSONObject repairJson = JSONObject.fromObject(requester);

		for (RdSameLinkPart part : linkParts) {

			if (linkTableName.equals(part.getTableName()) && repairLinkPid == part.getLinkPid()) {
				continue;
			}

			repairJson.element("objId", part.getLinkPid());

			repairLink(part, repairJson, result);
		}

		return null;

	}

	/**
	 * 打断link维护rdsamelink。
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public String breakLink(IObj breakLink, List<IObj> newLinks, IRow newNode, String requester, Result result)
			throws Exception {

		int[] info = getOperationInfo(breakLink);

		int breakLinkPid = info[0];

		// 级别：：1:道路>2:行政区划>
		// 3:ZONELINK>4:土地利用（BUA边界线）＞5土地利用（大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线）；
		int currLevel = info[1];

		String linkTableName = breakLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart originalPart = sameLinkSelector.loadLinkPartByLink(breakLinkPid, linkTableName, true);

		// 被打断link不存在同一关系，不处理
		if (originalPart == null) {

			return null;
		}

		if (currLevel == 5) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		RdSameLink originalSameLink = (RdSameLink) sameLinkSelector.loadById(originalPart.getGroupId(), true);

		// 同一线中其他的组成link
		List<RdSameLinkPart> linkParts = new ArrayList<RdSameLinkPart>();

		int highLevel = getLinkPartsInfo(currLevel, originalSameLink, linkParts);

		if (currLevel > highLevel) {

			throw new Exception("此link不是该组同一关系中的主要素，不能进行此操作");
		}

		// 新生成同一点node组
		List<IRow> newNodes = new ArrayList<IRow>();

		newNodes.add(newNode);

		// 新生成同一线link组1
		List<IRow> links1 = new ArrayList<IRow>();

		// 新生成同一线link组2
		List<IRow> links2 = new ArrayList<IRow>();

		Coordinate falgPoint = getFalgPoint(breakLink);

		handleCurrNewLink(falgPoint, newLinks, links1, links2);

		// link的打断requester可用信息相同，可共用。
		com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject.parseObject(requester);

		JSONObject breakJson = JsonUtils.fastJson2netJson(fastJson);

		if (breakJson.getJSONObject("data").containsKey("breakNodePid")) {
			breakJson.getJSONObject("data").element("breakNodePid", 0);
		}

		for (RdSameLinkPart part : linkParts) {

			if (linkTableName.equals(part.getTableName()) && breakLinkPid == part.getLinkPid()) {
				continue;
			}

			ObjType type = ReflectionAttrUtils.getObjTypeByTableName(part.getTableName());

			breakJson.element("objId", part.getLinkPid());

			if (type == ObjType.ADLINK) {

				breakAdLink(breakJson, newNodes, links1, links2, falgPoint, result);
			}
			if (type == ObjType.LULINK) {
				breakLuLink(breakJson, newNodes, links1, links2, falgPoint, result);
			}
			if (type == ObjType.ZONELINK) {
				breakZoneLink(breakJson, newNodes, links1, links2, falgPoint, result);
			}
		}

		// 新建同一点
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation samenodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
				null, null);
		samenodeOperation.breakSameLink(result, newNodes);

		// 新建同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Operation sameLinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Operation(
				null, null);

		sameLinkOperation.breakSameLink(result, links1);

		sameLinkOperation.breakSameLink(result, links2);

		// 删除原始同一线
		result.insertObject(originalSameLink, ObjStatus.DELETE, originalSameLink.getPid());
		return null;
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
	private int getLinkPartsInfo(int currLevel, RdSameLink originalSameLink, List<RdSameLinkPart> linkParts) {

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

	/**
	 * 获取标识点位，打断时判断新生成的link分组
	 * 
	 * @param oldLink
	 * @return
	 */
	private Coordinate getFalgPoint(IObj oldLink) {

		ObjType linkType = oldLink.objType();

		Coordinate coordinate = null;

		if (linkType == ObjType.RDLINK) {

			coordinate = ((RdLink) oldLink).getGeometry().getCoordinates()[0];

		} else if (linkType == ObjType.ADLINK) {

			coordinate = ((AdLink) oldLink).getGeometry().getCoordinates()[0];

		} else if (linkType == ObjType.ZONELINK) {

			coordinate = ((ZoneLink) oldLink).getGeometry().getCoordinates()[0];

		} else if (linkType == ObjType.LULINK) {

			coordinate = ((LuLink) oldLink).getGeometry().getCoordinates()[0];
		}

		return coordinate;
	}

	/**
	 * 打断时通过标识点位 将新生成link进行分组
	 * 
	 * @param falgPoint
	 * @param newLinks
	 * @param links1
	 * @param links2
	 */
	private void handleCurrNewLink(Coordinate falgPoint, List<IObj> newLinks, List<IRow> links1, List<IRow> links2) {

		ObjType linkType = newLinks.get(0).objType();

		Coordinate[] coors = null;

		if (linkType == ObjType.RDLINK) {

			coors = ((RdLink) newLinks.get(0)).getGeometry().getCoordinates();

		} else if (linkType == ObjType.ADLINK) {

			coors = ((AdLink) newLinks.get(0)).getGeometry().getCoordinates();

		} else if (linkType == ObjType.ZONELINK) {

			coors = ((ZoneLink) newLinks.get(0)).getGeometry().getCoordinates();

		} else if (linkType == ObjType.LULINK) {

			coors = ((LuLink) newLinks.get(0)).getGeometry().getCoordinates();
		}

		if (falgPoint.equals(coors[0]) || falgPoint.equals(coors[coors.length - 1])) {
			links1.add(newLinks.get(0));

			links2.add(newLinks.get(1));

		} else {

			links1.add(newLinks.get(1));

			links2.add(newLinks.get(0));
		}
	}

	/**
	 * 调用adlink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void breakAdLink(JSONObject breakJson, List<IRow> newNodes, List<IRow> links1, List<IRow> links2,
			Coordinate falgPoint, Result result) throws Exception {

		breakJson.element("type", "ADLINK");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command adCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
				breakJson, null);

		adCommand.setOperationType("innerRun");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process adProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
				adCommand, result, conn);

		adProcess.innerRun();

		newNodes.add(adCommand.getBreakNode());

		Coordinate[] coors = adCommand.getsAdLink().getGeometry().getCoordinates();

		if (falgPoint.equals(coors[0]) || falgPoint.equals(coors[coors.length - 1])) {
			links1.add(adCommand.getsAdLink());
			links2.add(adCommand.geteAdLink());
		} else {
			links1.add(adCommand.geteAdLink());
			links2.add(adCommand.getsAdLink());
		}

	}

	/**
	 * 调用lulink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void breakLuLink(JSONObject breakJson, List<IRow> newNodes, List<IRow> links1, List<IRow> links2,
			Coordinate falgPoint, Result result) throws Exception {

		breakJson.element("type", "LULINK");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command luCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(
				breakJson, null);

		luCommand.setOperationType("innerRun");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process luProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(
				luCommand, result, conn);

		luProcess.innerRun();

		newNodes.add(luCommand.getBreakNode());

		Coordinate[] coors = luCommand.getsLuLink().getGeometry().getCoordinates();

		if (falgPoint.equals(coors[0]) || falgPoint.equals(coors[coors.length - 1])) {
			links1.add(luCommand.getsLuLink());
			links2.add(luCommand.geteLuLink());
		} else {
			links1.add(luCommand.geteLuLink());
			links2.add(luCommand.getsLuLink());
		}
	}

	/**
	 * 调用zonelink打断接口
	 * 
	 * @param type
	 * @throws Exception
	 */
	private IRow breakZoneLink(JSONObject breakJson, List<IRow> newNodes, List<IRow> links1, List<IRow> links2,
			Coordinate falgPoint, Result result) throws Exception {

		breakJson.element("type", "ZONELINK");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command zoneCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(
				breakJson, null);

		zoneCommand.setOperationType("innerRun");

		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process zoneProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(
				zoneCommand, result, conn);

		zoneProcess.innerRun();

		newNodes.add(zoneCommand.getBreakNode());

		Coordinate[] coors = zoneCommand.getsZoneLink().getGeometry().getCoordinates();

		if (falgPoint.equals(coors[0]) || falgPoint.equals(coors[coors.length - 1])) {
			links1.add(zoneCommand.getsZoneLink());
			links2.add(zoneCommand.geteZoneLink());
		} else {
			links1.add(zoneCommand.geteZoneLink());
			links2.add(zoneCommand.getsZoneLink());
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
	private void repairLink(RdSameLinkPart part, JSONObject repairJson, Result result) throws Exception {
		ObjType type = ReflectionAttrUtils.getObjTypeByTableName(part.getTableName());

		switch (type) {

		case ADLINK:
			repairJson.element("type", "ADLINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command adCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(
					repairJson, null);

			adCommand.setOperationType("innerRun");

			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process adProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(
					adCommand, result, conn);
			adProcess.innerRun();
			break;
		case ZONELINK:
			repairJson.element("type", "ZONELINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command zoneCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(
					repairJson, null);
			zoneCommand.setOperationType("innerRun");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process zoneProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(
					zoneCommand, result, conn);
			zoneProcess.innerRun();
			break;
		case LULINK:
			repairJson.element("type", "LULINK");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command luCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(
					repairJson, null);
			luCommand.setOperationType("innerRun");
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process luProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(
					luCommand, result, conn);
			luProcess.innerRun();
			break;
		default:
			break;
		}
	}

}
