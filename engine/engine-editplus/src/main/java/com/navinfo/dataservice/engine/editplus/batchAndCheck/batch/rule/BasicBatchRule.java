package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;

public abstract class BasicBatchRule {
	private BatchRuleCommand batchRuleCommand;
	private BatchRule batchRule;

	public BatchRule getBatchRule() {
		return batchRule;
	}

	public void setBatchRule(BatchRule batchRule) {
		this.batchRule = batchRule;
	}

	public BasicBatchRule() {
		// TODO Auto-generated constructor stub
	}
	
	public Map<Long, BasicObj> getRowList(){
		Map<Long, BasicObj> rows=new HashMap<Long, BasicObj>();
		//若全库批处理，则数据统一都是初始化状态，新增修改列表没有记录
		for(String objName:batchRule.getObjNameSet()){
			if(batchRuleCommand.getAllDatas().containsKey(objName)){
				Map<Long, BasicObj> map=batchRuleCommand.getAllDatas().get(objName);
				if(map!=null){rows.putAll(map);}}
			}
		return rows;
	}
	
	public void run() throws Exception{
		Map<Long, BasicObj> rows=getRowList();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
				runBatch(obj);
			}
		}
		}
	
	public abstract void runBatch(BasicObj obj) throws Exception;

	public BatchRuleCommand getBatchRuleCommand() {
		return batchRuleCommand;
	}

	public void setBatchRuleCommand(BatchRuleCommand batchRuleCommand) {
		this.batchRuleCommand = batchRuleCommand;
	}

}
