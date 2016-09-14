package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class ResponseJobHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		Connection conn = null;
		try{
			//持久化
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			//解析message生成jobInfo
			JSONObject o = JSONObject.fromObject(message);
			long jobId = o.getLong("jobId");
			JSONObject step = o.getJSONObject("step");
			int stepSeq = step.getInt("stepSeq");
			String stepMsg = step.getString("stepMsg");
			String stepSql = "INSERT INTO JOB_STEP(JOB_ID,STEP_SEQ,STEP_MSG,END_TIME,STATUS,PROGRESS) VALUES (?,?,?,SYSDATE,1,100)";
			runner.update(conn, stepSql, jobId,stepSeq,stepMsg);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static void main(String[] args){
		try{
			JSONObject o = JSONObject.fromObject("{}");
			Object type = o.get("type");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
