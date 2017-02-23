package com.navinfo.dataservice.engine.meta.scFmControl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScFmControl {

	private static class SingletonHolder {
		private static final ScFmControl INSTANCE = new ScFmControl();
	}
	
	public static final ScFmControl getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SC_FM_CONTROL
	 * @author Han Shaoming
	 * @param kindCode
	 * @return
	 * @throws Exception
	 */
	public Map<String,Integer> searchScFmControl(String kindCode) throws Exception{
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection conn = null;
			
			String sql = "SELECT KIND_CODE,PARENT FROM SC_FM_CONTROL WHERE KIND_CODE ='"+kindCode+"'";
			try {
				conn = DBConnector.getInstance().getMetaConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					map.put(rs.getString("KIND_CODE"), rs.getInt("PARENT"));
				} 
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				DbUtils.close(conn);
			}
		} catch (Exception e) {
			throw new SQLException("加载scFmControlList失败："+ e.getMessage(), e);
		}
		return map;
	}
}
