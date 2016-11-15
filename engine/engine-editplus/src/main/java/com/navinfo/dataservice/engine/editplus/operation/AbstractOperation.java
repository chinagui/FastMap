package com.navinfo.dataservice.engine.editplus.operation;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.editplus.bo.CommandCreateException;

/** 
 * @ClassName: AbstractOperation
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AbstractOperator.java
 */
public abstract class AbstractOperation {
	
	protected AbstractCommand cmd;
	
	protected Connection conn;
	
	public abstract void createCmd(JSONObject data) throws CommandCreateException;
	
    public abstract void loadData() throws Exception;
    
    public abstract OperationResult execute() throws Exception;
    
    public void flush(OperationResult result) throws Exception{
//    	LogWriter lw = new LogWriter(conn);
//		lw.generateLog(cmd, result);
//		OperatorFactory.recordData(conn, result);
//		lw.recordLog(cmd, result);

//		PoiMsgPublisher.publish(result);
    }
    
    public AbstractCommand getCmd() {
		return cmd;
	}
	public void setCmd(AbstractCommand cmd) {
		this.cmd = cmd;
	}
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public OperType getOpType() {
		return cmd.opType;
	}

	public ObjType getObjType() {
		return cmd.objType;
	}

}
