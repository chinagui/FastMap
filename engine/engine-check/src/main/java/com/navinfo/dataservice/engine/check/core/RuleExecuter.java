package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class RuleExecuter {
	
	CheckCommand checkCommand=new CheckCommand();
	private List<IRow> dataList=new ArrayList<IRow>();
	private List<VariableName> checkSuitVariables=new ArrayList<VariableName>();
	private Connection conn;
	private Map<VariableName,List<String>> variablesValueMap=new HashMap<VariableName,List<String>>();
	
	private static Logger log = Logger.getLogger(CheckEngine.class);

	public RuleExecuter(CheckCommand checkCommand,List<VariableName> checkSuitVariables,Connection conn) {
		// TODO Auto-generated constructor stub
		this.checkCommand=checkCommand;
		this.setDataList(checkCommand.getGlmList());
		this.checkSuitVariables=checkSuitVariables;
		this.conn=conn;
		createVariablesValues();
	}
	
	//根据数据确定变量的values
	private void createVariablesValues(){
		for(int i=0;i<dataList.size();i++){
			for(int j=0;j<checkSuitVariables.size();j++){
				createVariableFactory(dataList.get(i),checkSuitVariables.get(j));
			}
		}
	}
	
	private void createVariableFactory(IRow data,VariableName variable){
		String variablevalue=null;
		switch (variable) {
			case RDLINK_PID:
				{variablevalue=VariablesFactory.getRdLinkPid(data);break;}
				}
		if(!variablesValueMap.containsKey(variable)){
			variablesValueMap.put(variable, new ArrayList<String>());}
		variablesValueMap.get(variable).add(variablevalue);
	}

	public List<IRow> getDataList() {
		return dataList;
	}

	public void setDataList(List<IRow> dataList) {
		this.dataList = dataList;
	}
	
	public List<NiValException> exeRule(CheckRule rule) throws Exception{
		if(rule.getAccessorType()==AccessorType.SQL){
			return exeSqlRule(rule);
		}else{return exeJavaRule(rule);}
	}
	
	private List<NiValException> exeJavaRule(CheckRule rule) throws Exception{
		baseRule obj = (baseRule) rule.getRuleClass().newInstance();
		obj.setRuleDetail(rule);
		obj.setConn(this.conn);
		//调用规则的后检查
		try{
			obj.postCheck(this.checkCommand);
		}catch(Exception e) {
			log.error(e);
		}
		return obj.getCheckResultList();
	}
	
	private List<NiValException> exeSqlRule(CheckRule rule) throws Exception{
		String sql=rule.getAccessorName();
		List<String> sqlList=new ArrayList<String>();
		List<String> sqlListTmp=new ArrayList<String>();
		sqlList.add(sql);
		List<VariableName> variableList=rule.getVariables();
		//将sql语句中的参数进行替换，形成可执行的sql语句
		for(int i=0;i<variableList.size();i++){
			List<String> variableValueList=variablesValueMap.get(variableList.get(i));
			if(sqlListTmp.size()!=0){sqlList=sqlListTmp;sqlListTmp=new ArrayList<String>();}
			for(int m=0;m<sqlList.size();m++){
				for(int j=0;j<variableValueList.size();j++){
					sqlListTmp.add(sqlList.get(m).replaceAll(variableList.get(i).toString(), variableValueList.get(j)));
				}
			}
		}
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
			
			STRUCT struct = (STRUCT) resultSet.getObject("geometry");
			Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);			
			Geometry pointGeo=GeoHelper.getPointFromGeo(geometry);
			String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
			
			String targets=resultSet.getString(2);
			int meshId=resultSet.getInt(3);
			String log=resultSet.getString(4);	
			
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