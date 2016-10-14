package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class EndJobHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		sendMsg(message);
	}
	
	public void sendMsg(String message){
		try{
			log.debug("Handle end job message:"+message);
			JSONObject jobMsg = JSONObject.fromObject(message);
			long userId = jobMsg.getLong("userId");
			long jobId = jobMsg.getLong("jobId");
			int status = jobMsg.getInt("status");
			String resultMsg = jobMsg.getString("resultMsg");
			JSONObject resp = jobMsg.getJSONObject("response");
			SysMsgPublisher.publishMsg("job("+jobId+")执行完成", resultMsg, 0, new long[]{userId});
		}catch(Exception e){
			log.warn("接收到end_job消息,但处理过程中出错，消息已消费。message："+message);
			log.error(e.getMessage(),e);
		}
	}
	public void persMsg(String message){
		Connection conn = null;
		try{
			//持久化
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			//解析message生成jobInfo
			log.debug("Handle end job message:"+message);
			JSONObject jobMsg = JSONObject.fromObject(message);
			long jobId = jobMsg.getLong("jobId");
			int status = jobMsg.getInt("status");
			String resultMsg = jobMsg.getString("resultMsg");
			JSONObject resp = jobMsg.getJSONObject("response");
			String jobInfoSql = "UPDATE JOB_INFO SET END_TIME=SYSDATE,STATUS=?,RESULT_MSG=?,JOB_RESPONSE=? WHERE JOB_ID=?";
			runner.update(conn, jobInfoSql,status,resultMsg,resp.toString(),jobId);
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
