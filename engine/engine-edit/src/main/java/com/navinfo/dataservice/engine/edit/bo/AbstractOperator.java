package com.navinfo.dataservice.engine.edit.bo;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.model.OperationResult;
import com.navinfo.dataservice.engine.edit.operation.OperatorFactory;
import com.navinfo.dataservice.engine.edit.operation.PoiMsgPublisher;

/** 
 * @ClassName: AbstractOperator
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AbstractOperator.java
 */
public abstract class AbstractOperator {
	
	protected OperType opType;
	protected ObjType objType;
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
		return opType;
	}

	public void setOpType(OperType opType) {
		this.opType = opType;
	}

	public ObjType getObjType() {
		return objType;
	}

	public void setObjType(ObjType objType) {
		this.objType = objType;
	}
}
