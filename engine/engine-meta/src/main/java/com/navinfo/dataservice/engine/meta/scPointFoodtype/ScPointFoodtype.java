package com.navinfo.dataservice.engine.meta.scPointFoodtype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointFoodtype {
	
	private List<String> kinds= new ArrayList<String>();

	private static class SingletonHolder {
		private static final ScPointFoodtype INSTANCE = new ScPointFoodtype();
	}

	public static final ScPointFoodtype getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * select poiKind from SC_POINT_FOODTYP
	 * @return List<String> SC_POINT_FOODTYP的poikind列表
	 * @throws Exception
	 */
	public List<String> scPointFoodtypeKindList() throws Exception{
		if (kinds==null||kinds.isEmpty()) {
				synchronized (this) {
					if (kinds==null||kinds.isEmpty()) {
						try {
							String sql = "select poiKind from SC_POINT_FOODTYPE";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									kinds.add(rs.getString("poiKind"));			
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
								DbUtils.close(rs);
								DbUtils.close(pstmt);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_FOODTYPE失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return kinds;
	}
}
