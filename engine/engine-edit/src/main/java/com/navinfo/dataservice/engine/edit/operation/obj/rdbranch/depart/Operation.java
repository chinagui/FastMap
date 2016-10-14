package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.depart;

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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;

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


	private Map<Integer, RdBranch> delBranch = new HashMap<Integer, RdBranch>();

	private Set<Integer> updateBranch = new HashSet<Integer>();

	RdBranchSelector branchSelector = null;

	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		branchSelector = new RdBranchSelector(this.conn);
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

		for (RdBranch branch : delBranch.values()) {

			if (updateBranch.contains(branch.getPid())) {

				continue;
			}

			result.insertObject(branch, ObjStatus.DELETE,
					branch.getPid());
		}
	}

	/**
	 * 处理目标link上的分歧
	 * 
	 * @throws Exception
	 */
	private void handleTargetLinkDel() throws Exception {

		List<RdBranch> branchs = new ArrayList<RdBranch>();

		// 目标link为进入线
		branchs.addAll(branchSelector.loadByLinks(targetLinkPids,
				1, true));
		// 目标link为退出线
		branchs.addAll(branchSelector.loadByLinks(targetLinkPids,
				2, true));
		// 目标link为经过线
		branchs.addAll(branchSelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdBranch branch : branchs) {

			delBranch.put(branch.getPid(), branch);
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

		List<RdBranch> branchs = new ArrayList<RdBranch>();

		// 端点改变的非目标link做进入线的分歧
		branchs = branchSelector.loadByLinks(changedNodeLinks, 1,
				true);

		for (RdBranch branch : branchs) {

			if (delBranch.containsKey(branch.getPid())) {

				continue;
			}

			// 进入点不是 制作上下线分离link的连接点
			if (!connectNodePid.contains(branch.getNodePid())) {

				continue;
			}
			
			delBranch.put(branch.getPid(), branch);
		}

		// 端点改变的非目标link做退出线的分歧
		branchs = branchSelector.loadByLinks(changedNodeLinks, 2,
				true);

		for (RdBranch branch : branchs) {

			if (delBranch.containsKey(branch.getPid())) {

				continue;
			}
		
			if (!changedNodeLinkMap.containsKey(branch.getOutLinkPid())) {
				continue;
			}

			int newNodePid = getNewNodePid(branch.getOutLinkPid());

			List<Integer> connectLinkPids = new ArrayList<Integer>();

			connectLinkPids.add(branch.getInLinkPid());

			for (IRow rowVia : branch.getVias()) {

				RdBranchVia via = (RdBranchVia) rowVia;

				connectLinkPids.add(via.getLinkPid());
			}

			// 退出线不与其他非目标link挂接
			if (!containNoTargetLink(connectLinkPids)) {
				continue;
			}

			boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);

			if (!isHave) {

				delBranch.put(branch.getPid(), branch);

				break;
			}
			
		}

		// 端点改变的非目标link做经过线的分歧
		branchs = branchSelector.loadByLinks(changedNodeLinks, 3,
				true);

		for (RdBranch branch : branchs) {

			if (delBranch.containsKey(branch.getPid())) {

				continue;
			}

			boolean isHave = false;

			Set<Integer> allConnectLinkPids = new HashSet<Integer>();

			allConnectLinkPids.add(branch.getInLinkPid());

			allConnectLinkPids.add(branch.getOutLinkPid());

			for (IRow rowVia : branch.getVias()) {

				RdBranchVia via = (RdBranchVia) rowVia;

				allConnectLinkPids.add(via.getLinkPid());
			}

			for (IRow rowVia : branch.getVias()) {

				RdBranchVia via = (RdBranchVia) rowVia;

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

				delBranch.put(branch.getPid(), branch);

				break;
			}			
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的分歧
	 * 
	 * @throws Exception
	 */
	private void handleInLinkUpdate() throws Exception {

		Map<Integer, RdBranch> handleMap = new HashMap<Integer, RdBranch>();

		List<RdBranch> branchs = new ArrayList<RdBranch>();

		branchs = branchSelector.loadByLinkPid(preLinkPid, 1, true);

		for (RdBranch branch : branchs) {

			if (branch.getNodePid() == preNodePid) {

				handleMap.put(branch.getPid(), branch);
			}
		}

		branchs = branchSelector.loadByLinkPid(lastLinkPid, 1, true);

		for (RdBranch branch : branchs) {

			if (branch.getNodePid() == lastNodePid) {

				handleMap.put(branch.getPid(), branch);
			}
		}

		for (RdBranch branch : handleMap.values()) {

			RdLink lRdLink = leftLinkMapping.get(branch.getInLinkPid());

			if ((lRdLink.getsNodePid() == branch.getNodePid() && lRdLink
					.getDirect() == 3)
					|| (lRdLink.geteNodePid() == branch.getNodePid() && lRdLink
							.getDirect() == 2)) {
				branch.changedFields()
						.put("inLinkPid", lRdLink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE,
						branch.getPid());

				updateBranch.add(branch.getPid());
			}

			RdLink rRdLink = rightLinkMapping.get(branch.getInLinkPid());

			if ((rRdLink.getsNodePid() == branch.getNodePid() && rRdLink
					.getDirect() == 3)
					|| (rRdLink.geteNodePid() == branch.getNodePid() && rRdLink
							.getDirect() == 2)) {
				
				branch.changedFields()
						.put("inLinkPid", rRdLink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE,
						branch.getPid());

				updateBranch.add(branch.getPid());
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做退出线的分歧
	 * 
	 * @throws Exception
	 */
	private void handleOutLinkUpdate() throws Exception {

		handleOutLink(preLinkPid, preNodePid);

		handleOutLink(lastLinkPid, lastNodePid);
	}

	
	/**
	 * 处理与端点挂接的目标link做退出线的分歧
	 * 
	 * @throws Exception
	 */
	private void handleOutLink(int linkPid, int nodePid) throws Exception {

		Map<Integer, RdBranch> handleMap = new HashMap<Integer, RdBranch>();

		List<RdBranch> branchs = branchSelector
				.loadByLinkPid(linkPid, 2, true);

		for (RdBranch branch : branchs) {

			List<Integer> connectLinkPids = new ArrayList<Integer>();

			connectLinkPids.add(branch.getInLinkPid());

			for (IRow rowVia : branch.getVias()) {

				RdBranchVia via = (RdBranchVia) rowVia;

				connectLinkPids.add(via.getLinkPid());
			}

			if (containNoTargetLink(connectLinkPids)
					|| containTargetLink(connectLinkPids)) {

				continue;
			}			

			handleMap.put(branch.getPid(), branch);
		}

		if (!handleMap.isEmpty()) {

			updateBranch(handleMap, nodePid);
		}
	}

	private void updateBranch(Map<Integer, RdBranch> handleLastMap,
			int nodePid) {

		for (RdBranch branch : handleLastMap.values()) {

			RdLink rRdLink = rightLinkMapping.get(branch.getOutLinkPid());

			if ((rRdLink.getsNodePid() == nodePid && rRdLink.getDirect() == 2)
					|| (rRdLink.geteNodePid() == nodePid && rRdLink.getDirect() == 3)) {

				branch.changedFields().put("outLinkPid", rRdLink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE,
						branch.getPid());

				updateBranch.add(branch.getPid());
			}

			RdLink lRdLink = leftLinkMapping.get(branch.getOutLinkPid());

			if ((lRdLink.getsNodePid() == nodePid && lRdLink.getDirect() == 2)
					|| (lRdLink.geteNodePid() == nodePid && lRdLink.getDirect() == 3)) {

				branch.changedFields().put("outLinkPid", lRdLink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE,
						branch.getPid());

				updateBranch.add(branch.getPid());
			}
		}
	}
}
