package org.navinfo.dataservice.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdNameSelector {

	private Connection conn;
	
	public RdNameSelector(Connection conn){
		this.conn = conn;
	}
	
	public JSONArray getByName(String name) throws Exception{
		JSONArray array = new JSONArray();
		
		String sql = "select name_id,name from rd_name where name like :1";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, "%"+name+"%");

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int nameId = resultSet.getInt("name_id");
				
				String nameStr = resultSet.getString("name");
				
				JSONObject json = new JSONObject();
				
				json.put("nameId", nameId);
				
				json.put("name", nameStr);
				
				array.add(json);
			}
		} catch (Exception e) {
			
			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {
					
				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					
				}
			}

		}
		
		return array;
	}
	
}
