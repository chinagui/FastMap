package com.navinfo.dataservice.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.util.MeshUtils;

public class GridSelector {
	
	private Connection conn;
	
	public GridSelector(Connection conn){
		this.conn = conn;
	}

	public JSONObject getByUser(int userId, int projectId) throws Exception{
		JSONObject result = new JSONObject();
		
		String sql = "select grid_id, user_id, handle_user_id, project_id,handle_project_id from grid where (user_id=:1 or handle_user_id=:2) and (project_id=:3 or handle_project_id=:4)";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, userId);
			
			pstmt.setInt(2, userId);
			
			pstmt.setInt(3, projectId);
			
			pstmt.setInt(4, projectId);

			resultSet = pstmt.executeQuery();
			
			JSONArray normalGrids = new JSONArray();
			JSONArray borrowInGrids = new JSONArray();
			JSONArray borrowOutGrids = new JSONArray();
			JSONArray canBorrowGrids = new JSONArray();
			
			//该用户在该项目下的所有图幅
			Set<String> allMesh = new HashSet<String>();
			
			//该用户在该项目下借来的图幅
			Set<String> borrowInMesh = new HashSet<String>();
			
			//该用户在该项目下借出的图幅
			Set<String> borrowOutMesh = new HashSet<String>();

			while (resultSet.next()) {
				
				int gridId = resultSet.getInt("grid_id");
				
				String meshId= String.valueOf(gridId/100);
				
				int initUserId = resultSet.getInt("user_id");
				
				int handleUserId = resultSet.getInt("handle_user_id");
				
				int initProjectId = resultSet.getInt("project_id");
				
				int handleProjectId = resultSet.getInt("handle_project_id");
				
				if(userId == initUserId && projectId == initProjectId){
					allMesh.add(meshId);
					
					if(userId != handleUserId){
						borrowOutGrids.add(gridId);
						borrowOutMesh.add(meshId);
					}
					else{
						normalGrids.add(gridId);
					}
				}
				
				if(userId != initUserId && userId == handleUserId && projectId == handleProjectId){
					borrowInGrids.add(gridId);
					borrowInMesh.add(meshId);
				}
			}

			Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(allMesh);
			
			extendMeshes.removeAll(allMesh);
			
			extendMeshes.removeAll(borrowInMesh);
			
			extendMeshes.removeAll(borrowOutMesh);
					
			for(String mesh : extendMeshes){
				int grid=Integer.valueOf(mesh) * 100;
				
				canBorrowGrids.add(grid + 1);
				canBorrowGrids.add(grid + 2);
				canBorrowGrids.add(grid + 3);
				canBorrowGrids.add(grid + 4);
				
			}
			
			result.put("CanBorrow", canBorrowGrids);
			
			result.put("BorrowIn", borrowInGrids);
			
			result.put("BorrowOut", borrowOutGrids);
			
			result.put("Normal", normalGrids);
			
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
		
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/Desktop/config.properties");
		
		Connection conn = DBOraclePoolManager
				.getConnectionByName("man");
		
		GridSelector s = new GridSelector(conn);
		
		System.out.println(s.getByUser(4408, 11));
	}
}
