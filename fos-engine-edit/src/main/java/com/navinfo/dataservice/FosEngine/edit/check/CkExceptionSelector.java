package com.navinfo.dataservice.FosEngine.edit.check;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;

public class CkExceptionSelector {
	
	private static Logger logger = Logger.getLogger(CkExceptionSelector.class);

	private Connection conn;

	public CkExceptionSelector(Connection conn) {
		this.conn = conn;
	}

	public void loadById(int id, boolean isLock) throws Exception {

	}

	/**
	 * 获取某作业员的检查结果, 并分页， 排序
	 * 
	 * @param worker
	 *            作业员ID
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
	public List<CkException> loadByWorker(String worker, int pageSize,
			int pageNum, String orderField, int orderDirection)
			throws Exception {
		List<CkException> reses = new ArrayList<CkException>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String sql = "";

		if (-1 == orderDirection) {

			sql = "SELECT * FROM (     SELECT a.*, rownum rn     FROM     (         select exception_id, rule_id, task_name, status, group_id, rank, situation, information,"
					+ "suggestion, geometry, targets, addition_info, memo, to_char(create_date,'yyyymmddhh24miss') create_date,                 to_char(update_date,'yyyymmddhh24miss') update_date, mesh_id, scope_flag, province_name, map_scale,"
					+ "reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, memo_1, memo_2, memo_3                  from ck_exception where worker = :1 order by :2 desc     ) a     WHERE rownum <= :3 ) WHERE rn >= :4";

		} else {

			sql = "SELECT * FROM (     SELECT a.*, rownum rn     FROM     (         select exception_id, rule_id, task_name, status, group_id, rank, situation, information,"
					+ "suggestion, geometry, targets, addition_info, memo, to_char(create_date,'yyyymmddhh24miss') create_date,                 to_char(update_date,'yyyymmddhh24miss') update_date, mesh_id, scope_flag, province_name, map_scale,"
					+ "reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, memo_1, memo_2, memo_3                  from ck_exception where worker = :1 order by :2 asc     ) a     WHERE rownum <= :3 ) WHERE rn >= :4";

		}

		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, worker);

			pstmt.setString(2, StringUtils.toColumnName(orderField));

			pstmt.setInt(3, endRow);

			pstmt.setInt(4, startRow);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				CkException res = new CkException();

				res.setExceptionId(resultSet.getInt("pid"));

				res.setRuleId(resultSet.getString("rule_id"));

				res.setTaskName(resultSet.getString("task_name"));

				res.setStatus(resultSet.getInt("status"));

				res.setGroupId(resultSet.getInt("group_id"));

				res.setRank(resultSet.getInt("rank"));

				res.setSituation(resultSet.getString("situation"));

				res.setInformation(resultSet.getString("information"));

				res.setSuggestion(resultSet.getString("suggestion"));

				res.setGeometry(resultSet.getString("geometry"));

				res.setTargets(resultSet.getString("targets"));

				res.setAdditionInfo(resultSet.getString("addition_info"));

				res.setMemo(resultSet.getString("memo"));

				res.setCreateDate(resultSet.getString("create_date"));

				res.setUpdateDate(resultSet.getString("update_date"));

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

				res.setMemo1(resultSet.getString("memo1"));

				res.setMemo2(resultSet.getString("memo2"));

				res.setMemo3(resultSet.getString("memo3"));

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
