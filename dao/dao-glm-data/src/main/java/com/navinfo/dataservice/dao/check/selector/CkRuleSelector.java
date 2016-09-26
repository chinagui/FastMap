package com.navinfo.dataservice.dao.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CkRuleSelector extends AbstractSelector {
	
	private Connection conn;

	public CkRuleSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	/**
	 * 根据suite查询检查项信息
	 * @param suiteArray
	 * @return
	 */
	public JSONArray getRules(JSONArray suiteArray) throws Exception {
		JSONArray result = new JSONArray();
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select c.*,m.suite_id from ck_rule_cop c,ck_suite_rule_mapping_cop m ");
			sb.append("where c.rule_code = m.rule_code and m.suite_id in (");
			String suiteCode = "";
			for (int i=0;i<suiteArray.size();i++) {
				JSONObject suiteObj = suiteArray.getJSONObject(i);
				suiteCode += suiteObj.getString("ruleCode") + ",";
			}
			suiteCode = suiteCode.substring(0, suiteCode.length()-1);
			sb.append(suiteCode + ") ");
			sb.append("order by m.suite_id");
			
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			
			JSONObject ckRules = new JSONObject();
			
			while (resultSet.next()) {
				String suiteKey = resultSet.getString("suite_id");
				
				JSONObject data = new JSONObject();
				data.put("suiteId", suiteKey);
				data.put("ruleCode", resultSet.getString("rule_code"));
				data.put("ruleName", resultSet.getString("rule_name"));
				data.put("ruleDesc", resultSet.getString("rule_desc"));
				data.put("severity", resultSet.getString("severity"));
				data.put("memo", resultSet.getString("rule_memo"));
				
				if (ckRules.containsKey(suiteKey)) {
					JSONArray tempRuleList = ckRules.getJSONArray(suiteKey);
					tempRuleList.add(data);
					ckRules.put(suiteKey, tempRuleList);
				} else {
					JSONArray tempRuleList = new JSONArray();
					tempRuleList.add(data);
					ckRules.put(suiteKey, tempRuleList);
				}
			}
			for (int i=0;i<suiteArray.size();i++) {
				JSONObject suiteObj = suiteArray.getJSONObject(i);
				suiteObj.put("rules", ckRules.getJSONArray(suiteObj.getString("ruleCode")));
				result.add(suiteObj);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	

}
