package com.navinfo.dataservice.engine.meta.scPointSpecKindcode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointSpecKindcode {
	private Map<String, String> typeMap8= new HashMap<String, String>();

	private static class SingletonHolder {
		private static final ScPointSpecKindcode INSTANCE = new ScPointSpecKindcode();
	}

	public static final ScPointSpecKindcode getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, String> scPointSpecKindCodeType8() throws Exception{
		if (typeMap8==null||typeMap8.isEmpty()) {
				synchronized (this) {
					if (typeMap8==null||typeMap8.isEmpty()) {
						try {
							String sql = "select POI_KIND,CHAIN from sc_point_spec_kindcode_new t WHERE TYPE=8";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeMap8.put(rs.getString("POI_KIND"), rs.getString("CHAIN"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap8;
	}
	/**
	 * 重要分类判断方法
	 * 传入poi的kindCode和chain，返回boolean，是否为重要分类
	 * @param kindCode
	 * @param chain
	 * @return true重要分类，false 非重要分类
	 * @throws Exception
	 */
	public boolean judgeScPointKind(String kindCode,String chain) throws Exception {
		String sql = "select 1 from sc_point_spec_kindcode_new t WHERE (poi_kind=:1 and category=1) or (chain=:2 and category=3)";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, kindCode);
			pstmt.setString(2, chain);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.close(rs);
			DbUtils.close(pstmt);
			DbUtils.close(conn);
		}
	}

}
