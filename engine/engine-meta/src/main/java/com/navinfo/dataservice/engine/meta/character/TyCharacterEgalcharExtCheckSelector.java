package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;

/** 
 * @ClassName: TyCharacterEgalcharExtCheckSelector 
 * @author Gao Pengrong
 * @date 2016-11-12 上午9:41:24 
 * @Description: 加载ty_character_egalchar_ext表中数据
 */

public class TyCharacterEgalcharExtCheckSelector {
	
	private Connection conn;
	
	public TyCharacterEgalcharExtCheckSelector() {
		
	}
	
	public TyCharacterEgalcharExtCheckSelector(Connection conn) {
		this.conn = conn;
	}
	
	public JSONObject getCharacterMap() throws Exception{
		
		String sql = "SELECT distinct character,extention_type FROM ty_character_egalchar_ext";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject characterMap = new JSONObject();
		
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				characterMap.put(resultSet.getString("character"), resultSet.getString("extention_type"));
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
