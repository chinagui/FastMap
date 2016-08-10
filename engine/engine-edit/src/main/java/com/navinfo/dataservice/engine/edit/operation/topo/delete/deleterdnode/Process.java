package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
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
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
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
		// TODO Auto-generated constructor stub
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

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.getConn());

		List<RdRestriction> restrictions = restriction.loadRdRestrictionByNodePid(this.getCommand().getNodePid(), true);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByNodePid(this.getCommand().getNodePid(), true);

		this.getCommand().setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByNodePid(this.getCommand().getNodePid(), true);

		this.getCommand().setBranches(branches);
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

	public void lockRdGsc() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPids(this.getCommand().getLinkPids(), "RD_LINK", true);

		this.getCommand().setRdGscs(rdGscList);
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

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		lockRdCross();

		lockRdSpeedlimits();

		lockRdGsc();

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

				prepareData();

				Map<String, List<Integer>> infects = confirmRelationObj();

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
		IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
		opRefRestrict.run(this.getResult());

		// 分歧
		IOperation opRefBranch = new OpRefBranch(this.getCommand());
		opRefBranch.run(this.getResult());

		// 路口
		IOperation opRefCross = new OpRefCross(this.getCommand());
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

		// 警示信息
		OpRefRdWarninginfo opRefRdWarninginfo = new OpRefRdWarninginfo(this.getConn());
		opRefRdWarninginfo.run(this.getResult(), this.getCommand());

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

		// 顺行
		OpRefRdDirectroute opRefRdDirectroute = new OpRefRdDirectroute(this.getConn());
		opRefRdDirectroute.run(this.getResult(), this.getCommand());

		// CRF交叉点
		OpRefRdInter opRefRdInter = new OpRefRdInter(this.getConn());
		opRefRdInter.run(this.getResult(), this.getCommand());

		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand());
	}

	/**
	 * 删除node影响到的关联要素
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

		// 警示信息
		infectList = new ArrayList<Integer>();

		RdWarninginfoSelector selector = new RdWarninginfoSelector(this.getConn());

		for (Integer linkPid : this.getCommand().getLinkPids()) {
			infectList.addAll(selector.loadPidByLink(linkPid, false));
		}

		infects.put("RDWARNINGINFO", infectList);

		// 信号灯
		RdTrafficsignalSelector trafficsignalSelector = new RdTrafficsignalSelector(this.getConn());

		List<RdTrafficsignal> trafficsignals = trafficsignalSelector.loadByNodeId(true, this.getCommand().getNodePid());

		for (RdTrafficsignal trafficsignal : trafficsignals) {
			infectList.add(trafficsignal.getPid());
		}

		infects.put("RDTRAFFICSIGNAL", infectList);

		// 电子眼
		infectList = new ArrayList<Integer>();
		RdElectroniceyeSelector rdElectroniceyeSelector = new RdElectroniceyeSelector(this.getConn());
		List<RdElectroniceye> rdElectroniceyes = null;
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			rdElectroniceyes = rdElectroniceyeSelector.loadListByRdLinkId(linkPid, true);
			for (RdElectroniceye eleceye : rdElectroniceyes) {
				infectList.add(eleceye.pid());
			}
		}
		infects.put("RDELECTRONICEYE", infectList);

		// 大门
		RdGateSelector rdGateSelector = new RdGateSelector(this.getConn());
		infectList = new ArrayList<Integer>();
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			List<RdGate> rdGate = rdGateSelector.loadByLink(linkPid, true);
			for (RdGate gate : rdGate) {
				infectList.add(gate.getPid());
			}
		}
		infects.put("RDGATE", infectList);

		// 分岔路提示
		infectList = new ArrayList<Integer>();
		RdSeSelector rdSeSelector = new RdSeSelector(this.getConn());
		List<RdSe> rdSes = null;
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			rdSes = rdSeSelector.loadRdSesWithLinkPid(linkPid, true);
			for (RdSe rdSe : rdSes) {
				infectList.add(rdSe.pid());
			}
		}
		infects.put("RDSE", infectList);

		// 减速带
		infectList = new ArrayList<Integer>();
		RdSpeedbumpSelector rdSpeedbumpSelector = new RdSpeedbumpSelector(this.getConn());
		List<RdSpeedbump> rdSpeedbumps = null;
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			rdSpeedbumps = rdSpeedbumpSelector.loadByLinkPid(linkPid, true);
			for (RdSpeedbump rdSpeedbump : rdSpeedbumps) {
				infectList.add(rdSpeedbump.pid());
			}
		}
		infects.put("RDSPEEDBUMP", infectList);

		// 顺行
		infectList = new ArrayList<Integer>();
		RdDirectrouteSelector directrouteSelector = new RdDirectrouteSelector(this.getConn());
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			infectList.addAll(directrouteSelector.loadPidByLink(linkPid, false));
		}
		infects.put("RDDIRECTROUTE", infectList);

		// CRF交叉点
		RdInterSelector interSelector = new RdInterSelector(this.getConn());

		StringBuilder sb = new StringBuilder();

		for (RdLink link : this.getCommand().getLinks()) {
			sb.append(link.getsNodePid() + "," + link.geteNodePid());
		}

		infectList = interSelector.loadInterPidByNodePid(sb.deleteCharAt(sb.lastIndexOf(",")).toString(), false);

		infects.put("RDINTER", infectList);
		return infects;
	}
}
