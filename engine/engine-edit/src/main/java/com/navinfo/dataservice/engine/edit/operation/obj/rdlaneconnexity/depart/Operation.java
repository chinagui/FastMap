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
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

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

	// 端口改变的非目标link映射
	private Map<Integer, RdLink> changedNodeLinkMap = new HashMap<Integer, RdLink>();

	// 非目标link映射
	private Map<Integer, RdLink> noTargetLinkMap = new HashMap<Integer, RdLink>();

	// 连接Node
	private Set<Integer> connectNodePid = new HashSet<Integer>();

	// 目标linkPid
	private List<Integer> targetLinkPids = new ArrayList<Integer>();	


	private Map<Integer, RdLaneConnexity> delLaneConnexity = new HashMap<Integer, RdLaneConnexity>();

	private Set<Integer> updateLaneConnexity = new HashSet<Integer>();

	RdLaneConnexitySelector laneConnexitySelector = null;
	
	RdLinkSelector linkSelector = null;

	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		laneConnexitySelector = new RdLaneConnexitySelector(this.conn);
		
		linkSelector = new RdLinkSelector(this.conn);
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
			
			connectNodePid.add(link.getsNodePid());
			
			connectNodePid.add(link.geteNodePid());
		}		

		connectNodePid.remove((Integer) preNodePid);

		connectNodePid.remove((Integer) lastNodePid);

		for (RdLink link : noTargetLinks.values()) {

			noTargetLinkMap.put(link.getPid(), link);

			if (link.changedFields().containsKey("eNodePid")) {

				changedNodeLinkMap.put(link.getPid(), link);
				
			}
			if (link.changedFields().containsKey("sNodePid")) {

				changedNodeLinkMap.put(link.getPid(), link);
			}
		}
	}

	public void upDownPart(Map<Integer, RdLink> leftLinkMapping,
			Map<Integer, RdLink> rightLinkMapping, Result result)
			throws Exception {

		this.result = result;

		this.rightLinkMapping = rightLinkMapping;

		this.leftLinkMapping = leftLinkMapping;

		handleTargetLinkDel();

		handleChangedNodeLinkDel();

		handleInLinkUpdate();

		handleOutLinkUpdate();

		for (RdLaneConnexity laneConnexity : delLaneConnexity.values()) {

			if (updateLaneConnexity.contains(laneConnexity.getPid())) {

				continue;
			}

			result.insertObject(laneConnexity, ObjStatus.DELETE,
					laneConnexity.getPid());
		}
	}

	/**
	 * 处理目标link上的车信
	 * 
	 * @throws Exception
	 */
	private void handleTargetLinkDel() throws Exception {

		List<RdLaneConnexity> laneConnexitys = new ArrayList<RdLaneConnexity>();

		// 目标link为进入线
		laneConnexitys.addAll(laneConnexitySelector.loadByLinks(targetLinkPids,
				1, true));
		// 目标link为退出线
		laneConnexitys.addAll(laneConnexitySelector.loadByLinks(targetLinkPids,
				2, true));
		// 目标link为经过线
		laneConnexitys.addAll(laneConnexitySelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);
		}
	}

	private int getNewNodePid(int changedLinkPid) {

		if (!changedNodeLinkMap.containsKey(changedLinkPid)) {

			return 0;
		}

		RdLink changedLink = changedNodeLinkMap.get(changedLinkPid);

		int newNodePid = 0;

		if (changedLink.changedFields().containsKey("sNodePid")) {

			newNodePid = (Integer) changedLink.changedFields().get("sNodePid");

		} else if (changedLink.changedFields().containsKey("eNodePid")) {

			newNodePid = (Integer) changedLink.changedFields().get("eNodePid");
		}

		return newNodePid;
	}

	private boolean haveNewNodePid(List<Integer> connectLinkPids, int newNodePid) {

		boolean isHave = false;

		for (int linkPid : connectLinkPids) {

			if (!noTargetLinkMap.containsKey(linkPid)) {

				continue;
			}

			RdLink link = noTargetLinkMap.get(linkPid);

			int sPid = link.getsNodePid();

			int ePid = link.geteNodePid();

			if (link.changedFields().containsKey("sNodePid")) {

				sPid = (Integer) link.changedFields().get("sNodePid");

			} else if (link.changedFields().containsKey("eNodePid")) {

				sPid = (Integer) link.changedFields().get("eNodePid");
			}

			if (sPid == newNodePid || ePid == newNodePid) {

				isHave = true;

				break;
			}
		}

		return isHave;
	}

	/**
	 * 是否包含非目标link
	 * 
	 * @param connectLinkPids
	 * @return
	 */
	private boolean containNoTargetLink(List<Integer> linkPids) {

		for (int pid : linkPids) {

			if (this.noTargetLinkMap.containsKey(pid)) {

				return true;
			}
		}

		return false;
	}

	/**
	 * 是否包含目标link
	 * 
	 * @param connectLinkPids
	 * @return
	 */
	private boolean containTargetLink(List<Integer> linkPids) {

		for (int pid : linkPids) {

			if (this.targetLinkPids.contains(pid)) {

				return true;
			}
		}

		return false;
	}

	private void handleChangedNodeLinkDel() throws Exception {

		if (changedNodeLinkMap == null || changedNodeLinkMap.isEmpty()) {
			return;
		}

		List<Integer> changedNodeLinks = new ArrayList<Integer>();

		changedNodeLinks.addAll(changedNodeLinkMap.keySet());

		List<RdLaneConnexity> laneConnexitys = new ArrayList<RdLaneConnexity>();

		// 端点改变的非目标link做进入线的车信
		laneConnexitys = laneConnexitySelector.loadByLinks(changedNodeLinks, 1,
				true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {

				continue;
			}

			// 进入点不是 制作上下线分离link的连接点
			if (!connectNodePid.contains(laneConnexity.getNodePid())) {

				continue;
			}
			
			delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

//			int newNodePid = getNewNodePid(laneConnexity.getInLinkPid());
//
//			for (IRow rowTopology : laneConnexity.getTopos()) {
//
//				List<Integer> connectLinkPids = new ArrayList<Integer>();
//
//				RdLaneTopology topology = (RdLaneTopology) rowTopology;
//
//				connectLinkPids.add(topology.getOutLinkPid());
//
//				for (IRow rowVia : topology.getVias()) {
//
//					RdLaneVia via = (RdLaneVia) rowVia;
//
//					connectLinkPids.add(via.getLinkPid());
//				}
//
//				boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);
//
//				if (!isHave) {
//
//					delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);
//
//					break;
//				}
//			}
		}

		// 端点改变的非目标link做退出线的车信
		laneConnexitys = laneConnexitySelector.loadByLinks(changedNodeLinks, 2,
				true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {

				continue;
			}

			for (IRow rowTopology : laneConnexity.getTopos()) {

				RdLaneTopology topology = (RdLaneTopology) rowTopology;

				if (!changedNodeLinkMap.containsKey(topology.getOutLinkPid())) {
					continue;
				}

				int newNodePid = getNewNodePid(topology.getOutLinkPid());

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(laneConnexity.getInLinkPid());

				for (IRow rowVia : topology.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					connectLinkPids.add(via.getLinkPid());
				}

				// 退出线不与其他非目标link挂接
				if (!containNoTargetLink(connectLinkPids)) {
					continue;
				}

				boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);

				if (!isHave) {

					delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

					break;
				}
			}
		}

		// 端点改变的非目标link做经过线的车信
		laneConnexitys = laneConnexitySelector.loadByLinks(changedNodeLinks, 3,
				true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (delLaneConnexity.containsKey(laneConnexity.getPid())) {

				continue;
			}

			boolean isHave = false;

			for (IRow rowTopology : laneConnexity.getTopos()) {

				RdLaneTopology topology = (RdLaneTopology) rowTopology;

				Set<Integer> allConnectLinkPids = new HashSet<Integer>();

				allConnectLinkPids.add(laneConnexity.getInLinkPid());

				allConnectLinkPids.add(topology.getOutLinkPid());

				for (IRow rowVia : topology.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					allConnectLinkPids.add(via.getLinkPid());
				}

				for (IRow rowVia : topology.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					if (!changedNodeLinkMap.containsKey(via.getLinkPid())) {

						continue;
					}

					int newNodePid = getNewNodePid(via.getLinkPid());

					List<Integer> connectLinkPids = new ArrayList<Integer>();

					connectLinkPids.addAll(allConnectLinkPids);

					connectLinkPids.remove((Integer) via.getLinkPid());

					isHave = haveNewNodePid(connectLinkPids, newNodePid);

					if (!isHave) {

						break;
					}
				}

				if (!isHave) {

					delLaneConnexity.put(laneConnexity.getPid(), laneConnexity);

					break;
				}
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的车信
	 * 
	 * @throws Exception
	 */
	private void handleInLinkUpdate() throws Exception {

		Map<Integer, RdLaneConnexity> handleMap = new HashMap<Integer, RdLaneConnexity>();

		List<RdLaneConnexity> laneConnexitys = new ArrayList<RdLaneConnexity>();

		laneConnexitys = laneConnexitySelector.loadByLink(preLinkPid, 1, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (laneConnexity.getNodePid() == preNodePid) {

				handleMap.put(laneConnexity.getPid(), laneConnexity);
			}
		}

		laneConnexitys = laneConnexitySelector.loadByLink(lastLinkPid, 1, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (laneConnexity.getNodePid() == lastNodePid) {

				handleMap.put(laneConnexity.getPid(), laneConnexity);
			}
		}

		for (RdLaneConnexity laneConnexity : handleMap.values()) {

			RdLink lRdLink = leftLinkMapping.get(laneConnexity.getInLinkPid());

			if ((lRdLink.getsNodePid() == laneConnexity.getNodePid() && lRdLink
					.getDirect() == 3)
					|| (lRdLink.geteNodePid() == laneConnexity.getNodePid() && lRdLink
							.getDirect() == 2)) {
				laneConnexity.changedFields()
						.put("inLinkPid", lRdLink.getPid());

				result.insertObject(laneConnexity, ObjStatus.UPDATE,
						laneConnexity.getPid());

				updateLaneConnexity.add(laneConnexity.getPid());
			}

			RdLink rRdLink = rightLinkMapping.get(laneConnexity.getInLinkPid());

			if ((rRdLink.getsNodePid() == laneConnexity.getNodePid() && rRdLink
					.getDirect() == 3)
					|| (rRdLink.geteNodePid() == laneConnexity.getNodePid() && rRdLink
							.getDirect() == 2)) {
				
				laneConnexity.changedFields()
						.put("inLinkPid", rRdLink.getPid());

				result.insertObject(laneConnexity, ObjStatus.UPDATE,
						laneConnexity.getPid());

				updateLaneConnexity.add(laneConnexity.getPid());
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做退出线的车信
	 * 
	 * @throws Exception
	 */
	private void handleOutLinkUpdate() throws Exception {

		handleOutLink(preLinkPid, preNodePid);

		handleOutLink(lastLinkPid, lastNodePid);
	}

	
	/**
	 * 处理与端点挂接的目标link做退出线的车信
	 * 
	 * @throws Exception
	 */
	private void handleOutLink(int linkPid, int nodePid) throws Exception {

		Map<Integer, RdLaneTopology> handleMap = new HashMap<Integer, RdLaneTopology>();

		List<RdLaneConnexity> laneConnexitys = laneConnexitySelector
				.loadByLink(linkPid, 2, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			// 进入线、经过线是目标link或者是非目标link
			boolean isContain = false;

			RdLaneTopology updateTopology = null;

			for (IRow rowTopology : laneConnexity.getTopos()) {

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(laneConnexity.getInLinkPid());

				RdLaneTopology topology = (RdLaneTopology) rowTopology;

				if (topology.getOutLinkPid() == linkPid) {					

					boolean isOutLinkInNode = isOutLinkInNode(laneConnexity,
							topology, nodePid);

					if (isOutLinkInNode) {

						updateTopology = topology;
					}

				} else {
					connectLinkPids.add(topology.getOutLinkPid());
				}

				for (IRow rowVia : topology.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					connectLinkPids.add(via.getLinkPid());
				}

				if (containNoTargetLink(connectLinkPids)
						|| containTargetLink(connectLinkPids)) {

					isContain = true;

					break;
				}
			}

			if (isContain) {
				
				continue;
			}
			if (updateTopology != null) {
				handleMap.put(updateTopology.getPid(), updateTopology);
			}
		}

		if (!handleMap.isEmpty()) {

			updateTopology(handleMap, nodePid);
		}
	}
	
	/**
	 * 判断node是不是 进入退出线的点。
	 * 
	 * @param restriction
	 * @param detail
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isOutLinkInNode(RdLaneConnexity laneConnexity,
			RdLaneTopology topology, int nodePid) throws Exception {
		
		if (topology.getVias().size() == 0) {

			if (laneConnexity.getNodePid() != nodePid) {

				return false;
			} else {
				return true;
			}
		}

		List<Integer> viaPids = new ArrayList<Integer>();

		for (IRow rowVia : topology.getVias()) {

			RdLaneVia via = (RdLaneVia) rowVia;

			if (!viaPids.contains(via.getLinkPid())) {

				viaPids.add(via.getLinkPid());
			}
		}		

		List<IRow> linkRows = linkSelector.loadByIds(viaPids, true,
				false);

		for (IRow linkRow : linkRows) {
			
			RdLink link = (RdLink) linkRow;

			if (link.getsNodePid() == nodePid || link.geteNodePid() == nodePid) {

				return true;
			}			
		}
		
		return false;
	}

	private void updateTopology(Map<Integer, RdLaneTopology> handleLastMap,
			int nodePid) {

		for (RdLaneTopology topology : handleLastMap.values()) {

			RdLink rRdLink = rightLinkMapping.get(topology.getOutLinkPid());

			if ((rRdLink.getsNodePid() == nodePid && rRdLink.getDirect() == 2)
					|| (rRdLink.geteNodePid() == nodePid && rRdLink.getDirect() == 3)) {

				topology.changedFields().put("outLinkPid", rRdLink.getPid());

				result.insertObject(topology, ObjStatus.UPDATE,
						topology.getPid());

				updateLaneConnexity.add(topology.getConnexityPid());
			}

			RdLink lRdLink = leftLinkMapping.get(topology.getOutLinkPid());

			if ((lRdLink.getsNodePid() == nodePid && lRdLink.getDirect() == 2)
					|| (lRdLink.geteNodePid() == nodePid && lRdLink.getDirect() == 3)) {

				topology.changedFields().put("outLinkPid", lRdLink.getPid());

				result.insertObject(topology, ObjStatus.UPDATE,
						topology.getPid());

				updateLaneConnexity.add(topology.getConnexityPid());
			}
		}
	}
}
