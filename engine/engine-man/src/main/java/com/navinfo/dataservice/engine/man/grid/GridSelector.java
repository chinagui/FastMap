package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.engine.dao.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class GridSelector {
	private GridSelector(){}
	private static class SingletonHolder{
		private static final GridSelector INSTANCE =new GridSelector();
	}
	public static GridSelector getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public JSONObject getByUser(int userId, int projectId) throws Exception {
		JSONObject result = new JSONObject();

		String sql = "select grid_id, user_id, handle_user_id, project_id,handle_project_id from grid where (user_id=:1 or handle_user_id=:2) and (project_id=:3 or handle_project_id=:4)";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getConnection();

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

			// 该用户在该项目下的所有图幅
			Set<String> allMesh = new HashSet<String>();

			// 该用户在该项目下借来的图幅
			Set<String> borrowInMesh = new HashSet<String>();

			// 该用户在该项目下借出的图幅
			Set<String> borrowOutMesh = new HashSet<String>();

			while (resultSet.next()) {

				int gridId = resultSet.getInt("grid_id");

				String meshId = String.valueOf(gridId / 100);

				int initUserId = resultSet.getInt("user_id");

				int handleUserId = resultSet.getInt("handle_user_id");

				int initProjectId = resultSet.getInt("project_id");

				int handleProjectId = resultSet.getInt("handle_project_id");

				if (userId == initUserId && projectId == initProjectId) {
					allMesh.add(meshId);

					if (userId != handleUserId) {
						borrowOutGrids.add(gridId);
						borrowOutMesh.add(meshId);
					} else {
						normalGrids.add(gridId);
					}
				}

				if (userId != initUserId && userId == handleUserId
						&& projectId == handleProjectId) {
					borrowInGrids.add(gridId);
					borrowInMesh.add(meshId);
				}
			}

			Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(allMesh);

			extendMeshes.removeAll(allMesh);

			extendMeshes.removeAll(borrowInMesh);

			extendMeshes.removeAll(borrowOutMesh);

			for (String mesh : extendMeshes) {
				int grid = Integer.valueOf(mesh) * 100;

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

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

		return result;
	}
	/**
	 * @param gridList  <br/>
	 * <b>注意：如果参数gridList太长，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；<br/>
	 * @throws Exception 
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception{
		String sql = "select grid_id,region_id from grid where 1=1  ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			OracleSchema schema = (OracleSchema)new DbManager().getOnlyDbByType("fmMan");//TODO:
			conn = schema.getDriverManagerDataSource().getConnection();
			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>(){

				@Override
				public MultiValueMap handle(ResultSet rs) throws SQLException {
					if (rs!=null){
						MultiValueMap mvMap = new MultiValueMap();
						while(rs.next()){
							int gridId = rs.getInt("grid_id");
							int regionId = rs.getInt("region_id");
							mvMap.put(regionId, gridId);
						}
						return mvMap;
					}
					return null;
				}};
			StringBuffer InClause = buildInClause("grid_id",gridList);
			sql=sql+InClause;
			if(InClause!=null){
				return queryRunner.query(conn, sql, rsh);
			}else{
				return queryRunner.query(conn, sql, gridList.toArray(), rsh);
			}
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	private StringBuffer buildInClause(String columName,List inValuesList){
		int size = inValuesList.size();
		if (size==0) return null;
		StringBuffer whereClaus= new StringBuffer();
		for (int i=0;i<size;i++){
			if (i==0){
				whereClaus.append("and "+columName+" in (?");//grid_id 
			}else{
				if (i==size-1){
					whereClaus.append(",?)");
				}else{
					whereClaus.append(",?");
				}
				
			}
		}
		return whereClaus;
	}
	public static void main(String[] args) throws Exception {

		GridSelector s = new GridSelector();

		System.out.println(s.getByUser(4408, 11));
	}
	
}
