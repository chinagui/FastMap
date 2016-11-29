package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class BasicRuleCommand {
	private Map<String,Map<Long,BasicObj>> allDatas;
	private Connection conn;

	public BasicRuleCommand() {
		// TODO Auto-generated constructor stub
	}
	
	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public Map<String,Map<Long,BasicObj>> getAllDatas() {
		return allDatas;
	}

	public void setAllDatas(Map<String,Map<Long,BasicObj>> allDatas) {
		this.allDatas = allDatas;
	}

}
