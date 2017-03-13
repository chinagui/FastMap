package com.navinfo.dataservice.day2mon;

import java.sql.Connection;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class PostBatchOperation extends AbstractOperation{

	public PostBatchOperation(Connection conn, OperationResult preResult) {
		super(conn, preResult);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
