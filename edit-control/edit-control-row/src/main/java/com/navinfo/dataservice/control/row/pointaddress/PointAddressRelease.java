package com.navinfo.dataservice.control.row.pointaddress;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 点门牌提交
 * @Title:PointAddressRelease
 * @Package:com.navinfo.dataservice.control.row.pointaddress
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年9月29日
 */
public class PointAddressRelease {
	private static final Logger logger = Logger.getLogger(PointAddressRelease.class);
	
	private PointAddressRelease() {
	}

	private static class SingletonHolder {
		private static final PointAddressRelease INSTANCE = new PointAddressRelease();
	}

	public static PointAddressRelease getInstance() {
		return SingletonHolder.INSTANCE;
	}


	/**
	 * 点门牌提交
	 * @param parameter
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public long release(String parameter, long userId) throws Exception {

		Connection conn = null;
		long jobId = 0;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			long subtaskId = (long)(jsonReq.getInt("subtaskId"));

			JobApi apiService = (JobApi) ApplicationContextUtil
					.getBean("jobApi");
			Integer JobIdExe = apiService.getJobByUserAndSubTask(userId,subtaskId,"pointAddressRelease");
			if(JobIdExe!=null ){
				throw new Exception("你已经存在jobid="+JobIdExe+"的提交job，正在执行；无法重复提交！");
			}
			jobId = apiService.createJob("pointAddressRelease", jsonReq, userId,subtaskId,
					"点门牌提交");
			return jobId;
		}catch (Exception e) {
			logger.error("点门牌提交错误", e);
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
