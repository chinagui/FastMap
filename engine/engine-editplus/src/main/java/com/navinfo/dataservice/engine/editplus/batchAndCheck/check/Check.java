package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;

public class Check {
	private static Logger log = Logger.getLogger(Check.class);

	public Check() {
		// TODO Auto-generated constructor stub
	}
	//执行检查
	public static List<NiValException> run(CheckCommand checkCommand) throws Exception{
		List<NiValException> checkResult=new ArrayList<NiValException>();
		CheckExcuter excuter=new CheckExcuter();
		//循环执行检查规则
		for(String ruleId:checkCommand.getRuleId()){
			List<NiValException> checkResultTmp=new ArrayList<NiValException>();
			CheckRule rule=CheckRuleLoader.getInstance().loadByRuleId(ruleId);
			checkResult=excuter.exeRule(rule, checkCommand);
			checkResult.addAll(checkResultTmp);
			//isErrorReturn为ture，表示有错误log，则直接停止后续检查；false则继续执行，最后检查结果统一返回
			if(checkCommand.isErrorReturn() && !checkResult.isEmpty() && checkResult.size()>0){
				break;
			}
		}
		//isSaveResult=true，则检查结果保存；否则不保存检查结果
		if(checkCommand.isSaveResult() &&!checkResult.isEmpty() && checkResult.size()>0){
			Check check=new Check();
			check.saveCheckResult(checkCommand.getConn(),checkResult);
		}
		return checkResult;
	}
	
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
}
