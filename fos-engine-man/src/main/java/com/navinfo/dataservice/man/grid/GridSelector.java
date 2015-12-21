package com.navinfo.dataservice.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GridSelector {
	
	private Connection conn;
	
	public GridSelector(Connection conn){
		this.conn = conn;
	}

	public List<Integer> getByUser(int userId) throws Exception{
		List<Integer> list = new ArrayList<Integer>();
		
		String sql = "select grid_id from grid where user_id=?";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, userId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				int gridId = resultSet.getInt("grid_id");
				
				list.add(gridId);
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
		
		return list;
	}
}
