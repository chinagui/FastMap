package com.navinfo.dataservice.dao.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CkSuiteSelector extends AbstractSelector {
	
	private Connection conn;

	public CkSuiteSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	public JSONArray getSuite(int pageSize,int pageNum, String type) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			JSONArray result = new JSONArray();
			
			int startRow = (pageNum-1) * pageSize + 1;

			int endRow = pageNum * pageSize;
			
			sb.append("SELECT * FROM (SELECT c.*, rownum rn FROM (select COUNT (1) OVER (PARTITION BY 1) total,a.* from ck_suite_cop a  where a.feature=:1) c WHERE rownum <= :2)  WHERE rn >= :3");
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, type);

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("feature", resultSet.getString("feature"));
				data.put("suiteName", resultSet.getString("suite_name"));
				data.put("ruleCode", resultSet.getString("rule_code"));
				data.put("total", resultSet.getInt("total"));
				result.add(data);
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
