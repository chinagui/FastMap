package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule.BasicBatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;

public class BatchExcuter {

	public BatchExcuter() {
		// TODO Auto-generated constructor stub
	}
	
	public void exeRule(BatchRule batchRule,BatchCommand batchCommand) throws Exception{
		if(batchRule.getAccessorType().equals("JAVA")){
			exeJavaRule(batchRule, batchCommand);
		}
	}
	
	public void exeJavaRule(BatchRule batchRule,BatchCommand batchCommand) throws Exception{
		BasicBatchRule ruleObj=(BasicBatchRule) batchRule.getAccessorClass().newInstance();
		ruleObj.setBatchCommand(batchCommand);
		ruleObj.run();
	}

}
