package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {


	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		List<RdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		
		List<Integer> linkPids = new ArrayList<Integer>();
		
		for(RdLink link : links){
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

		for (Integer linkPid: this.getCommand().getLinkPids()) {

			List<RdNode> list = selector.loadEndRdNodeByLinkPid(linkPid,
					true);

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

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByNodePid(this.getCommand().getNodePid(), true);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByNodePid(
				this.getCommand().getNodePid(), true);

		this.getCommand().setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByNodePid(
				this.getCommand().getNodePid(), true);

		this.getCommand().setBranches(branches);
	}

	public void lockRdCross() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(
				this.getCommand().getNodePids(), this.getCommand().getLinkPids(), true);

		this.getCommand().setCrosses(crosses);
	}

	public void lockRdSpeedlimits() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.getConn());

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPids(
				this.getCommand().getLinkPids(), true);

		this.getCommand().setLimits(limits);
	}
	
	public void lockRdGsc() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPids(this.getCommand().getLinkPids(), true);

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
				
				IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
				opRefRestrict.run(this.getResult());
				
				IOperation opRefBranch = new OpRefBranch(this.getCommand());
				opRefBranch.run(this.getResult());
				
				IOperation opRefCross = new OpRefCross(this.getCommand());
				opRefCross.run(this.getResult());
				
				IOperation opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand());
				opRefLaneConnexity.run(this.getResult());
				
				IOperation opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
				opRefSpeedlimit.run(this.getResult());
				
				IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand());
				opRefRdGsc.run(this.getResult());

				IOperation opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
				opRefAdAdmin.run(this.getResult());
				
				recordData();
				postCheck();
				getConn().commit();
			} else {
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

	private void releaseResource(PreparedStatement pstmt, ResultSet resultSet) {
		try {
			resultSet.close();
		} catch (Exception e) {

		}

		try {
			pstmt.close();
		} catch (Exception e) {

		}
	}

	@Override
	public String exeOperation() {
		// TODO Auto-generated method stub
		return null;
	}

}
