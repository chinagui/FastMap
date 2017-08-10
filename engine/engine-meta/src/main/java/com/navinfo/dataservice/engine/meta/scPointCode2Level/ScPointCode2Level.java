package com.navinfo.dataservice.engine.meta.scPointCode2Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;


public class ScPointCode2Level {

	private Map<String, String> kindCodeMap = new HashMap<String, String>();
	private Map<String, String> kindCodeMapOld = new HashMap<String, String>();
	
	private static class SingletonHolder {
		private static final ScPointCode2Level INSTANCE = new ScPointCode2Level();
	}

	public static final ScPointCode2Level getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SELECT KIND_CODE,NEW_POI_LEVEL FROM SC_POINT_CODE2LEVEL
	 * 
	 * @returnList Map<String, List<String>> key:KIND_CODE,value:NEW_POI_LEVEL
	 * @throws Exception
	 */
	public Map<String, String> scPointCode2Level() throws Exception {
		if (kindCodeMap == null || kindCodeMap.isEmpty()) {
			synchronized (this) {
				if (kindCodeMap == null || kindCodeMap.isEmpty()) {
					try {
						String sql = "SELECT DISTINCT KIND_CODE,NEW_POI_LEVEL FROM SC_POINT_CODE2LEVEL";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								String kind = rs.getString("KIND_CODE");
								String level = rs.getString("NEW_POI_LEVEL");
								kindCodeMap.put(kind, level);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.closeQuietly(conn, pstmt, rs);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_CODE2LEVEL失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return kindCodeMap;
	}
	
	public Map<String, String> scPointCode2LevelOld() throws Exception {
		if (kindCodeMapOld == null || kindCodeMapOld.isEmpty()) {
			synchronized (this) {
				if (kindCodeMapOld == null || kindCodeMapOld.isEmpty()) {
					try {
						String sql = "SELECT DISTINCT KIND_CODE,OLD_POI_LEVEL FROM SC_POINT_CODE2LEVEL";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								String kind = rs.getString("KIND_CODE");
								String level = rs.getString("OLD_POI_LEVEL");
								kindCodeMapOld.put(kind, level);
							}
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_CODE2LEVEL失败：" + e.getMessage(), e);
					}
				}
			}
		}
		return kindCodeMapOld;
	}
}
