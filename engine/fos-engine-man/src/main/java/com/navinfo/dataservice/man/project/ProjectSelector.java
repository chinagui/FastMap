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
		
		String sql = "select project_id, project_name from project_info order by project_id";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				
				int projectId = resultSet.getInt("project_id");
				
				String prjName = resultSet.getString("project_name");
				
				JSONObject json = new JSONObject();
				
				json.put("projectId", projectId);
				
				json.put("projectName", prjName);
				
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
	
	public int getDbId(int projectId) throws Exception{

		String sql = "select db_id from project_info where project_id=:1";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, projectId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int dbid = resultSet.getInt("db_id");
				
				return dbid;
			}
			else{
				throw new Exception("未找到该项目的信息");
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
		
	}
}
