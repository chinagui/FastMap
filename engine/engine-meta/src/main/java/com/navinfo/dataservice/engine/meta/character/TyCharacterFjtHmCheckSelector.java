package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

public class TyCharacterFjtHmCheckSelector {
	
	private Connection conn;
	
	public TyCharacterFjtHmCheckSelector() {
		
	}
	
	public TyCharacterFjtHmCheckSelector(Connection conn) {
		this.conn = conn;
	}
	
	public JSONObject getCharacterMap(int type) throws Exception{
		
		String sql = "SELECT distinct hz,correct FROM ty_character_fjt_hm_check";
		
		if (type != 0) {
			sql += " WHERE type="+type;
		}
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject characterMap = new JSONObject();
		
		boolean connFlag = false;
		try {
			if (conn == null) {
				conn = DBConnector.getInstance().getMetaConnection();
				connFlag = true;
			}
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				characterMap.put(resultSet.getString("hz"), resultSet.getString("correct"));
			}
			return characterMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (connFlag) {
				DBUtils.closeConnection(conn);
			}
			
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

}
