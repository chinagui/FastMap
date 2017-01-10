package com.navinfo.dataservice.engine.meta.scPointAddrAdmin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointAddrAdmin {
	
	private Map<String, Integer> addrAdminMap= new HashMap<String, Integer>();

	private static class SingletonHolder {
		private static final ScPointAddrAdmin INSTANCE = new ScPointAddrAdmin();
	}

	public static final ScPointAddrAdmin getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, Integer> scEngshortListMap() throws Exception{
		if (addrAdminMap==null||addrAdminMap.isEmpty()) {
				synchronized (this) {
					if (addrAdminMap==null||addrAdminMap.isEmpty()) {
						try {
							String sql = "select t.admin_name,t.admin_level from SC_POINT_ADDR_ADMIN t";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									addrAdminMap.put(rs.getString("admin_name"), rs.getInt("admin_level"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_ENGSHORT_LIST失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return addrAdminMap;
	}
}
