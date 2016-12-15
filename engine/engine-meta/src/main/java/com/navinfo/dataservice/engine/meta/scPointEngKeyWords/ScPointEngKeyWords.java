package com.navinfo.dataservice.engine.meta.scPointEngKeyWords;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointEngKeyWords {
	private Map<String, String> typeMap1= new HashMap<String, String>();

	private static class SingletonHolder {
		private static final ScPointEngKeyWords INSTANCE = new ScPointEngKeyWords();
	}

	public static final ScPointEngKeyWords getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, String> scPointEngKeyWordsType1() throws Exception{
		if (typeMap1==null||typeMap1.isEmpty()) {
				synchronized (this) {
					if (typeMap1==null||typeMap1.isEmpty()) {
						try {
							String sql = "SELECT CHIKEYWORDS,ENGKEYWORDS FROM SC_POINT_ENGKEYWORDS WHERE TYPE=1";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeMap1.put(rs.getString("CHIKEYWORDS"), rs.getString("ENGKEYWORDS"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_ENGKEYWORDS失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap1;
	}
	
}
