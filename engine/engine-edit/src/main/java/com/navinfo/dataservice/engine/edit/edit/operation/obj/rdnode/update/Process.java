package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdnode.update;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.engine.edit.edit.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.model.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	private RdNode rdnode;

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

		RdNodeSelector selector = new RdNodeSelector(this.conn);

		this.rdnode = (RdNode) selector.loadById(command.getPid(),
				true);

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

			IOperation operation = new Operation(command, this.rdnode);

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

		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;
	}

}
