package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;

import net.sf.json.JSONObject;

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
	
	public abstract void createCmd(JSONObject data);
	
    public abstract void loadData();
    
    public abstract Result execute();
    
    public void flush(){
    	
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
