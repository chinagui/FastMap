package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;

import com.navinfo.dataservice.engine.check.core.NiValException;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.engine.check.core.NiValException;

public class baseRule {
	private String ruleCode;
	private String ruleLog;
	List<NiValException> checkResultList=new ArrayList<NiValException>();

	public String getRuleCode() {
		return ruleCode;
	}
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	public String getRuleLog() {
		return ruleLog;
	}
	public void setRuleLog(String ruleLog) {
		this.ruleLog = ruleLog;
	}
	
	//通过rule对象赋值private String ruleCode;ruleDesc;ruleLog;
	public void setRuleDetail(CheckRule rule){
		this.ruleCode=rule.ruleCode;
		this.ruleLog=rule.ruleLog;
	}
	
	//添加检查错误
	public void setCheckResult(NiValException niValexception){}
	
	public void setCheckResult(String loc, String targets,int meshId){
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,this.ruleLog);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(String loc, String targets,int meshId,String log){
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,log);
		this.checkResultList.add(checkResult);
	}
	
	//获取检查执行的错误结果list
	public List<NiValException> getCheckResultList(){
		return this.checkResultList;
	}
	
	public void preCheck(CheckCommand checkCommand){};
	
	public void postCheck(CheckCommand checkCommand){};
}
