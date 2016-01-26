package com.navinfo.dataservice.FosEngine.edit.operation.topo.departnode;

import java.sql.Connection;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node.RdNodeSelector;
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
	
	private RdLink updateLink;
	
	private RdNode updateNode;
	
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
		
		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

		this.updateLink = (RdLink) linkSelector.loadById(command.getLinkPid(),
				true);
		
		RdNodeSelector nodeSelector = new RdNodeSelector(this.conn);
		
		this.updateNode = (RdNode) nodeSelector.loadById(command.getNodePid(), true);
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkIsCrossNode(conn, command.getNodePid());
		
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

			IOperation operation = new Operation(command, updateLink,updateNode,check);

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
