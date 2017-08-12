package com.navinfo.dataservice.engine.meta.workitem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Workitem {
	
	public Workitem () {
		
	}
	
	public JSONArray getDataMap(int type) throws Exception {
		String sql = "select * from work_item";
		
		if (type!=0) {
			sql += " where type="+type;
		}
		
		sql += " order by type";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);
			
			resultSet = pstmt.executeQuery();
			
			JSONArray result = new JSONArray();
			
			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("type", resultSet.getInt("type"));
				data.put("code", resultSet.getString("code"));
				data.put("name", resultSet.getString("name"));
				result.add(data);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn, pstmt, resultSet);

		}
	}

}
