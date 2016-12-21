package com.navinfo.dataservice.day2mon;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
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
		log.info("开始执行前批");
		// 批处理
		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setOperationName("BATCH_DAY2MONTH_BEFORE");
//		batchCommand.setRuleId("FM-BAT-20-124");
//		batchCommand.setRuleId("FM-BAT-20-137");
//		batchCommand.setRuleId("FM-BAT-20-138");
//		batchCommand.setRuleId("FM-BAT-20-110");
		
		log.info("要执行的规则号:"+batchCommand.getRuleIdList().toString());
		Batch batch=new Batch(conn,opResult);
		batch.operate(batchCommand);
		batch.persistChangeLog(OperationSegment.SG_COLUMN, 0);//FIXME:修改默认的用户
		log.info("前批完成");
		return opResult;
	}
	
}
