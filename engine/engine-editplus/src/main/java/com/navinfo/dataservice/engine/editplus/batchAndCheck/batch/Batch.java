package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;

public class Batch extends AbstractOperation{
	private BatchCommand batchCommand;

	public BatchCommand getBatchCommand() {
		return batchCommand;
	}

	public void setBatchCommand(BatchCommand batchCommand) {
		this.batchCommand = batchCommand;
	}

	public Batch(Connection conn,  OperationResult preResult) {
		super(conn,  preResult);
		// TODO Auto-generated constructor stub
	}

	//执行批处理
	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		log.info("start exe batch");
		BatchCommand batchCommand =(BatchCommand) cmd;
		setBatchCommand(batchCommand);
		//按照规则号list加载规则列表，以及汇总需要参考的子表map
		log.info("start load batch rule");
		Map<String, Set<String>> selConfig=new HashMap<String, Set<String>>();
		List<BatchRule> batchRuleList=new ArrayList<BatchRule>();
		for(String ruleId:batchCommand.getRuleIdList()){
			log.info("rule ID:"+ruleId);
			BatchRule rule=BatchRuleLoader.getInstance().loadByRuleId(ruleId);
			if (rule ==null) {
				log.warn("批处理规则加载失败，rule ID:"+ruleId);
				continue;
			}
			batchRuleList.add(rule);
			Map<String, Set<String>> tmpMap = rule.getReferSubtableMap();
			for(String manObjName:tmpMap.keySet()){
				Set<String> tmpSubtableSet=tmpMap.get(manObjName);
				if(selConfig.containsKey(manObjName)){
					tmpSubtableSet.addAll(selConfig.get(manObjName));
				}
				selConfig.put(manObjName, tmpSubtableSet);
			}
		}
		log.info("start load incre batch data");
		//增量加载需要参考的子表数据
		ObjChildrenIncreSelector.increSelect(conn,result.getAllObjsMap(), selConfig);
		log.info("end load incre batch data");
		//构造批处理规则的参数command
		BatchRuleCommand batchRuleCommand=new BatchRuleCommand();
		batchRuleCommand.setConn(conn);
		batchRuleCommand.setAllDatas(result.getAllObjsMap());
		log.info("start run batch rule");
		//顺序执行批处理规则
		BatchExcuter excuter=new BatchExcuter();
		for(BatchRule rule:batchRuleList){
			excuter.exeRule(rule, batchRuleCommand);
		}
		log.info("start put changeReferData to operationResult");
		/*若存在修改参考数据的规则，则遍历batchRuleCommand中的referDatas将修改的数据put入result中；
		 * 调用batch的调用方，通过batch.persistChangeLog将变更持久化*/
		//if(changeReferData){
		for(Map<Long, BasicObj> referMap:batchRuleCommand.getReferDatas().values()){
			for(BasicObj obj:referMap.values()){
				if (obj.isChanged()) {
					result.putObj(obj);
				}
			}
		}
		//}
		log.info("end exe batch");
	}

	@Override
	public String getName() {
		if(getBatchCommand().getOperationName()==null||getBatchCommand().getOperationName().isEmpty()){
			return getBatchCommand().getRuleId();
		}else{return getBatchCommand().getOperationName();}
	}
}
