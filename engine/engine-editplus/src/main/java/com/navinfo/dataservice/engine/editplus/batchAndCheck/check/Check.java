package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchExcuter;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchRuleLoader;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;

public class Check extends AbstractOperation{
	private List<NiValException> returnExceptions =new ArrayList<NiValException>();
	public Check(Connection conn, OperationResult preResult) {
		super(conn, preResult);
		// TODO Auto-generated constructor stub
	}

	//private static Logger log = Logger.getLogger(Check.class);
	
	/*
	 * 保存检查结果，调用此方法将检查结果插入到Ni_val_exception中
	 */
	public void saveCheckResult(Connection conn,List<NiValException> checkResultList) throws Exception{
		log.debug("start call insert ni_val");
		if (checkResultList==null || checkResultList.size()==0) {return;}		
		NiValExceptionOperator check = new NiValExceptionOperator(conn);
		for(int i=0;i<checkResultList.size();i++){			
			check.insertCheckLog(checkResultList.get(i).getRuleId(), checkResultList.get(i).getLoc(), checkResultList.get(i).getTargets(), checkResultList.get(i).getMeshId(),checkResultList.get(i).getInformation(), "TEST");
		}
		log.debug("end call insert ni_val");
	}
	@Override
	public void operate() throws Exception {
		log.info("start exe check");
		CheckCommand checkCommand =(CheckCommand) cmd;
		//按照规则号list加载规则列表，以及汇总需要参考的子表map
		log.info("load check rule");
		Map<String, Set<String>> selConfig=new HashMap<String, Set<String>>();
		List<CheckRule> checkRuleList=new ArrayList<CheckRule>();
		for(String ruleId:checkCommand.getRuleIdList()){
			CheckRule rule=CheckRuleLoader.getInstance().loadByRuleId(ruleId);
			checkRuleList.add(rule);
			Map<String, Set<String>> tmpMap = rule.getReferSubtableMap();
			for(String manObjName:tmpMap.keySet()){
				Set<String> tmpSubtableSet=tmpMap.get(manObjName);
				if(selConfig.containsKey(manObjName)){
					tmpSubtableSet.addAll(selConfig.get(manObjName));
				}
				selConfig.put(manObjName, tmpSubtableSet);
			}
		}
		log.info("start load incre check data");
		//增量加载需要参考的子表数据
		ObjChildrenIncreSelector.increSelect(conn,result.getAllObjsMap(), selConfig);
		log.info("end load incre check data");
		//构造批处理规则的参数command
		CheckRuleCommand checkRuleCommand=new CheckRuleCommand();
		checkRuleCommand.setConn(conn);
		checkRuleCommand.setAllDatas(result.getAllObjsMap());
		//顺序执行检查规则
		List<NiValException> checkResult=new ArrayList<NiValException>();
		CheckExcuter excuter=new CheckExcuter();
		log.info("start run check rule");
		for(CheckRule rule:checkRuleList){
			List<NiValException> checkResultTmp=new ArrayList<NiValException>();
			checkResultTmp=excuter.exeRule(rule, checkRuleCommand);
			checkResult.addAll(checkResultTmp);
			//isErrorReturn为ture，表示有错误log，则直接停止后续检查；false则继续执行，最后检查结果统一返回
			if(checkCommand.isErrorReturn() && !checkResult.isEmpty() && checkResult.size()>0){
				break;
			}
		}
		log.info("start save checkResult");
		//isSaveResult=true，则检查结果保存；否则不保存检查结果
		if(checkCommand.isSaveResult() &&!checkResult.isEmpty() && checkResult.size()>0){
			saveCheckResult(conn,checkResult);
		}
		log.info("end exe check");
		setReturnExceptions(checkResult);
	}

	public List<NiValException> getReturnExceptions() {
		return returnExceptions;
	}

	public void setReturnExceptions(List<NiValException> returnExceptions) {
		this.returnExceptions = returnExceptions;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CHECK";
	}
}
