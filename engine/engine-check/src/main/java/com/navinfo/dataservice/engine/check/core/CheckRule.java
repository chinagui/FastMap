package com.navinfo.dataservice.engine.check.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 检查规则
 * @author songdongyan
 *
 */

public class CheckRule {
	
	//规则号
	public String ruleCode;
		
	//规则提示信息
	public String ruleLog;
	
	//规则等级
	public int severity;
	
	//检查规则类
//	public String ruleClass;
	
	public Class ruleClass;
	
	public CheckRule(String initRuleCode,String initRuleLog,int initSeverity,String initCheckClassPath) {

		
		try{
			ruleClass = Class.forName(initCheckClassPath);

			ruleCode = initRuleCode;
			ruleLog = initRuleLog;
			severity = initSeverity;
		}catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("没有指定类名称");
        } catch (ClassNotFoundException e) {
            System.out.println("找不到指定的类");
        }
		
	}

	
	public static void main(String args[]) {
		
		CheckRule myCheckRule = new CheckRule("1","2",2,"3");
		System.out.println("CheckRule.ruleCode:" + myCheckRule.ruleCode);
		System.out.println("CheckRule.ruleLog:" + myCheckRule.ruleLog);
		System.out.println("CheckRule.severity:" + myCheckRule.severity);
		System.out.println("CheckRule.ruleClass:" + myCheckRule.ruleClass);
		
	}
	

}