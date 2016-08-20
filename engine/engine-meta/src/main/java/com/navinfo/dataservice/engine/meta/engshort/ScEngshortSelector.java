package com.navinfo.dataservice.engine.meta.engshort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.navicommons.database.sql.DBUtils;

public class ScEngshortSelector {
	
	private Connection conn;
	
	public ScEngshortSelector() {
		
	}
	
	public ScEngshortSelector(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * 英文简称查询
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> getEngShortMap() throws Exception {
		
		String sql = "SELECT full_name,short_name FROM sc_engshort_list";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		Map<String,String> engshortMap = new HashMap<String,String>();
		
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				engshortMap.put(resultSet.getString("full_name"), resultSet.getString("short_name"));
			}
			
			return engshortMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
		
	}

}
