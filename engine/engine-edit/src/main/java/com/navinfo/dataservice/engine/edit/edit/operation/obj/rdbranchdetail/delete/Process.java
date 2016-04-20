package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranchdetail.delete;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	private RdBranchDetail detail;
	
	private RdBranch branch;

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

	@Override
	public boolean prepareData() throws Exception {

		RdBranchDetailSelector selector = new RdBranchDetailSelector(this.conn);

		this.detail = (RdBranchDetail) selector.loadById(command.getPid(),
				true);
		
		RdBranchSelector branchSelector = new RdBranchSelector(this.conn);
		
		this.branch = (RdBranch) branchSelector.loadById(detail.getBranchPid(), true);

		return true;
	}

	@Override
	public String preCheck() throws Exception {

		return null;
	}

	@Override
	public String run() throws Exception {
		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(command, this.detail, this.branch);

			msg = operation.run(result);

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

		return msg;
	}

	@Override
	public void postCheck() throws Exception {

	}

	@Override
	public String getPostCheck() throws Exception {

		return postCheckMsg;
	}

	@Override
	public boolean recordData() throws Exception {
		
		LogWriter lw = new LogWriter(conn, this.command.getProjectId());
		
		lw.generateLog(command, result);
		
		OperatorFactory.recordData(conn, result);

		lw.recordLog(command, result);

		return true;
	}

}
