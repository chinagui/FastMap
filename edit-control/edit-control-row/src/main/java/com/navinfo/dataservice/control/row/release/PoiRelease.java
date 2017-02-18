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
			long taskId = jsonReq.getLong("taskId");
			int dbId = jsonReq.getInt("dbId");
			JSONArray gridIds = jsonReq.getJSONArray("gridIds");

			JSONObject jobReq = new JSONObject();
			jobReq.put("targetDbId", dbId);
			jobReq.put("gridIds", gridIds);
			//get poi all check rules
			/*conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT WM_CONCAT(RULE_CODE) FROM CK_RULE_COP WHERE SUITE_ID='suite1'";
			String rules = new QueryRunner().queryForString(conn, sql);
			JSONArray ckRulesJA = new JSONArray();
			if(StringUtils.isNotEmpty(rules)){
				ckRulesJA.addAll(Arrays.asList(rules.split(",")));
			}
			
			jobReq.put("checkRules", ckRulesJA);*/
			JobApi apiService = (JobApi) ApplicationContextUtil
					.getBean("jobApi");
			jobId = apiService.createJob("editPoiBaseRelease", jobReq, userId,taskId,
					"POI行编提交");
			return jobId;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
