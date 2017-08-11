package com.navinfo.dataservice.engine.meta.scPointKindRule;

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

public class ScPointKindRule {
	
	private List<Map<String, Object>> scPointKindRule = new ArrayList<Map<String, Object>>();
	
	private Map<String, String> scPointKindRule5 = new HashMap<String, String>();
	
	private static class SingletonHolder {
		private static final ScPointKindRule INSTANCE = new ScPointKindRule();
	}

	public static final ScPointKindRule getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE TYPE IN(1,2,3)
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> scPointKindRule() throws Exception {
		if (scPointKindRule == null || scPointKindRule.isEmpty()) {
			synchronized (this) {
				if (scPointKindRule == null || scPointKindRule.isEmpty()) {
					try {
						String sql = "SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE TYPE IN(1,2,3)";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								Map<String,Object> map = new HashMap<String,Object>();
								String poiKind = rs.getString("POI_KIND");
								String poiKindName = rs.getString("POI_KIND_NAME");
								int type = rs.getInt("TYPE");
								map.put("poiKind", poiKind);
								map.put("poiKindName", poiKindName);
								map.put("type", type);
								scPointKindRule.add(map);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.closeQuietly(conn, pstmt, rs);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_KIND_RULE失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return scPointKindRule;
	}
	
	/**
	 * SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE TYPE IN(5)
	 * 
	 * @return Map<String, String> key:POI_KIND;value:POI_KIND_NAME
	 * @throws Exception
	 */
	public Map<String, String> scPointKindRule5() throws Exception {
		if (scPointKindRule5 == null || scPointKindRule5.isEmpty()) {
			synchronized (this) {
				if (scPointKindRule5 == null || scPointKindRule5.isEmpty()) {
					try {
						String sql = "SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE CHECK_RULE IN(5)";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								String poiKind = rs.getString("POI_KIND");
								String poiKindName = rs.getString("POI_KIND_NAME");
								scPointKindRule5.put(poiKind, poiKindName);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.closeQuietly(conn, pstmt, rs);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_KIND_RULE失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return scPointKindRule5;
	}

}
