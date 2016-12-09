package com.navinfo.dataservice.day2mon;

import org.apache.log4j.Logger;

import com.mysql.jdbc.Connection;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;

public class PreBatch {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult ;
	private Connection conn;

	public PreBatch(OperationResult opResult,Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
	}
	public OperationResult execute() throws Exception{
		OperationResult opResult = this.opResult;
		// 批处理
		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-124");
		batchCommand.setRuleId("FM-BAT-20-137");
		batchCommand.setRuleId("FM-BAT-20-138");
		batchCommand.setRuleId("FM-BAT-20-110");
		
		Batch batch=new Batch(conn,opResult);
		batch.operate(batchCommand);
		return opResult;
	}
	
}
