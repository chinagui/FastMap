package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.depart;

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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;

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

	private Map<Integer, RdRestriction> delRestriction = new HashMap<Integer, RdRestriction>();

	RdRestrictionSelector restrictionSelector = null;
	
	RdCrossNodeSelector crossNodeSelector = null;	
	
	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		restrictionSelector = new RdRestrictionSelector(this.conn);		
	
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
		
		for (RdRestriction restriction : delRestriction.values()) {

			result.insertObject(restriction, ObjStatus.DELETE,
					restriction.getPid());
		}
	}	
	
	/**
	 * 处理目标link为经过线的交限
	 * 
	 * @throws Exception
	 */
	private void handlePassLinkDel() throws Exception {

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		// 目标link为经过线
		restrictions.addAll(restrictionSelector.loadByLinks(targetLinkPids, 3,
				true));

		for (RdRestriction restriction : restrictions) {

			delRestriction.put(restriction.getPid(), restriction);
		}
	}
	
	/**
	 * 处理目标link连接点关联的交限
	 * 
	 * @throws Exception
	 */
	private void handleConnectNodeDel() throws Exception {
		
		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.addAll(connectNodePids);

		Set<Integer> pids = new HashSet<Integer>();

		// 经过点获取pid
		pids.addAll(restrictionSelector.getPidByPassNode(nodePids));

		// 进入点获取pid
		pids.addAll(restrictionSelector.getPidByInNode(nodePids));

		List<Integer> restrictionPids = new ArrayList<Integer>();

		restrictionPids.addAll(pids);

		List<IRow> rows = restrictionSelector.loadByIds(restrictionPids, true, true);

		for (IRow row : rows) {
			
			RdRestriction restriction = (RdRestriction) row;


			delRestriction.put(restriction.getPid(), restriction);
		}
		
		for (int nodePid : nodePids) {

			// 获取node关联link做为退出线的交限pid
			List<Integer> pidTmps = restrictionSelector.getPidByOutNode(nodePid);

			List<Integer> outPids = new ArrayList<>();

			for (int pid : pidTmps) {

				if (!delRestriction.containsKey(pid)) {

					outPids.add(pid);
				}
			}

			rows = restrictionSelector.loadByIds(outPids, true, true);

			for (IRow row : rows) {

				RdRestriction restriction = (RdRestriction) row;

				if (isSameCross(restriction.getNodePid(), nodePid)) {

					delRestriction.put(restriction.getPid(), restriction);
				}
			}
		}
	}
	
	
	
	/**
	 * 处理端点
	 * @throws Exception
	 */
	private void handleEndPoint() throws Exception {

		List<RdRestriction> updataRestrictions = new ArrayList<RdRestriction>();

		Map<Integer, List<RdRestrictionDetail>> updataTopologys = new HashMap<Integer, List<RdRestrictionDetail>>();

		updataRestrictions.addAll(handleInLinkUpdate(preNodePid, preLinkPid));

		updataTopologys.putAll(handleOutLink(preNodePid, preLinkPid));

		updataRestrictions.addAll(handleInLinkUpdate(lastNodePid, lastLinkPid));

		updataTopologys.putAll(handleOutLink(lastNodePid, lastLinkPid));

		for (RdRestriction restriction : updataRestrictions) {
			
			if (delRestriction.containsKey(restriction.getPid())) {
				continue;
			}

			result.insertObject(restriction, ObjStatus.UPDATE,
					restriction.getPid());
		}

		for (Map.Entry<Integer, List<RdRestrictionDetail>> entry : updataTopologys
				.entrySet()) {

			updateDetail(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * 处理与端点挂接的目标link做进入线的车信
	 * 
	 * @throws Exception
	 */
	private List<RdRestriction> handleInLinkUpdate(int nodePid, int linkPid)
			throws Exception {

		Map<Integer, RdRestriction> handleMap = new HashMap<Integer, RdRestriction>();

		List<RdRestriction> restrictions = restrictionSelector.loadByLink(
				linkPid, 1, true);

		for (RdRestriction restriction : restrictions) {

			if (restriction.getNodePid() != nodePid) {
				continue;
			}

			if (!isCrossNode(nodePid)) {

				delRestriction.put(restriction.getPid(), restriction);

				continue;
			}

			for (IRow row : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) row;

				if (detail.getRelationshipType() == 2) {

					delRestriction.put(restriction.getPid(), restriction);

					break;
				}
			}

			if (delRestriction.containsKey(restriction.getPid())) {
				continue;
			}

			handleMap.put(restriction.getPid(), restriction);
		}

		List<RdRestriction> updataRestrictions = new ArrayList<RdRestriction>();

		for (RdRestriction restriction : handleMap.values()) {

			RdLink rdLink = leftLinkMapping.get(restriction.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				restriction.changedFields().put("inLinkPid", rdLink.getPid());

				updataRestrictions.add(restriction);

				continue;
			}

			rdLink = rightLinkMapping.get(restriction.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				restriction.changedFields().put("inLinkPid", rdLink.getPid());

				updataRestrictions.add(restriction);
			}
		}

		return updataRestrictions;
	}
	
	
	/**
	 * 处理与端点挂接的目标link做退出线的交限
	 * 
	 * @throws Exception
	 */
	private Map<Integer, List<RdRestrictionDetail>> handleOutLink( int nodePid,int linkPid) throws Exception {

		Map<Integer, List<RdRestrictionDetail>> handleMap = new HashMap<Integer, List<RdRestrictionDetail>>();
		
		List<RdRestrictionDetail> details = new ArrayList<RdRestrictionDetail>();
		
		handleMap.put(nodePid, details);
		
		List<RdRestriction> restrictions = restrictionSelector
				.loadByLink(linkPid, 2, true);

		for (RdRestriction restriction : restrictions) {
			
			RdRestrictionDetail updateDetail = null;
			
			for (IRow row : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) row;
				
				if (detail.getRelationshipType() == 2) {

					delRestriction.put(restriction.getPid(), restriction);

					break;
				}
				
				if (detail.getOutLinkPid() == linkPid) {

					boolean sameCrossNode = isSameCross(
							restriction.getNodePid(), nodePid);

					if (sameCrossNode) {

						updateDetail = detail;
					}
				}
			}

			if (delRestriction.containsKey(restriction.getPid())) {
				continue;
			}
			if (updateDetail != null) {
				
				handleMap.get(nodePid).add(updateDetail);
			}		
		}
		
		return handleMap;
	}
	
	/**
	 * 更新Topology退出线
	 */
	private void updateDetail(List<RdRestrictionDetail> details, int nodePid) {

		if (details == null || details.isEmpty()) {

			return;
		}

		for (RdRestrictionDetail detail : details) {

			if (delRestriction.containsKey(detail.getRestricPid())) {

				continue;
			}

			RdLink rdLink = leftLinkMapping.get(detail.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getRestricPid());

				continue;
			}

			rdLink = rightLinkMapping.get(detail.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getRestricPid());

			}
		}
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
