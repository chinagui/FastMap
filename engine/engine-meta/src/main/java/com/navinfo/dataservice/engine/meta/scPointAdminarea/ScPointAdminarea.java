package com.navinfo.dataservice.engine.meta.scPointAdminarea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointAdminarea {
	
	private Map<String, List<String>> contactMap= new HashMap<String, List<String>>();

	private static class SingletonHolder {
		private static final ScPointAdminarea INSTANCE = new ScPointAdminarea();
	}

	public static final ScPointAdminarea getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA
	 * @return Map<String, List<String>> :key,AREACODE电话区号;value,ADMINAREACODE列表，对应的行政区划号列表
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointAdminareaContactMap() throws Exception{
		if (contactMap==null||contactMap.isEmpty()) {
				synchronized (this) {
					if (contactMap==null||contactMap.isEmpty()) {
						try {
							String sql = "SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									String admin=rs.getString("ADMINAREACODE");
									String contact=rs.getString("AREACODE");
									if(!contactMap.containsKey(contact)){
										contactMap.put(contact, new ArrayList<String>());}
									contactMap.get(contact).add(admin);					
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
			return contactMap;
	}
	
}
