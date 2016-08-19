package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.navicommons.database.sql.DBUtils;

public class TyCharacterFjtHmCheckSelector {
	
	private Connection conn;
	
	public TyCharacterFjtHmCheckSelector() {
		
	}
	
	public TyCharacterFjtHmCheckSelector(Connection conn) {
		this.conn = conn;
	}
	
	public Map<String, String> getCharacterMap() throws Exception{
		
		String sql = "SELECT distinct hz,correct FROM ty_character_fjt_hm_check";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		Map<String,String> characterMap = new HashMap<String,String>();
		
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
