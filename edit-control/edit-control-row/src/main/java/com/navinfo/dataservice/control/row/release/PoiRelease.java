package com.navinfo.dataservice.control.row.release;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;

import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiRelease {
	private static final Logger logger = Logger.getLogger(PoiRelease.class);

	/**
	 * @zhaokk POI行編保存
	 * @param classNames
	 * @param poi
	 * @return
	 * @throws Exception
	 */
	public long release(String parameter, long userId) throws Exception {

		Connection conn = null;
		long jobId = 0;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId = jsonReq.getInt("dbId");
			JSONArray gridIds = jsonReq.getJSONArray("gridIds");

			JSONObject jobReq = new JSONObject();
			jobReq.put("targetDbId", dbId);
			jobReq.put("gridIds", gridIds);
			JobApi apiService = (JobApi) ApplicationContextUtil
					.getBean("jobApi");
			jobId = apiService.createJob("editPoiBaseRelease", jobReq, userId,
					"POI行编提交");

		} catch (DataNotChangeException e) {
			DbUtils.rollbackAndClose(conn);
			logger.error(e.getMessage(), e);

		} catch (Exception e) {
			DbUtils.rollbackAndClose(conn);
			logger.error(e.getMessage(), e);
		} finally {
			DbUtils.commitAndClose(conn);
		}
		return jobId;
	}

	/**
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus(String pids, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT row_id as a , 2 AS b,0 AS C FROM ix_poi where pid in ("
				+ pids + ")) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
		}

	}

}
