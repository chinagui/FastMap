package com.navinfo.dataservice.engine.meta.scPointNameck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointNameck {
	private Map<String, String> typeD1 = new HashMap<String, String>();
	
	private Map<String, String> typeD10 = new HashMap<String, String>();
	
	private Map<String, String> typeD5 = new HashMap<String, String>();
	
	private Map<String, String> typeD7 = new HashMap<String, String>();

	private static class SingletonHolder {
		private static final ScPointNameck INSTANCE = new ScPointNameck();
	}

	public static final ScPointNameck getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, String> scPointNameckTypeD1() throws Exception{
		if (typeD1==null||typeD1.isEmpty()) {
				synchronized (this) {
					if (typeD1==null||typeD1.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 1"
									+ "   AND HM_FLAG = 'D'"
									+ " order by length(pre_key) desc";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD1.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载scpointNameck失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeD1;
	}
	public Map<String,String> scPointNameckTypeD10() throws Exception{
		if (typeD10==null||typeD10.isEmpty()) {
				synchronized (this) {
					if (typeD10==null||typeD10.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 10"
									+ "   AND HM_FLAG = 'D'";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD10.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载scpointNameck失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeD10;
	}
	public Map<String,String> scPointNameckTypeD5() throws Exception{
		if (typeD5==null||typeD5.isEmpty()) {
				synchronized (this) {
					if (typeD5==null||typeD5.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 5"
									+ "   AND HM_FLAG <> 'HM'";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD5.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载scpointNameck失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeD5;
	}
	public Map<String,String> scPointNameckTypeD7() throws Exception{
		if (typeD7==null||typeD7.isEmpty()) {
				synchronized (this) {
					if (typeD7==null||typeD7.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 7"
									+ "   AND HM_FLAG <> 'HM'";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD7.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载scpointNameck失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeD7;
	}


}
