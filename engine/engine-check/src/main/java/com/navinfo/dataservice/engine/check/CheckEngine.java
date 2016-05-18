package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
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
	private List<VariableName> myCheckSuitVariables=new ArrayList<VariableName>();
	private int projectId;
	
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
	
	public CheckEngine(CheckCommand checkCommand,Connection conn,int projectId) throws Exception{
		this.log = LoggerRepos.getLogger(this.log);
		this.checkCommand=checkCommand;
		this.conn=conn;
		this.projectId=projectId;
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
		this.myCheckSuitVariables=CheckSuitLoader.getInstance().getCheckSuitVariables(suitCode);
		return myCheckSuit;
	}
	
	private List<CheckRule> getRulesTest(ObjType objType, OperType operType,String checkType) throws Exception{
		String initRuleCode="test2";
		String initRuleLog="testsql";
		int initSeverity=1;
		String initCheckClassPath=null;
		String accessorType="SQL";
		String accessorName="select r.geometry,'[RD_LINK,'||r.link_pid||']',R.MESH_ID from rd_link r where r.kind in (10,11,15) and r.link_pid=RDLINK_PID";
		String variables="RDLINK_PID";
		CheckRule rule=new CheckRule(initRuleCode,initRuleLog,initSeverity,initCheckClassPath,accessorType,accessorName,variables);
		
		String ruleCode="GLM01197";
		String ruleLog="log";
		String ruleClass="com.navinfo.dataservice.engine.check.rules.GLM01197";
		
		CheckRule rule2=new CheckRule(ruleCode,ruleLog,1,ruleClass,null,null,null);
		CheckRule rule3=new CheckRule(ruleCode,ruleLog,1,ruleClass,null,null,null);
		List<CheckRule> myCheckSuit = new ArrayList<CheckRule>();
		//myCheckSuit.add(rule);
		myCheckSuit.add(rule2);
		myCheckSuit.add(rule3);
		this.myCheckSuitVariables.addAll(rule.getVariables());
		return myCheckSuit;
	}
	
	/*
	 * 对后检查需要保存检查结果，调用此方法将检查结果插入到Ni_val_exception中
	 */
	private void saveCheckResult(List<NiValException> checkResultList) throws Exception{
		if (checkResultList==null || checkResultList.size()==0) {return;}
		
		NiValExceptionOperator check = new NiValExceptionOperator(this.conn, this.projectId);
		
		for(int i=0;i<checkResultList.size();i++){
			
			check.insertCheckLog(checkResultList.get(i).getRuleId(), checkResultList.get(i).getLoc(), checkResultList.get(i).getTargets(), checkResultList.get(i).getMeshId(),checkResultList.get(i).getInformation(), "TEST");
		}
	}
	
//	private void isValidConn() throws Exception{
//		if (this.conn.isClosed()){
//			this.conn = GlmDbPoolManager.getInstance().getConnection(this.checkCommand.getProjectId());
//			this.conn.setAutoCommit(true);}		
//	}
	
	/*
	 * 前检查
	 */
	public String preCheck() throws Exception{
		log.info("start preCheck");
		//isValidConn();
		//获取前检查需要执行规则列表
		List<CheckRule> rulesList=getRules(checkCommand.getObjType(),checkCommand.getOperType(),"PRE");		
		for (int i=0;i<rulesList.size();i++){
			CheckRule rule=rulesList.get(i);
			baseRule obj = (baseRule) rule.getRuleClass().newInstance();
			obj.setRuleDetail(rule);
			obj.setConn(this.conn);
			try{
			//调用规则的前检查
				obj.preCheck(this.checkCommand);
				
				if(obj.getCheckResultList().size()!=0){
					log.info("end preCheck");
					return obj.getCheckResultList().get(0).getInformation();
					}
			}catch(Exception e) {
				log.error("error preCheck",e);
				return null;
			}
		}
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
		RuleExecuter ruleExecuterObj=new RuleExecuter(this.checkCommand,this.myCheckSuitVariables,this.conn);
		for (int i=0;i<rulesList.size();i++){
			CheckRule rule=rulesList.get(i);
			try{
				List<NiValException> resultTmp=ruleExecuterObj.exeRule(rule);
				if(resultTmp.size()>0){checkResultList.addAll(resultTmp);}}
			catch(Exception e){
				log.error("error postCheck",e);
			}
		}
		saveCheckResult(checkResultList);
		log.info("end postCheck");
	}
	
	public static void main(String[] args) throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(13474047);
		link.setsNodePid(2);
		link.seteNodePid(2);
		
		Connection conn = GlmDbPoolManager.getInstance().getConnection(11);
		
		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		link = (RdLink) linkSelector.loadById(13474047,false);
		
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(11);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(link.objType());
		
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn,11);
		checkEngine.postCheck();
		conn.commit();
		
//		
//		GLM01025 glm=new GLM01025();
//		glm.setConn(conn);
//		glm.postCheck(checkCommand);	
//		List<NiValException> checkResultList=glm.getCheckResultList();
//		for(NiValException ni:checkResultList){
//			System.out.println(ni.getRuleId());
//			System.out.println(ni.getLoc());
//		}
	}
	
}
