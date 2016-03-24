package com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	private RdLink updateLink;

	private Check check = new Check();

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

	}
	
	public Process(ICommand command, Connection conn)  {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = conn;
	}

	@Override
	public ICommand getCommand() {

		return command;
	}

	@Override
	public Result getResult() {

		return result;
	}

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		RdRestrictionSelector restriction = new RdRestrictionSelector(this.conn);

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByLinkNode(command.getLinkPid(),
						command.getsNodePid(), command.geteNodePid(), true);

		command.setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.conn);

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkNode(
				command.getLinkPid(), command.getsNodePid(),
				command.geteNodePid(), true);

		command.setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.conn);

		List<RdBranch> branches = selector.loadRdBranchByLinkNode(
				command.getLinkPid(), command.getsNodePid(),
				command.geteNodePid(), true);

		command.setBranches(branches);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

		this.updateLink = (RdLink) linkSelector.loadById(command.getLinkPid(),
				true);

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		return true;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkIsCrossNode(conn, command.getsNodePid());

		check.checkIsCrossNode(conn, command.geteNodePid());

		check.checkIsVia(conn, command.getLinkPid());

		return null;
	}

	@Override
	public String run() throws Exception {
		try {
			conn.setAutoCommit(false);

			String preCheckMsg = this.preCheck();

			this.prepareData();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(command, updateLink, check);

			operation.run(result);

			processRefObj();

			this.recordData();

			this.postCheck();

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

	public void processRefObj() throws Exception {
		IOperation opRefRestrict = new OpRefRestrict(command);
		opRefRestrict.run(result);

		IOperation opRefBranch = new OpRefBranch(command);
		opRefBranch.run(result);

		IOperation opRefLaneConnexity = new OpRefLaneConnexity(command);
		opRefLaneConnexity.run(result);
	}

	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPostCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordData() throws Exception {
		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;
	}

}
