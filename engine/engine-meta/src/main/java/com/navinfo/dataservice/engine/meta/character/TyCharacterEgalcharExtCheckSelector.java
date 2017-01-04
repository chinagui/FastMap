package com.navinfo.dataservice.engine.meta.character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;

/** 
 * @ClassName: TyCharacterEgalcharExtCheckSelector 
 * @author Gao Pengrong
 * @date 2016-11-12 上午9:41:24 
 * @Description: 加载ty_character_egalchar_ext表中数据
 */

public class TyCharacterEgalcharExtCheckSelector {
	
	
	public TyCharacterEgalcharExtCheckSelector() {
		
	}
	
	public JSONObject getCharacterMap() throws Exception{
		
		Connection conn = null;
		
		String sql = "SELECT distinct character,extention_type FROM ty_character_egalchar_ext";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject characterMap = new JSONObject();
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
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
			DbUtils.closeQuietly(conn);
		}
	}
	
	public JSONObject getCheckMap() throws Exception {
		
		String sql = "SELECT distinct character,extention_type FROM ty_character_egalchar_ext where extention_type in ('GBK','ENG_F_U','ENG_F_L','DIGIT_F','SYMBOL_F')";
		
		Connection conn = null;
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject characterMap = new JSONObject();
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				characterMap.put(resultSet.getString("character"), resultSet.getString("extention_type"));
			}
			return characterMap;
		} catch(Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeConnection(conn);
		}
	}

}
