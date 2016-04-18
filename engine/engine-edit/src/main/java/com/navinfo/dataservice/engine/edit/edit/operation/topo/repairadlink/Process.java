package com.navinfo.dataservice.engine.edit.edit.operation.topo.repairadlink;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {
	
	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private AdLink updateLink;
	
	private AdNode snode;
	
	private AdNode enode;
	
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
		
		this.updateLink = (AdLink) new AdLinkSelector(conn).loadById(command.getLinkPid(), true);
		
		AdNodeSelector nodeSelector = new AdNodeSelector(conn);
		
		this.snode = (AdNode) nodeSelector.loadById(updateLink.getStartNodePid(), true);
		
		this.enode = (AdNode) nodeSelector.loadById(updateLink.getEndNodePid(), true);
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		check.checkIsVia(conn, command.getLinkPid());
		
		check.checkShapePointDistance(command.getLinkGeom());
		
		return null;
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

			Operation op = new Operation(conn, command,updateLink,snode,enode,check);

			op.run(result);

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
	public void postCheck() throws Exception {
		

	}

	@Override
	public String getPostCheck() throws Exception {
		
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
