package com.navinfo.dataservice.engine.edit.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

public class RdCRFOperateUtils {

	private Connection conn;

	private List<Integer> nodePids = new ArrayList<Integer>();

	private List<Integer> linkPids = new ArrayList<Integer>();

	public RdCRFOperateUtils(Connection conn) {

		this.conn = conn;
	}

	/***
	 * 根据目标link的连接点获取RdInter
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<RdInter> getRdInter() throws Exception {

		RdInterSelector interSelector = new RdInterSelector(conn);

		// 根据nodePids查询组成的CRF交叉点
		List<RdInter> rdInters = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(this.nodePids)) {

			String StrNodePids = StringUtils.getInteStr(this.nodePids);

			// 根据nodePids查询组成的CRF交叉点
			rdInters = interSelector.loadInterByNodePid(StrNodePids, true);

		}
		if (rdInters.isEmpty() && CollectionUtils.isNotEmpty(this.linkPids)) {
			// 根据linkPids查询组成的CRF交叉点
			rdInters = interSelector.loadRdInterByOutLinkPid(this.linkPids, true);
		}

		return rdInters;
	}

	/***
	 * 根据目标link获取RdRoad
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<RdRoad> getRdRoad() throws Exception {

		List<RdRoad> rdRoads = new ArrayList<RdRoad>();

		RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(
				this.conn);

		List<RdRoadLink> rdRoadLinks = rdRoadLinkSelector.loadByLinks(
				this.linkPids, true);

		if (rdRoadLinks.size() < 1) {

			return rdRoads;
		}

		List<Integer> rdRoadPids = new ArrayList<Integer>();

		for (RdRoadLink roadLink : rdRoadLinks) {

			if (!rdRoadPids.contains(roadLink.getPid())) {

				rdRoadPids.add(roadLink.getPid());
			}
		}

		AbstractSelector roadSelector = new AbstractSelector(RdRoad.class,
				this.conn);

		List<IRow> rows = roadSelector.loadByIds(rdRoadPids, true, true);

		for (IRow row : rows) {

			RdRoad road = (RdRoad) row;

			rdRoads.add(road);
		}

		return rdRoads;
	}

	/***
	 * 根据rdInters、rdRoads、目标link获取RdObject
	 * 
	 * @param rdInters
	 * @param rdRoads
	 * @return
	 * @throws Exception
	 */
	private List<RdObject> getRdObject(List<RdInter> rdInters,
			List<RdRoad> rdRoads) throws Exception {

		RdObjectSelector selector = new RdObjectSelector(conn);

		// 通过RdLink获取RdObject
		String strLinkPids = StringUtils.getInteStr(this.linkPids);

		Map<String, RdObject> linkObjMap = selector.loadRdObjectByPidAndType(
				strLinkPids, ObjType.RDLINK, true);

		// 通过RdInter获取RdObject
		List<Integer> interPids = new ArrayList<Integer>();

		for (RdInter inter : rdInters) {

			interPids.add(inter.getPid());
		}

		String strInterPids = StringUtils.getInteStr(interPids);

		Map<String, RdObject> interObjMap = selector.loadRdObjectByPidAndType(
				strInterPids, ObjType.RDINTER, true);

		// 通过RdRoad获取RdObject
		List<Integer> roadPids = new ArrayList<Integer>();

		for (RdRoad road : rdRoads) {

			roadPids.add(road.getPid());
		}

		String strRoadPids = StringUtils.getInteStr(roadPids);

		Map<String, RdObject> roadObjMap = selector.loadRdObjectByPidAndType(
				strRoadPids, ObjType.RDROAD, true);

		Set<Integer> rdObjectPids = new HashSet<Integer>();

		List<RdObject> rdObjects = new ArrayList<RdObject>();

		for (RdObject obj : linkObjMap.values()) {

			if (rdObjectPids.contains(obj.getPid())) {

				continue;
			}

			rdObjectPids.add(obj.getPid());

			rdObjects.add(obj);
		}
		for (RdObject obj : interObjMap.values()) {

			if (rdObjectPids.contains(obj.getPid())) {

				continue;
			}

			rdObjectPids.add(obj.getPid());

			rdObjects.add(obj);
		}
		for (RdObject obj : roadObjMap.values()) {

			if (rdObjectPids.contains(obj.getPid())) {

				continue;
			}

			rdObjectPids.add(obj.getPid());

			rdObjects.add(obj);
		}
		return rdObjects;
	}

	/**
	 * 上下线分离时初始化node、link
	 * 
	 * @param targetLinks
	 * @param preNodePid
	 * @param lastNodePid
	 */
	private void initDepart(List<RdLink> targetLinks, int preNodePid,
			int lastNodePid) {

		for (RdLink link : targetLinks) {

			if (!this.nodePids.contains(link.getsNodePid())) {

				this.nodePids.add(link.getsNodePid());
			}
			if (!this.nodePids.contains(link.geteNodePid())) {

				this.nodePids.add(link.geteNodePid());
			}
			if (!this.linkPids.contains(link.getPid())) {

				this.linkPids.add(link.getPid());
			}
		}

		this.nodePids.remove((Integer) preNodePid);

		this.nodePids.remove((Integer) lastNodePid);
	}

	/**
	 * 上下线分离维护CRF要素
	 * 
	 * @param result
	 * @param targetLinks
	 *            目标link
	 * @param preNodePid
	 *            目标link串第一个点pid
	 * @param lastNodePid
	 *            目标link串最后一个点pid
	 * @return
	 * @throws Exception
	 */
	public String updownDepart(Result result, List<RdLink> targetLinks,
			int preNodePid, int lastNodePid) throws Exception {

		initDepart(targetLinks, preNodePid, lastNodePid);

		List<RdInter> rdInters = getRdInter();

		List<RdRoad> rdRoads = getRdRoad();

		List<RdObject> rdObjects = getRdObject(rdInters, rdRoads);

		for (RdInter inter : rdInters) {

			result.insertObject(inter, ObjStatus.DELETE, inter.pid());
		}
		for (RdRoad road : rdRoads) {

			result.insertObject(road, ObjStatus.DELETE, road.pid());
		}
		for (RdObject obj : rdObjects) {

			result.insertObject(obj, ObjStatus.DELETE, obj.pid());
		}

		return "";
	}

	/**
	 * 删除rdnode、rdlink维护CRF要素
	 * 
	 * @param result
	 * @param linkPids
	 * @param nodePids
	 * @return
	 * @throws Exception
	 */
	public String delNodeLink(Result result, List<Integer> linkPids,
			List<Integer> nodePids) throws Exception {

		this.nodePids = nodePids;

		this.linkPids = linkPids;

		List<RdInter> rdInters = getRdInter();

		Map<Integer, RdInter> delInters = handleRdInterForDel(result, rdInters);

		List<RdRoad> rdRoads = getRdRoad();

		Map<Integer, RdRoad> delRoads = handleRdRoadForDel(result, rdRoads);

		handleRdObjectForDel(result, delInters, delRoads);

		return "";
	}

	/**
	 * 维护RdInter
	 * 
	 * @param result
	 * @param rdInters
	 * @return 被删除的RdInter
	 */
	private Map<Integer, RdInter> handleRdInterForDel(Result result,
			List<RdInter> rdInters) {

		Map<Integer, RdInter> delInters = new HashMap<Integer, RdInter>();

		for (RdInter inter : rdInters) {

			// RdInter的组成node都是被删除的node
			boolean isAllNode = true;

			for (IRow row : inter.getNodes()) {

				RdInterNode interNode = (RdInterNode) row;

				if (!this.nodePids.contains(interNode.getNodePid())) {

					isAllNode = false;

					break;
				}
			}

			if (isAllNode) {

				// 参与node均被删除，删除该inter
				result.insertObject(inter, ObjStatus.DELETE, inter.pid());

				delInters.put(inter.getPid(), inter);

				continue;
			}

			for (IRow row : inter.getNodes()) {

				RdInterNode interNode = (RdInterNode) row;

				// 删除参与node
				if (this.nodePids.contains(interNode.getNodePid())) {

					result.insertObject(row, ObjStatus.DELETE, inter.getPid());
				}
			}

			for (IRow row : inter.getLinks()) {

				RdInterLink interLink = (RdInterLink) row;

				// 删除参与link
				if (this.linkPids.contains(interLink.getLinkPid())) {

					result.insertObject(row, ObjStatus.DELETE, inter.getPid());
				}
			}
		}

		return delInters;

	}

	/**
	 * 维护RdRoad
	 * 
	 * @param result
	 * @param rdRoads
	 * @return 被删除的RdRoad
	 */
	private Map<Integer, RdRoad> handleRdRoadForDel(Result result,
			List<RdRoad> rdRoads) {

		Map<Integer, RdRoad> delRoads = new HashMap<Integer, RdRoad>();

		for (RdRoad road : rdRoads) {

			// RdRoad的组成link都是被删除的link
			boolean isAllLink = true;

			for (IRow row : road.getLinks()) {

				RdRoadLink roadLink = (RdRoadLink) row;

				if (!this.linkPids.contains(roadLink.getLinkPid())) {

					isAllLink = false;

					break;
				}
			}

			if (isAllLink) {

				// 参与link均被删除，删除该road
				result.insertObject(road, ObjStatus.DELETE, road.getPid());

				delRoads.put(road.getPid(), road);

				continue;
			}

			for (IRow row : road.getLinks()) {

				RdRoadLink roadLink = (RdRoadLink) row;

				// 删除参与link
				if (this.linkPids.contains(roadLink.getLinkPid())) {

					result.insertObject(row, ObjStatus.DELETE, road.getPid());
				}
			}
		}

		return delRoads;
	}

	/**
	 * 维护RdObject
	 * 
	 * @param result
	 * @param delInters
	 * @param delRoads
	 * @throws Exception
	 */
	private void handleRdObjectForDel(Result result,
			Map<Integer, RdInter> delInters, Map<Integer, RdRoad> delRoads)
			throws Exception {

		List<RdInter> rdInters = new ArrayList<RdInter>();

		rdInters.addAll(delInters.values());

		List<RdRoad> rdRoads = new ArrayList<RdRoad>();

		rdRoads.addAll(delRoads.values());

		List<RdObject> rdObjects = getRdObject(rdInters, rdRoads);

		for (RdObject object : rdObjects) {

			boolean isAllLink = true;

			boolean isAllInter = true;
			
			boolean isAllRoad = true;
			
			for (IRow row : object.getLinks()) {

				RdObjectLink objectLink = (RdObjectLink) row;

				if (!this.linkPids.contains(objectLink.getLinkPid())) {

					isAllLink = false;

					break;
				}
			}

			for (IRow row : object.getInters()) {

				RdObjectInter objectInter = (RdObjectInter) row;

				if (!delInters.containsKey(objectInter.getInterPid())) {

					isAllInter = false;

					break;
				}
			}

			for (IRow row : object.getRoads()) {

				RdObjectRoad objectRoad = (RdObjectRoad) row;

				if (!delRoads.containsKey(objectRoad.getRoadPid())) {

					isAllRoad = false;

					break;
				}
			}

			// 参与RdObject的link、Inter、Road均被删除时，该RdObject删除
			if (isAllLink && isAllInter && isAllRoad) {

				result.insertObject(object, ObjStatus.DELETE, object.getPid());
				
				continue;
			}
			
			// 在RdObject中去除被删的link
			for (IRow row : object.getLinks()) {

				RdObjectLink objectLink = (RdObjectLink) row;

				if (this.linkPids.contains(objectLink.getLinkPid())) {

					result.insertObject(objectLink, ObjStatus.DELETE,
							object.getPid());
				}
			}
			
			// 在RdObject中去除被删的Inter
			for (IRow row : object.getInters()) {

				RdObjectInter objectInter = (RdObjectInter) row;

				if (delInters.containsKey(objectInter.getInterPid())) {

					result.insertObject(objectInter, ObjStatus.DELETE,
							object.getPid());
				}
			}			
			
			// 在RdObject中去除被删的Road
			for (IRow row : object.getRoads()) {

				RdObjectRoad objectRoad = (RdObjectRoad) row;

				if (delRoads.containsKey(objectRoad.getRoadPid())) {

					result.insertObject(objectRoad, ObjStatus.DELETE,
							object.getPid());
				}
			}
		}

	}
}
