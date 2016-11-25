package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.operation.OperationResult;

public class Batch {

	public Batch() {
		// TODO Auto-generated constructor stub
	}
	//执行批处理
	public static OperationResult run(BatchCommand batchCommand) throws Exception{
		BatchExcuter excuter=new BatchExcuter();
		for(String ruleId:batchCommand.getRuleId()){
			BatchRule rule=BatchRuleLoader.getInstance().loadByRuleId(ruleId);
			/*BatchRule rule=new BatchRule();
			rule.setAccessorType("JAVA");
			rule.setAccessor("com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule.GLM001TEST");
			*/
			excuter.exeRule(rule, batchCommand);
		}
		//需要调用数据入库的方法，方法：将变更持久化，同时返回整理后的OperationResult
		return batchCommand.getOperationResult();
	}
}
