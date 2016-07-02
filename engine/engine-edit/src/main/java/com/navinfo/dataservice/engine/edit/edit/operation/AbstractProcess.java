package com.navinfo.dataservice.engine.edit.edit.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.check.CheckEngine;

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
	private CheckCommand checkCommand=new CheckCommand();
	private CheckEngine checkEngine=null;
	
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
	
	public AbstractProcess(AbstractCommand command) throws Exception {
		this.command = (T)command;
		this.result = new Result();
		this.conn = DBConnector.getInstance().getConnectionById(this.command
				.getDbId());
		//初始化检查参数
		this.initCheckCommand();
	}
	
	//初始化检查参数
	public void initCheckCommand() throws Exception{
		this.checkCommand.setObjType(this.command.getObjType());
		this.checkCommand.setOperType(this.command.getOperType());
		//this.checkCommand.setGlmList(this.command.getGlmList());
		this.checkEngine=new CheckEngine(checkCommand,this.conn);
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
		createPreCheckGlmList();
		return checkEngine.preCheck();
	}
	//构造前检查参数。前检查，如果command中的构造不满足前检查参数需求，则需重写该方法，具体可参考createPostCheckGlmList
	public void createPreCheckGlmList(){
		List<IRow> resultList=new ArrayList<IRow>();
		Result resultObj=this.getResult();
		if(resultObj.getAddObjects().size()>0){resultList.addAll(resultObj.getAddObjects());}
		if(resultObj.getUpdateObjects().size()>0){resultList.addAll(resultObj.getUpdateObjects());}
		if(resultObj.getDelObjects().size()>0){resultList.addAll(resultObj.getDelObjects());}
		this.checkCommand.setGlmList(resultList);
	} 
	
	public abstract String exeOperation() throws Exception;
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#run()
	 */
	@Override
	public String run() throws Exception {
		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			msg =  exeOperation();//new Operation(command, conn);
			
			if(this.getCommand().getOperType().equals(OperType.CREATE))
			{
				handleResult(this.getCommand().getObjType(), result);
			}
			
			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
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
		this.createPostCheckGlmList();
		this.checkEngine.postCheck();

	}
	//构造后检查参数
	public void createPostCheckGlmList(){
		List<IRow> resultList=new ArrayList<IRow>();
		Result resultObj=this.getResult();
		if(resultObj.getAddObjects().size()>0){resultList.addAll(resultObj.getAddObjects());}
		if(resultObj.getUpdateObjects().size()>0){resultList.addAll(resultObj.getUpdateObjects());}
		this.checkCommand.setGlmList(resultList);
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
		LogWriter lw = new LogWriter(conn);
		lw.generateLog(command, result);
		OperatorFactory.recordData(conn, result);
		lw.recordLog(command, result);
		
		PoiMsgPublisher.publish(result);
		
		return true;
	}

	public CheckCommand getCheckCommand() {
		return checkCommand;
	}

	public void setCheckCommand(CheckCommand checkCommand) {
		this.checkCommand = checkCommand;
	}
	
	public void handleResult(ObjType objType,Result result)
	{
		for(IRow row : result.getAddObjects())
		{
			if(objType.equals(row.objType()))
			{
				result.setPrimaryPid(row.parentPKValue());
				
				break;
			}
		}
	}
}
