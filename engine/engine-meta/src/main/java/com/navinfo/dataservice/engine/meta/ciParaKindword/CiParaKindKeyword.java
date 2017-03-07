package com.navinfo.dataservice.engine.meta.ciParaKindword;

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

public class CiParaKindKeyword {
	
	private Map<String, List<String>> ciParaKindKeywordMap= new HashMap<String, List<String>>();

	private static class SingletonHolder {
		private static final CiParaKindKeyword INSTANCE = new CiParaKindKeyword();
	}

	public static final CiParaKindKeyword getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * SELECT KIND_ID, KEYWORD FROM CI_PARA_KIND_KEYWORD
	 * @return Map<String, String> key:kind_id,value:keyword
	 * @throws Exception
	 */
	public Map<String, List<String>> ciParaKindKeywordMap() throws Exception{
		if (ciParaKindKeywordMap==null||ciParaKindKeywordMap.isEmpty()) {
				synchronized (this) {
					if (ciParaKindKeywordMap==null||ciParaKindKeywordMap.isEmpty()) {
						try {
							String sql = "SELECT KIND_ID, KEYWORD FROM CI_PARA_KIND_KEYWORD";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									String kindId = rs.getString("KIND_ID");
									String keyWord = rs.getString("KEYWORD");
									if (!ciParaKindKeywordMap.containsKey(kindId)) {
										ciParaKindKeywordMap.put(kindId, new ArrayList<String>());
									}
									ciParaKindKeywordMap.get(kindId).add(keyWord);
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载CI_PARA_KIND_KEYWORD失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return ciParaKindKeywordMap;
	}
	
}
