package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.move;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {
	
	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private AdAdmin moveAdmin;
	
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
		AdAdminSelector adAdminSelector = new AdAdminSelector(this.conn);
		
		this.moveAdmin = (AdAdmin) adAdminSelector.loadById(command.getPid(), true);
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		return null;
	}

	@Override
	public String run() throws Exception {
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(command,moveAdmin);

			operation.run(result);

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
