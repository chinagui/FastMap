package com.navinfo.dataservice.engine.check.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.OperType;

/**
 * 检查规则
 * @author songdongyan
 *
 */

public class CheckRule {
	
	//规则号
//	public String ruleCode;
	private String ruleCode; 
	//规则提示信息
//	public String ruleLog;
	private String ruleLog;
	
	//规则等级
//	public int severity;
	private int severity;
	//检查规则类
//	public String ruleClass;
	
//	public Class ruleClass;
	private Class ruleClass;
	
	//accessor_type,accessor_name,variables
	private AccessorType accessortype;
	private String accessorName;
	private List<VariableName> variables=new ArrayList<VariableName>();
	
	
	public String getRuleCode(){
		return ruleCode;
	}
	
	public String getRuleLog(){
		return ruleLog;
	}
	
	public int getSeverity(){
		return severity;
	}
	
	public Class getRuleClass(){
		return ruleClass;
	}
	
	public CheckRule(String initRuleCode,String initRuleLog,int initSeverity,String initCheckClassPath,String accessorType,String accessorName,String variables) {

		
		try{
			if(initCheckClassPath!=null && !initCheckClassPath.isEmpty()){ruleClass = Class.forName(initCheckClassPath);}
			ruleCode = initRuleCode;
			ruleLog = initRuleLog;
			severity = initSeverity;
			
			if(accessorType==null || accessorType.isEmpty()){this.accessortype=null;}
			else{this.accessortype=AccessorType.valueOf(accessorType);}
			
			this.accessorName=accessorName;
			
			if(variables==null || variables.isEmpty()){this.variables=null;}
			else{
				List<String> variableTmp=java.util.Arrays.asList(variables);
				for(int i=0;i<variableTmp.size();i++)
					{this.variables.add(Enum.valueOf(VariableName.class, variableTmp.get(i)));}}
		}catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("没有指定类名称");
        } catch (ClassNotFoundException e) {
            System.out.println("找不到指定的类");
        }
		
	}

	
	public static void main(String args[]) {
		AccessorType taccessortype;
		String accessorType="SQL";
		if(accessorType.isEmpty()){taccessortype=null;}
		else{taccessortype=AccessorType.valueOf(accessorType);}
		System.out.println(taccessortype);
		
		String variables="a,b";
		List<String> lvariables=java.util.Arrays.asList(variables);
		System.out.println(lvariables);

//		CheckRule myCheckRule = new CheckRule("1","2",2,"3","","","");
//		System.out.println("CheckRule.ruleCode:" + myCheckRule.ruleCode);
//		System.out.println("CheckRule.ruleLog:" + myCheckRule.ruleLog);
//		System.out.println("CheckRule.severity:" + myCheckRule.severity);
//		System.out.println("CheckRule.ruleClass:" + myCheckRule.ruleClass);
		
	}

	public AccessorType getAccessorType() {
		return accessortype;
	}

	public void setAccessorType(AccessorType accessor_type) {
		this.accessortype = accessor_type;
	}

	public String getAccessorName() {
		return accessorName;
	}

	public void setAccessorName(String accessor_name) {
		this.accessorName = accessor_name;
	}

	public List<VariableName> getVariables() {
		return variables;
	}

	public void setVariables(List<VariableName> variables) {
		this.variables = variables;
	}
	

}