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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;

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

	private Map<Integer, RdBranch> delBranch = new HashMap<Integer, RdBranch>();

	RdBranchSelector branchSelector = null;
	
	RdCrossNodeSelector crossNodeSelector = null;
	
	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		branchSelector = new RdBranchSelector(this.conn);
		
		crossNodeSelector = new RdCrossNodeSelector(
				this.conn);
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
		
		for (RdBranch branch  : delBranch.values()) {

			result.insertObject(branch, ObjStatus.DELETE,
					branch.getPid());
		}
	}
	
	/**
	 * 处理目标link为经过线的分歧
	 * 
	 * @throws Exception
	 */
	private void handlePassLinkDel() throws Exception {

		List<RdBranch> branchs = new ArrayList<RdBranch>();
		
		// 目标link为经过线
		branchs.addAll(branchSelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdBranch branch : branchs) {

			delBranch.put(branch.getPid(), branch);
		}
	}	
	
	/**
	 * 处理目标link连接点关联的分歧
	 * 
	 * @throws Exception
	 */
	private void handleConnectNodeDel() throws Exception {
		
		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.addAll(connectNodePids);

		Set<Integer> pids = new HashSet<Integer>();

		// 经过点获取pid
		pids.addAll(branchSelector.getPidByPassNode(nodePids));

		// 进入点获取pid
		pids.addAll(branchSelector.getPidByInNode(nodePids));

		List<Integer> branchPids = new ArrayList<Integer>();

		branchPids.addAll(pids);

		List<IRow> rows = branchSelector.loadByIds(branchPids, true, true);

		for (IRow row : rows) {
			
			RdBranch branch = (RdBranch) row;


			delBranch.put(branch.getPid(), branch);
		}
		
		for (int nodePid : nodePids) {

			// 获取node关联link做为退出线的分歧pid
			List<Integer> pidTmps = branchSelector.getPidByOutNode(nodePid);

			List<Integer> outPids = new ArrayList<>();

			for (int pid : pidTmps) {

				if (!delBranch.containsKey(pid)) {

					outPids.add(pid);
				}
			}

			rows = branchSelector.loadByIds(outPids, true, true);

			for (IRow row : rows) {

				RdBranch branch = (RdBranch) row;

				if (branch.getRelationshipType() == 2) {

					continue;
				}

				if (isSameCross(branch.getNodePid(), nodePid)) {

					delBranch.put(branch.getPid(), branch);
				}
			}
		}
	}
	
	/**
	 * 处理端点
	 * @throws Exception
	 */
	private void handleEndPoint() throws Exception {

		List<RdBranch> updataBranchs = new ArrayList<RdBranch>();	

		updataBranchs.addAll(handleInLinkUpdate(preNodePid, preLinkPid));

		updataBranchs.addAll(handleOutLink(preNodePid, preLinkPid));

		updataBranchs.addAll(handleInLinkUpdate(lastNodePid, lastLinkPid));

		updataBranchs.addAll(handleOutLink(lastNodePid, lastLinkPid));

		for (RdBranch branch : updataBranchs) {
			
			if (delBranch.containsKey(branch.getPid())) {
				continue;
			}

			result.insertObject(branch, ObjStatus.UPDATE,
					branch.getPid());
		}
	}
	
	/**
	 * 处理与端点挂接的目标link做进入线的分歧
	 * 
	 * @throws Exception
	 */
	private List<RdBranch> handleInLinkUpdate(int nodePid,int linkPid ) throws Exception {

		Map<Integer, RdBranch> handleMap = new HashMap<Integer, RdBranch>();

		List<RdBranch> branchs = branchSelector
				.loadByLinkPid(linkPid, 1, true);

		for (RdBranch branch : branchs) {

			if (branch.getNodePid() != nodePid) {
				continue;
			}

			if (branch.getRelationshipType() == 2) {

				delBranch.put(branch.getPid(), branch);

				continue;
			}			

			if (delBranch.containsKey(branch.getPid())) {
				continue;
			}

			handleMap.put(branch.getPid(), branch);
		}

		List<RdBranch> updatabranchs = new ArrayList<RdBranch>();

		for (RdBranch branch : handleMap.values()) {

			RdLink rdLink = leftLinkMapping.get(branch.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				branch.changedFields().put("inLinkPid", rdLink.getPid());

				updatabranchs.add(branch);

				continue;
			}

			rdLink = rightLinkMapping.get(branch.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				branch.changedFields().put("inLinkPid", rdLink.getPid());

				updatabranchs.add(branch);
			}
		}
		
		return updatabranchs;
	}
	
	
	/**
	 * 处理与端点挂接的目标link做退出线的分歧
	 * 
	 * @throws Exception
	 */
	private List<RdBranch> handleOutLink(int nodePid, int linkPid)
			throws Exception {

		Map<Integer, RdBranch> handleMap = new HashMap<Integer, RdBranch>();

		List<RdBranch> branchs = branchSelector.loadByLinkPid(linkPid, 2, true);

		for (RdBranch branch : branchs) {

			if (branch.getRelationshipType() == 2 ) {

				delBranch.put(branch.getPid(), branch);

				continue;
			}

			if (branch.getNodePid() == nodePid
					|| isSameCross(branch.getNodePid(), nodePid)) {

				handleMap.put(branch.getPid(), branch);
			}
		}

		List<RdBranch> updatabranchs = new ArrayList<RdBranch>();

		for (RdBranch branch : handleMap.values()) {

			RdLink rdLink = leftLinkMapping.get(branch.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				branch.changedFields().put("outLinkPid", rdLink.getPid());

				updatabranchs.add(branch);

				continue;
			}

			rdLink = rightLinkMapping.get(branch.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				branch.changedFields().put("outLinkPid", rdLink.getPid());
				
				updatabranchs.add(branch);
			}
		}

		return updatabranchs;
	}
	
	/**
	 * 判断退出线的端点node是否与进入node为同一路口。
	 * @param inNodePid
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isSameCross(int inNodePid, int nodePid)
			throws Exception {
		
		if (crossNodeSelector == null) {
			return false;
		}

		List<Integer> nodePids = crossNodeSelector.getCrossNodePidByNode(
				nodePid);

		if (nodePids.contains(inNodePid) && nodePids.contains(nodePid)) {

			return true;
		}
		return false;
	}

}
