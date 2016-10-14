package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.core.CheckRuleLoader;
import com.navinfo.dataservice.engine.check.core.CheckSuitLoader;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.core.RuleExecuter;

public class CheckEngine {
	private CheckCommand checkCommand = null;
	private Connection conn;
	public List<CheckRule> checkRuleList=new ArrayList<CheckRule>();
	
	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	private static Logger log = Logger.getLogger(CheckEngine.class);

	public CheckEngine(CheckCommand checkCommand) throws Exception{
		this.log = LoggerRepos.getLogger(this.log);
		this.checkCommand=checkCommand;
		//this.conn = GlmDbPoolManager.getInstance().getConnection(this.checkCommand.getProjectId());
		//this.conn.setAutoCommit(true);
	}
	
	public CheckEngine(CheckCommand checkCommand,Connection conn) throws Exception{
		this.log = LoggerRepos.getLogger(this.log);
		this.checkCommand=checkCommand;
		this.conn=conn;
		//this.conn = GlmDbPoolManager.getInstance().getConnection(this.checkCommand.getProjectId());
		//this.conn.setAutoCommit(true);
	}

	/*
	 * 获取本次要执行的检查规则
	 */
	private void getRules(ObjType objType, OperType operType,String checkType) throws Exception{
		String suitCode = objType.toString()+"_"+operType.toString()+"_"+checkType;
		log.info(suitCode);
		this.checkRuleList = CheckSuitLoader.getInstance().getCheckSuit(suitCode);
	}
	
	/*
	 * 对后检查需要保存检查结果，调用此方法将检查结果插入到Ni_val_exception中
	 */
	public void saveCheckResult(List<NiValException> checkResultList) throws Exception{
		log.debug("start call insert ni_val");
		if (checkResultList==null || checkResultList.size()==0) {return;}		
		NiValExceptionOperator check = new NiValExceptionOperator(this.conn);
		for(int i=0;i<checkResultList.size();i++){			
			check.insertCheckLog(checkResultList.get(i).getRuleId(), checkResultList.get(i).getLoc(), checkResultList.get(i).getTargets(), checkResultList.get(i).getMeshId(),checkResultList.get(i).getInformation(), "TEST");
		}
		log.debug("end call insert ni_val");
	}
	
	/*
	 * 前检查
	 */
	public String preCheck() throws Exception{
		log.info("start preCheck");
		//isValidConn();
		//获取前检查需要执行规则列表
		getRules(checkCommand.getObjType(),checkCommand.getOperType(),"PRE");
		List<NiValException> result=exePreCheck();
		if(result!=null && result.size()>0){
			log.info("end preCheck");
			return result.get(0).getInformation();
			}
		return null;
	}
	
	/*
	 * 前检查
	 */
	private List<NiValException> exePreCheck() throws Exception{
		log.info("start preCheck");
		//获取前检查需要执行规则列表
		RuleExecuter ruleExecuterObj=new RuleExecuter(this.checkCommand,this.conn);
		for (int i=0;i<this.checkRuleList.size();i++){			
			CheckRule rule=this.checkRuleList.get(i);
			try{
				List<NiValException> resultTmp=ruleExecuterObj.exePreRule(rule);
				if(resultTmp!=null && resultTmp.size()>0){
					log.info("end preCheck");
					return resultTmp;
					}
				}
			catch(Exception e){
				log.error("error preCheck"+rule.getRuleCode(),e);
			}}
		log.info("end preCheck");
		return null;
	}
	
	/*
	 * 后检查
	 */
	public void postCheck() throws Exception{
		log.info("start postCheck");
		//获取后检查需要执行规则列表
		getRules(this.checkCommand.getObjType(),this.checkCommand.getOperType(),"POST");
		saveCheckResult(exePostCheck());
		log.info("end postCheck");
	}
	
	/*
	 * 后检查
	 */
	public List<NiValException> exePostCheck() throws Exception{
		log.info("start postCheck");
		List<NiValException> checkResultList = new ArrayList<NiValException>();
		RuleExecuter ruleExecuterObj=new RuleExecuter(this.checkCommand,this.conn);
		for (int i=0;i<this.checkRuleList.size();i++){
			CheckRule rule=this.checkRuleList.get(i);
			try{
				List<NiValException> resultTmp=ruleExecuterObj.exePostRule(rule);
				if(resultTmp.size()>0){checkResultList.addAll(resultTmp);}}
			catch(Exception e){
				log.error("error postCheck"+rule.getRuleCode(),e);
			}
		}
		log.info("end postCheck");
		return checkResultList;
	}
	
	public List<NiValException> checkByRules(JSONArray ruleCodeArray,String checkType) throws Exception{
		getCheckByRules(ruleCodeArray,checkType);		
		if("POST".equals(checkType)){
			return exePostCheck();}
		if("PRE".equals(checkType)){
			return exePreCheck();
		}
		return null;
	}
	
	private void getCheckByRules(JSONArray ruleCodeArray,String checkType) throws Exception{
		Iterator ruleIter=ruleCodeArray.iterator();
		while (ruleIter.hasNext()) {
			String ruleStr=(String) ruleIter.next();						
			CheckRule myCheckRule = CheckRuleLoader.getInstance().getCheckRule(ruleStr);							
			if(myCheckRule != null){
				this.checkRuleList.add(myCheckRule);
			}							
		}
	}
}
