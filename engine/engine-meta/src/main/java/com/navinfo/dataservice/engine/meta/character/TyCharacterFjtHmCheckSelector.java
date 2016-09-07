package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

public class TyCharacterFjtHmCheckSelector {
	
	private Connection conn;
	
	public TyCharacterFjtHmCheckSelector() {
		
	}
	
	public TyCharacterFjtHmCheckSelector(Connection conn) {
		this.conn = conn;
	}
	
	public JSONObject getCharacterMap() throws Exception{
		
		String sql = "SELECT distinct hz,correct FROM ty_character_fjt_hm_check";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject characterMap = new JSONObject();
		
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				characterMap.put(resultSet.getString("hz"), resultSet.getString("correct"));
			}
			return characterMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

}
