package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.operation.OperationResult;

public class BasicCommand {
	private OperationResult operationResult;
	private List<String> ruleIdList;
	private Connection conn;

	public BasicCommand() {
		// TODO Auto-generated constructor stub
	}

	public List<String> getRuleId() {
		return ruleIdList;
	}

	public void setRuleId(List<String> ruleId) {
		this.ruleIdList = ruleId;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public OperationResult getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

}
