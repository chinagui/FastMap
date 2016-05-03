package com.navinfo.dataservice.engine.edit.edit.operation;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.update.Command;

/** 
 * @ClassName: Abstractprocess
 * @author MaYunFei
 * @date 上午10:54:43
 * @Description: Abstractprocess.java
 */
public abstract class AbstractProcess<T extends AbstractCommand> implements IProcess {
	private T command;
	private Result result;
	private Connection conn;	
	
	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}
	
	public void setCommand(T command) {
		this.command = command;
		
	}
	
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	private String postCheckMsg;
	
	public AbstractProcess(T command) throws Exception {
		this.command =  command;
		this.result = new Result();
		this.conn = GlmDbPoolManager.getInstance().getConnection(this.command
				.getProjectId());

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getCommand()
	 */
	@Override
	public T getCommand() {
		return command;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getResult()
	 */
	@Override
	public Result getResult() {
		return result;
	}
	
	public void setResult(Result result) {
		this.result = result;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#prepareData()
	 */
	@Override
	public  boolean prepareData() throws Exception {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#preCheck()
	 */
	@Override
	public String preCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public abstract IOperation createOperation();
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#run()
	 */
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

			IOperation operation = createOperation();//new Operation(command, conn);

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

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#postCheck()
	 */
	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getPostCheck()
	 */
	@Override
	public String getPostCheck() throws Exception {
		return postCheckMsg;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#recordData()
	 */
	@Override
	public boolean recordData() throws Exception {
		LogWriter lw = new LogWriter(conn, this.command.getProjectId());
		lw.generateLog(command, result);
		OperatorFactory.recordData(conn, result);
		lw.recordLog(command, result);
		return true;
	}

}
