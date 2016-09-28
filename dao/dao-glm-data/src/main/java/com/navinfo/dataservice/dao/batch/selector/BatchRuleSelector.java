package com.navinfo.dataservice.dao.batch.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BatchRuleSelector extends AbstractSelector {
	
	private Connection conn;

	public BatchRuleSelector(Connection conn) {
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
			sb.append("select c.*  from batch_rule_cop c ");
			sb.append("where c.rule_status=1 and c.suite_id in ( '' ");
			String suiteCode = "";
			for (int i=0;i<suiteArray.size();i++) {
				JSONObject suiteObj = suiteArray.getJSONObject(i);
				suiteCode += ",'" + suiteObj.getString("suiteId") + "'";
			}
			sb.append(suiteCode + ") ");
			sb.append("order by c.suite_id");
			
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			
			JSONObject batchRules = new JSONObject();
			
			while (resultSet.next()) {
				String suiteKey = resultSet.getString("suite_id");
				
				JSONObject data = new JSONObject();
				data.put("suiteId", suiteKey);
				data.put("ruleCode", resultSet.getString("rule_code"));
				data.put("ruleName", resultSet.getString("rule_name"));
				data.put("ruleDesc", resultSet.getString("rule_desc"));
				data.put("severity", resultSet.getString("severity"));
				data.put("depends", resultSet.getString("depends"));
				
				if (batchRules.containsKey(suiteKey)) {
					JSONArray tempRuleList = batchRules.getJSONArray(suiteKey);
					tempRuleList.add(data);
					batchRules.put(suiteKey, tempRuleList);
				} else {
					JSONArray tempRuleList = new JSONArray();
					tempRuleList.add(data);
					batchRules.put(suiteKey, tempRuleList);
				}
			}
			for (int i=0;i<suiteArray.size();i++) {
				JSONObject suiteObj = suiteArray.getJSONObject(i);
				suiteObj.put("rules", batchRules.getJSONArray(suiteObj.getString("suiteId")));
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
