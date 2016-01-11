package com.navinfo.dataservice.man.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ProjectSelector {
	
	private Connection conn;
	
	public ProjectSelector(Connection conn){
		this.conn = conn;
	}

	public JSONArray getByUser(int userId) throws Exception{
		JSONArray array = new JSONArray();
		
		String sql = "select project_id, prj_name from prj_project";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int projectId = resultSet.getInt("project_id");
				
				String prjName = resultSet.getString("prj_name");
				
				JSONObject json = new JSONObject();
				
				json.put("id", projectId);
				
				json.put("name", prjName);
				
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
			
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					
				}
			}

		}
		
		return array;
	}
}
