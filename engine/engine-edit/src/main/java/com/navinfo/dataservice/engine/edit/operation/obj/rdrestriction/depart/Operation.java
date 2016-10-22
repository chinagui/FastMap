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
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
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

	// 端口改变的非目标link映射
	private Map<Integer, RdLink> changedNodeLinkMap = new HashMap<Integer, RdLink>();

	// 非目标link映射
	private Map<Integer, RdLink> noTargetLinkMap = new HashMap<Integer, RdLink>();

	// 连接Node
	private Set<Integer> connectNodePid = new HashSet<Integer>();

	// 目标linkPid
	private List<Integer> targetLinkPids = new ArrayList<Integer>();

	private Map<Integer, RdRestriction> delRestriction = new HashMap<Integer, RdRestriction>();

	private Set<Integer> updateRestriction = new HashSet<Integer>();

	RdRestrictionSelector restrictionSelector = null;
	
	
	RdLinkSelector linkSelector = null;
	
	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		restrictionSelector = new RdRestrictionSelector(this.conn);
		
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

		for (RdRestriction restriction : delRestriction.values()) {

			if (updateRestriction.contains(restriction.getPid())) {

				continue;
			}

			result.insertObject(restriction, ObjStatus.DELETE,
					restriction.getPid());
		}
	}

	/**
	 * 处理目标link上的交限
	 * 
	 * @throws Exception
	 */
	private void handleTargetLinkDel() throws Exception {

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		// 目标link为进入线
		restrictions.addAll(restrictionSelector.loadByLinks(targetLinkPids, 1,
				true));
		// 目标link为退出线
		restrictions.addAll(restrictionSelector.loadByLinks(targetLinkPids, 2,
				true));
		// 目标link为经过线
		restrictions.addAll(restrictionSelector.loadByLinks(targetLinkPids, 3,
				true));

		for (RdRestriction restriction : restrictions) {

			delRestriction.put(restriction.getPid(), restriction);
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

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		// 端点改变的非目标link做进入线的交限
		restrictions = restrictionSelector.loadByLinks(changedNodeLinks, 1,
				true);

		for (RdRestriction restriction : restrictions) {

			if (delRestriction.containsKey(restriction.getPid())) {

				continue;
			}

			// 进入点不是 制作上下线分离link的连接点
			if (!connectNodePid.contains(restriction.getNodePid())) {

				continue;
			}

			delRestriction.put(restriction.getPid(), restriction);

			// int newNodePid = getNewNodePid(restriction.getInLinkPid());
			//
			// for (IRow rowDetail : restriction.getDetails()) {
			//
			// List<Integer> connectLinkPids = new ArrayList<Integer>();
			//
			// RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;
			//
			// connectLinkPids.add(detail.getOutLinkPid());
			//
			// for (IRow rowVia : detail.getVias()) {
			//
			// RdRestrictionVia via = (RdRestrictionVia) rowVia;
			//
			// connectLinkPids.add(via.getLinkPid());
			// }
			//
			// boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);
			//
			// if (!isHave) {
			//
			// delRestriction.put(restriction.getPid(), restriction);
			//
			// break;
			// }
			// }
		}

		// 端点改变的非目标link做退出线的交限
		restrictions = restrictionSelector.loadByLinks(changedNodeLinks, 2,
				true);

		for (RdRestriction restriction : restrictions) {

			if (delRestriction.containsKey(restriction.getPid())) {

				continue;
			}

			for (IRow rowDetail : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

				if (!changedNodeLinkMap.containsKey(detail.getOutLinkPid())) {
					continue;
				}

				int newNodePid = getNewNodePid(detail.getOutLinkPid());

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(restriction.getInLinkPid());

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

					connectLinkPids.add(via.getLinkPid());
				}

				// 退出线不与其他非目标link挂接
				if (!containNoTargetLink(connectLinkPids)) {
					continue;
				}

				boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);

				if (!isHave) {

					delRestriction.put(restriction.getPid(), restriction);

					break;
				}
			}
		}

		// 端点改变的非目标link做经过线的交限
		restrictions = restrictionSelector.loadByLinks(changedNodeLinks, 3,
				true);

		for (RdRestriction restriction : restrictions) {

			if (delRestriction.containsKey(restriction.getPid())) {

				continue;
			}

			boolean isHave = false;

			for (IRow rowDetail : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

				Set<Integer> allConnectLinkPids = new HashSet<Integer>();

				allConnectLinkPids.add(restriction.getInLinkPid());

				allConnectLinkPids.add(detail.getOutLinkPid());

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

					allConnectLinkPids.add(via.getLinkPid());
				}

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

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

					delRestriction.put(restriction.getPid(), restriction);

					break;
				}
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的交限
	 * 
	 * @throws Exception
	 */
	private void handleInLinkUpdate() throws Exception {

		Map<Integer, RdRestriction> handleMap = new HashMap<Integer, RdRestriction>();

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		restrictions = restrictionSelector.loadByLink(preLinkPid, 1, true);

		for (RdRestriction restriction : restrictions) {

			if (restriction.getNodePid() == preNodePid) {

				handleMap.put(restriction.getPid(), restriction);
			}
		}

		restrictions = restrictionSelector.loadByLink(lastLinkPid, 1, true);

		for (RdRestriction restriction : restrictions) {

			if (restriction.getNodePid() == lastNodePid) {

				handleMap.put(restriction.getPid(), restriction);
			}
		}

		for (RdRestriction restriction : handleMap.values()) {

			RdLink lRdLink = leftLinkMapping.get(restriction.getInLinkPid());

			if ((lRdLink.getsNodePid() == restriction.getNodePid() && lRdLink
					.getDirect() == 3)
					|| (lRdLink.geteNodePid() == restriction.getNodePid() && lRdLink
							.getDirect() == 2)) {

				restriction.changedFields().put("inLinkPid", lRdLink.getPid());

				result.insertObject(restriction, ObjStatus.UPDATE,
						restriction.getPid());

				updateRestriction.add(restriction.getPid());
			}

			RdLink rRdLink = rightLinkMapping.get(restriction.getInLinkPid());

			if ((rRdLink.getsNodePid() == restriction.getNodePid() && rRdLink
					.getDirect() == 3)
					|| (rRdLink.geteNodePid() == restriction.getNodePid() && rRdLink
							.getDirect() == 2)) {

				restriction.changedFields().put("inLinkPid", rRdLink.getPid());

				result.insertObject(restriction, ObjStatus.UPDATE,
						restriction.getPid());

				updateRestriction.add(restriction.getPid());
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做退出线的交限
	 * 
	 * @throws Exception
	 */
	private void handleOutLinkUpdate() throws Exception {

		handleOutLink(preLinkPid, preNodePid);

		handleOutLink(lastLinkPid, lastNodePid);
	}

	/**
	 * 处理与端点挂接的目标link做退出线的交限
	 * 
	 * @throws Exception
	 */
	private void handleOutLink(int linkPid, int nodePid) throws Exception {

		Map<Integer, RdRestrictionDetail> handleMap = new HashMap<Integer, RdRestrictionDetail>();

		List<RdRestriction> restrictions = restrictionSelector.loadByLink(
				linkPid, 2, true);

		for (RdRestriction restriction : restrictions) {

			// 进入线、经过线是目标link或者是非目标link
			boolean isContain = false;

			RdRestrictionDetail updateDetail = null;

			for (IRow rowDetail : restriction.getDetails()) {

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(restriction.getInLinkPid());

				RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

				if (detail.getOutLinkPid() == linkPid) {

					boolean isOutLinkInNode = isOutLinkInNode(restriction,
							detail, nodePid);

					if (isOutLinkInNode) {

						updateDetail = detail;
					}

				} else {
					connectLinkPids.add(detail.getOutLinkPid());
				}

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

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
			if (updateDetail != null) {
				handleMap.put(updateDetail.getPid(), updateDetail);
			}
		}

		if (!handleMap.isEmpty()) {

			updateDetail(handleMap, nodePid);
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
	private boolean isOutLinkInNode(RdRestriction restriction,
			RdRestrictionDetail detail, int nodePid) throws Exception {
		if (detail.getVias().size() == 0) {

			if (restriction.getNodePid() != nodePid) {

				return false;
			} else {
				return true;
			}
		}

		List<Integer> viaPids = new ArrayList<Integer>();

		for (IRow rowVia : detail.getVias()) {

			RdRestrictionVia via = (RdRestrictionVia) rowVia;

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

	private void updateDetail(Map<Integer, RdRestrictionDetail> handleLastMap,
			int nodePid) {

		for (RdRestrictionDetail detail : handleLastMap.values()) {

			RdLink rRdLink = rightLinkMapping.get(detail.getOutLinkPid());

			if ((rRdLink.getsNodePid() == nodePid && rRdLink.getDirect() == 2)
					|| (rRdLink.geteNodePid() == nodePid && rRdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rRdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE, detail.getPid());

				updateRestriction.add(detail.getRestricPid());
			}

			RdLink lRdLink = leftLinkMapping.get(detail.getOutLinkPid());

			if ((lRdLink.getsNodePid() == nodePid && lRdLink.getDirect() == 2)
					|| (lRdLink.geteNodePid() == nodePid && lRdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", lRdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE, detail.getPid());

				updateRestriction.add(detail.getRestricPid());
			}
		}
	}
}
