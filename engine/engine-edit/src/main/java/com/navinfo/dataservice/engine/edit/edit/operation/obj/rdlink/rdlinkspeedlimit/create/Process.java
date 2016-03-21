package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.engine.edit.edit.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.edit.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.model.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

public class Process implements IProcess {
	
	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private RdSpeedlimit rdSpeedlimit;

	private List<RdLink> rdLinks;
	
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

		RdSpeedlimitSelector slSelector = new RdSpeedlimitSelector(conn);
		
		rdSpeedlimit = (RdSpeedlimit) slSelector.loadById(command.getPid(), true);
		
		RdLinkSelector rdLinkSelector = new RdLinkSelector(conn);
		
		String path = slSelector.trackSpeedLimitLink(rdSpeedlimit.getLinkPid(), rdSpeedlimit.getDirect());
		
		String[] splits = path.split(",");
		
		rdLinks = new ArrayList<RdLink>();
		
		for(String str : splits){
			rdLinks.add((RdLink) rdLinkSelector.loadById(Integer.parseInt(str), true));
		}
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		// TODO Auto-generated method stub
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

			IOperation operation = new Operation(command,rdSpeedlimit,rdLinks);

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
