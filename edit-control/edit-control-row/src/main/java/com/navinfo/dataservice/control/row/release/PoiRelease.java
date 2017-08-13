package com.navinfo.dataservice.control.row.release;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;
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
			long subtaskId = jsonReq.getLong("subtaskId");
			int dbId = jsonReq.getInt("dbId");
			JSONArray gridIds = jsonReq.getJSONArray("gridIds");

			JSONObject jobReq = new JSONObject();
			jobReq.put("targetDbId", dbId);
			jobReq.put("gridIds", gridIds);
			JobApi apiService = (JobApi) ApplicationContextUtil
					.getBean("jobApi");
			Integer JobIdExe = apiService.getJobByUserAndSubTask(userId,subtaskId,"editPoiBaseRelease");
			if(JobIdExe!=null ){
				throw new Exception("你已经存在jobid="+JobIdExe+"的提交job，正在执行；无法重复提交！");
			}
			jobId = apiService.createJob("editPoiBaseRelease", jobReq, userId,subtaskId,
					"POI行编提交");
			return jobId;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
