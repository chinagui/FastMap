package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletenode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;
import com.navinfo.dataservice.FosEngine.edit.operation.OperatorFactory;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

	}

	@Override
	public ICommand getCommand() {

		return command;
	}

	@Override
	public Result getResult() {

		return result;
	}

	public String preCheck() throws Exception {
		return null;

	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.conn);

		List<RdLink> links = selector.loadByNodePid(command.getNodePid(), true);
		
		List<Integer> linkPids = new ArrayList<Integer>();
		
		for(RdLink link : links){
			linkPids.add(link.getPid());
		}

		command.setLinks(links);
		
		command.setLinkPids(linkPids);
	}

	public void lockRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.conn);

		RdNode node = (RdNode) selector.loadById(command.getNodePid(), true);

		command.setNode(node);

	}

	// 锁定盲端节点
	public void lockEndRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.conn);

		List<Integer> nodePids = new ArrayList<Integer>();
		
		nodePids.add(command.getNodePid());

		List<RdNode> nodes = new ArrayList<RdNode>();

		for (Integer linkPid: command.getLinkPids()) {

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
		
		nodes.add(command.getNode());

		command.setNodes(nodes);

		command.setNodePids(nodePids);
	}

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.conn);

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByNodePid(command.getNodePid(), true);

		command.setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.conn);

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByNodePid(
				command.getNodePid(), true);

		command.setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.conn);

		List<RdBranch> branches = selector.loadRdBranchByNodePid(
				command.getNodePid(), true);

		command.setBranches(branches);
	}

	public void lockRdCross() throws Exception {

		RdCrossSelector selector = new RdCrossSelector(this.conn);

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(
				command.getNodePids(), command.getLinkPids(), true);

		command.setCrosses(crosses);
	}

	public void lockRdSpeedlimits() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.conn);

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPids(
				command.getLinkPids(), true);

		command.setLimits(limits);
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

		if (command.getNode() == null) {

			throw new Exception("指定删除的RDNODE不存在！");
		}

		lockRdLink();

		lockEndRdNode();

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		lockRdCross();

		lockRdSpeedlimits();

		return true;
	}

	@Override
	public String run() throws Exception {

		try {
			if (!command.isCheckInfect()) {
				conn.setAutoCommit(false);
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				prepareData();
				
				IOperation op = new OpTopo(command);
				op.run(result);
				
				IOperation opRefRestrict = new OpRefRestrict(command);
				opRefRestrict.run(result);
				
				IOperation opRefBranch = new OpRefBranch(command);
				opRefBranch.run(result);
				
				IOperation opRefCross = new OpRefCross(command);
				opRefCross.run(result);
				
				IOperation opRefLaneConnexity = new OpRefLaneConnexity(command);
				opRefLaneConnexity.run(result);
				
				IOperation opRefSpeedlimit = new OpRefSpeedlimit(command);
				opRefSpeedlimit.run(result);
				
				
				recordData();
				postCheck();
				conn.commit();
			} else {
				Map<String, List<Integer>> infects = new HashMap<String, List<Integer>>();

				List<Integer> infectList = new ArrayList<Integer>();

				infectList = new ArrayList<Integer>();

				for (RdBranch branch : command.getBranches()) {
					infectList.add(branch.getPid());
				}

				infects.put("RDBRANCH", infectList);

				infectList = new ArrayList<Integer>();

				for (RdLaneConnexity laneConn : command.getLanes()) {
					infectList.add(laneConn.getPid());
				}

				infects.put("RDLANECONNEXITY", infectList);

				infectList = new ArrayList<Integer>();

				for (RdSpeedlimit limit : command.getLimits()) {
					infectList.add(limit.getPid());
				}

				infects.put("RDSPEEDLIMIT", infectList);

				infectList = new ArrayList<Integer>();

				for (RdRestriction res : command.getRestrictions()) {
					infectList.add(res.getPid());
				}

				infects.put("RDRESTRICTION", infectList);

				infectList = new ArrayList<Integer>();

				return JSONObject.fromObject(infects).toString();
			}

		} catch (Exception e) {

			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {

			}
		}

		return null;
	}

	@Override
	public boolean recordData() throws Exception {

		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;

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
	public void postCheck() {

		// 对数据进行检查、检查结果存储在数据库，并存储在临时变量postCheckMsg中
	}

	@Override
	public String getPostCheck() throws Exception {

		return postCheckMsg;
	}

}
