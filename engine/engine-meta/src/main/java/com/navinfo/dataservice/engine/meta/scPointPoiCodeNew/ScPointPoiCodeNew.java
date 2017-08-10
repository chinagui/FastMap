package com.navinfo.dataservice.engine.meta.scPointPoiCodeNew;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointPoiCodeNew {
	
	private Map<String,Integer> kindUser1Map = new HashMap<String,Integer>();
	
	private Map<String,String> kindNameMap = new HashMap<String,String>();

	private static class SingletonHolder {
		private static final ScPointPoiCodeNew INSTANCE = new ScPointPoiCodeNew();
	}
	
	public static final ScPointPoiCodeNew getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SC_POINT_POICODE_NEW
	 * @param kindCode
	 * @return Map<String,String> key:KIND_CODE,value:KIND_NAME
	 * @throws Exception
	 */
	public Map<String,String> getKindNameByKindCode() throws Exception{
		if (kindNameMap == null || kindNameMap.isEmpty()) {
			synchronized (this) {
				if (kindNameMap == null || kindNameMap.isEmpty()) {
					try {
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						
						String sql = "SELECT KIND_CODE,KIND_NAME FROM SC_POINT_POICODE_NEW ";
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								kindNameMap.put(rs.getString("KIND_CODE"), rs.getString("KIND_NAME"));
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.closeQuietly(conn, pstmt, rs);
						}
					} catch (Exception e) {
						throw new SQLException("加载scPointPoiCodeNewList失败："+ e.getMessage(), e);
					}
				}
			}
		}
		return kindNameMap;
	}
	
	/**
	 * SC_POINT_POICODE_NEW
	 * @param kindCode,kindUse
	 * @return
	 * @throws Exception
	 */
	public Map<String,Integer> searchScPointPoiCodeNew() throws Exception{
		if (kindUser1Map == null || kindUser1Map.isEmpty()) {
			synchronized (this) {
				if (kindUser1Map == null || kindUser1Map.isEmpty()) {
					try {
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						
						String sql = "SELECT KIND_CODE,KIND_USE FROM SC_POINT_POICODE_NEW WHERE KIND_USE =1 ";
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								kindUser1Map.put(rs.getString("KIND_CODE"), rs.getInt("KIND_USE"));
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载scPointPoiCodeNewList失败："+ e.getMessage(), e);
					}
				}
			}
		}
		return kindUser1Map;
	}
	
}
