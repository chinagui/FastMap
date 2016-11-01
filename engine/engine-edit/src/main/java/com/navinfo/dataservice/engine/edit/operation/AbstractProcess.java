package com.navinfo.dataservice.engine.edit.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.log.LoggerRepos;
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
	private CheckCommand checkCommand = new CheckCommand();
	private CheckEngine checkEngine = null;
	public static Logger log = Logger.getLogger(AbstractProcess.class);

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

	public AbstractProcess() {
		this.log = LoggerRepos.getLogger(this.log);
	}

	public AbstractProcess(AbstractCommand command, Result result, Connection conn) throws Exception {
		this.command = (T) command;
		if (conn != null) {
			this.conn = conn;
		}
		if (result != null) {
			this.result = result;
		} else {
			result = new Result();
		}
		// 初始化检查参数
		this.initCheckCommand();
	}

	public AbstractProcess(AbstractCommand command) throws Exception {
		this.command = (T) command;
		this.result = new Result();
		if (!command.isHasConn()) {
			this.conn = DBConnector.getInstance().getConnectionById(this.command.getDbId());
		}
		// 初始化检查参数
		this.initCheckCommand();
	}

	// 初始化检查参数
	public void initCheckCommand() throws Exception {
		this.checkCommand.setObjType(this.command.getObjType());
		this.checkCommand.setOperType(this.command.getOperType());
		// this.checkCommand.setGlmList(this.command.getGlmList());
		this.checkEngine = new CheckEngine(checkCommand, this.conn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getCommand()
	 */
	@Override
	public T getCommand() {
		return command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getResult()
	 */
	@Override
	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#prepareData()
	 */
	@Override
	public boolean prepareData() throws Exception {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#preCheck()
	 */
	@Override
	public String preCheck() throws Exception {
		// TODO Auto-generated method stub
		// createPreCheckGlmList();
		this.checkCommand.setGlmList(this.getResult().getAddObjects());
		this.checkCommand.setListStatus("ADD");
		String msg = checkEngine.preCheck();

		if (msg != null && !msg.isEmpty()) {
			return msg;
		}

		this.checkCommand.setGlmList(this.getResult().getUpdateObjects());
		this.checkCommand.setListStatus("UPDATE");
		msg = checkEngine.preCheck();
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}

		this.checkCommand.setGlmList(this.getResult().getDelObjects());
		this.checkCommand.setListStatus("DEL");
		msg = checkEngine.preCheck();
		return msg;
	}

	// 构造前检查参数。前检查，如果command中的构造不满足前检查参数需求，则需重写该方法，具体可参考createPostCheckGlmList
	public void createPreCheckGlmList() {
		List<IRow> resultList = new ArrayList<IRow>();
		Result resultObj = this.getResult();
		if (resultObj.getAddObjects().size() > 0) {
			resultList.addAll(resultObj.getAddObjects());
		}
		if (resultObj.getUpdateObjects().size() > 0) {
			resultList.addAll(resultObj.getUpdateObjects());
		}
		if (resultObj.getDelObjects().size() > 0) {
			resultList.addAll(resultObj.getDelObjects());
		}
		this.checkCommand.setGlmList(resultList);
	}

	public abstract String exeOperation() throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#run()
	 */
	@Override
	public String run() throws Exception {
		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			msg = exeOperation();

			checkResult();

			if (!this.getCommand().getOperType().equals(OperType.DELETE)
					&& !this.getCommand().getObjType().equals(ObjType.RDBRANCH)
					&& !this.getCommand().getObjType().equals(ObjType.RDELECEYEPAIR)
					&& !this.getCommand().getObjType().equals(ObjType.LUFACE)
					&& !this.getCommand().getObjType().equals(ObjType.LCFACE)) {
				handleResult(this.getCommand().getObjType(), this.getCommand().getOperType(), result);
			}

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			this.recordData();
			long startPostCheckTime = System.currentTimeMillis();
			log.info("BEGIN  POSTCHECK ");
			this.postCheck();
			long endPostCheckTime = System.currentTimeMillis();
			log.info("BEGIN  POSTCHECK ");
			log.info("post check use time   " + String.valueOf(endPostCheckTime - startPostCheckTime));
			conn.commit();

			System.out.print("操作成功\r\n");

		} catch (Exception e) {

			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();

				System.out.print("结束\r\n");
			} catch (Exception e) {

			}
		}

		return msg;
	}

	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();

			msg = exeOperation();

			checkResult();

			if (!this.getCommand().getOperType().equals(OperType.DELETE)
					&& !this.getCommand().getObjType().equals(ObjType.RDBRANCH)
					&& !this.getCommand().getObjType().equals(ObjType.RDELECEYEPAIR)
					&& !this.getCommand().getObjType().equals(ObjType.LUFACE)
					&& !this.getCommand().getObjType().equals(ObjType.LCFACE)) {
				handleResult(this.getCommand().getObjType(), this.getCommand().getOperType(), result);
			}

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			this.recordData();

			this.postCheck();

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				System.out.print("结束\r\n");
			} catch (Exception e) {

			}
		}

		return msg;
	}

	/**
	 * 检查请求是否执行了某些操作
	 * 
	 * @throws Exception
	 */
	private void checkResult() throws Exception {
		if (this.getCommand().getObjType().equals(ObjType.IXPOI)) {
			return;
		} else if (CollectionUtils.isEmpty(result.getAddObjects()) && CollectionUtils.isEmpty(result.getUpdateObjects())
				&& CollectionUtils.isEmpty(result.getDelObjects())) {
			throw new DataNotChangeException("属性值未发生变化");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IProcess#postCheck()
	 */
	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub
		// this.createPostCheckGlmList();
		this.checkCommand.setGlmList(this.getResult().getAddObjects());
		this.checkCommand.setListStatus("ADD");
		this.checkEngine.postCheck();

		this.checkCommand.setGlmList(this.getResult().getUpdateObjects());
		this.checkCommand.setListStatus("UPDATE");
		this.checkEngine.postCheck();

	}

	// 构造后检查参数
	public void createPostCheckGlmList() {
		List<IRow> resultList = new ArrayList<IRow>();
		Result resultObj = this.getResult();
		if (resultObj.getAddObjects().size() > 0) {
			resultList.addAll(resultObj.getAddObjects());
		}
		if (resultObj.getUpdateObjects().size() > 0) {
			resultList.addAll(resultObj.getUpdateObjects());
		}
		this.checkCommand.setGlmList(resultList);
	}

	@Override
	public String getPostCheck() throws Exception {
		return postCheckMsg;
	}

	@Override
	public boolean recordData() throws Exception {
		LogWriter lw = new LogWriter(conn);
		lw.setUserId(command.getUserId());
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

	public void handleResult(ObjType objType, OperType operType, Result result) {
		switch (operType) {
		case CREATE:
		case BREAK:
			List<Integer> addObjPidList = result.getListAddIRowObPid();
			for (int i = 0; i < result.getAddObjects().size(); i++) {
				IRow row = result.getAddObjects().get(i);
				if (objType.equals(row.objType())) {
					if (addObjPidList.get(i) != null) {
						result.setPrimaryPid(addObjPidList.get(i));
						break;
					}
				}
			}
			break;
		case UPDATE:
			for (IRow row : result.getAddObjects()) {
				result.setPrimaryPid(row.parentPKValue());
				return;
			}
			for (IRow row : result.getUpdateObjects()) {
				result.setPrimaryPid(row.parentPKValue());
				return;
			}
			for (IRow row : result.getDelObjects()) {
				result.setPrimaryPid(row.parentPKValue());
				return;
			}
			break;
		case REPAIR:
			List<Integer> allObjPidList = new ArrayList<>();
			List<IRow> allIRows = new ArrayList<>();
			allIRows.addAll(result.getUpdateObjects());
			allObjPidList.addAll(result.getListUpdateIRowObPid());
			if(CollectionUtils.isEmpty(allObjPidList))
			{
				allIRows.addAll(result.getAddObjects());
				allObjPidList.addAll(result.getListAddIRowObPid());
			}
			for (int i = 0; i < allIRows.size(); i++) {
				IRow row = allIRows.get(i);
				if (objType.equals(row.objType())) {
					if (allObjPidList.get(i) != null) {
						result.setPrimaryPid(allObjPidList.get(i));
						break;
					}
				}
			}
			break;
		default:
			break;
		}
	}
}
