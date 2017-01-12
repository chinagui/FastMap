package com.navinfo.dataservice.engine.meta.scPointBrandFoodtype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointBrandFoodtype {
	
	private Map<String,String> kindBrandMap= new HashMap<String,String>();

	private static class SingletonHolder {
		private static final ScPointBrandFoodtype INSTANCE = new ScPointBrandFoodtype();
	}

	public static final ScPointBrandFoodtype getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * select poikind,chain,foodType from SC_POINT_BRAND_FOODTYPE
	 * @return Map<String, String> key:poikind|chain value:foodType
	 * @throws Exception
	 */
	public Map<String, String> scPointBrandFoodtypeKindBrandMap() throws Exception{
		if (kindBrandMap==null||kindBrandMap.isEmpty()) {
				synchronized (this) {
					if (kindBrandMap==null||kindBrandMap.isEmpty()) {
						try {
							String sql = "select poikind,chain,foodType from SC_POINT_BRAND_FOODTYPE";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									kindBrandMap.put(rs.getString("poikind")+"|"+rs.getString("chain"),
											rs.getString("foodType"));	
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
			return kindBrandMap;
	}
}
