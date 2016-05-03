package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;
/**
 * @author zhaokk
 * 修改行政区划线参数基础类 
 */
public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	private AdLink updateLink;

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = GlmDbPoolManager.getInstance().getConnection(this.command
				.getProjectId());

	}
	public Process(ICommand command,Result result,Connection conn) throws Exception {
		this.command = (Command) command;
		this.result = result;
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

	@Override
	public boolean prepareData() throws Exception {
		
		AdLinkSelector linkSelector = new AdLinkSelector(this.conn);
		//加载对应AD_LINK信息
		this.updateLink = (AdLink)linkSelector.loadById(command.getLinkPid(),true);

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

			IOperation operation = new Operation(command, updateLink);

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
	public String innerRun() throws Exception {
		try {
			this.prepareData();
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			IOperation operation = new Operation(command, updateLink);
			operation.run(result);
			this.postCheck();

		} catch (Exception e) {

			conn.rollback();

			throw e;
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
		
		LogWriter lw = new LogWriter(conn, this.command.getProjectId());
		
		lw.generateLog(command, result);
		
		OperatorFactory.recordData(conn, result);

		lw.recordLog(command, result);

		return true;
	}

}
