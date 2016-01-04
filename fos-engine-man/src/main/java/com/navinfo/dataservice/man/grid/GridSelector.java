package com.navinfo.dataservice.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;

public class GridSelector {
	
	private Connection conn;
	
	public GridSelector(Connection conn){
		this.conn = conn;
	}

	public JSONArray getByUser(int userId, int projectId) throws Exception{
		JSONArray array = new JSONArray();
		
		String sql = "select grid_id from grid a, prj_progrid_relation b where a.grid_id = b.grid_id and a.user_id=:1 and b.project_id=:2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, userId);
			
			pstmt.setInt(2, projectId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int gridId = resultSet.getInt("grid_id");
				
				array.add(gridId);
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
