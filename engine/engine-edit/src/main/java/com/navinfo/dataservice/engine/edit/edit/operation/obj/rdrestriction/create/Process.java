package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private Check check = new Check();

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

		return false;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkNoSameRelation(conn, command.getInLinkPid(), command.getNodePid());
		
		check.checkGLM08004(conn, command.getInLinkPid(), command.getOutLinkPids());
		
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

			IOperation operation = new Operation(command, conn, check);

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
//		JSONArray array = new CkExceptionOperator(conn).check(result.getPrimaryPid(), ObjType.RDRESTRICTION);
//		
//		result.setCheckResults(array);
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
