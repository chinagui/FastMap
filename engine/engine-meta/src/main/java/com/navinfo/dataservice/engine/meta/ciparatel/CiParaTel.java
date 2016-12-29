package com.navinfo.dataservice.engine.meta.ciparatel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class CiParaTel {
	
	public Map<String,String> getCodeLength(String adminCode) throws Exception {
		String sql = "SELECT code,tel_len FROM ci_para_tel WHERE city_code=:1";
		
		Connection conn = null;
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		Map<String,String> ret = new HashMap<String,String>();
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, adminCode);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				ret.put("code", resultSet.getString("code"));
				ret.put("len", resultSet.getString("tel_len"));
			}
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.close(resultSet);
			DbUtils.close(pstmt);
			DbUtils.close(conn);
		}
	}

}
