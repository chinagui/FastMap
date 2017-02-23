package com.navinfo.dataservice.engine.meta.scPointFocus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointFocus {

	private static class SingletonHolder {
		private static final ScPointFocus INSTANCE = new ScPointFocus();
	}
	
	public static final ScPointFocus getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SC_POINT_FOCUS
	 * @author Han Shaoming
	 * @param poiNum
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public Map<String,Integer> searchScPointFocus(String poiNum) throws Exception{
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection conn = null;
			
			String sql = "SELECT POI_NUM,TYPE FROM SC_POINT_FOCUS WHERE TYPE = 2 AND POI_NUM ='"+poiNum+"'";
			try {
				conn = DBConnector.getInstance().getMetaConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					map.put(rs.getString("POI_NUM"), rs.getInt("TYPE"));
				} 
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				DbUtils.close(conn);
			}
		} catch (Exception e) {
			throw new SQLException("加载scPointFocusList失败："+ e.getMessage(), e);
		}
		return map;
	}
}
