package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		List<RdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);

		List<Integer> linkPids = new ArrayList<Integer>();

		for (RdLink link : links) {
			linkPids.add(link.getPid());
		}

		this.getCommand().setLinks(links);

		this.getCommand().setLinkPids(linkPids);
	}

	public void lockRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.getConn());

		RdNode node = (RdNode) selector.loadById(this.getCommand().getNodePid(), true);

		this.getCommand().setNode(node);

	}

	// 锁定盲端节点
	public void lockEndRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.getConn());

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.add(this.getCommand().getNodePid());

		List<RdNode> nodes = new ArrayList<RdNode>();

		for (Integer linkPid : this.getCommand().getLinkPids()) {

			List<RdNode> list = selector.loadEndRdNodeByLinkPid(linkPid, true);

			for (RdNode node : list) {
				int nodePid = node.getPid();

				if (nodePids.contains(nodePid)) {
					continue;
				}

				nodePids.add(node.getPid());

				nodes.add(node);
			}

		}

		nodes.add(this.getCommand().getNode());

		this.getCommand().setNodes(nodes);

		this.getCommand().setNodePids(nodePids);
	}

	public void lockRdBranch() throws Exception {

		List<RdBranch> branchList = new ArrayList<>();
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			RdBranchSelector selector = new RdBranchSelector(this.getConn());

			List<RdBranch> branches = selector.loadRdBranchByInLinkPid(linkPid, true);

			List<RdBranch> viaBranch = selector.loadByLinkPid(linkPid, 3, true);

			// 获取退出线为该link，并且只有一根退出线的车信
			List<RdBranch> branches2 = selector.loadRdBranchByOutLinkPid(linkPid, true);

			branchList.addAll(branches2);

			branchList.addAll(viaBranch);

			branchList.addAll(branches);
		}

		this.getCommand().setBranches(branchList);
	}

	public void lockRdCross() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(this.getCommand().getNodePids(),
				this.getCommand().getLinkPids(), true);

		this.getCommand().setCrosses(crosses);
	}

	public void lockRdSpeedlimits() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.getConn());

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPids(this.getCommand().getLinkPids(), true);

		this.getCommand().setLimits(limits);
	}

	private void lockAdAdmin() throws Exception {
		AdAdminSelector selector = new AdAdminSelector(this.getConn());

		List<AdAdmin> adAdminList = selector.loadRowsByLinkPids(this.getCommand().getLinkPids(), true);

		this.getCommand().setAdAdmins(adAdminList);
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该rdnode对象
		lockRdNode();

		if (this.getCommand().getNode() == null) {

			throw new Exception("指定删除的RDNODE不存在！");
		}

		lockRdLink();

		lockEndRdNode();

		lockRdBranch();

		lockRdCross();

		lockRdSpeedlimits();

		lockAdAdmin();

		return true;
	}

	@Override
	public String run() throws Exception {

		try {
			if (!this.getCommand().isCheckInfect()) {
				getConn().setAutoCommit(false);
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				prepareData();

				IOperation op = new OpTopo(this.getCommand());
				op.run(this.getResult());

				updataRelationObj();

				recordData();

				postCheck();

				getConn().commit();
			} else {

				Map<String, List<AlertObject>> infects = confirmRelationObj();

				this.getConn().commit();

				return JSONObject.fromObject(infects).toString();
			}

		} catch (Exception e) {

			getConn().rollback();

			throw e;
		} finally {
			try {
				getConn().close();
			} catch (Exception e) {

			}
		}

		return null;
	}

	@Override
	public String exeOperation() {
		return null;
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj() throws Exception {
		// 交限
		IOperation opRefRestrict = new OpRefRestrict(this.getCommand(), this.getConn());
		opRefRestrict.run(this.getResult());

		// 分歧
		IOperation opRefBranch = new OpRefBranch(this.getCommand());
		opRefBranch.run(this.getResult());

		// 路口
		IOperation opRefCross = new OpRefCross(this.getCommand());
		opRefCross.run(this.getResult());

		// 车信
		IOperation opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand(), this.getConn());
		opRefLaneConnexity.run(this.getResult());

		// 限速
		IOperation opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
		opRefSpeedlimit.run(this.getResult());

		// 立交
		IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
		opRefRdGsc.run(this.getResult());

		// 行政区划
		IOperation opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
		opRefAdAdmin.run(this.getResult());

		// 电子眼
		OpRefRdElectroniceye opRefRdElectroniceye = new OpRefRdElectroniceye(this.getConn(), this.getCommand());
		opRefRdElectroniceye.run(this.getResult());

		// 信号灯
		OpRefTrafficsignal opRefRdTrafficsignal = new OpRefTrafficsignal(this.getConn());
		opRefRdTrafficsignal.run(this.getResult(), this.getCommand().getLinkPids());

		// 大门
		OpRefRdGate opRefRdGate = new OpRefRdGate(this.getConn());
		opRefRdGate.run(this.getResult(), this.getCommand());

		// 分岔路提示
		OpRefRdSe opRefRdSe = new OpRefRdSe(this.getConn(), this.getCommand());
		opRefRdSe.run(this.getResult());

		// 减速带
		OpRefRdSpeedbump opRdSpeedbump = new OpRefRdSpeedbump(this.getCommand(), this.getConn());
		opRdSpeedbump.run(this.getResult());

		// 坡度
		OpRefRdSlope opRefRdSlope = new OpRefRdSlope(this.getConn());
		opRefRdSlope.run(this.getResult(), this.getCommand());

		// CRF交叉点
		OpRefRdInter opRefRdInter = new OpRefRdInter(this.getConn());
		opRefRdInter.run(this.getResult(), this.getCommand());

		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand());

		// 收费站
		OpRefRdTollgate opRefRdTollgate = new OpRefRdTollgate(this.getConn(), this.getCommand());
		opRefRdTollgate.run(this.getResult());

		OpRefRelationObj opRefRelationObj = new OpRefRelationObj(this.getConn());
		// 顺行
		opRefRelationObj.handleDirectroute(this.getResult(), this.getCommand());

		// CRF道路
		opRefRelationObj.handleRdroad(this.getResult(), this.getCommand());

		// 警示信息
		opRefRelationObj.handleWarninginfo(this.getResult(), this.getCommand());

		// 语音引导
		opRefRelationObj.handleVoiceguide(this.getResult(), this.getCommand());

		// 同一线
		opRefRelationObj.handleSameLink(this.getResult(), this.getCommand());
	}

	/**
	 * 删除node影响到的关联要素
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<AlertObject>> confirmRelationObj() throws Exception {

		Map<String, List<AlertObject>> infects = new HashMap<String, List<AlertObject>>();

		// 检查是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该rdnode对象
		lockRdNode();

		lockRdLink();

		lockEndRdNode();

		if (this.getCommand().getNode() == null) {

			throw new Exception("指定删除的RDNODE不存在！");
		}

		Connection conn = getConn();

		List<RdLink> links = this.getCommand().getLinks();

		// 行政区划代表点
		com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation adadminOperation = new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation(
				null, null);
		List<AlertObject> adminAlertDataList = new ArrayList<>();
		for (RdLink link : links) {
			int linkPid = link.getPid();
			adminAlertDataList.addAll(adadminOperation.getUpdateAdminInfectData(linkPid, conn));

		}
		if (CollectionUtils.isNotEmpty(adminAlertDataList)) {
			infects.put("维护link关联的行政区划信息", adminAlertDataList);
		}
		// link
		com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.OpTopo opTopo = new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.OpTopo(
				null);
		List<AlertObject> linkAlertDataList = new ArrayList<>();
		for (RdLink link : links) {
			linkAlertDataList.addAll(opTopo.getDeleteLinkInfectData(link, conn));
		}
		if (CollectionUtils.isNotEmpty(linkAlertDataList)) {
			infects.put("删除Link", linkAlertDataList);
		}
		// node
		OpTopo nodeTOpo = new OpTopo();
		List<AlertObject> nodeAlertDataList = nodeTOpo.getDeleteNodeInfectData(this.getCommand().getNodePids(), conn);
		if (CollectionUtils.isNotEmpty(nodeAlertDataList)) {
			infects.put("删除Node", nodeAlertDataList);
		}

		// 交限
		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation rdrestrictionOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation(
				conn);
		List<AlertObject> updateResAlertDataList = new ArrayList<>();
		List<AlertObject> delResAlertDataList = new ArrayList<>();

		delResAlertDataList.addAll(rdrestrictionOperation.getDeleteLinkResInfectData(this.getCommand().getLinkPids()));
		updateResAlertDataList.addAll(rdrestrictionOperation.getUpdateResInfectData(this.getCommand().getLinkPids()));
		if (CollectionUtils.isNotEmpty(updateResAlertDataList)) {
			infects.put("此link上存在交限关系信息，删除该Link会对应删除此组关系", updateResAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(delResAlertDataList)) {
			infects.put("此link上存在交限关系信息，删除该Link会对应删除该交限", delResAlertDataList);
		}

		// 车信
		List<AlertObject> updateRdLaneConAlertDataList = new ArrayList<>();
		List<AlertObject> delRdLaneConAlertDataList = new ArrayList<>();

		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation rdLaneConOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation(
				conn);
		updateRdLaneConAlertDataList.addAll(rdLaneConOperation.getUpdateResInfectData(this.getCommand().getLinkPids()));
		delRdLaneConAlertDataList
				.addAll(rdLaneConOperation.getDeleteRdLaneConnexityInfectData(this.getCommand().getLinkPids()));
		if (CollectionUtils.isNotEmpty(updateRdLaneConAlertDataList)) {
			infects.put("此link上存在车信关系信息，删除该Link会对应删除此组关系", updateRdLaneConAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(delRdLaneConAlertDataList)) {
			infects.put("此link上存在车信关系信息，删除该Link会对应删除该车信", delRdLaneConAlertDataList);
		}
		// 分歧
		List<AlertObject> delInRdBranchAlertDataList = new ArrayList<>();
		List<AlertObject> delOutRdBranchAlertDataList = new ArrayList<>();
		List<AlertObject> delViaRdBranchAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation rdBranchOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation(
				null, null, null);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			delInRdBranchAlertDataList.addAll(rdBranchOperation.getDeleteBranchInfectData(linkPid, conn));
			delOutRdBranchAlertDataList.addAll(rdBranchOperation.getDeleteBOutLinkranchInfectData(linkPid, conn));
			delViaRdBranchAlertDataList.addAll(rdBranchOperation.getDeleteBViaLinkranchInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(delInRdBranchAlertDataList)) {
			infects.put("删除link作为进入线的分歧信息", delInRdBranchAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(delOutRdBranchAlertDataList)) {
			infects.put("删除link作为退出线的分歧信息", delOutRdBranchAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(delViaRdBranchAlertDataList)) {
			infects.put("删除link作为经过线的分歧信息", delViaRdBranchAlertDataList);
		}

		// 路口
		List<AlertObject> delCrossAlertDataList = new ArrayList<>();
		List<AlertObject> updateCrossAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.OpTopo rdCrossOperation = new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.OpTopo(
				null, null);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			delCrossAlertDataList.addAll(rdCrossOperation.getDeleteRdCross(linkPid, conn));
			updateCrossAlertDataList.addAll(rdCrossOperation.getUpdateRdCross(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(delCrossAlertDataList)) {
			infects.put("删除link,删除路口关系", delCrossAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(updateCrossAlertDataList)) {
			infects.put("删除link,维护路口关系", updateCrossAlertDataList);
		}

		// 立交
		List<AlertObject> delGscAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation rdGscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				conn);
		delGscAlertDataList.addAll(rdGscOperation.getDeleteRdGscInfectData(links));
		if (CollectionUtils.isNotEmpty(delGscAlertDataList)) {
			infects.put("删除link维护立交", delGscAlertDataList);
		}
		// 限速关系
		List<AlertObject> updateSpeedLimitAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation rdSpeedLimitOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation(
				null, null);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			updateSpeedLimitAlertDataList.addAll(rdSpeedLimitOperation.getUpdateRdSpeedLimitInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(updateSpeedLimitAlertDataList)) {
			infects.put("删除link维护限速关系", updateSpeedLimitAlertDataList);
		}
		// 电子眼
		List<AlertObject> delRdEyeAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation rdEyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			delRdEyeAlertDataList.addAll(rdEyeOperation.getUpdateRdEyeInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(delRdEyeAlertDataList)) {
			infects.put("删除link维护电子眼", delRdEyeAlertDataList);
		}
		// 大门
		List<AlertObject> delRdGateAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation rdGateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			delRdGateAlertDataList.addAll(rdGateOperation.getDeleteRdGateInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(delRdGateAlertDataList)) {
			infects.put("删除link删除大门", delRdGateAlertDataList);
		}
		// CRF交叉点
		List<AlertObject> updateRdInterAlertDataList = new ArrayList<>();
		List<AlertObject> delRdInterAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation rdInterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			updateRdInterAlertDataList.addAll(rdInterOperation.getUpdateRdInterInfectData(linkPid, conn));
			delRdInterAlertDataList.addAll(rdInterOperation.getDeleteRdInterInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(updateRdInterAlertDataList)) {
			infects.put("删除link维护CRF交叉点", updateRdInterAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(delRdInterAlertDataList)) {
			infects.put("删除link删除CRF交叉点", delRdInterAlertDataList);
		}
		// CRF Road
		List<AlertObject> updateRdRoadAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			updateRdRoadAlertDataList.addAll(rdRoadOperation.getUpdateRdRoadInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(updateRdRoadAlertDataList)) {
			infects.put("删除link维护CRF道路", updateRdRoadAlertDataList);
		}
		// CRF对象 TODO

		// 详细车道
		List<AlertObject> delRdLaneAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation rdLaneOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			delRdLaneAlertDataList.addAll(rdLaneOperation.getDeleteRdLaneInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(delRdLaneAlertDataList)) {
			infects.put("删除link删除详细车道", delRdLaneAlertDataList);
		}
		// 同一点
		List<AlertObject> sameNodeAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
				conn);
		sameNodeAlertDataList.addAll(
				sameNodeOperation.getDeleteLinkSameNodeInfectData(this.getCommand().getNodePids(), "RD_NODE", conn));
		if (CollectionUtils.isNotEmpty(sameNodeAlertDataList)) {
			infects.put("删除link影响的同一点", sameNodeAlertDataList);
		}

		// 同一线
		List<AlertObject> sameLinkAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation sameLinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);
		for (RdLink link : links) {
			sameLinkAlertDataList.addAll(sameLinkOperation.getDeleteLinkSameLinkInfectData(link, conn));
		}
		if (CollectionUtils.isNotEmpty(sameLinkAlertDataList)) {
			infects.put("删除link影响的同一线", sameLinkAlertDataList);
		}
		// 分叉口提示
		List<AlertObject> rdSeAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation rdSeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			rdSeAlertDataList.addAll(rdSeOperation.getDeleteRdSeInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(rdSeAlertDataList)) {
			infects.put("删除link删除分叉口", rdSeAlertDataList);
		}
		// 坡度
		List<AlertObject> rdSlopDeleteAlertDataList = new ArrayList<>();
		List<AlertObject> rdSlopUpdateAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation rdSlopOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			rdSlopDeleteAlertDataList.addAll(rdSlopOperation.getDeleteRdSlopeInfectData(linkPid, conn));
			rdSlopUpdateAlertDataList.addAll(rdSlopOperation.getUpdateRdSlopeInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(rdSlopDeleteAlertDataList)) {
			infects.put("删除link删除坡度", rdSlopDeleteAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(rdSlopUpdateAlertDataList)) {
			infects.put("删除link维护坡度", rdSlopUpdateAlertDataList);
		}
		// 减速带
		List<AlertObject> rdSpeedbumpAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation rdSpeedbumpOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			rdSpeedbumpAlertDataList.addAll(rdSpeedbumpOperation.getDeleteRdSpeedbumpInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(rdSpeedbumpAlertDataList)) {
			infects.put("删除link删除减速带", rdSpeedbumpAlertDataList);
		}
		// 收费站
		List<AlertObject> rdTollageAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation rdTollgateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			rdTollageAlertDataList.addAll(rdTollgateOperation.getDeleteRdTollageInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(rdTollageAlertDataList)) {
			infects.put("删除link删除收费站", rdTollageAlertDataList);
		}
		// 可变限速
		List<AlertObject> rdVariableAlertDataList = new ArrayList<>();
		List<AlertObject> rdVariableUpdateAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation rdVariableOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			rdVariableAlertDataList.addAll(rdVariableOperation.getDeleteRdVariableInfectData(linkPid, conn));
			rdVariableUpdateAlertDataList.addAll(rdVariableOperation.getUpdateRdVariableInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(rdVariableAlertDataList)) {
			infects.put("删除link删除可变限速", rdVariableAlertDataList);
		}
		if (CollectionUtils.isNotEmpty(rdVariableUpdateAlertDataList)) {
			infects.put("删除link维护可变限速", rdVariableUpdateAlertDataList);
		}
		// 信号灯
		List<AlertObject> trafficAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation trafficOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			trafficAlertDataList.addAll(trafficOperation.getDeleteRdTrafficInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(trafficAlertDataList)) {
			infects.put("删除link删除信号灯", trafficAlertDataList);
		}
		// poi被动维护
		List<AlertObject> poiAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation poiOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			poiAlertDataList.addAll(poiOperation.getUpdatePoiInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(poiAlertDataList)) {
			infects.put("删除link维护poi", poiAlertDataList);
		}
		// 顺行
		List<AlertObject> routeAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation routerOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			routeAlertDataList.addAll(routerOperation.getDeleteRdDirectrouteInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(routeAlertDataList)) {
			infects.put("删除link删除顺行", routeAlertDataList);
		}
		// 语音引导
		List<AlertObject> voiceGuideAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation voiceGuideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			voiceGuideAlertDataList.addAll(voiceGuideOperation.getDeleteRdVoiceguideInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(voiceGuideAlertDataList)) {
			infects.put("删除link删除语音引导", voiceGuideAlertDataList);
		}
		// 警示信息
		List<AlertObject> warningInfoAlertDataList = new ArrayList<>();
		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warningInfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);
		for (RdLink link : links) {
			int linkPid = link.getPid();
			warningInfoAlertDataList.addAll(warningInfoOperation.getDeleteRdWarningInfectData(linkPid, conn));
		}
		if (CollectionUtils.isNotEmpty(warningInfoAlertDataList)) {
			infects.put("删除link删除警示信息", warningInfoAlertDataList);
		}
		return infects;
	}
}
