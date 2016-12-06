package com.navinfo.dataservice.jobframework.runjob;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStatus;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class RunJobHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		Connection conn = null;
		try{
			//解析message生成jobInfo
			log.debug("Handle run job message:"+message);
			JSONObject jo = JSONObject.fromObject(message);
			long jobId = jo.getLong("jobId");
			String jobGuid = jo.getString("jobGuid");
			String type = jo.getString("type");
			long userId = jo.getLong("userId");
			long taskId=jo.getLong("taskId");
			JSONObject request = JSONObject.fromObject(jo.get("request"));
			JobInfo jobInfo = new JobInfo(jobId,jobGuid);
			jobInfo.setType(type);
			jobInfo.setRequest(request);
			jobInfo.setUserId(userId);
			jobInfo.setTaskId(taskId);
			//添加到任务线程池
			JobThreadPoolExecutor.getInstance().execute(jobInfo);
			//
			log.debug("设置job:"+jobId+"状态为执行中");
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "UPDATE JOB_INFO SET BEGIN_TIME=SYSDATE,STEP_COUNT=?,STATUS=?,RESULT_MSG=? WHERE JOB_ID=?";
			new QueryRunner().update(conn, sql, jobInfo.getStepCount(),jobInfo.getStatus(),jobInfo.getResultMsg(),jobInfo.getId());
		}catch(Exception e){
			if(e instanceof SQLException){
				log.warn("job已加入线程池，job设置执行状态时发生SQL错误，可以忽略，不影响job主体");
				DbUtils.rollbackAndCloseQuietly(conn);
			}else{
				log.warn("接收到job执行消息,但加入job线程池失败，该消息已消费。message："+message);
			}
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
