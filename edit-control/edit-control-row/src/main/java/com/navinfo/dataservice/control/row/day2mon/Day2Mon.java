package com.navinfo.dataservice.control.row.day2mon;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONObject;

public class Day2Mon {
	private static final Logger logger = Logger.getLogger(Day2Mon.class);
	
	/**
	 * 日落月job创建
	 * @param parameter {"cityId":"2"} cityId为空，则全部city都要落
	 * @param userId 当前用户id
	 * @return jobid
	 * @throws Exception
	 */
	public long sync(String parameter, long userId) throws Exception {

		Connection conn = null;
		long jobId = 0;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			String specRegionId = jsonReq.getString("specRegionId");
			String specMeshes = jsonReq.getString("specMeshes");
			JSONObject jobReq = new JSONObject();
			jobReq.element("specRegionId", specRegionId);
			jobReq.element("specMeshes", specMeshes);
			JobApi apiService = (JobApi) ApplicationContextUtil
					.getBean("jobApi");
			jobId = apiService.createJob("day2MonSync", jobReq, userId,0,
					"日落月");
			return jobId;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
