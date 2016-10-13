package com.navinfo.dataservice.engine.edit.operation.obj.adlink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Check;
/**
 * @author zhaokk
 * 修改行政区划线参数基础类 
 */
public class Process extends AbstractProcess<Command> {

//	private Command command;
//
//	private Result result;
//
//	private Connection conn;
//
//	private String postCheckMsg;

	private AdLink updateLink;
	
	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
//		this.command = (Command) command;
//
//		this.result = new Result();
//
//		this.conn = GlmDbPoolManager.getInstance().getConnection(this.command
//				.getProjectId());

	}
	
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),this.updateLink).run(this.getResult());

	}
	
//	@Override
//	public ICommand getCommand() {
//		
//		return command;
//	}
//
//	@Override
//	public Result getResult() {
//		
//		return result;
//	}
//
	@Override
	public boolean prepareData() throws Exception {
		
		AdLinkSelector linkSelector = new AdLinkSelector(this.getConn());
		//加载对应AD_LINK信息
		this.updateLink = (AdLink)linkSelector.loadById(this.getCommand().getLinkPid(),true);

		return false;
	}

//	@Override
//	public String preCheck() throws Exception {
//		
//		return null;
//	}

//
//	@Override
//	public String run() throws Exception {
//		try {
//			this.getConn().setAutoCommit(false);
//
//			this.prepareData();
//
//			String preCheckMsg = this.preCheck();
//
//			if (preCheckMsg != null) {
//				throw new Exception(preCheckMsg);
//			}
//
//			IOperation operation = new Operation(command, updateLink);
//
//			operation.run(result);
//
//			this.recordData();
//
//			this.postCheck();
//
//			conn.commit();
//
//		} catch (Exception e) {
//
//			conn.rollback();
//
//			throw e;
//		} finally {
//			try {
//				conn.close();
//			} catch (Exception e) {
//
//			}
//		}
//
//		return null;
//	}
	public String innerRun() throws Exception {
		try {
			this.prepareData();
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			IOperation operation = new Operation(this.getCommand(), updateLink);
			operation.run(this.getResult());
			this.postCheck();

		} catch (Exception e) {

			getConn().rollback();

			throw e;
		}

		return null;
	}
//	@Override
//	public void postCheck() throws Exception {
//		
//
//	}
//
//	@Override
//	public String getPostCheck() throws Exception {
//		
//		return null;
//	}
//
//	@Override
//	public boolean recordData() throws Exception {
//		
//		LogWriter lw = new LogWriter(conn, this.command.getDbId());
//		
//		lw.generateLog(command, result);
//		
//		OperatorFactory.recordData(conn, result);
//
//		lw.recordLog(command, result);
//
//		return true;
//	}


}
