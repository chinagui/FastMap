package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

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

		PreparedStatement stmt = null;

		ResultSet resultSet = null;

		try {
			// 检查link是否作为交线、分歧、车信的退出线或者经过线
			String sql = "select a.detail_id, '交限' type   from rd_restriction_detail a  where a.out_link_pid = :1 union all (select b.link_pid, '交限' type from rd_restriction_via b where b.link_pid = :2)  union all (select c.topo_id,'车信' type from rd_lane_topo_detail c where c.out_link_pid = :3)  union all (select d.link_pid,'车信' type from rd_lane_via d where d.link_pid = :4)  union all (select e.branch_pid, '分歧' type from rd_branch e where e.out_link_pid = :5)  union all (select f.branch_pid, '分歧' type from rd_branch_via f where f.link_pid=:6) ";
			
			stmt = conn.prepareStatement(sql);

			stmt.setInt(1, command.getLinkPid());

			stmt.setInt(2, command.getLinkPid());
			
			stmt.setInt(3, command.getLinkPid());
			
			stmt.setInt(4, command.getLinkPid());
			
			stmt.setInt(5, command.getLinkPid());
			
			stmt.setInt(6, command.getLinkPid());

			resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				String type = resultSet.getString("type");
				
				return "此link上存在"+type+"关系信息，删除该Link会对应删除此组关系";
			} else {
				return null;
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			releaseResource(stmt, resultSet);
		}
	}

	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.conn);

		RdLink link = (RdLink) selector.loadById(command.getLinkPid(), true);

		command.setLink(link);
	}

	// 锁定盲端节点
	public void lockRdNode() throws Exception {

		RdNodeSelector selector = new RdNodeSelector(this.conn);

		List<RdNode> nodes = selector.loadEndRdNodeByLinkPid(command.getLinkPid(),
				false);
		
		List<Integer> nodePids = new ArrayList<Integer>();
		
		for(RdNode node : nodes){
			nodePids.add(node.getPid());
		}

		command.setNodes(nodes);
		
		command.setNodePids(nodePids);
	}

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.conn);

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByLinkPid(command.getLinkPid(), true);

		command.setRestrictions(restrictions);
	}
	
	public void lockRdLaneConnexity() throws Exception {
		
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.conn);
		
		List<RdLaneConnexity> lanes  = selector.loadRdLaneConnexityByLinkPid(command.getLinkPid(), true);
		
		command.setLanes(lanes);
	}
	
	public void lockRdBranch() throws Exception {
		
		RdBranchSelector selector = new RdBranchSelector(this.conn);
		
		List<RdBranch> branches = selector.loadRdBranchByInLinkPid(command.getLinkPid(), true);
		
		command.setBranches(branches);
	}
	
	public void lockRdCross() throws Exception {
		
		RdCrossSelector selector = new RdCrossSelector(this.conn);
		
		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(command.getNodePids(), command.getLinkPid(), true);
		
		command.setCrosses(crosses);
	}
	
	public void lockRdSpeedlimits() throws Exception {
		
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.conn);
		
		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPid(command.getLinkPid(), true);
		
		command.setLimits(limits);
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

		if (command.getLink() == null) {

			throw new Exception("指定删除的LINK不存在！");
		}

		lockRdNode();

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
				recordData();
				postCheck();
				conn.commit();
			}else{
				Map<String,List<Integer>> infects = new HashMap<String,List<Integer>>();
				
				List<Integer> infectList = new ArrayList<Integer>();
				
				infectList = new ArrayList<Integer>();
				
				for(RdBranch branch: command.getBranches()){
					infectList.add(branch.getPid());
				}
				
				infects.put("RDBRANCH", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdLaneConnexity laneConn: command.getLanes()){
					infectList.add(laneConn.getPid());
				}
				
				infects.put("RDLANECONNEXITY", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdSpeedlimit limit : command.getLimits()){
					infectList.add(limit.getPid());
				}
				
				infects.put("RDSPEEDLIMIT", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdRestriction res: command.getRestrictions()){
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
