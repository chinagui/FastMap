package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.core.CheckSuitLoader;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.core.RuleExecuter;
import com.navinfo.dataservice.engine.check.core.VariableName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;

public class CheckEngine {
	private CheckCommand checkCommand = null;
	private Connection conn;
	private List<VariableName> myCheckSuitPostVariables=new ArrayList<VariableName>();
	private List<VariableName> myCheckSuitPreVariables=new ArrayList<VariableName>();
	
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
	private List<CheckRule> getRules(ObjType objType, OperType operType,String checkType) throws Exception{
		String suitCode = objType.toString()+"_"+operType.toString()+"_"+checkType;
		log.info(suitCode);
		List<CheckRule> myCheckSuit = CheckSuitLoader.getInstance().getCheckSuit(suitCode);
		this.myCheckSuitPostVariables=CheckSuitLoader.getInstance().getCheckSuitPostVariables(suitCode);
		this.myCheckSuitPreVariables=CheckSuitLoader.getInstance().getCheckSuitPreVariables(suitCode);
		return myCheckSuit;
	}
	
	/*
	 * 对后检查需要保存检查结果，调用此方法将检查结果插入到Ni_val_exception中
	 */
	private void saveCheckResult(List<NiValException> checkResultList) throws Exception{
		if (checkResultList==null || checkResultList.size()==0) {return;}		
		NiValExceptionOperator check = new NiValExceptionOperator(this.conn);		
		for(int i=0;i<checkResultList.size();i++){			
			check.insertCheckLog(checkResultList.get(i).getRuleId(), checkResultList.get(i).getLoc(), checkResultList.get(i).getTargets(), checkResultList.get(i).getMeshId(),checkResultList.get(i).getInformation(), "TEST");
		}
	}
	
	/*
	 * 前检查
	 */
	public String preCheck() throws Exception{
		log.info("start preCheck");
		//isValidConn();
		//获取前检查需要执行规则列表
		List<CheckRule> rulesList=getRules(checkCommand.getObjType(),checkCommand.getOperType(),"PRE");
		RuleExecuter ruleExecuterObj=new RuleExecuter(this.checkCommand,this.myCheckSuitPreVariables,this.conn);
		for (int i=0;i<rulesList.size();i++){			
			CheckRule rule=rulesList.get(i);
			try{
				String logMsg=ruleExecuterObj.exePreRule(rule);
				if(logMsg!=null && !logMsg.isEmpty()){
					log.info("end preCheck");
					return logMsg;}
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
		//isValidConn();
		//获取后检查需要执行规则列表
		List<CheckRule> rulesList=getRules(this.checkCommand.getObjType(),this.checkCommand.getOperType(),"POST");
		List<NiValException> checkResultList = new ArrayList<NiValException>();
		RuleExecuter ruleExecuterObj=new RuleExecuter(this.checkCommand,this.myCheckSuitPostVariables,this.conn);
		for (int i=0;i<rulesList.size();i++){
			CheckRule rule=rulesList.get(i);
			try{
				List<NiValException> resultTmp=ruleExecuterObj.exePostRule(rule);
				if(resultTmp.size()>0){checkResultList.addAll(resultTmp);}}
			catch(Exception e){
				log.error("error postCheck"+rule.getRuleCode(),e);
			}
		}
		saveCheckResult(checkResultList);
		log.info("end postCheck");
	}	
}
