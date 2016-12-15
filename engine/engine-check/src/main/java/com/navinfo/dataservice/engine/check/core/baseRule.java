package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;

import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.graph.ChainLoader;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.vividsolutions.jts.geom.Geometry;

public abstract class baseRule {
	private String ruleCode;
	private String ruleLog;
	private ChainLoader loader;
	
	private Connection conn;
	public Logger log = Logger.getLogger(baseRule.class);
	List<NiValException> checkResultList=new ArrayList<NiValException>();
	
	public baseRule(){
		//this.log = LoggerRepos.getLogger(this.log);
	}
	
	public Connection setConn(Connection conn) {
		return this.conn = conn;
	}
	
	public Connection getConn() {
		return conn;
	}

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
		this.ruleCode=rule.getRuleCode();
		this.ruleLog=rule.getRuleLog();
	}
	
	//添加检查错误
	public void setCheckResult(NiValException niValexception){
		this.checkResultList.add(niValexception);
	}
	
	public void setCheckResult(String loc, String targets,int meshId){
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,this.ruleLog);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId) throws Exception{
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,this.ruleLog);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(String loc, String targets,int meshId,String log){
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,log);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId,String log) throws Exception{
		NiValException checkResult=new NiValException(this.ruleCode, loc, targets, meshId,log);
		this.checkResultList.add(checkResult);
	}
	
	//获取检查执行的错误结果list
	public List<NiValException> getCheckResultList(){
		return this.checkResultList;
	}
	
	public abstract void preCheck(CheckCommand checkCommand) throws Exception;
	
	public abstract void postCheck(CheckCommand checkCommand) throws Exception;

	public ChainLoader getLoader() {
		return loader;
	}

	public void setLoader(ChainLoader loader) {
		this.loader = loader;
	}
}
