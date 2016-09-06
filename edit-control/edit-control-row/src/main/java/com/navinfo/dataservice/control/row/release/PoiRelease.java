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

}
