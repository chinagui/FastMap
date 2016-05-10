package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.dao.DBConnector;


/** 
 * @ClassName: CheckSuitLoader
 * @author songdongyan
 * @date 上午10:24:55
 * @Description: CheckSuitLoader.java 检查项suit加载
 */
public class CheckSuitLoader {
	
	private static class SingletonHolder {
		private static final CheckSuitLoader INSTANCE = new CheckSuitLoader();
	}

	public static final CheckSuitLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	
	/**
	 * 存放各个检查suit的key为suitCode(feature_operation_operationType拼接而成)， value为[ruleCode]
	 */
	private Map<String, ArrayList<CheckRule>> map = new HashMap<String, ArrayList<CheckRule>>();
	private Map<String, List<VariableName>> mapVariables = new HashMap<String, List<VariableName>>();
	
	
	public ArrayList<CheckRule> getCheckSuit(String suitCode) throws Exception {
		
		if (!map.containsKey(suitCode)) {
			synchronized(this) {
				if (!map.containsKey(suitCode)) {
					//切分suitCode为FEATURE OPERATION OPERATION_TYPE
					String[] paras = suitCode.split("_");
					
					String sql = "select RULE_CODE from CK_SUIT where FEATURE = ? AND OPERATION = ? AND OPERATION_TYPE = ?";
					
					PreparedStatement pstmt = null;

					ResultSet resultSet = null;

					Connection conn = null;
					
					ArrayList<CheckRule> checkRuleList = new ArrayList<CheckRule>();
					List<VariableName> variablesList=new ArrayList<VariableName>();
					
					try {

						conn = DBConnector.getInstance().getConnection();

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,paras[0]);
						pstmt.setString(2,paras[1]);
						pstmt.setString(3,paras[2]);

						resultSet = pstmt.executeQuery();
						
						while (resultSet.next()) {

							String RULE_CODE = resultSet.getString("RULE_CODE");
							
							CheckRule myCheckRule = CheckRuleLoader.getInstance().getCheckRule(RULE_CODE);
							
							if(myCheckRule != null){
								checkRuleList.add(myCheckRule);
								variablesList.removeAll(myCheckRule.getVariables());
								variablesList.addAll(myCheckRule.getVariables());
							}	
							
						}
						
						map.put(suitCode,checkRuleList);
						mapVariables.put(suitCode, variablesList);
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

		return map.get(suitCode);
	}
	
	public List<VariableName> getCheckSuitVariables(String suitCode){
		return mapVariables.get(suitCode);
	}
	
	
	public static void main(String args[]) throws Exception {
		
		String suitCode = "adlink_create_pre";
		
		ArrayList<CheckRule> myCheckSuit = CheckSuitLoader.getInstance().getCheckSuit(suitCode);
		
		for(int i=0;i<myCheckSuit.size();i++){
			   System.out.println("CheckSuit.ruleCode:" + i + ":" + myCheckSuit.get(i));
			   myCheckSuit.get(i).getRuleClass().newInstance();
			  }
		
		ArrayList<CheckRule> myCheckSuit_1 = CheckSuitLoader.getInstance().getCheckSuit(suitCode);
		
		for(int i=0;i<myCheckSuit_1.size();i++){
			   System.out.println("CheckSuit.ruleCode:" + i + ":" + myCheckSuit_1.get(i));
			   myCheckSuit.get(i).getRuleClass().newInstance();
			  }


	}

}
