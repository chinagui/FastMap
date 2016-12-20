package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
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
	private CheckRuleCommand checkRuleCommand;
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
	public void operate(AbstractCommand cmd) throws Exception {
		log.info("start exe check");
		CheckCommand checkCommand =(CheckCommand) cmd;
		//按照规则号list加载规则列表，以及汇总需要参考的子表map
		log.info("load check rule");
		Map<String, Set<String>> selConfig=new HashMap<String, Set<String>>();
		List<CheckRule> checkRuleList=new ArrayList<CheckRule>();
		for(String ruleId:checkCommand.getRuleIdList()){
			CheckRule rule = loadCheckRule(ruleId);
			if(rule==null){
				log.error("检查规则加载失败,rule Id:"+ruleId);
				continue;
			}
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
		this.checkRuleCommand=new CheckRuleCommand();
		this.checkRuleCommand.setConn(conn);
		this.checkRuleCommand.setAllDatas(result.getAllObjsMap());
		//顺序执行检查规则
		List<NiValException> checkResult=new ArrayList<NiValException>();
		CheckExcuter excuter=new CheckExcuter();
		log.info("start run check rule");
		for(CheckRule rule:checkRuleList){
			List<NiValException> checkResultTmp=new ArrayList<NiValException>();
			checkResultTmp=excuter.exeRule(rule, this.checkRuleCommand);
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

	private CheckRule loadCheckRule(String ruleId) throws Exception {
		try{
			return CheckRuleLoader.getInstance().loadByRuleId(ruleId);
		}catch(Exception e){
			log.info("加载检查规则失败,ruleId:"+ruleId+",errorMsg:"+e.getMessage());
			return null;
		}
		
		
	}

	public List<NiValException> getReturnExceptions() {
		return returnExceptions;
	}

	public void setReturnExceptions(List<NiValException> returnExceptions) {
		this.returnExceptions = returnExceptions;
	}
	
	/**
	 * 
	 * @return:
	 * IX_POI对象的检查错误，按照pid查找规则号的集合
	 * key:ObjectName.java中的对象名 
	 * value：Map<Long, Set<String>> key:pid value:检查规则号集合
	 */
	public Map<String, Map<Long, Set<String>>> getErrorPidMap() {
		return this.checkRuleCommand.getErrorPidRuleMap();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CHECK";
	}
}
