package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;




/**
 * 加载检查规则
 * @author songdongyan
 *
 */

public class CheckRuleLoader {

	private static class SingletonHolder {
		private static final CheckRuleLoader INSTANCE = new CheckRuleLoader();
	}

	public static final CheckRuleLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 存放各个检查项的key为检查项的规则号， value为CheckRule
	 */
	private Map<String, CheckRule> map = new HashMap<String, CheckRule>();
	
	
	public CheckRule getCheckRule(String ruleCode) throws Exception {
		
		if (!map.containsKey(ruleCode)) {
			synchronized(this) {
				if (!map.containsKey(ruleCode)) {
					
					String sql = "select RULE_CODE, RULE_LOG, SEVERITY, RULE_CLASS,ACCESSOR_TYPE,ACCESSOR_NAME,VARIABLES from CK_RULE where RULE_CODE = ? AND RULE_STATUS='E'";
					
					PreparedStatement pstmt = null;

					ResultSet resultSet = null;

					Connection conn = null;
					
					try {

						conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ruleCode);

						resultSet = pstmt.executeQuery();

						if (resultSet.next()) {

							String RULE_CODE = resultSet.getString("RULE_CODE");
							String RULE_LOG = resultSet.getString("RULE_LOG");	
							int SEVERITY = resultSet.getInt("SEVERITY");							
							String CLASS = resultSet.getString("RULE_CLASS");
							//ACCESSOR_TYPE,ACCESSOR_NAME,VARIABLES
							String ACCESSOR_TYPE = resultSet.getString("ACCESSOR_TYPE");							
							String ACCESSOR_NAME = resultSet.getString("ACCESSOR_NAME");
							String VARIABLES = resultSet.getString("VARIABLES");
	
							CheckRule myCheckRule = new CheckRule(RULE_CODE,RULE_LOG,SEVERITY,CLASS,ACCESSOR_TYPE,ACCESSOR_NAME,VARIABLES);
							
							map.put(ruleCode,myCheckRule);
					
						} 
					} catch (Exception e) {

						throw new Exception(e);

					} finally {
						if (resultSet != null) {
							try {
								resultSet.close();
							} catch (Exception e) {

							}
						}

						if (pstmt != null) {
							try {
								pstmt.close();
							} catch (Exception e) {

							}
						}

						if (conn != null) {
							try {
								conn.close();
							} catch (Exception e) {

							}
						}
					}
				}
			}
		}

		return map.get(ruleCode);
	}
	
	
	public static void main(String args[]) throws Exception {
		
		String ruleCode = "GLM2006";
		
		CheckRule myCheckRule = CheckRuleLoader.getInstance().getCheckRule(ruleCode);
		
		System.out.println("CheckRule.ruleCode:" + myCheckRule.getRuleCode());
		System.out.println("CheckRule.ruleLog:" + myCheckRule.getRuleLog());
		System.out.println("CheckRule.severity:" + myCheckRule.getSeverity());
		System.out.println("CheckRule.ruleClass:" + myCheckRule.getRuleClass());
		
		myCheckRule.getRuleClass().newInstance();
	}
	
	
	
	
	
}
	
	
	
	
	
	
	
	
	
	
	
	