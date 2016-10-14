package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.depart;

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
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;

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


	private Map<Integer, RdVoiceguide> delVoiceguide = new HashMap<Integer, RdVoiceguide>();

	private Set<Integer> updateVoiceguide = new HashSet<Integer>();

	RdVoiceguideSelector voiceguideSelector = null;

	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		voiceguideSelector = new RdVoiceguideSelector(this.conn);
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

		for (RdVoiceguide voiceguide : delVoiceguide.values()) {

			if (updateVoiceguide.contains(voiceguide.getPid())) {

				continue;
			}

			result.insertObject(voiceguide, ObjStatus.DELETE,
					voiceguide.getPid());
		}
	}

	/**
	 * 处理目标link上的语音引导
	 * 
	 * @throws Exception
	 */
	private void handleTargetLinkDel() throws Exception {

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		// 目标link为进入线
		voiceguides.addAll(voiceguideSelector.loadByLinks(targetLinkPids,
				1, true));
		// 目标link为退出线
		voiceguides.addAll(voiceguideSelector.loadByLinks(targetLinkPids,
				2, true));
		// 目标link为经过线
		voiceguides.addAll(voiceguideSelector.loadByLinks(targetLinkPids,
				3, true));

		for (RdVoiceguide voiceguide : voiceguides) {

			delVoiceguide.put(voiceguide.getPid(), voiceguide);
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

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		// 端点改变的非目标link做进入线的语音引导
		voiceguides = voiceguideSelector.loadByLinks(changedNodeLinks, 1,
				true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (delVoiceguide.containsKey(voiceguide.getPid())) {

				continue;
			}

			// 进入点不是 制作上下线分离link的连接点
			if (!connectNodePid.contains(voiceguide.getNodePid())) {

				continue;
			}
			
			delVoiceguide.put(voiceguide.getPid(), voiceguide);

//			int newNodePid = getNewNodePid(voiceguide.getInLinkPid());
//
//			for (IRow rowDetail : voiceguide.getDetails()) {
//
//				List<Integer> connectLinkPids = new ArrayList<Integer>();
//
//				RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;
//
//				connectLinkPids.add(detail.getOutLinkPid());
//
//				for (IRow rowVia : detail.getVias()) {
//
//					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;
//
//					connectLinkPids.add(via.getLinkPid());
//				}
//
//				boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);
//
//				if (!isHave) {
//
//					delVoiceguide.put(voiceguide.getPid(), voiceguide);
//
//					break;
//				}
//			}
		}

		// 端点改变的非目标link做退出线的语音引导
		voiceguides = voiceguideSelector.loadByLinks(changedNodeLinks, 2,
				true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (delVoiceguide.containsKey(voiceguide.getPid())) {

				continue;
			}

			for (IRow rowDetail : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

				if (!changedNodeLinkMap.containsKey(detail.getOutLinkPid())) {
					continue;
				}

				int newNodePid = getNewNodePid(detail.getOutLinkPid());

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(voiceguide.getInLinkPid());

				for (IRow rowVia : detail.getVias()) {

					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

					connectLinkPids.add(via.getLinkPid());
				}

				// 退出线不与其他非目标link挂接
				if (!containNoTargetLink(connectLinkPids)) {
					continue;
				}

				boolean isHave = haveNewNodePid(connectLinkPids, newNodePid);

				if (!isHave) {

					delVoiceguide.put(voiceguide.getPid(), voiceguide);

					break;
				}
			}
		}

		// 端点改变的非目标link做经过线的语音引导
		voiceguides = voiceguideSelector.loadByLinks(changedNodeLinks, 3,
				true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (delVoiceguide.containsKey(voiceguide.getPid())) {

				continue;
			}

			boolean isHave = false;

			for (IRow rowDetail : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

				Set<Integer> allConnectLinkPids = new HashSet<Integer>();

				allConnectLinkPids.add(voiceguide.getInLinkPid());

				allConnectLinkPids.add(detail.getOutLinkPid());

				for (IRow rowVia : detail.getVias()) {

					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

					allConnectLinkPids.add(via.getLinkPid());
				}

				for (IRow rowVia : detail.getVias()) {

					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

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

					delVoiceguide.put(voiceguide.getPid(), voiceguide);

					break;
				}
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的语音引导
	 * 
	 * @throws Exception
	 */
	private void handleInLinkUpdate() throws Exception {

		Map<Integer, RdVoiceguide> handleMap = new HashMap<Integer, RdVoiceguide>();

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		voiceguides = voiceguideSelector.loadRdVoiceguideByLinkPid(preLinkPid, 1, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (voiceguide.getNodePid() == preNodePid) {

				handleMap.put(voiceguide.getPid(), voiceguide);
			}
		}

		voiceguides = voiceguideSelector.loadRdVoiceguideByLinkPid(lastLinkPid, 1, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (voiceguide.getNodePid() == lastNodePid) {

				handleMap.put(voiceguide.getPid(), voiceguide);
			}
		}

		for (RdVoiceguide voiceguide : handleMap.values()) {

			RdLink lRdLink = leftLinkMapping.get(voiceguide.getInLinkPid());

			if ((lRdLink.getsNodePid() == voiceguide.getNodePid() && lRdLink
					.getDirect() == 3)
					|| (lRdLink.geteNodePid() == voiceguide.getNodePid() && lRdLink
							.getDirect() == 2)) {
				voiceguide.changedFields()
						.put("inLinkPid", lRdLink.getPid());

				result.insertObject(voiceguide, ObjStatus.UPDATE,
						voiceguide.getPid());

				updateVoiceguide.add(voiceguide.getPid());
			}

			RdLink rRdLink = rightLinkMapping.get(voiceguide.getInLinkPid());

			if ((rRdLink.getsNodePid() == voiceguide.getNodePid() && rRdLink
					.getDirect() == 3)
					|| (rRdLink.geteNodePid() == voiceguide.getNodePid() && rRdLink
							.getDirect() == 2)) {
				
				voiceguide.changedFields()
						.put("inLinkPid", rRdLink.getPid());

				result.insertObject(voiceguide, ObjStatus.UPDATE,
						voiceguide.getPid());

				updateVoiceguide.add(voiceguide.getPid());
			}
		}
	}

	/**
	 * 处理与端点挂接的目标link做退出线的语音引导
	 * 
	 * @throws Exception
	 */
	private void handleOutLinkUpdate() throws Exception {

		handleOutLink(preLinkPid, preNodePid);

		handleOutLink(lastLinkPid, lastNodePid);
	}

	
	/**
	 * 处理与端点挂接的目标link做退出线的语音引导
	 * 
	 * @throws Exception
	 */
	private void handleOutLink(int linkPid, int nodePid) throws Exception {

		Map<Integer, RdVoiceguideDetail> handleMap = new HashMap<Integer, RdVoiceguideDetail>();

		List<RdVoiceguide> voiceguides = voiceguideSelector
				.loadRdVoiceguideByLinkPid(linkPid, 2, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			// 进入线、经过线是目标link或者是非目标link
			boolean isContain = false;

			RdVoiceguideDetail updateDetail = null;

			for (IRow rowDetail : voiceguide.getDetails()) {

				List<Integer> connectLinkPids = new ArrayList<Integer>();

				connectLinkPids.add(voiceguide.getInLinkPid());

				RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

				if (detail.getOutLinkPid() == linkPid) {

					updateDetail = detail;

				} else {
					connectLinkPids.add(detail.getOutLinkPid());

					
				}

				for (IRow rowVia : detail.getVias()) {

					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

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

			handleMap.put(updateDetail.getPid(), updateDetail);
		}

		if (!handleMap.isEmpty()) {

			updateDetail(handleMap, nodePid);
		}
	}

	private void updateDetail(Map<Integer, RdVoiceguideDetail> handleLastMap,
			int nodePid) {

		for (RdVoiceguideDetail detail : handleLastMap.values()) {

			RdLink rRdLink = rightLinkMapping.get(detail.getOutLinkPid());

			if ((rRdLink.getsNodePid() == nodePid && rRdLink.getDirect() == 2)
					|| (rRdLink.geteNodePid() == nodePid && rRdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rRdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getPid());

				updateVoiceguide.add(detail.getVoiceguidePid());
			}

			RdLink lRdLink = leftLinkMapping.get(detail.getOutLinkPid());

			if ((lRdLink.getsNodePid() == nodePid && lRdLink.getDirect() == 2)
					|| (lRdLink.geteNodePid() == nodePid && lRdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", lRdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getPid());

				updateVoiceguide.add(detail.getVoiceguidePid());
			}
		}
	}
}
