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
	private Class preRuleClass;
	
	//accessor_type,accessor_name,variables
	private AccessorType postAccessorType;
	private String postAccessorName;
	private Class postRuleClass;
	private List<VariableName> postVariables=new ArrayList<VariableName>();
	
	
	public String getRuleCode(){
		return ruleCode;
	}
	
	public String getRuleLog(){
		return ruleLog;
	}
	
	public int getSeverity(){
		return severity;
	}
	
	public CheckRule(String initRuleCode,String initRuleLog,int initSeverity,String preCheckClassPath,String postAccessorType,String postAccessorName,String postVariables) {

		
		try{
			ruleCode = initRuleCode;
			ruleLog = initRuleLog;
			severity = initSeverity;
			
			if(preCheckClassPath!=null && !preCheckClassPath.isEmpty()){this.preRuleClass = Class.forName(preCheckClassPath);}
						
			if(postAccessorType==null || postAccessorType.isEmpty()){this.postAccessorType=null;}
			else{
				this.postAccessorType=AccessorType.valueOf(postAccessorType);
				if(this.postAccessorType==AccessorType.SQL){
					this.postAccessorName=postAccessorName;
					if(postVariables==null || postVariables.isEmpty()){this.postVariables=null;}
					else{
						List<String> variableTmp=java.util.Arrays.asList(postVariables.split(","));
						for(int i=0;i<variableTmp.size();i++)
							{this.postVariables.add(Enum.valueOf(VariableName.class, variableTmp.get(i)));}}
					}
				else{this.postRuleClass=Class.forName(postAccessorName);}
			}			
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

	public Class getPreRuleClass() {
		return preRuleClass;
	}

	public void setPreRuleClass(Class preRuleClass) {
		this.preRuleClass = preRuleClass;
	}

	public String getPostAccessorName() {
		return postAccessorName;
	}

	public void setPostAccessorName(String postAccessorName) {
		this.postAccessorName = postAccessorName;
	}

	public Class getPostRuleClass() {
		return postRuleClass;
	}

	public void setPostRuleClass(Class postRuleClass) {
		this.postRuleClass = postRuleClass;
	}

	public List<VariableName> getPostVariables() {
		return postVariables;
	}

	public void setPostVariables(List<VariableName> postVariables) {
		this.postVariables = postVariables;
	}
	public AccessorType getPostAccessorType() {
		return postAccessorType;
	}

	public void setPostAccessorType(AccessorType postAccessorType) {
		this.postAccessorType = postAccessorType;
	}

}