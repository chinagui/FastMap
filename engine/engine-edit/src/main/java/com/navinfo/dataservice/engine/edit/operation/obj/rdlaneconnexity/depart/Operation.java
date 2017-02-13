package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.depart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;

public class Operation {

	private Connection conn;

	private int preNodePid;

	private int lastNodePid;

	private int preLinkPid;

	private int lastLinkPid;

	Result result;

	// 左侧link映射。Integer：原linkpid RdLink；新生成link
	private Map<Integer, RdLink> leftLinkMapping = new HashMap<Integer, RdLink>();

	// 右侧link映射。Integer：原linkpid RdLink；新生成link
	private Map<Integer, RdLink> rightLinkMapping = new HashMap<Integer, RdLink>();

	// 连接Node
	private Set<Integer> connectNodePids = new HashSet<Integer>();

	// 目标linkPid
	private List<Integer> targetLinkPids = new ArrayList<Integer>();

	private Map<Integer, RdLaneConnexity> delLaneConnexity = new HashMap<Integer, RdLaneConnexity>();

	RdLaneConnexitySelector laneConnexitySelector = null;
	
	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		laneConnexitySelector = new RdLaneConnexitySelector(this.conn);
	}

	private void init(Map<Integer, RdLink> noTargetLinks,
			List<RdLink> targetLinks) {
		
		preLinkPid = 0;
		
		lastLinkPid = 0;
		
		for (RdLink link : targetLinks) {

			if (preLinkPid == 0
					&& (link.getsNodePid() == preNodePid || link.geteNodePid() == preNodePid)) {

				preLinkPid = link.getPid();
			}
			if (lastLinkPid == 0
					&& (link.getsNodePid() == lastNodePid || link.geteNodePid() == lastNodePid)) {

				lastLinkPid = link.getPid();
			}
			if (!targetLinkPids.contains(link.getPid())) {

				targetLinkPids.add(link.getPid());
			}
			
			connectNodePids.add(link.getsNodePid());
			
			connectNodePids.add(link.geteNodePid());
		}		

		connectNodePids.remove((Integer) preNodePid);

		connectNodePids.remove((Integer) lastNodePid);
	}

	public void upDownPart(Map<Integer, RdLink> leftLinkMapping,
			Map<Integer, RdLink> rightLinkMapping, Result result)
			throws Exception {

		this.result = result;

		this.rightLinkMapping = rightLinkMapping;

		this.leftLinkMapping = leftLinkMapping;

		if (connectNodePids.size() == 0) {

			handlePassLinkDel();
		} else {

			handleConnectNodeDel();
		}
		
		handleEndPoint();
		
		for (RdLaneConnexity laneConnexity : delLaneConnexity.values()) {

			result.insertObject(laneConnexity, ObjStatus.DELETE,
					laneConnexity.getPid());
		}
	}
	
	/**
	 * 处理目标link为经过线的车信
	 * 
	 * @throws Exception
	 */
	private void handlePassLinkDel() throws Exception {

		List<RdLaneConnexity> laneConnexitys = new ArrayList<RdLaneConnexity>();
		
		// 目标link为经过线
		laneConnexitys.addAll(laneConnexitySelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);
		}
	}	
	
	/**
	 * 处理目标link连接点关联的车信
	 * 
	 * @throws Exception
	 */
	private void handleConnectNodeDel() throws Exception {
		
		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.addAll(connectNodePids);

		Set<Integer> pids = new HashSet<Integer>();

		// 经过点获取车信pid
		pids.addAll(laneConnexitySelector.getPidByPassNode(nodePids));

		// 进入点获取pid
		pids.addAll(laneConnexitySelector.getPidByInNode(nodePids));

		List<Integer> connexityPids = new ArrayList<Integer>();

		connexityPids.addAll(pids);

		List<IRow> rows = laneConnexitySelector.loadByIds(connexityPids, true,
				true);

		for (IRow row : rows) {
			
			RdLaneConnexity connexity = (RdLaneConnexity) row;

			delLaneConnexity.put(connexity.getPid(), connexity);
		}
	}
	
	/**
	 * 处理端点
	 * @throws Exception
	 */
	private void handleEndPoint() throws Exception {

		List<RdLaneConnexity> updataConnexitys = new ArrayList<RdLaneConnexity>();

		Map<Integer, List<RdLaneTopology>> updataTopologys = new HashMap<Integer, List<RdLaneTopology>>();

		updataConnexitys.addAll(handleInLinkUpdate(preNodePid, preLinkPid));

		updataTopologys.putAll(handleOutLink(preNodePid, preLinkPid));

		updataConnexitys.addAll(handleInLinkUpdate(lastNodePid, lastLinkPid));

		updataTopologys.putAll(handleOutLink(lastNodePid, lastLinkPid));

		for (RdLaneConnexity laneConnexity : updataConnexitys) {
			
			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {
				continue;
			}

			result.insertObject(laneConnexity, ObjStatus.UPDATE,
					laneConnexity.getPid());
		}

		for (Map.Entry<Integer, List<RdLaneTopology>> entry : updataTopologys
				.entrySet()) {

			updateTopology(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * 处理与端点挂接的目标link做进入线的车信
	 * 
	 * @throws Exception
	 */
	private List<RdLaneConnexity> handleInLinkUpdate(int nodePid,int linkPid ) throws Exception {

		Map<Integer, RdLaneConnexity> handleMap = new HashMap<Integer, RdLaneConnexity>();

		List<RdLaneConnexity> laneConnexitys = laneConnexitySelector
				.loadByLink(linkPid, 1, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (laneConnexity.getNodePid() != nodePid) {
				continue;
			}

			if (!isCrossNode(nodePid)) {

				delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

				continue;
			}

			for (IRow row : laneConnexity.getTopos()) {

				RdLaneTopology topology = (RdLaneTopology) row;

				if (topology.getRelationshipType() == 2) {

					delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

					break;
				}
			}

			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {
				continue;
			}

			handleMap.put(laneConnexity.getPid(), laneConnexity);
		}

		List<RdLaneConnexity> updataLaneConnexitys = new ArrayList<RdLaneConnexity>();

		for (RdLaneConnexity laneConnexity : handleMap.values()) {

			RdLink rdLink = leftLinkMapping.get(laneConnexity.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				laneConnexity.changedFields().put("inLinkPid", rdLink.getPid());

				updataLaneConnexitys.add(laneConnexity);

				continue;
			}

			rdLink = rightLinkMapping.get(laneConnexity.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				laneConnexity.changedFields().put("inLinkPid", rdLink.getPid());

				updataLaneConnexitys.add(laneConnexity);
			}
		}
		
		return updataLaneConnexitys;
	}
	
	/**
	 * 处理与端点挂接的目标link做退出线的车信
	 * 
	 * @throws Exception
	 */
	private Map<Integer, List<RdLaneTopology>> handleOutLink( int nodePid,int linkPid) throws Exception {

		Map<Integer, List<RdLaneTopology>> handleMap = new HashMap<Integer, List<RdLaneTopology>>();
		
		List<RdLaneTopology> updataTopology = new ArrayList<RdLaneTopology>();
		
		handleMap.put(nodePid, updataTopology);
		
		List<RdLaneConnexity> laneConnexitys = laneConnexitySelector
				.loadByLink(linkPid, 2, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {
			
			RdLaneTopology updateTopology = null;
			
			for (IRow row : laneConnexity.getTopos()) {

				RdLaneTopology topology = (RdLaneTopology) row;
				
				if (topology.getRelationshipType() == 2) {

					delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

					break;
				}
				
				if (topology.getOutLinkPid() == linkPid) {

					boolean sameCrossNode = isSameCross(laneConnexity,
							nodePid);

					if (sameCrossNode) {

						updateTopology = topology;
					}
				}
			}

			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {
				continue;
			}
			if (updateTopology != null) {
				
				handleMap.get(nodePid).add(updateTopology);
			}		
		}
		
		return handleMap;
	}
	
	/**
	 * 更新Topology退出线
	 */
	private void updateTopology(List<RdLaneTopology> topologys, int nodePid) {

		if (topologys == null || topologys.isEmpty()) {

			return;
		}

		for (RdLaneTopology topology : topologys) {

			if (delLaneConnexity.containsKey(topology.getConnexityPid())) {

				continue;
			}

			RdLink rdLink = leftLinkMapping.get(topology.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				topology.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(topology, ObjStatus.UPDATE,
						topology.getConnexityPid());

				continue;
			}

			rdLink = rightLinkMapping.get(topology.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				topology.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(topology, ObjStatus.UPDATE,
						topology.getConnexityPid());

			}
		}
	}
	
	/**
	 * 判断退出线的端点node是否与进入node为同一路口。
	 * 
	 * @param laneConnexity
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isSameCross(RdLaneConnexity laneConnexity, int nodePid)
			throws Exception {

		RdCrossNodeSelector crossNodeSelector = new RdCrossNodeSelector(
				this.conn);

		List<Integer> nodePids = crossNodeSelector.getCrossNodePidByNode(
				nodePid);

		if (nodePids.contains(laneConnexity.getNodePid())
				&& nodePids.contains(nodePid)) {

			return true;
		}
		return false;
	}
	
	/**
	 * 判断node是否是路口组成node。
	 * 
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isCrossNode(int nodePid) throws Exception {

		RdCrossNodeSelector crossNodeSelector = new RdCrossNodeSelector(
				this.conn);

		List<Integer> nodePids = crossNodeSelector.getCrossNodePidByNode(
				nodePid);

		if (nodePids.size() > 0) {
			
			return true;
		}
		return false;
	}

}
