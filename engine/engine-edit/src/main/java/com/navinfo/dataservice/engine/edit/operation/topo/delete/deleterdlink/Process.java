package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
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

	public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
		super(command);
		this.setResult(result);
		this.setConn(conn);
	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		RdLink link = (RdLink) selector.loadById(this.getCommand().getLinkPid(), true);

		this.getCommand().setLink(link);
	}

	// 锁定盲端节点
	public void lockRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.getConn());

		List<RdNode> nodes = selector.loadEndRdNodeByLinkPid(this.getCommand().getLinkPid(), false);

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdNode node : nodes) {
			nodePids.add(node.getPid());
		}

		this.getCommand().setNodes(nodes);

		this.getCommand().setNodePids(nodePids);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByInLinkPid(this.getCommand().getLinkPid(), true);

		List<RdBranch> viaBranch = selector.loadByLinkPid(this.getCommand().getLinkPid(), 3, true);

		// 获取退出线为该link，并且只有一根退出线的车信
		List<RdBranch> branches2 = selector.loadRdBranchByOutLinkPid(this.getCommand().getLinkPid(), true);

		branches.addAll(branches2);

		branches.addAll(viaBranch);

		this.getCommand().setBranches(branches);
	}

	public void lockRdCross() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(this.getCommand().getLinkPid());

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(this.getCommand().getNodePids(), linkPids, true);

		this.getCommand().setCrosses(crosses);
	}

	public void lockRdGsc() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RD_LINK", true);

		this.getCommand().setRdGscs(rdGscList);
	}

	public void lockRdSpeedlimits() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.getConn());

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPid(this.getCommand().getLinkPid(), true);

		this.getCommand().setLimits(limits);
	}

	public void lockRdElectroniceye() throws Exception {
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.getConn());

		List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(this.getCommand().getLinkPid(), true);

		this.getCommand().setElectroniceyes(eleceyes);
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该link对象
		lockRdLink();

		if (this.getCommand().getLink() == null) {

			throw new Exception("指定删除的LINK不存在！");
		}

		lockRdNode();

		lockRdBranch();

		lockRdCross();

		lockRdSpeedlimits();

		lockRdGsc();

		lockAdAdmin();

		lockRdElectroniceye();

		return true;
	}

	private void lockAdAdmin() throws Exception {
		AdAdminSelector selector = new AdAdminSelector(this.getConn());

		List<AdAdmin> adAdminList = selector.loadRowsByLinkId(this.getCommand().getLinkPid(), true);

		this.getCommand().setAdAdmins(adAdminList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.engine.edit.operation.AbstractProcess#run()
	 */
	@Override
	public String run() throws Exception {

		try {
			if (!this.getCommand().isCheckInfect()) {
				this.getConn().setAutoCommit(false);
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

				this.getConn().commit();
			} else {

				Map<String, List<AlertObject>> infects = confirmRelationObj();

				this.getConn().commit();

				return JSONObject.fromObject(infects).toString();
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		} finally {
			try {
				this.getConn().close();
			} catch (Exception e) {

			}
		}

		return null;
	}

	public String innerRun() throws Exception {

		try {
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

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
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

		// 车信
		IOperation opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand(), this.getConn());
		opRefLaneConnexity.run(this.getResult());

		// 分歧
		IOperation opRefBranch = new OpRefBranch(this.getCommand());
		opRefBranch.run(this.getResult());

		// 路口
		IOperation opRefCross = new OpRefCross(this.getCommand(), this.getConn());
		opRefCross.run(this.getResult());

		// 限速
		IOperation opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
		opRefSpeedlimit.run(this.getResult());

		// 立交
		IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand(),this.getConn());
		opRefRdGsc.run(this.getResult());

		// 行政区划
		IOperation opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
		opRefAdAdmin.run(this.getResult());

		// 电子眼
		IOperation opRefElectroniceye = new OpRefElectroniceye(this.getCommand());
		opRefElectroniceye.run(this.getResult());

		// 大门
		OpRefRdGate opRefRdGate = new OpRefRdGate(this.getConn());
		opRefRdGate.run(this.getResult(), this.getCommand().getLinkPid());

		// 信号灯
		OpRefTrafficsignal opRefRdTrafficsignal = new OpRefTrafficsignal(this.getConn());
		opRefRdTrafficsignal.run(this.getResult(), this.getCommand().getLinkPid());

		// 分岔路提示
		OpRefRdSe opRefRdSe = new OpRefRdSe(this.getCommand(), this.getConn());
		opRefRdSe.run(this.getResult());

		// 减速带
		OpRefRdSpeedbump opRefRdSpeedbump = new OpRefRdSpeedbump(this.getCommand(), this.getConn());
		opRefRdSpeedbump.run(this.getResult());
		// 坡度
		OpRefRdSlope opRefSlope = new OpRefRdSlope(this.getConn());
		opRefSlope.run(this.getResult(), this.getCommand().getLinkPid());

		// CRF交叉点
		OpRefRdInter opRefRdInter = new OpRefRdInter(this.getConn());
		opRefRdInter.run(this.getResult(), this.getCommand().getLink());

		// CRF对象
		OpRefRdObject opRefRdObject = new OpRefRdObject(this.getConn());
		opRefRdObject.run(this.getResult(), this.getCommand().getLinkPid());

		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand().getLink());

		// 收费站
		OpRefRdTollgate opRefRdTollgate = new OpRefRdTollgate(this.getCommand(), this.getConn());
		opRefRdTollgate.run(this.getResult());

		// 可变限速
		OpRefRdVariableSpeed opRefRdVariableSpeed = new OpRefRdVariableSpeed(this.getConn());
		opRefRdVariableSpeed.run(this.getResult(), this.getCommand().getLink());

		// 车道信息
		OpRefRdLane refRdLane = new OpRefRdLane(this.getConn());
		refRdLane.run(this.getResult(), this.getCommand().getLink().getPid());

		OpRefRelationObj opRefRelationObj = new OpRefRelationObj(this.getConn());

		// 警示信息
		opRefRelationObj.handleWarninginfo(this.getResult(), this.getCommand().getLinkPid());

		// 语音引导
		opRefRelationObj.handleVoiceguide(this.getResult(), this.getCommand().getLink());

		// CRF道路
		opRefRelationObj.handleRoad(this.getResult(), this.getCommand());

		// 顺行
		opRefRelationObj.handleDirectroute(this.getResult(), this.getCommand().getLink());
		// 同一线
		opRefRelationObj.handleSameLink(this.getResult(), this.getCommand());

		// poi引导link
		opRefRelationObj.handlePoiGuideLink(this.getResult(), this.getCommand().getLink());
	}

	/**
	 * 删除link影响到的关联要素
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<AlertObject>> confirmRelationObj() throws Exception {
		Map<String, List<AlertObject>> infects = new HashMap<String, List<AlertObject>>();

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该link对象
		lockRdLink();

		RdLink link = this.getCommand().getLink();

		if (link == null) {
			throw new Exception("指定删除的LINK不存在！");
		}

		int linkPid = link.getPid();
		
		List<Integer> linkPidList = new ArrayList<>();
		
		linkPidList.add(linkPid);

		Connection conn = getConn();

		// 行政区划代表点
		com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation adadminOperation = new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation(
				null, null);
		List<AlertObject> adminAlertDataList = adadminOperation.getUpdateAdminInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(adminAlertDataList)) {
			infects.put("维护link关联的行政区划信息", adminAlertDataList);
		}

		// link
		OpTopo opTopo = new OpTopo(this.getCommand());
		List<AlertObject> linkAlertDataList = opTopo.getDeleteLinkInfectData(this.getCommand().getLink(), conn);
		if (CollectionUtils.isNotEmpty(linkAlertDataList)) {
			infects.put("删除Link", linkAlertDataList);
		}

		// node
		List<AlertObject> nodeAlertDataList = opTopo.getDeleteNodeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(nodeAlertDataList)) {
			infects.put("删除Node", nodeAlertDataList);
		}

		// 交限
		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation rdrestrictionOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation(
				conn);
		List<AlertObject> updateResAlertDataList = rdrestrictionOperation.getUpdateResInfectData(linkPidList);
		if (CollectionUtils.isNotEmpty(updateResAlertDataList)) {
			infects.put("此link上存在交限关系信息，删除该Link会对应删除此组关系", updateResAlertDataList);
		}
		List<AlertObject> delResAlertDataList = rdrestrictionOperation.getDeleteLinkResInfectData(linkPidList);
		if (CollectionUtils.isNotEmpty(delResAlertDataList)) {
			infects.put("此link上存在交限关系信息，删除该Link会对应删除该交限", delResAlertDataList);
		}
		
		// 车信
		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation rdLaneConOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation(
				conn);
		List<AlertObject> updateRdLaneConAlertDataList = rdLaneConOperation.getUpdateResInfectData(linkPidList);
		if (CollectionUtils.isNotEmpty(updateRdLaneConAlertDataList)) {
			infects.put("此link上存在车信关系信息，删除该Link会对应删除此组关系", updateRdLaneConAlertDataList);
		}
		List<AlertObject> delRdLaneConAlertDataList = rdLaneConOperation.getDeleteRdLaneConnexityInfectData(linkPidList);
		if (CollectionUtils.isNotEmpty(delRdLaneConAlertDataList)) {
			infects.put("此link上存在车信关系信息，删除该Link会对应删除该车信", delRdLaneConAlertDataList);
		}
		// 分歧
		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation rdBranchOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation(
				null, null, null);
		List<AlertObject> delInRdBranchAlertDataList = rdBranchOperation.getDeleteBranchInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delInRdBranchAlertDataList)) {
			infects.put("删除link作为进入线的分歧信息", delInRdBranchAlertDataList);
		}
		List<AlertObject> delOutRdBranchAlertDataList = rdBranchOperation.getDeleteBOutLinkranchInfectData(linkPid,
				conn);
		if (CollectionUtils.isNotEmpty(delOutRdBranchAlertDataList)) {
			infects.put("删除link作为退出线的分歧信息", delOutRdBranchAlertDataList);
		}
		List<AlertObject> delViaRdBranchAlertDataList = rdBranchOperation.getDeleteBViaLinkranchInfectData(linkPid,
				conn);
		if (CollectionUtils.isNotEmpty(delViaRdBranchAlertDataList)) {
			infects.put("删除link作为经过线的分歧信息", delViaRdBranchAlertDataList);
		}

		// 路口
		com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.OpTopo rdCrossOperation = new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.OpTopo(
				null, null);
		List<AlertObject> delCrossAlertDataList = rdCrossOperation.getDeleteRdCross(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delCrossAlertDataList)) {
			infects.put("删除link,删除路口关系", delCrossAlertDataList);
		}
		List<AlertObject> updateCrossAlertDataList = rdCrossOperation.getUpdateRdCross(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateCrossAlertDataList)) {
			infects.put("删除link,维护路口关系", updateCrossAlertDataList);
		}

		// 立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation rdGscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				conn);
		List<IRow> linkList = new ArrayList<>(); 
		linkList.add(link);
		List<AlertObject> delGscAlertDataList = rdGscOperation.getDeleteRdGscInfectData(linkList);
		if (CollectionUtils.isNotEmpty(delGscAlertDataList)) {
			infects.put("删除link维护立交", delGscAlertDataList);
		}
		// 限速关系
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation rdSpeedLimitOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation(
				null, null);
		List<AlertObject> updateSpeedLimitAlertDataList = rdSpeedLimitOperation.getUpdateRdSpeedLimitInfectData(linkPid,
				conn);
		if (CollectionUtils.isNotEmpty(updateSpeedLimitAlertDataList)) {
			infects.put("删除link维护限速关系", updateSpeedLimitAlertDataList);
		}
		// 电子眼
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation rdEyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation(
				conn);
		List<AlertObject> delRdEyeAlertDataList = rdEyeOperation.getUpdateRdEyeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdEyeAlertDataList)) {
			infects.put("删除link维护电子眼", delRdEyeAlertDataList);
		}
		// 大门
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation rdGateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
				conn);
		List<AlertObject> delRdGateAlertDataList = rdGateOperation.getDeleteRdGateInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdGateAlertDataList)) {
			infects.put("删除link删除大门", delRdGateAlertDataList);
		}
		// CRF交叉点
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation rdInterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation(
				conn);
		List<AlertObject> updateRdInterAlertDataList = rdInterOperation.getUpdateRdInterInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateRdInterAlertDataList)) {
			infects.put("删除link维护CRF交叉点", updateRdInterAlertDataList);
		}
		List<AlertObject> delRdInterAlertDataList = rdInterOperation.getDeleteRdInterInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdInterAlertDataList)) {
			infects.put("删除link删除CRF交叉点", delRdInterAlertDataList);
		}
		// CRF Road
		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Operation(
				conn);
		List<AlertObject> updateRdRoadAlertDataList = rdRoadOperation.getUpdateRdRoadInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateRdRoadAlertDataList)) {
			infects.put("删除link维护CRF道路", updateRdRoadAlertDataList);
		}
		// CRF对象 TODO

		// 详细车道
		com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation rdLaneOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
				conn);
		List<AlertObject> delRdLaneAlertDataList = rdLaneOperation.getDeleteRdLaneInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdLaneAlertDataList)) {
			infects.put("删除link删除详细车道", delRdLaneAlertDataList);
		}

		// 同一点
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
				conn);
		List<AlertObject> sameNodeAlertDataList = sameNodeOperation.getDeleteLinkSameNodeInfectData(link.getsNodePid(),
				link.geteNodePid(), "RD_NODE", conn);
		if (CollectionUtils.isNotEmpty(sameNodeAlertDataList)) {
			infects.put("删除link影响的同一点", sameNodeAlertDataList);
		}

		// 同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation sameLinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);
		List<AlertObject> sameLinkAlertDataList = sameLinkOperation.getDeleteLinkSameLinkInfectData(link, conn);
		if (CollectionUtils.isNotEmpty(sameLinkAlertDataList)) {
			infects.put("删除link影响的同一线", sameLinkAlertDataList);
		}

		// 分叉口提示
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation rdSeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation(
				conn);
		List<AlertObject> rdSeAlertDataList = rdSeOperation.getDeleteRdSeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdSeAlertDataList)) {
			infects.put("删除link删除分叉口", rdSeAlertDataList);
		}

		// 坡度
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation rdSlopOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation(
				conn);
		List<AlertObject> rdSlopDeleteAlertDataList = rdSlopOperation.getDeleteRdSlopeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdSlopDeleteAlertDataList)) {
			infects.put("删除link删除坡度", rdSlopDeleteAlertDataList);
		}
		List<AlertObject> rdSlopUpdateAlertDataList = rdSlopOperation.getUpdateRdSlopeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdSlopUpdateAlertDataList)) {
			infects.put("删除link维护坡度", rdSlopUpdateAlertDataList);
		}
		// 减速带
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation rdSpeedbumpOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation(
				conn);
		List<AlertObject> rdSpeedbumpAlertDataList = rdSpeedbumpOperation.getDeleteRdSpeedbumpInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdSpeedbumpAlertDataList)) {
			infects.put("删除link删除减速带", rdSpeedbumpAlertDataList);
		}
		// 收费站
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation rdTollgateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation(
				conn);
		List<AlertObject> rdTollageAlertDataList = rdTollgateOperation.getDeleteRdTollageInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdTollageAlertDataList)) {
			infects.put("删除link删除收费站", rdTollageAlertDataList);
		}
		// 可变限速
		com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation rdVariableOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation(
				conn);
		List<AlertObject> rdVariableAlertDataList = rdVariableOperation.getDeleteRdVariableInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(rdVariableAlertDataList)) {
			infects.put("删除link删除可变限速", rdVariableAlertDataList);
		}
		List<AlertObject> rdVariableUpdateAlertDataList = rdVariableOperation.getUpdateRdVariableInfectData(linkPid,
				conn);
		if (CollectionUtils.isNotEmpty(rdVariableUpdateAlertDataList)) {
			infects.put("删除link维护可变限速", rdVariableUpdateAlertDataList);
		}

		// 信号灯
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation trafficOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
				conn);
		List<AlertObject> trafficAlertDataList = trafficOperation.getDeleteRdTrafficInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(trafficAlertDataList)) {
			infects.put("删除link删除信号灯", trafficAlertDataList);
		}
		// poi被动维护
		com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation poiOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation(
				conn);
		List<AlertObject> poiAlertDataList = poiOperation.getUpdatePoiInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(poiAlertDataList)) {
			infects.put("删除link维护poi", poiAlertDataList);
		}
		// 顺行
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation routerOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);
		List<AlertObject> routeAlertDataList = routerOperation.getDeleteRdDirectrouteInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(routeAlertDataList)) {
			infects.put("删除link删除顺行", routeAlertDataList);
		}
		// 语音引导
		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation voiceGuideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(
				conn);
		List<AlertObject> voiceGuideAlertDataList = voiceGuideOperation.getDeleteRdVoiceguideInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(voiceGuideAlertDataList)) {
			infects.put("删除link删除语音引导", voiceGuideAlertDataList);
		}
		// 警示信息
		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warningInfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);
		List<AlertObject> warningInfoAlertDataList = warningInfoOperation.getDeleteRdWarningInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(warningInfoAlertDataList)) {
			infects.put("删除link删除警示信息", warningInfoAlertDataList);
		}
		return infects;
	}

}
