package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.log.LogGenerator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: Operation
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: Operation.java
 */
public class CheckOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	protected String name="CHECK";
	protected CheckCommand cmd;
	protected OperationResult result;
	protected Connection conn;
	
	public CheckOperation(Connection conn,OperationResult preResult){
		this.conn=conn;
		if(preResult==null){
			result=new OperationResult();
		}else{
			this.result=preResult;
		}
	}
	public String getName() {
		return name;
	}
	public OperationResult getResult() {
		return result;
	}
	public CheckCommand getCmd() {
		return cmd;
	}
	public void setCmd(CheckCommand cmd) {
		this.cmd = cmd;
	}
}
