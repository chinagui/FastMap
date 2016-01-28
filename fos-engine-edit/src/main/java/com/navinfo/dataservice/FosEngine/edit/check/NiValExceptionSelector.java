package com.navinfo.dataservice.FosEngine.edit.check;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

public class NiValExceptionSelector {

	private Connection conn;
	
	public NiValExceptionSelector() {
	}

	public NiValExceptionSelector(Connection conn) {
		this.conn = conn;
	}

	public void loadById(int id, boolean isLock) throws Exception {

	}

	/**
	 * 根据图幅获取检查结果, 并分页， 排序
	 * 
	 * @param meshes
	 *            图幅列表
	 * @param pageSize
	 *            每页大小
	 * @param pageNum
	 *            页数
	 * @param orderField
	 *            排序的字段名
	 * @param orderDirection
	 *            排序方向
	 * @return 检查结果列表
	 * @throws Exception
	 */
	public List<NiValException> loadByMesh(List<Integer> meshes, int pageSize,
			int pageNum, String orderField, int orderDirection)
			throws Exception {
		List<NiValException> reses = new ArrayList<NiValException>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String sql = "";

		String meshStr = "";

		for (int i = 0; i < meshes.size(); i++) {
			meshStr += meshes.get(i);

			if ((i + 1) < meshes.size()) {
				meshStr += ",";
			}
		}

		sql = "select val_exception_id, rule_id, task_name, group_id, level, situation, information,"
				+ "suggestion, location, targets, addition_info,del_flag, to_char(created,'yyyymmddhh24miss') created,                 to_char(updated,'yyyymmddhh24miss') updated, mesh_id, scope_flag, province_name, map_scale,"
				+ "reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, from ni_val_exception where mesh_id in ("
				+ meshStr + ")";

		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				NiValException res = new NiValException();

				res.setValExceptionId(resultSet.getInt("val_exception_id"));

				res.setRuleId(resultSet.getString("rule_id"));

				res.setTaskName(resultSet.getString("task_name"));

				res.setGroupId(resultSet.getInt("group_id"));

				res.setLevel(resultSet.getInt("level"));

				res.setSituation(resultSet.getString("situation"));

				res.setInformation(resultSet.getString("information"));

				res.setSuggestion(resultSet.getString("suggestion"));

				STRUCT struct = (STRUCT) resultSet.getObject("location");

				Geometry location = GeoTranslator.struct2Jts(struct, 100000, 0);

				res.setLocation(location);

				res.setTargets(resultSet.getString("targets"));

				res.setAdditionInfo(resultSet.getString("addition_info"));

				res.setDelFlag(resultSet.getInt("del_flag"));

				res.setCreated(resultSet.getString("created"));

				res.setUpdated(resultSet.getString("updated"));

				res.setMeshId(resultSet.getInt("mesh_id"));

				res.setScopeFlag(resultSet.getInt("scope_flag"));

				res.setProvinceName(resultSet.getString("province_name"));

				res.setMapScale(resultSet.getInt("map_scale"));

				res.setReserved(resultSet.getString("reserved"));

				res.setExtended(resultSet.getString("extended"));

				res.setTaskId(resultSet.getString("task_id"));

				res.setQaStatus(resultSet.getInt("qa_status"));

				res.setQaTaskId(resultSet.getString("qa_task_id"));

				res.setWorker(resultSet.getString("worker"));

				res.setQaWorker(resultSet.getString("qa_worker"));

				res.setLogType(resultSet.getInt("log_type"));

				reses.add(res);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
			}

			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}

		return reses;
	}
	
	
	public JSONArray queryException(int projectId,JSONArray meshes,int pageSize,int page) throws Exception{
		
		conn = DBOraclePoolManager.getConnection(projectId);
		
		JSONArray results = new JSONArray();
		
		Statement stmt = null;
		
		ResultSet rs = null;
		
		StringBuilder sql = new StringBuilder("select * from (select b.*,rownum rn from (select ruleid,situation,'level' level_,targets,information,a.location.sdo_point.x x," +
				"a.location.sdo_point.y y,created,worker from ni_val_exception a where del_flag = 0 and mesh_id in (");
		
		for(int i=0;i<meshes.size();i++){
			if (i > 0){
				sql.append(",");
				
				sql.append(meshes.getInt(i));
			}else{
				sql.append(meshes.getInt(i));
			}
		}
		
		sql.append(") order by created ) b where rownum<=");
		
		sql.append(pageSize * page);
		
		sql.append(") where rn>");
		
		sql.append((page - 1) * pageSize);
		
		try{
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()){
				JSONObject json = new JSONObject();
				
				json.put("ruleid", rs.getString("ruleid"));
				
				json.put("situation",rs.getString("situation"));
				
				json.put("rank", rs.getInt("level_"));
				
				json.put("targets", rs.getString("targets"));
				
				json.put("information", rs.getString("information"));
				
				json.put("geometry", "("+rs.getInt("x")+","+rs.getInt("y")+")");
				
				json.put("create_date", rs.getString("created"));
				
				json.put("worker", rs.getString("worker"));
				
				results.add(json);
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
			
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
		
		return results;
	}
	

}
