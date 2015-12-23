package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;
import com.navinfo.dataservice.FosEngine.edit.operation.OperatorFactory;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;

public class Process implements IProcess {

	private static Logger logger = Logger.getLogger(Process.class);
	
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
			// 检查link是否作为交线的退出线或者经过线
			String sql = "select a.detail_id from rd_restriction_detail a where a.out_link_pid=:1 "
					+ "union all (select b.link_pid from rd_restriction_via b where b.link_pid=:2)";

			stmt = conn.prepareStatement(sql);

			stmt.setInt(1, command.getLinkPid());

			stmt.setInt(2, command.getLinkPid());

			resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				return "此link上存在交限关系信息，删除该Link会对应删除此组关系";
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

		RdNodeSelector node = new RdNodeSelector(this.conn);

		List<RdNode> nodes = node.loadEndRdNodeByLinkPid(command.getLinkPid(),
				false);

		command.setNodes(nodes);
	}

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		// 获取进入线为该link的交限

		RdRestrictionSelector restriction = new RdRestrictionSelector(this.conn);

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByLinkPid(command.getLinkPid(), true);

		command.setRestrictions(restrictions);
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

		return true;
	}

	@Override
	public String run() throws Exception {

		try {
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
