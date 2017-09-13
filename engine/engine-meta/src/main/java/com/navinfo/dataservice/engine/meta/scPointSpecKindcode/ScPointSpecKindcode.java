package com.navinfo.dataservice.engine.meta.scPointSpecKindcode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.model.ScPointSpecKindcodeNewObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointSpecKindcode {
	private Map<String,ScPointSpecKindcodeNewObj> typeMap2= new HashMap<String,ScPointSpecKindcodeNewObj>();
	private Map<String, String> typeMap8= new HashMap<String, String>();
	private Map<String, String> typeMap15= new HashMap<String, String>();
	private Map<String, List<String>> typeMap7= new HashMap<String, List<String>>();


	private Map<String, List<String>> typeMap14= new HashMap<String, List<String>>();
	private List<String> typeList16= new ArrayList<String>();

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
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap8;
	}
	public Map<String, List<String>> scPointSpecKindCodeType14() throws Exception{
		if (typeMap14==null||typeMap14.isEmpty()) {
				synchronized (this) {
					if (typeMap14==null||typeMap14.isEmpty()) {
						try {
							String sql = "SELECT POI_KIND, RATING FROM SC_POINT_SPEC_KINDCODE_NEW T WHERE TYPE = 14";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									List<String> ratings = new ArrayList<String>();
									if(typeMap14.containsKey(rs.getString("POI_KIND"))){
										ratings = typeMap14.get(rs.getString("POI_KIND"));
									}
									ratings.add(rs.getString("RATING"));
									typeMap14.put(rs.getString("POI_KIND"),ratings);					
								} 
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap14;
	}
	
	public boolean judgeScPointKind(String kindCode,String chain) throws Exception {
		String sql = "select 1 from sc_point_spec_kindcode_new t WHERE ((poi_kind=:1 and category=1) or (chain=:2 and category=3 and poi_kind=:3)) and t.type=8";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, kindCode);
			pstmt.setString(2, chain);
			pstmt.setString(3, kindCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
			return false;
		} finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
	}
	
	public Map<String, ScPointSpecKindcodeNewObj> scPointSpecKindCodeType2() throws Exception{
		if (typeMap2==null||typeMap2.isEmpty()) {
				synchronized (this) {
					if (typeMap2==null||typeMap2.isEmpty()) {
						try {
							String sql = "SELECT DISTINCT POI_KIND, RATING, TOPCITY"
									+ "  FROM SC_POINT_SPEC_KINDCODE_NEW"
									+ " WHERE TYPE = 2";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									ScPointSpecKindcodeNewObj tmpKindcodeNewObj=new ScPointSpecKindcodeNewObj();
									tmpKindcodeNewObj.setPoiKind(rs.getString("POI_KIND"));
									tmpKindcodeNewObj.setRating(rs.getInt("RATING"));
									tmpKindcodeNewObj.setTopcity(rs.getInt("TOPCITY"));
									typeMap2.put(rs.getString("POI_KIND"), tmpKindcodeNewObj);					
								} 
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap2;
	}
	
	public Map<String, String> scPointSpecKindCodeType15() throws Exception{
		if (typeMap15==null||typeMap15.isEmpty()) {
				synchronized (this) {
					if (typeMap15==null||typeMap15.isEmpty()) {
						try {
							String sql = "select POI_KIND,CHAIN from sc_point_spec_kindcode_new t WHERE TYPE=15";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeMap15.put(rs.getString("POI_KIND"), rs.getString("CHAIN"));					
								} 
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeMap15;
	}

	public Map<String, List<String>> scPointSpecKindCodeType7() throws Exception{
		if (typeMap7==null||typeMap7.isEmpty()) {
			synchronized (this) {
				if (typeMap7==null||typeMap7.isEmpty()) {
					try {
						String sql = "SELECT POI_KIND, CHAIN FROM SC_POINT_SPEC_KINDCODE_NEW T WHERE TYPE = 7";

						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {

								String poiKind = rs.getString("POI_KIND");

								if (poiKind == null || poiKind.isEmpty()) {

									continue;
								}

								String chain = rs.getString("CHAIN");

								if (!typeMap7.containsKey(poiKind)) {

									typeMap7.put(poiKind, new ArrayList<String>());
								}

								List<String> chains = typeMap7.get(poiKind);

								chains.add(chain == null ? "" : chain);

								typeMap7.put(rs.getString("POI_KIND"), chains);
							}
						} finally {
							DbUtils.closeQuietly(conn, pstmt, rs);
						}
					} catch (Exception e) {
						throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
					}
				}
			}
		}
		return typeMap7;
	}

	public List<String> scPointSpecKindCodeType16() throws Exception{
		if (typeList16==null||typeList16.isEmpty()) {
				synchronized (this) {
					if (typeList16==null||typeList16.isEmpty()) {
						try {
							String sql = "select distinct POI_KIND from sc_point_spec_kindcode_new t WHERE TYPE=16";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									typeList16.add(rs.getString("POI_KIND"));					
								} 
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载sc_point_spec_kindcode_new失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return typeList16;
	}
}
