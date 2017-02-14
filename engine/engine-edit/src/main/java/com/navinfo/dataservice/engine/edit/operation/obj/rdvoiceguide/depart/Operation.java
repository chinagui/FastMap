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
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
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



	// 连接Node
	private Set<Integer> connectNodePids = new HashSet<Integer>();

	// 目标linkPid
	private List<Integer> targetLinkPids = new ArrayList<Integer>();	


	private Map<Integer, RdVoiceguide> delVoiceguide = new HashMap<Integer, RdVoiceguide>();

	

	RdVoiceguideSelector voiceguideSelector = null;
	
	RdCrossNodeSelector crossNodeSelector = null;	

	public Operation(Connection conn, int preNodePid, int lastNodePid,
			Map<Integer, RdLink> noTargetLinks, List<RdLink> targetLinks) {

		this.conn = conn;

		this.preNodePid = preNodePid;

		this.lastNodePid = lastNodePid;

		init(noTargetLinks, targetLinks);

		voiceguideSelector = new RdVoiceguideSelector(this.conn);
		
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

		for (RdVoiceguide voiceguide : delVoiceguide.values()) {

			result.insertObject(voiceguide, ObjStatus.DELETE,
					voiceguide.getPid());
		}
	}

	/**
	 * 处理目标link上的语音引导
	 * 
	 * @throws Exception
	 */
	private void handlePassLinkDel() throws Exception {

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		// 目标link为经过线
		voiceguides.addAll(voiceguideSelector.loadByLinks(targetLinkPids, 3,
				true));

		for (RdVoiceguide voiceguide : voiceguides) {

			delVoiceguide.put(voiceguide.getPid(), voiceguide);
		}
	}

	/**
	 * 处理目标link连接点关联的语音引导
	 * 
	 * @throws Exception
	 */
	private void handleConnectNodeDel() throws Exception {

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.addAll(connectNodePids);

		Set<Integer> pids = new HashSet<Integer>();

		// 经过点获取pid
		pids.addAll(voiceguideSelector.getPidByPassNode(nodePids));

		// 进入点获取pid
		pids.addAll(voiceguideSelector.getPidByInNode(nodePids));

		List<Integer> voiceguidePids = new ArrayList<Integer>();

		voiceguidePids.addAll(pids);

		List<IRow> rows = voiceguideSelector.loadByIds(voiceguidePids, true,
				true);

		for (IRow row : rows) {

			RdVoiceguide voiceguide = (RdVoiceguide) row;

			delVoiceguide.put(voiceguide.getPid(), voiceguide);
		}

		for (int nodePid : nodePids) {

			// 获取node关联link做为退出线的语音引导pid
			List<Integer> pidTmps = voiceguideSelector.getPidByOutNode(nodePid);

			List<Integer> outPids = new ArrayList<>();

			for (int pid : pidTmps) {

				if (!delVoiceguide.containsKey(pid)) {

					outPids.add(pid);
				}
			}

			rows = voiceguideSelector.loadByIds(outPids, true, true);

			for (IRow row : rows) {

				RdVoiceguide voiceguide = (RdVoiceguide) row;

				if (isSameCross(voiceguide.getNodePid(), nodePid)) {

					delVoiceguide.put(voiceguide.getPid(), voiceguide);
				}
			}
		}
	}

	/**
	 * 处理端点
	 * 
	 * @throws Exception
	 */
	private void handleEndPoint() throws Exception {

		List<RdVoiceguide> updataVoiceguides = new ArrayList<RdVoiceguide>();

		Map<Integer, List<RdVoiceguideDetail>> updataDetails = new HashMap<Integer, List<RdVoiceguideDetail>>();

		updataVoiceguides.addAll(handleInLinkUpdate(preNodePid, preLinkPid));

		updataDetails.putAll(handleOutLink(preNodePid, preLinkPid));

		updataVoiceguides.addAll(handleInLinkUpdate(lastNodePid, lastLinkPid));

		updataDetails.putAll(handleOutLink(lastNodePid, lastLinkPid));

		for (RdVoiceguide voiceguide : updataVoiceguides) {

			if (delVoiceguide.containsKey(voiceguide.getPid())) {
				continue;
			}

			result.insertObject(voiceguide, ObjStatus.UPDATE,
					voiceguide.getPid());
		}

		for (Map.Entry<Integer, List<RdVoiceguideDetail>> entry : updataDetails
				.entrySet()) {

			updateDetail(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * 处理与端点挂接的目标link做进入线的车信
	 * 
	 * @throws Exception
	 */
	private List<RdVoiceguide> handleInLinkUpdate(int nodePid, int linkPid)
			throws Exception {

		Map<Integer, RdVoiceguide> handleMap = new HashMap<Integer, RdVoiceguide>();

		List<RdVoiceguide> voiceguides = voiceguideSelector
				.loadRdVoiceguideByLinkPid(linkPid, 1, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			if (voiceguide.getNodePid() != nodePid) {
				continue;
			}

			if (!isCrossNode(nodePid)) {

				delVoiceguide.put(voiceguide.getPid(), voiceguide);

				continue;
			}

			for (IRow row : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) row;

				if (detail.getRelationshipType() == 2) {

					delVoiceguide.put(voiceguide.getPid(), voiceguide);

					break;
				}
			}

			if (delVoiceguide.containsKey(voiceguide.getPid())) {
				continue;
			}

			handleMap.put(voiceguide.getPid(), voiceguide);
		}

		List<RdVoiceguide> updataVoiceguides = new ArrayList<RdVoiceguide>();

		for (RdVoiceguide voiceguide : handleMap.values()) {

			RdLink rdLink = leftLinkMapping.get(voiceguide.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				voiceguide.changedFields().put("inLinkPid", rdLink.getPid());

				updataVoiceguides.add(voiceguide);

				continue;
			}

			rdLink = rightLinkMapping.get(voiceguide.getInLinkPid());

			if ((rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 3)) {

				voiceguide.changedFields().put("inLinkPid", rdLink.getPid());

				updataVoiceguides.add(voiceguide);
			}
		}

		return updataVoiceguides;
	}

	/**
	 * 处理与端点挂接的目标link做退出线的语音引导
	 * 
	 * @throws Exception
	 */
	private Map<Integer, List<RdVoiceguideDetail>> handleOutLink(int nodePid,
			int linkPid) throws Exception {

		Map<Integer, List<RdVoiceguideDetail>> handleMap = new HashMap<Integer, List<RdVoiceguideDetail>>();

		List<RdVoiceguideDetail> details = new ArrayList<RdVoiceguideDetail>();

		handleMap.put(nodePid, details);

		List<RdVoiceguide> voiceguides = voiceguideSelector
				.loadRdVoiceguideByLinkPid(linkPid, 2, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			RdVoiceguideDetail updateDetail = null;

			for (IRow row : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) row;

				if (detail.getRelationshipType() == 2) {

					delVoiceguide.put(voiceguide.getPid(), voiceguide);

					break;
				}

				if (detail.getOutLinkPid() == linkPid) {

					boolean sameCrossNode = isSameCross(
							voiceguide.getNodePid(), nodePid);

					if (sameCrossNode) {

						updateDetail = detail;
					}
				}
			}

			if (delVoiceguide.containsKey(voiceguide.getPid())) {
				continue;
			}
			if (updateDetail != null) {

				handleMap.get(nodePid).add(updateDetail);
			}
		}

		return handleMap;
	}

	/**
	 * 更新Detail退出线
	 */
	private void updateDetail(List<RdVoiceguideDetail> details, int nodePid) {

		if (details == null || details.isEmpty()) {

			return;
		}

		for (RdVoiceguideDetail detail : details) {

			if (delVoiceguide.containsKey(detail.getVoiceguidePid())) {

				continue;
			}

			RdLink rdLink = leftLinkMapping.get(detail.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getVoiceguidePid());

				continue;
			}

			rdLink = rightLinkMapping.get(detail.getOutLinkPid());

			if ((rdLink.getsNodePid() == nodePid && rdLink.getDirect() == 2)
					|| (rdLink.geteNodePid() == nodePid && rdLink.getDirect() == 3)) {

				detail.changedFields().put("outLinkPid", rdLink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getVoiceguidePid());

			}
		}
	}

	/**
	 * 判断退出线的端点node是否与进入node为同一路口。
	 * 
	 * @param inNodePid
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isSameCross(int inNodePid, int nodePid) throws Exception {

		if (crossNodeSelector == null) {
			return false;
		}

		List<Integer> nodePids = crossNodeSelector
				.getCrossNodePidByNode(nodePid);

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

		crossNodeSelector = new RdCrossNodeSelector(this.conn);

		List<Integer> nodePids = crossNodeSelector
				.getCrossNodePidByNode(nodePid);

		if (nodePids.size() > 0) {

			return true;
		}
		return false;
	}
}

