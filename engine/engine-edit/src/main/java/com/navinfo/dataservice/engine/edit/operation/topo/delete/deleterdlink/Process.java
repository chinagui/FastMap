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
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	private Map<String, List<AlertObject>> infects = new HashMap<String, List<AlertObject>>();

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

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.getConn());

		List<RdRestriction> restrictions = restriction.loadRdRestrictionByLinkPid(this.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的交限
		List<RdRestriction> restrictions2 = restriction.loadRdRestrictionByOutLinkPid(this.getCommand().getLinkPid(),
				true);
		List<RdRestriction> outLinkDeleteResList = new ArrayList<>();

		for (RdRestriction rdRestriction : restrictions2) {
			List<IRow> details = rdRestriction.getDetails();

			if (details.size() == 1) {
				outLinkDeleteResList.add(rdRestriction);
			}
		}

		restrictions.addAll(outLinkDeleteResList);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkPid(this.getCommand().getLinkPid(), true);

		// 获取退出线为该link

		List<RdLaneConnexity> lanes2 = selector.loadRdLaneConnexityByOutLinkPid(this.getCommand().getLinkPid(), true);

		List<RdLaneConnexity> outLinkDeleteLaneList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : lanes2) {
			List<IRow> topos = rdLaneConnexity.getTopos();

			if (topos.size() == 1) {
				outLinkDeleteLaneList.add(rdLaneConnexity);
			}
		}

		lanes.addAll(lanes2);

		this.getCommand().setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByInLinkPid(this.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的车信
		List<RdBranch> branches2 = selector.loadRdBranchByOutLinkPid(this.getCommand().getLinkPid(), true);

		branches.addAll(branches2);

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

		lockRdRestriction();

		lockRdLaneConnexity();

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
		IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
		opRefRestrict.run(this.getResult());

		// 分歧
		IOperation opRefBranch = new OpRefBranch(this.getCommand());
		opRefBranch.run(this.getResult());

		// 路口
		IOperation opRefCross = new OpRefCross(this.getCommand(), this.getConn());
		opRefCross.run(this.getResult());

		// 车信
		IOperation opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand());
		opRefLaneConnexity.run(this.getResult());

		// 限速
		IOperation opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
		opRefSpeedlimit.run(this.getResult());

		// 立交
		IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand());
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

		int linkPid = this.getCommand().getLinkPid();
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
		List<AlertObject> linkAlertDataList = opTopo.getDeleteLinkInfectData(linkPid, conn);
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
				null, null);
		List<AlertObject> updateResAlertDataList = rdrestrictionOperation.getUpdateResInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateResAlertDataList)) {
			infects.put("维护link关联的交限信息", updateResAlertDataList);
		}
		List<AlertObject> delInResAlertDataList = rdrestrictionOperation.getDeleteInLinkResInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delInResAlertDataList)) {
			infects.put("删除link作为进入线的交限信息", delInResAlertDataList);
		}
		List<AlertObject> delOutResAlertDataList = rdrestrictionOperation.getDeleteOutLinkResInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delOutResAlertDataList)) {
			infects.put("删除link作为退入线的交限信息", delOutResAlertDataList);
		}

		// 车信
		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation rdLaneConOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation(
				null, null);
		List<AlertObject> updateRdLaneConAlertDataList = rdLaneConOperation.getUpdateResInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateRdLaneConAlertDataList)) {
			infects.put("维护link关联的车信信息", updateRdLaneConAlertDataList);
		}
		List<AlertObject> delInRdLaneConAlertDataList = rdLaneConOperation
				.getDeleteInLinkRdLaneConnexityInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delInRdLaneConAlertDataList)) {
			infects.put("删除link作为进入线的车信信息", delInRdLaneConAlertDataList);
		}
		List<AlertObject> delOutRdLaneConAlertDataList = rdLaneConOperation
				.getDeleteOutLinkRdLanConnexityInfectData(linkPid, conn);

		if (CollectionUtils.isNotEmpty(delOutRdLaneConAlertDataList)) {
			infects.put("删除link作为退出线的车信信息", delOutRdLaneConAlertDataList);
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
				null,null);
		List<AlertObject> delCrossAlertDataList = rdCrossOperation.getDeleteRdCross(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delCrossAlertDataList)) {
			infects.put("删除link,删除路口关系", delCrossAlertDataList);
		}
		List<AlertObject> updateCrossAlertDataList = rdCrossOperation.getUpdateRdCross(linkPid, conn);
		if (CollectionUtils.isNotEmpty(updateCrossAlertDataList)) {
			infects.put("删除link,维护路口关系", updateCrossAlertDataList);
		}
		
		//立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation rdGscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				null, null);
		List<AlertObject> delGscAlertDataList = rdGscOperation.getDeleteRdGscInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delGscAlertDataList)) {
			infects.put("删除link删除立交", delGscAlertDataList);
		}
		//限速关系
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation rdSpeedLimitOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Operation(
				null, null);
		List<AlertObject> delSpeedLimitAlertDataList = rdSpeedLimitOperation.getDeleteRdSpeedLimitInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delSpeedLimitAlertDataList)) {
			infects.put("删除link删除限速关系", delSpeedLimitAlertDataList);
		}
		//电子眼
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation rdEyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation(
				conn);
		List<AlertObject> delRdEyeAlertDataList = rdEyeOperation.getDeleteRdEyeInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdEyeAlertDataList)) {
			infects.put("删除link删除电子眼", delRdEyeAlertDataList);
		}
		//大门
		com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation rdGateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
				conn);
		List<AlertObject> delRdGateAlertDataList = rdGateOperation.getDeleteRdGateInfectData(linkPid, conn);
		if (CollectionUtils.isNotEmpty(delRdGateAlertDataList)) {
			infects.put("删除link删除大门", delRdGateAlertDataList);
		}
		//CRF交叉点
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
		
		return infects;
	}

}
