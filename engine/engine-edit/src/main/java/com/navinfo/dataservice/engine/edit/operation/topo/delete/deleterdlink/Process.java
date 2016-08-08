package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;
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

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.getConn());

		List<RdRestriction> restrictions = restriction.loadRdRestrictionByLinkPid(this.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的交限

		List<RdRestriction> restrictions2 = restriction.loadRdRestrictionByOutLinkPid(this.getCommand().getLinkPid(),
				true);

		restrictions.addAll(restrictions2);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkPid(this.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的车信

		List<RdLaneConnexity> lanes2 = selector.loadRdLaneConnexityByOutLinkPid(this.getCommand().getLinkPid(), true);

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

				prepareData();

				Map<String, List<Integer>> infects = confirmRelationObj();

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

		// 警示信息
		OpRefRdWarninginfo opRefRdWarninginfo = new OpRefRdWarninginfo(this.getConn());
		opRefRdWarninginfo.run(this.getResult(), this.getCommand().getLinkPid());

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

	}

	/**
	 * 删除link影响到的关联要素
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<Integer>> confirmRelationObj() throws Exception {
		Map<String, List<Integer>> infects = new HashMap<String, List<Integer>>();

		List<Integer> infectList = new ArrayList<Integer>();

		infectList = new ArrayList<Integer>();

		for (RdBranch branch : this.getCommand().getBranches()) {
			infectList.add(branch.getPid());
		}

		infects.put("RDBRANCH", infectList);

		infectList = new ArrayList<Integer>();

		for (RdLaneConnexity laneConn : this.getCommand().getLanes()) {
			infectList.add(laneConn.getPid());
		}

		infects.put("RDLANECONNEXITY", infectList);

		infectList = new ArrayList<Integer>();

		for (RdSpeedlimit limit : this.getCommand().getLimits()) {
			infectList.add(limit.getPid());
		}

		infects.put("RDSPEEDLIMIT", infectList);

		infectList = new ArrayList<Integer>();

		for (RdRestriction res : this.getCommand().getRestrictions()) {
			infectList.add(res.getPid());
		}

		infects.put("RDRESTRICTION", infectList);

		infectList = new ArrayList<Integer>();

		for (RdGsc rdGsc : this.getCommand().getRdGscs()) {
			infectList.add(rdGsc.getPid());
		}

		infects.put("RDGSC", infectList);

		infectList = new ArrayList<Integer>();

		for (AdAdmin adAdmin : this.getCommand().getAdAdmins()) {
			infectList.add(adAdmin.getPid());
		}

		infects.put("ADADMIN", infectList);

		// 警示信息
		RdWarninginfoSelector selector = new RdWarninginfoSelector(this.getConn());

		infectList = selector.loadPidByLink(this.getCommand().getLinkPid(), true);

		infects.put("RDWARNINGINFO", infectList);

		// 信号灯
		RdTrafficsignalSelector trafficsignalSelector = new RdTrafficsignalSelector(this.getConn());

		List<RdTrafficsignal> trafficsignals = trafficsignalSelector.loadByLinkPid(true,
				this.getCommand().getLinkPid());

		infectList = new ArrayList<Integer>();

		for (RdTrafficsignal trafficsignal : trafficsignals) {
			infectList.add(trafficsignal.getPid());
		}

		infects.put("RDTRAFFICSIGNAL", infectList);

		// 电子眼
		RdElectroniceyeSelector rdElectroniceyeSelector = new RdElectroniceyeSelector(this.getConn());
		List<RdElectroniceye> eleceyes = rdElectroniceyeSelector.loadListByRdLinkId(this.getCommand().getLinkPid(),
				true);
		infectList = new ArrayList<Integer>();

		for (RdElectroniceye eleceye : eleceyes) {
			infectList.add(eleceye.pid());
		}
		infects.put("RDELECTRONICEYE", infectList);

		// 大门
		RdGateSelector rdGateSelector = new RdGateSelector(this.getConn());
		List<RdGate> rdGate = rdGateSelector.loadByLink(this.getCommand().getLinkPid(), true);

		infectList = new ArrayList<Integer>();

		for (RdGate gate : rdGate) {
			infectList.add(gate.getPid());
		}
		infects.put("RDGATE", infectList);

		// 分岔路提示
		RdSeSelector rdSeSelector = new RdSeSelector(this.getConn());
		List<RdSe> rdSes = rdSeSelector.loadRdSesWithLinkPid(this.getCommand().getLinkPid(), true);
		infectList = new ArrayList<Integer>();

		for (RdSe rdSe : rdSes) {
			infectList.add(rdSe.pid());
		}
		infects.put("RDSE", infectList);

		// 减速带
		RdSpeedbumpSelector rdSpeedbumpSelector = new RdSpeedbumpSelector(this.getConn());
		List<RdSpeedbump> rdSpeedbumps = rdSpeedbumpSelector.loadByLinkPid(this.getCommand().getLinkPid(), true);
		infectList = new ArrayList<Integer>();

		for (RdSpeedbump rdSpeedbump : rdSpeedbumps) {
			infectList.add(rdSpeedbump.pid());
		}
		infects.put("RDSPEEDBUMP", infectList);

		return infects;
	}

}
