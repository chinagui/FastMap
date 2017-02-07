package com.navinfo.dataservice.engine.meta.scPointChainBrandKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointChainBrandKey {
	private Map<String,String> brandDMap= new HashMap<String,String>();

	private static class SingletonHolder {
		private static final ScPointChainBrandKey INSTANCE = new ScPointChainBrandKey();
	}

	public static final ScPointChainBrandKey getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * select PRE_KEY,CHAIN from SC_POINT_CHAIN_BRAND_KEY where hm_flag='D'
	 * @return Map<String, String> key:PRE_KEY value:CHAIN
	 * @throws Exception
	 */
	public Map<String, String> scPointChainBrandKeyDMap() throws Exception{
		if (brandDMap==null||brandDMap.isEmpty()) {
				synchronized (this) {
					if (brandDMap==null||brandDMap.isEmpty()) {
						try {
							String sql = "select PRE_KEY,CHAIN from SC_POINT_CHAIN_BRAND_KEY where hm_flag='D'";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									brandDMap.put(rs.getString("PRE_KEY"),rs.getString("CHAIN"));	
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载scPointFoodtypeKindBrandMap失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return brandDMap;
	}
}
