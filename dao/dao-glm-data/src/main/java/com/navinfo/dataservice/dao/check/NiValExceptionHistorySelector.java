package com.navinfo.dataservice.dao.check;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import oracle.sql.STRUCT;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;


public class NiValExceptionHistorySelector {

	private Connection conn;

	public NiValExceptionHistorySelector(Connection conn) {
		this.conn = conn;
	}

	public NiValExceptionHistory loadById(String id, boolean isLock) throws Exception {

		NiValExceptionHistory exception = new NiValExceptionHistory();

		String sql = "select * from ni_val_exception_history where md5_code=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				exception.setValExceptionId(resultSet
						.getInt("val_exception_id"));

				exception.setRuleid(resultSet.getString("ruleid"));

				exception.setTaskName(resultSet.getString("task_name"));

				exception.setGroupid(resultSet.getInt("groupid"));

				exception.setLevel(resultSet.getInt("level"));

				exception.setSituation(resultSet.getString("situation"));

				exception.setInformation(resultSet.getString("information"));

				exception.setSuggestion(resultSet.getString("suggestion"));

				STRUCT struct = (STRUCT) resultSet.getObject("location");

				exception.setLocation(GeoTranslator.struct2Jts(struct));

				exception.setTargets(resultSet.getString("targets"));

				exception.setAdditionInfo(resultSet.getString("addition_info"));

				exception.setDelFlag(resultSet.getInt("del_flag"));

				exception.setCreated(resultSet.getString("created"));

				exception.setUpdated(resultSet.getString("updated"));

				exception.setMeshId(resultSet.getInt("mesh_id"));

				exception.setScopeFlag(resultSet.getInt("scope_flag"));

				exception.setProvinceName(resultSet.getString("province_name"));

				exception.setMapScale(resultSet.getInt("map_scale"));

				exception.setReserved(resultSet.getString("reserved"));

				exception.setExtended(resultSet.getString("extended"));

				exception.setTaskId(resultSet.getString("task_id"));

				exception.setQaTaskId(resultSet.getString("qa_task_id"));

				exception.setQaStatus(resultSet.getInt("qa_status"));

				exception.setWorker(resultSet.getString("worker"));

				exception.setQaWorker(resultSet.getString("qa_worker"));

				exception.setLogType(resultSet.getInt("log_type"));

				exception.setMd5Code(resultSet.getString("md5_code"));

			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return exception;

	}

}
