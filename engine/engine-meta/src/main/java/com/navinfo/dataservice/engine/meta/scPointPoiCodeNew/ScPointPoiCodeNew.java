package com.navinfo.dataservice.engine.meta.scPointPoiCodeNew;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointPoiCodeNew {

	private static class SingletonHolder {
		private static final ScPointPoiCodeNew INSTANCE = new ScPointPoiCodeNew();
	}
	
	public static final ScPointPoiCodeNew getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SC_POINT_POICODE_NEW
	 * @author Han Shaoming
	 * @param kindCode,kindUse
	 * @return
	 * @throws Exception
	 */
	public Map<String,Integer> searchScPointPoiCodeNew(List<String> kindCodes) throws Exception{
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection conn = null;
			
			String sql = "SELECT KIND_CODE,KIND_USE FROM SC_POINT_POICODE_NEW WHERE KIND_USE =1 AND KIND_CODE IN('"+StringUtils.join(kindCodes, "','")+"')";
			try {
				conn = DBConnector.getInstance().getMetaConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					map.put(rs.getString("KIND_CODE"), rs.getInt("KIND_USE"));
				} 
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				DbUtils.close(conn);
			}
		} catch (Exception e) {
			throw new SQLException("加载scPointPoiCodeNewList失败："+ e.getMessage(), e);
		}
		return map;
	}
}
