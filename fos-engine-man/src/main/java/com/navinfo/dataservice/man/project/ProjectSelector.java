package com.navinfo.dataservice.man.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;

public class ProjectSelector {
	
	private Connection conn;
	
	public ProjectSelector(Connection conn){
		this.conn = conn;
	}

	public JSONArray getByUser(int userId) throws Exception{
		JSONArray array = new JSONArray();
		
		String sql = "select project_id from prj_project";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int projectId = resultSet.getInt("project_id");
				
				array.add(projectId);
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
