package com.navinfo.dataservice.engine.meta.scPointNameck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointNameck {
	private List<ScPointNameckObj> typeD1 = new ArrayList<ScPointNameckObj>();
	
	private Map<String, String> typeD10 = new HashMap<String, String>();
	
	private Map<String, String> typeD4 = new HashMap<String, String>();
	private Map<String, String> typeD3 = new HashMap<String, String>();
	
	private Map<String, String> typeD5 = new HashMap<String, String>();
	
	private Map<String, Map<String, String>> typeD6 = new HashMap<String, Map<String, String>>();
	
	private Map<String, String> typeD7 = new HashMap<String, String>();
	
	private List<String> type9=new ArrayList<String>();

	private static class SingletonHolder {
		private static final ScPointNameck INSTANCE = new ScPointNameck();
	}

	public static final ScPointNameck getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * 需要按照顺序进行key值替换名称，所以用list，按照key长度存放。
	 * 获取sc_Point_Nameck元数据库表中type=1的大陆的记录列表
	 */
	public List<ScPointNameckObj> scPointNameckTypeD1() throws Exception{
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
									ScPointNameckObj obj=new ScPointNameckObj();
									obj.setPreKey(rs.getString("PRE_KEY"));
									obj.setResultKey(rs.getString("RESULT_KEY"));
									obj.setType(1);
									typeD1.add(obj);
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
	public Map<String,String> scPointNameckTypeD3() throws Exception{
		if (typeD3==null||typeD3.isEmpty()) {
				synchronized (this) {
					if (typeD3==null||typeD3.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 3"
									+ "   AND HM_FLAG = 'D'";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD3.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
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
			return typeD3;
	}
	/**
	 * 返回SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’的PRE_KEY, RESULT_KEY
	 * @return Map<String,String> key:PRE_KEY,value:RESULT_KEY
	 * @throws Exception
	 */
	public Map<String,String> scPointNameckTypeD4() throws Exception{
		if (typeD4==null||typeD4.isEmpty()) {
				synchronized (this) {
					if (typeD4==null||typeD4.isEmpty()) {
						try {
							String sql = "SELECT PRE_KEY, RESULT_KEY"
									+ "  FROM SC_POINT_NAMECK"
									+ " WHERE TYPE = 4"
									+ "   AND HM_FLAG <> 'HM'";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeD4.put(rs.getString("PRE_KEY"), rs.getString("RESULT_KEY"));					
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
			return typeD4;
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
	
	public List<String> scPointNameckType9() throws Exception {
		if (type9==null||type9.isEmpty()) {
			synchronized (this) {
				if (type9==null||type9.isEmpty()) {
					try {
						String sql = "SELECT PRE_KEY"
								+ "  FROM SC_POINT_NAMECK"
								+ " WHERE TYPE = 9";
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								type9.add(rs.getString("PRE_KEY"));					
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
		return type9;
	}


	public Map<String,Map<String,String>> scPointNameckTypeD6() throws Exception{
		if (typeD6==null||typeD6.isEmpty()) {
			synchronized (this) {
				if (typeD6==null||typeD6.isEmpty()) {
					try {
						String sql = "SELECT PRE_KEY,RESULT_KEY,ADMINAREA"
								+ "  FROM SC_POINT_NAMECK"
								+ " WHERE TYPE = 6 AND HM_FLAG != 'HM' ";
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								Map<String,String> tempMap = new HashMap<String,String>();
								tempMap.put("resultKey", rs.getString("RESULT_KEY"));
								tempMap.put("adminArea", rs.getString("ADMINAREA"));
								typeD6.put(rs.getString("PRE_KEY"), tempMap);
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
		return typeD6;
	}
}
