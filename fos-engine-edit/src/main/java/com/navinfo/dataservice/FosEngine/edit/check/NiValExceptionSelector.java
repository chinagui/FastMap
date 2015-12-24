package com.navinfo.dataservice.FosEngine.edit.check;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

public class NiValExceptionSelector {

	private Connection conn;

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

}
