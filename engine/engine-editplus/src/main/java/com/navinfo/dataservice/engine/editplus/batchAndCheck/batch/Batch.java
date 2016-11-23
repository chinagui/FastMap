package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;

public class Batch {

	public Batch() {
		// TODO Auto-generated constructor stub
	}
	//执行批处理
	public static void run(BatchCommand batchCommand) throws Exception{
		BatchExcuter excuter=new BatchExcuter();
		for(String ruleId:batchCommand.getRuleId()){
			//BatchRule rule=BatchRuleLoader.getInstance().loadByRuleId(ruleId);
			BatchRule rule=new BatchRule();
			rule.setAccessorType("JAVA");
			rule.setAccessor("com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule.GLM001TEST");
			excuter.exeRule(rule, batchCommand);
		}
	}
}
