package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
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

import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	public Process(AbstractCommand command, Result result, Connection conn)
			throws Exception {
		super(command);
		this.setResult(result);
		this.setConn(conn);
	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		RdLink link = (RdLink) selector.loadById(
				this.getCommand().getLinkPid(), true);

		this.getCommand().setLink(link);
	}

	// 锁定盲端节点
	public void lockRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.getConn());

		List<RdNode> nodes = selector.loadEndRdNodeByLinkPid(this.getCommand()
				.getLinkPid(), false);

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

		RdRestrictionSelector restriction = new RdRestrictionSelector(
				this.getConn());

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByLinkPid(this.getCommand().getLinkPid(),
						true);

		// 获取退出线为该link，并且只有一根退出线的交限

		List<RdRestriction> restrictions2 = restriction
				.loadRdRestrictionByOutLinkPid(this.getCommand().getLinkPid(),
						true);

		restrictions.addAll(restrictions2);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkPid(
				this.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的车信

		List<RdLaneConnexity> lanes2 = selector
				.loadRdLaneConnexityByOutLinkPid(
						this.getCommand().getLinkPid(), true);

		lanes.addAll(lanes2);

		this.getCommand().setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByInLinkPid(this
				.getCommand().getLinkPid(), true);

		// 获取退出线为该link，并且只有一根退出线的车信

		List<RdBranch> branches2 = selector.loadRdBranchByOutLinkPid(this
				.getCommand().getLinkPid(), true);

		branches.addAll(branches2);

		this.getCommand().setBranches(branches);
	}

	public void lockRdCross() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(this.getCommand().getLinkPid());

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(this
				.getCommand().getNodePids(), linkPids, true);

		this.getCommand().setCrosses(crosses);
	}

	public void lockRdGsc() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this
				.getCommand().getLinkPid(),"RD_LINK", true);

		this.getCommand().setRdGscs(rdGscList);
	}

	public void lockRdSpeedlimits() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.getConn());

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPid(this
				.getCommand().getLinkPid(), true);

		this.getCommand().setLimits(limits);
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

		return true;
	}

	private void lockAdAdmin() throws Exception {
		AdAdminSelector selector = new AdAdminSelector(this.getConn());

		List<AdAdmin> adAdminList = selector.loadRowsByLinkId(this.getCommand()
				.getLinkPid(), true);

		this.getCommand().setAdAdmins(adAdminList);
	}

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

				IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
				opRefRestrict.run(this.getResult());

				IOperation opRefBranch = new OpRefBranch(this.getCommand());
				opRefBranch.run(this.getResult());

				IOperation opRefCross = new OpRefCross(this.getCommand());
				opRefCross.run(this.getResult());

				IOperation opRefLaneConnexity = new OpRefLaneConnexity(
						this.getCommand());
				opRefLaneConnexity.run(this.getResult());

				IOperation opRefSpeedlimit = new OpRefSpeedlimit(
						this.getCommand());
				opRefSpeedlimit.run(this.getResult());

				IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand());
				opRefRdGsc.run(this.getResult());

				IOperation opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
				opRefAdAdmin.run(this.getResult());

				recordData();

				postCheck();

				this.getConn().commit();
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

				for (RdGsc rdGsc : this.getCommand().getRdGscs()) {
					infectList.add(rdGsc.getPid());
				}

				infects.put("RDGSC", infectList);

				infectList = new ArrayList<Integer>();

				for (AdAdmin adAdmin : this.getCommand().getAdAdmins()) {
					infectList.add(adAdmin.getPid());
				}

				infects.put("ADADMIN", infectList);

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
			IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
			opRefRestrict.run(this.getResult());
			IOperation opRefBranch = new OpRefBranch(this.getCommand());
			opRefBranch.run(this.getResult());
			IOperation opRefCross = new OpRefCross(this.getCommand());
			opRefCross.run(this.getResult());
			IOperation opRefLaneConnexity = new OpRefLaneConnexity(
					this.getCommand());
			opRefLaneConnexity.run(this.getResult());
			IOperation opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
			opRefSpeedlimit.run(this.getResult());
			IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand());
			opRefRdGsc.run(this.getResult());
			IOperation opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
			opRefAdAdmin.run(this.getResult());

			recordData();

			postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
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
