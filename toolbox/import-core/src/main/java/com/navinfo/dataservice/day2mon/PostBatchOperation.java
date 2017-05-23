package com.navinfo.dataservice.day2mon;

import java.sql.Connection;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class PostBatchOperation extends AbstractOperation{
	
	String actionName=null;

	public PostBatchOperation(Connection conn, OperationResult preResult) {
		super(conn, preResult);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return actionName;
	}

	public void setName(String actName) {
		// TODO Auto-generated method stub
		actionName=actName;
	}
	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
