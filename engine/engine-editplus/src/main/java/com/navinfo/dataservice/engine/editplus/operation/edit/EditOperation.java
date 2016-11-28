package com.navinfo.dataservice.engine.editplus.operation.edit;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

/** 
 * @ClassName: AbstractOperation
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AbstractOperator.java
 */
public abstract class EditOperation extends AbstractOperation{
	
	/**
	 * @param conn
	 * @param name
	 * @param preResult
	 */
	public EditOperation(Connection conn, String name, OperationResult preResult) {
		super(conn, name, preResult);
		// TODO Auto-generated constructor stub
	}
	
    @Override
	public void operate() throws Exception {
//		op.setConn(conn);
//		op.loadData();
//		OperationResult result = op.execute();
//		CheckCommand checkCmd=null;
//		String preCheckMsg = preCheck();
//		if (preCheckMsg != null) {
//			throw new Exception(preCheckMsg);
//		}
//		op.flush(result);

//		postCheck();
	}




	public abstract void loadData() throws Exception;
    
    public abstract OperationResult execute() throws Exception;
    
    public void flush(OperationResult result) throws Exception{
//    	LogWriter lw = new LogWriter(conn);
//		lw.generateLog(cmd, result);
//		OperatorFactory.recordData(conn, result);
//		lw.recordLog(cmd, result);

//		PoiMsgPublisher.publish(result);
    }
   
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public OperType getOpType() {
		return ((EditCommand)cmd).opType;
	}

	public ObjType getObjType() {
		return ((EditCommand)cmd).objType;
	}

}
