package com.navinfo.dataservice.engine.meta.scEngshortList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScEngshortList {
	
	private Map<String, String> engshortMap= new HashMap<String, String>();

	private static class SingletonHolder {
		private static final ScEngshortList INSTANCE = new ScEngshortList();
	}

	public static final ScEngshortList getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, String> scEngshortListMap() throws Exception{
		if (engshortMap==null||engshortMap.isEmpty()) {
				synchronized (this) {
					if (engshortMap==null||engshortMap.isEmpty()) {
						try {
							String sql = "select full_name,short_name from SC_ENGSHORT_LIST";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									engshortMap.put(rs.getString("full_name"), rs.getString("short_name"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_ENGSHORT_LIST失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return engshortMap;
	}
	
}
