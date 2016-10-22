package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.depart;

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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
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


	private Map<Integer, RdDirectroute> delBranch = new HashMap<Integer, RdDirectroute>();

	private Set<Integer> updateBranch = new HashSet<Integer>();

	RdDirectrouteSelector directrouteSelector = null;
	
	RdLinkSelector linkSelector = null;

	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		directrouteSelector = new RdDirectrouteSelector(this.conn);
		
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

		for (RdDirectroute directroute : delBranch.values()) {

			if (updateBranch.contains(directroute.getPid())) {

				continue;
			}

			result.insertObject(directroute, ObjStatus.DELETE,
					directroute.getPid());
		}
	}

	/**
	 * 处理目标link上的顺行
	 * 
	 * @throws Exception
	 */
	private void handleTargetLinkDel() throws Exception {

		List<RdDirectroute> directroutes = new ArrayList<RdDirectroute>();

		// 目标link为进入线
		directroutes.addAll(directrouteSelector.loadByLinks(targetLinkPids,
				1, true));
		// 目标link为退出线
		directroutes.addAll(directrouteSelector.loadByLinks(targetLinkPids,
				2, true));
		// 目标link为经过线
		directroutes.addAll(directrouteSelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdDirectroute directroute : directroutes) {

			delBranch.put(directroute.getPid(), directroute);
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

		List<RdDirectroute> directroutes = new ArrayList<RdDirectroute>();

		// 端点改变的非目标link做进入线的顺行
		directroutes = directrouteSelector.loadByLinks(changedNodeLinks, 1,
				true);

		for (RdDirectroute directroute : directroutes) {

			if (delBranch.containsKey(directroute.getPid())) {

				continue;
			}

			// 进入点不是 制作上下线分离link的连接点
			if (!connectNodePid.contains(directroute.getNodePid())) {

				continue;
			}
			
			delBranch.put(directroute.getPid(), directroute);
		}

		// 端点改变的非目标link做退出线的顺行
		directroutes = directrouteSelector.loadByLinks(changedNodeLinks, 2,
				true);

		for (RdDirectroute directroute : directroutes) {

			if (delBranch.containsKey(directroute.getPid())) {

				continue;
			}
		
			if (!changedNodeLinkMap.containsKey(directroute.getOutLinkPid())) {
				continue;
			}

			int newNodePid = getNewNodePid(directroute.getOutLinkPid());

			List<Integer> connectLinkPids = new ArrayList<Integer>();

			connectLinkPids.add(directroute.getInLinkPid());

			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

				connectLinkPids.add(via.getLinkPid());
			}

			// 退出线不与其他非目标link挂接
			if (!containNoTargetLink(connectLinkPids)) {
				continue;
			}

			boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);

			if (!isHave) {

				delBranch.put(directroute.getPid(), directroute);

				break;
			}
			
		}

		// 端点改变的非目标link做经过线的顺行
		directroutes = directrouteSelector.loadByLinks(changedNodeLinks, 3,
				true);

		for (RdDirectroute directroute : directroutes) {

			if (delBranch.containsKey(directroute.getPid())) {

				continue;
			}

			boolean isHave = false;

			Set<Integer> allConnectLinkPids = new HashSet<Integer>();

			allConnectLinkPids.add(directroute.getInLinkPid());

			allConnectLinkPids.add(directroute.getOutLinkPid());

			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

				allConnectLinkPids.add(via.getLinkPid());
			}

			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

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

				delBranch.put(directroute.getPid(), directroute);

				break;
			}			
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的顺行
	 * 
	 * @throws Exception
	 */
	private void handleInLinkUpdate() throws Exception {

		Map<Integer, RdDirectroute> handleMap = new HashMap<Integer, RdDirectroute>();

		List<RdDirectroute> directroutes = new ArrayList<RdDirectroute>();

		directroutes = directrouteSelector.loadByLinkPid(preLinkPid, 1, true);

		for (RdDirectroute directroute : directroutes) {

			if (directroute.getNodePid() == preNodePid) {

				handleMap.put(directroute.getPid(), directroute);
			}
		}

		directroutes = directrouteSelector.loadByLinkPid(lastLinkPid, 1, true);

		for (RdDirectroute directroute : directroutes) {

			if (directroute.getNodePid() == lastNodePid) {

				handleMap.put(directroute.getPid(), directroute);
			}
		}

		for (RdDirectroute directroute : handleMap.values()) {

			RdLink lRdLink = leftLinkMapping.get(directroute.getInLinkPid());

			if ((lRdLink.getsNodePid() == directroute.getNodePid() && lRdLink
					.getDirect() == 3)
					|| (lRdLink.geteNodePid() == directroute.getNodePid() && lRdLink
							.getDirect() == 2)) {
				directroute.changedFields()
						.put("inLinkPid", lRdLink.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.getPid());

				updateBranch.add(directroute.getPid());
			}

			RdLink rRdLink = rightLinkMapping.get(directroute.getInLinkPid());

			if ((rRdLink.getsNodePid() == directroute.getNodePid() && rRdLink
					.getDirect() == 3)
					|| (rRdLink.geteNodePid() == directroute.getNodePid() && rRdLink
							.getDirect() == 2)) {
				
				directroute.changedFields()
						.put("inLinkPid", rRdLink.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.getPid());

				updateBranch.add(directroute.getPid());
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做退出线的顺行
	 * 
	 * @throws Exception
	 */
	private void handleOutLinkUpdate() throws Exception {

		handleOutLink(preLinkPid, preNodePid);

		handleOutLink(lastLinkPid, lastNodePid);
	}

	
	/**
	 * 处理与端点挂接的目标link做退出线的顺行
	 * 
	 * @throws Exception
	 */
	private void handleOutLink(int linkPid, int nodePid) throws Exception {

		Map<Integer, RdDirectroute> handleMap = new HashMap<Integer, RdDirectroute>();

		List<RdDirectroute> directroutes = directrouteSelector
				.loadByLinkPid(linkPid, 2, true);

		for (RdDirectroute directroute : directroutes) {

			List<Integer> connectLinkPids = new ArrayList<Integer>();	

			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

				connectLinkPids.add(via.getLinkPid());
			}
			
			boolean isOutLinkInNode = isOutLinkInNode(directroute, connectLinkPids,
					nodePid);

			if (!isOutLinkInNode) {
				
				continue;
			}
			
			connectLinkPids.add(directroute.getInLinkPid());

			if (containNoTargetLink(connectLinkPids)
					|| containTargetLink(connectLinkPids)) {

				continue;
			}			

			handleMap.put(directroute.getPid(), directroute);
		}

		if (!handleMap.isEmpty()) {

			updateBranch(handleMap, nodePid);
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
	private boolean isOutLinkInNode(RdDirectroute directroute ,List<Integer> viaPids,
			 int nodePid) throws Exception {
		
		if (viaPids.size() == 0) {

			if (directroute.getNodePid() != nodePid) {

				return false;
			} else {
				return true;
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


	private void updateBranch(Map<Integer, RdDirectroute> handleLastMap,
			int nodePid) {

		for (RdDirectroute directroute : handleLastMap.values()) {

			RdLink rRdLink = rightLinkMapping.get(directroute.getOutLinkPid());

			if ((rRdLink.getsNodePid() == nodePid && rRdLink.getDirect() == 2)
					|| (rRdLink.geteNodePid() == nodePid && rRdLink.getDirect() == 3)) {

				directroute.changedFields().put("outLinkPid", rRdLink.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.getPid());

				updateBranch.add(directroute.getPid());
			}

			RdLink lRdLink = leftLinkMapping.get(directroute.getOutLinkPid());

			if ((lRdLink.getsNodePid() == nodePid && lRdLink.getDirect() == 2)
					|| (lRdLink.geteNodePid() == nodePid && lRdLink.getDirect() == 3)) {

				directroute.changedFields().put("outLinkPid", lRdLink.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.getPid());

				updateBranch.add(directroute.getPid());
			}
		}
	}
}
