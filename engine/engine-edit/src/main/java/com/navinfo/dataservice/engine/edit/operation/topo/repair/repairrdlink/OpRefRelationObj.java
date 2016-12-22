package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class OpRefRelationObj {

	private Connection conn;

	// 被移动的端点nodepid
	List<Integer> departNodePids = new ArrayList<Integer>();

	//RdLink updateLink = null;
	
	public OpRefRelationObj(Connection conn) {

		this.conn = conn;
	}

	private void getDepartNodePids(Command command) {
		
		departNodePids = new ArrayList<Integer>();
		
		if( command.getCatchInfos()==null)
		{
			return;
		}

		for (int i = 0; i < command.getCatchInfos().size(); i++) {

			JSONObject obj = command.getCatchInfos().getJSONObject(i);
			// 分离移动的node
			int nodePid = obj.getInt("nodePid");

			if (!departNodePids.contains(nodePid)) {

				departNodePids.add(nodePid);
			}
		}
	}

	public String handleRelationObj(Command command, List<RdLink> newLinks,
			Result result) throws Exception {

		getDepartNodePids(command);
		
		// 路口
		handleRdCross(result, newLinks, command.getUpdateLink());

		// 处理交限
		handleRdRestriction(result, newLinks, command.getUpdateLink());

		// 处理车信
		handleRdLaneconnexity(result, newLinks, command.getUpdateLink());

		// 处理语音引导
		handleRdVoiceguide(result, newLinks, command.getUpdateLink());

		// 处理顺行
		handleRdDirectroute(result, newLinks, command.getUpdateLink());

		// 处理分歧
		handleRdBranch(result, newLinks, command.getUpdateLink());

		// 处理分岔口提示
		handleRdSe(result, newLinks, command.getUpdateLink());

		// 处理大门
		handleRdGate(result, newLinks, command.getUpdateLink());

		// 处理收费站
		handleRdTollgate(result, newLinks, command.getUpdateLink());

		// 处理立交
		handleRdGsc(command, result, newLinks, command.getUpdateLink());

		// 处理CRF道路
		handleRdRoad(result, newLinks, command.getUpdateLink());

		// 处理CRF对象
		handleRdObject(result, newLinks, command.getUpdateLink());

		// 处理同一关系
		handleRdSame(command, result, newLinks, command.getUpdateLink());

		return null;
	}
	
	/**
	 * 处理路口
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdCross(Result result, List<RdLink> newLinks,
			RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Operation(
				this.conn);

		// 仅移link动形状点且新link个数大于1，调用打断维护
		if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 处理交限
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdRestriction(Result result, List<RdLink> newLinks,RdLink updateLink)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation(
				this.conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {
			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 处理车信
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdLaneconnexity(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation(
				this.conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 语音引导
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdVoiceguide(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
				conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 顺行
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdDirectroute(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 分歧
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdBranch(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation(
				conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 分岔路提示
	 * 
	 * @param command
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdSe(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
				this.conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdSe(result, updateLink.getPid(), newLinks);
		}

		return null;
	}

	/**
	 * 处理大门
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdGate(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation(
				this.conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdLink(updateLink.getPid(), newLinks, result);
		}

		return null;
	}

	/**
	 * 处理收费站
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdTollgate(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
				this.conn);

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			operation.departNode(updateLink, departNodePids, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			operation.breakRdTollgate(result, updateLink.getPid(), newLinks);
		}

		return null;
	}

	/**
	 * 立交
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdGsc(Command command, Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation(
				"RD_LINK", this.conn);

		if (newLinks.size() > 1) {

			operation.breakRdLink(result, updateLink, newLinks);
		} else {

			Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

			for (RdLink link : newLinks) {
				newLinkMap.put(link.getPid(), link.getGeometry());
			}

			operation.repairLink(command.getGscList(), newLinkMap, updateLink,
					result);
		}

		return null;
	}

	/**
	 * CRF道路
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdRoad(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			com.navinfo.dataservice.engine.edit.operation.obj.rdroad.depart.Opeartion operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.depart.Opeartion(
					this.conn);

			operation.depart(0, updateLink, newLinks, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation(
					this.conn);
			rdRoadOperation.breakRdLink(updateLink.getPid(), newLinks, result);
		}

		return null;
	}

	/**
	 * CRF对象
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdObject(Result result, List<RdLink> newLinks,RdLink updateLink) throws Exception {

		// 移动link的端点 调用分离节点维护
		if (departNodePids.size() > 0) {

			// 分离节点后，如果link作为CRFO的组成link，需要删除RDOBJECTLINK关系
			com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
					this.conn);

			List<Integer> linkPidList = new ArrayList<>();

			linkPidList.add(updateLink.getPid());

			rdinterOperation.deleteByType(linkPidList, ObjType.RDLINK, result);
		}
		// 仅移link动形状点且新link个数大于1，调用打断维护
		else if (newLinks.size() > 1) {
			com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
					this.conn);
			rdObjectOperation.breakRdObjectLink(updateLink, newLinks, result);
		}

		return null;
	}

	/**
	 * 同一关系
	 * 
	 * @param command
	 * @param newLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleRdSame(Command command, Result result, List<RdLink> newLinks,RdLink updateLink)
			throws Exception {

		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(this.conn);

		Map<Integer, RdSameNode> sameNodeMap = new HashMap<Integer, RdSameNode>();

		for (int nodePid : departNodePids) {

			List<RdSameNode> sameNodes = sameNodeSelector
					.loadSameNodeByNodePids(String.valueOf(nodePid), "RD_NODE",
							true);

			if (sameNodes.size() == 1) {

				sameNodeMap.put(nodePid, sameNodes.get(0));

			} else if (sameNodes.size() == 2) {
				throw new Exception("Node " + String.valueOf(nodePid)
						+ "是多个同一点的组成node");
			}
		}

		if (sameNodeMap.size() > 1) {
			throw new Exception("link两端点均是同一点关系的组成node，不能同时对两个端点node进行修形操作");
		}

		// 端点坐标不变
		if (departNodePids.size() == 0) {

			if (newLinks.size() == 1) {

				com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation samelinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
						this.conn);

				samelinkOperation.repairLink(newLinks.get(0),
						command.getRequester(), result);
			}

			if (newLinks.size() > 1) {
				
				updateLink.setGeometry(GeoTranslator.transform(command.getLinkGeom(),
						GeoTranslator.geoUpgrade, 0));
				
				breakSameLink(updateLink, newLinks, result);
			}

			return null;
		}

		// 获取当前link所在的同一线
		String linkTableName = updateLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart originalPart = sameLinkSelector.loadLinkPartByLink(
				updateLink.getPid(), linkTableName, true);

		RdSameLink originalSameLink = null;

		if (originalPart != null) {

			originalSameLink = (RdSameLink) sameLinkSelector.loadById(
					originalPart.getGroupId(), true);
		}

		Map<Integer, Geometry> nodeGeoMap = new HashMap<Integer, Geometry>();

		for (int i = 0; i < command.getCatchInfos().size(); i++) {
			
			JSONObject obj = command.getCatchInfos().getJSONObject(i);
			// 分离移动的node
			int nodePid = obj.getInt("nodePid");
	
			for (RdLink link : newLinks) {

				LineString linkGeo = (LineString) link.getGeometry();

				if (link.getsNodePid() == nodePid) {
					nodeGeoMap.put(nodePid, linkGeo.getStartPoint());
				} else if (link.geteNodePid() == nodePid) {
					nodeGeoMap.put(nodePid, linkGeo.getEndPoint());
				}
			}
		}

		for (int nodePid : sameNodeMap.keySet()) {

			com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
					null, this.conn);

			JSONObject json = new JSONObject();

			json.accumulate("objId", nodePid);

			json.accumulate("dbId", 0);

			json.accumulate("type", "RDNODE");

			JSONObject data = new JSONObject();
			
			Geometry nodeGeo=GeoTranslator.transform(nodeGeoMap.get(nodePid), GeoTranslator.dPrecisionMap, 5) ;//GeoTranslator

			data.accumulate("longitude", nodeGeo.getCoordinate().x);
			
			data.accumulate("latitude", nodeGeo.getCoordinate().y);

			json.accumulate("data", data);

			sameNodeOperation.moveMainNodeForTopo(json, ObjType.RDNODE, result);
		}
		
		// link修形跨图幅且移动端点 、移动端点且移动形状点 删除同一线关系
		
		Geometry oldGeo = updateLink.getGeometry();

		Geometry newGeo = command.getLinkGeom();
		
		if (newLinks.size() > 1 || isMoveShapePoint(oldGeo, newGeo)) {

			if (originalSameLink != null) {
				result.insertObject(originalSameLink, ObjStatus.DELETE,
						originalSameLink.getPid());
			}
		}

		return null;
	}

	/**
	 * 打断同一线
	 * 
	 * @param breakLink
	 * @param command
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private String breakSameLink(RdLink breakLink, List<RdLink> newLinks,
			Result result) throws Exception {

		Map<IRow, Geometry> breakNodeMap = new HashMap<IRow, Geometry>();

		LinkedHashMap<IRow, Geometry> linkMap = new LinkedHashMap<IRow, Geometry>();

		Set<Integer> pidFlags = new HashSet<Integer>();

		pidFlags.add(breakLink.geteNodePid());

		pidFlags.add(breakLink.getsNodePid());

		for (RdLink link : newLinks) {

			linkMap.put(link, GeoTranslator.transform(link.getGeometry(), 1, 0));

			int sNodePid = link.getsNodePid();

			int eNodePid = link.geteNodePid();

			if (!pidFlags.contains(sNodePid)) {

				RdNode node = new RdNode();

				node.setPid(sNodePid);

				LineString linkGeo = (LineString) link.getGeometry();

				node.setGeometry(linkGeo.getStartPoint());

				breakNodeMap.put(node, node.getGeometry());

				pidFlags.add(sNodePid);
			}

			if (!pidFlags.contains(eNodePid)) {

				RdNode node = new RdNode();

				node.setPid(eNodePid);

				LineString linkGeo = (LineString) link.getGeometry();

				node.setGeometry(linkGeo.getEndPoint());

				breakNodeMap.put(node, node.getGeometry());

				pidFlags.add(eNodePid);
			}
		}

		// 打断link维护同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
				this.conn);
		operation.breakLinkForRepair(breakLink, breakNodeMap, linkMap,
				breakLink.getGeometry(), result);

		return null;
	}

	private boolean isMoveShapePoint(Geometry oldGeo, Geometry newGeo) {

		Coordinate[] oldCoordinate = oldGeo.getCoordinates();

		Coordinate[] newCoordinate = newGeo.getCoordinates();

		if (oldCoordinate.length != newCoordinate.length) {

			return true;
		}

		for (int i = 1; i < newCoordinate.length - 1; i++) {

			if (!oldCoordinate[i].equals(newCoordinate[i])) {
				return true;
			}
		}

		return false;
	}
}
