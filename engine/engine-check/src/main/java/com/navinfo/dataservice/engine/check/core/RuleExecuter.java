package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.graph.ChainLoader;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;

public class RuleExecuter {
	
	CheckCommand checkCommand=new CheckCommand();
	private List<IRow> dataList=new ArrayList<IRow>();
	//private List<VariableName> checkSuitVariables=new ArrayList<VariableName>();
	private Connection conn;
	private Map<VariableName,Set<String>> variablesValueMap=new HashMap<VariableName,Set<String>>();
	private Map<String,List<IRow>> dataMap=new HashMap<String,List<IRow>>();
	private ChainLoader loader=new ChainLoader();
	
	private static Logger log = Logger.getLogger(CheckEngine.class);

	public RuleExecuter(CheckCommand checkCommand,Connection conn) {
		// TODO Auto-generated constructor stub
		this.checkCommand=checkCommand;
		this.setDataList(checkCommand.getGlmList());
		//this.checkSuitVariables=checkSuitVariables;
		this.conn=conn;
		//if(!checkSuitVariables.isEmpty()){createVariablesValues();}
	}
	
	/*
	 * 根据数据确定变量的values
	 * 随后执行sql语句的时候会通过变量替换values，形成可执行sql
	 */
	private void createVariablesValues(VariableName variable){
		for(int i=0;i<dataList.size();i++){
			createVariableFactory(dataList.get(i),variable);
		}
	}
	
	private void createVariableFactory(IRow data,VariableName variable){
		Set<String> variablevalue=new HashSet<String>();
		switch (variable) {
			case RDLINK_PID:
			{variablevalue=VariablesFactory.getRdLinkPid(data);break;}
			case RDNODE_PID:
			{variablevalue=VariablesFactory.getRdNodePid(data);break;}
			case RDGATE_INLINKPID:
			{variablevalue=VariablesFactory.getRdGateInLinkPid(data);break;}
			case RDGATE_OUTLINKPID:
			{variablevalue=VariablesFactory.getRdGateOutLinkPid(data);break;}
			case RDDIRECTROUTE_PID:
			{variablevalue=VariablesFactory.getRdDirectroutePid(data);break;}
			case RDSLOPE_PID:
			{variablevalue=VariablesFactory.getRdSlopePid(data);break;}
			case RDWARNINGINFO_PID:
			{variablevalue=VariablesFactory.getRdWarninginfoPid(data);break;}
			case RDBRANCH_PID:
			{variablevalue=VariablesFactory.getRdBranchPid(data);break;}
			case RDVOICEGUIDE_PID:
			{variablevalue=VariablesFactory.getRdVoiceGuidePid(data);break;}
			case RDTOLLGATE_PID:
			{variablevalue=VariablesFactory.getRdTollgatePid(data);break;}
			case RDGATE_PID:
			{variablevalue=VariablesFactory.getRdGatePid(data);break;}
			case RDELECTRONICEYE_PID:
			{variablevalue=VariablesFactory.getRdElectroniceye(data);break;}
			case RDLANE_PID:
			{variablevalue=VariablesFactory.getRdLanePid(data);break;}			
			case RDLANECONNEXITY_PID:
			{variablevalue=VariablesFactory.getRdLaneConnexityPid(data);break;}
			case RDRESTRICTION_PID:
			{variablevalue=VariablesFactory.getRdRestrictionPid(data);break;}			
			case RDVOICEGUIDEDETAIL_PID:
			{variablevalue=VariablesFactory.getRdVoiceguideDetailPid(data);break;}
			case RDTRAFFICSIGNAL_PID:
			{variablevalue=VariablesFactory.getRdTrafficsignalPid(data);break;}
			case RDSPEEDBUMP_PID:
			{variablevalue=VariablesFactory.getRdSpeedbumpPid(data);break;}
		}
		if(!variablesValueMap.containsKey(variable)){
			variablesValueMap.put(variable, new HashSet<String>());}
		variablesValueMap.get(variable).addAll(variablevalue);
	}

	public List<IRow> getDataList() {
		return dataList;
	}

	public void setDataList(List<IRow> dataList) {
		this.dataList = dataList;
	}
	
	public List<NiValException> exePreRule(CheckRule rule) throws Exception{
		try{
			log.info("start exe "+rule.getRuleCode());
			if(rule.getPreAccessorType()==AccessorType.SQL){
				return exePreSqlRule(rule);
			}else{return exePreJavaRule(rule);}}
		finally{
			log.info("end exe "+rule.getRuleCode());}
	}
	
	/*
	 * 执行java写的检查规则
	 */
	private List<NiValException> exePreJavaRule(CheckRule rule) throws Exception{
		baseRule obj = (baseRule) rule.getPreRuleClass().newInstance();
		obj.setLoader(loader);
		obj.setRuleDetail(rule);
		obj.setConn(this.conn);
		//调用规则的后检查
		try{
			obj.preCheck(this.checkCommand);
		}catch(Exception e) {
			log.error("error exejavacheck",e);
		}
		List<NiValException> preResult=obj.getCheckResultList();
		if(preResult==null || preResult.size()==0){return null;}
		return preResult;
	}
	
	/*
	 * 执行sql语句写的检查规则
	 */
	private List<NiValException> exePreSqlRule(CheckRule rule) throws Exception{
		String sql=rule.getPreAccessorName();
		List<VariableName> variableList=rule.getPreVariables();
		return exeSqlRule(rule,sql,variableList);
	}
	
	public List<NiValException> exePostRule(CheckRule rule) throws Exception{
		try{
			log.info("start exe "+rule.getRuleCode());
			loadGlmList(rule);
			if(rule.getPostAccessorType()==AccessorType.SQL){
				return exePostSqlRule(rule);
			}else{return exePostJavaRule(rule);}}
		finally{
			log.info("end exe "+rule.getRuleCode());}
	}
	/**
	 * 执行具体规则前，先加载规则数据
	 * @throws Exception 
	 */
	private void loadGlmList(CheckRule rule) throws Exception{
		String logSql=checkCommand.getLogSql();
		//通过履历变更加载查询数据
		if(logSql!=null && !logSql.isEmpty()){
			variablesValueMap.clear();
			List<IRow> dataList=new ArrayList<IRow>();
			String tableName="RD_LINK";
			if(!dataMap.containsKey(tableName)){
				dataMap.put(tableName, LoadGlmList.loadByLogSql(conn, "RD_LINK", null, logSql));
			}
			List<IRow> dataListTmp=dataMap.get(tableName);
			if(dataListTmp!=null && dataListTmp.size()>0){
				dataList.addAll(dataListTmp);}
			setDataList(dataList);
			return;
		}
		String wkt=checkCommand.getWkt();
		if(wkt!=null && !wkt.isEmpty()){
			variablesValueMap.clear();
			List<IRow> dataList=new ArrayList<IRow>();
			String tableName="RD_LINK";
			if(!dataMap.containsKey(tableName)){
				dataMap.put(tableName, LoadGlmList.loadByWkt(conn, tableName, wkt));
			}
			List<IRow> dataListTmp=dataMap.get(tableName);
			if(dataListTmp!=null && dataListTmp.size()>0){
				dataList.addAll(dataListTmp);}
			setDataList(dataList);
			return;
		}
		//如果上面的情况都没有发生，则使用checkCommand传过来的glmlist，即本类中的dataList
	}
	
	/*
	 * 执行java写的检查规则
	 */
	private List<NiValException> exePostJavaRule(CheckRule rule) throws Exception{
		baseRule obj = (baseRule) rule.getPostRuleClass().newInstance();
		obj.setLoader(loader);
		obj.setRuleDetail(rule);
		obj.setConn(this.conn);
		//调用规则的后检查
		try{
			obj.postCheck(this.checkCommand);
		}catch(Exception e) {
			log.error("error exejavacheck",e);
		}
		return obj.getCheckResultList();
	}
	
	/*
	 * 执行sql语句写的检查规则
	 */
	private List<NiValException> exePostSqlRule(CheckRule rule) throws Exception{
		String sql=rule.getPostAccessorName();
		List<VariableName> variableList=rule.getPostVariables();
		return exeSqlRule(rule,sql,variableList);
	}
	
	private Set<String> getVariableValue(VariableName variableName){
		if(variablesValueMap.containsKey(variableName)){
			return variablesValueMap.get(variableName);
		}
		createVariablesValues(variableName);
		return variablesValueMap.get(variableName);
	}
	
	private List<NiValException> exeSqlRule(CheckRule rule,String sql,List<VariableName> variableList) throws Exception{
		List<String> sqlList=new ArrayList<String>();
		List<String> sqlListTmp=new ArrayList<String>();
		sqlList.add(sql);
		String firstVariable=variableList.get(0).toString();
		//将sql语句中的参数进行替换，形成可执行的sql语句
		if(variableList.size()==1 
				&& (sql.replaceAll(firstVariable, "").length()+firstVariable.length())==sql.length()
				&& ((sql.replaceAll("="+firstVariable, "").length()+firstVariable.length()+1)==sql.length()
				||(sql.replaceAll("= "+firstVariable, "").length()+firstVariable.length()+2)==sql.length())){
			//有1个变量，或者sql中变量出现1次,若set有多个。。可直接替换成in
			Set<String> variableValueList=getVariableValue(variableList.get(0));
			if(variableValueList==null || variableValueList.size()==0){
				sqlListTmp=new ArrayList<String>();
				sqlList=new ArrayList<String>();
			}else if(variableValueList.size()==1){
				sqlListTmp.add(sql.replace(firstVariable, variableValueList.iterator().next()));
			}else{
				String sqlTmp=sql.replace("= "+firstVariable, "="+firstVariable);
				sqlListTmp.add(sqlTmp.replace("="+firstVariable, "in ("+variableValueList.toString().replace("[", "").replace("]", "")+")"));
			}
		}else{
			//有多个变量，或者sql中变量出现多次
			for(int i=0;i<variableList.size();i++){
				Set<String> variableValueList=getVariableValue(variableList.get(i));
				if(variableValueList==null || variableValueList.size()==0){
					sqlListTmp=new ArrayList<String>();
					sqlList=new ArrayList<String>();
					break;
				}
				if(sqlListTmp.size()!=0){sqlList=sqlListTmp;sqlListTmp=new ArrayList<String>();}
				for(int m=0;m<sqlList.size();m++){
					Iterator<String> varIterator=variableValueList.iterator();
					while(varIterator.hasNext()){
						sqlListTmp.add(sqlList.get(m).replaceAll(variableList.get(i).toString(), varIterator.next()));
					}
				}
			}}
		if(sqlListTmp.size()!=0){sqlList=sqlListTmp;}
		//执行sql语句
		List<NiValException> niValExceptionList=new ArrayList<NiValException>();
		checkResultDatabaseOperator getObj=new checkResultDatabaseOperator();
		getObj.setRule(rule);
		for(int i=0;i<sqlList.size();i++){
			List<Object> resultList=new ArrayList<Object>();
			resultList=getObj.exeSelect(this.conn, sqlList.get(i));
			if (resultList.size()>0){
				for(int j=0;j<resultList.size();j++){
					niValExceptionList.add((NiValException) resultList.get(j));}
				}
		}
		return niValExceptionList;
	}
}

class checkResultDatabaseOperator extends DatabaseOperator{
	private CheckRule rule;
	
	//pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId()
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		while (resultSet.next()){
			String pointWkt ="";
			try{
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);			
				Geometry pointGeo=GeoHelper.getPointFromGeo(geometry);
				pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
			}catch(Exception e){}
			
			String targets=resultSet.getString(2);
			int meshId=resultSet.getInt(3);
			String log=rule.getRuleLog();
			try{
				log=resultSet.getString(4);
			}catch(Exception e){}
			
			NiValException checkResult=new NiValException(rule.getRuleCode(), pointWkt, targets, meshId,log);
			resultList.add(checkResult);
		} 
		return resultList;
	}
	
	public CheckRule getRule() {
		return rule;
	}

	public void setRule(CheckRule rule) {
		this.rule = rule;
	}
}