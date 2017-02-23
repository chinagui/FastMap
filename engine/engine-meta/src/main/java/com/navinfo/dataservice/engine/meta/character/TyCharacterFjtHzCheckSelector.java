package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class TyCharacterFjtHzCheckSelector {
	private Map<String, JSONObject> getFtCharacterMap = new HashMap<String, JSONObject>();
	private Map<Integer,Map<String, String>> convertFtMap=new HashMap<Integer,Map<String, String>>();
	
	private Map<String, JSONObject> getjtCharacterMap = new HashMap<String, JSONObject>();
	
	private static class SingletonHolder {
		private static final TyCharacterFjtHzCheckSelector INSTANCE = new TyCharacterFjtHzCheckSelector();
	}

	public static final TyCharacterFjtHzCheckSelector getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:ft value:对应其它
	 * @throws Exception
	 */
	public Map<String, JSONObject> getFtExtentionTypeMap() throws Exception{
		if (getFtCharacterMap==null||getFtCharacterMap.isEmpty()) {
				synchronized (this) {
					if (getFtCharacterMap==null||getFtCharacterMap.isEmpty()) {
						try {
							String sql = "SELECT hz.ft,hz.jt,hz.convert,hz.ftorder FROM ty_character_fjt_hz hz";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									JSONObject data=new JSONObject();
									data.put("jt",rs.getString("jt"));
									data.put("convert",rs.getInt("convert"));
									data.put("ftorder",rs.getInt("ftorder"));
									getFtCharacterMap.put(rs.getString("ft"), data);
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载ty_character_fjt_hz失败："+ e.getMessage(), e);
						}
					}
				}
		}
		return getFtCharacterMap;
	}
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:jt value:对应其它
	 * @throws Exception
	 */
	public Map<String, JSONObject> getJtExtentionTypeMap() throws Exception{
		if (getjtCharacterMap==null||getjtCharacterMap.isEmpty()) {
				synchronized (this) {
					if (getjtCharacterMap==null||getjtCharacterMap.isEmpty()) {
						try {
							String sql = "SELECT hz.ft,hz.jt,hz.convert,hz.ftorder FROM ty_character_fjt_hz hz";
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									JSONObject data=new JSONObject();
									data.put("ft",rs.getString("ft"));
									data.put("convert",rs.getInt("convert"));
									data.put("ftorder",rs.getInt("ftorder"));
									getFtCharacterMap.put(rs.getString("jt"), data);
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载ty_character_fjt_hz失败："+ e.getMessage(), e);
						}
					}
				}
		}
		return getjtCharacterMap;
	}
	public Map<Integer, Map<String, String>> tyCharacterFjtHzConvertFtMap() throws Exception {
		if (convertFtMap==null||convertFtMap.isEmpty()) {
			synchronized (this) {
				if (convertFtMap==null||convertFtMap.isEmpty()) {
					try {
						String sql = "SELECT hz.ft,hz.jt,hz.convert FROM ty_character_fjt_hz hz";
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								int convert=rs.getInt("convert");
								if(!convertFtMap.containsKey(convert)){
									convertFtMap.put(convert,new HashMap<String, String>());
								}
								convertFtMap.get(convert).put(rs.getString("jt"), rs.getString("ft"));
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.commitAndCloseQuietly(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载ty_character_fjt_hz失败："+ e.getMessage(), e);
					}
				}
			}
	}
	return convertFtMap;
	}

}
