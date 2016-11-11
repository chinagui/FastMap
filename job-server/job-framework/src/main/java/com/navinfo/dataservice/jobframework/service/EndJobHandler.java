package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
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
		log.debug("Handle end job message:"+message);
		persMsg(message);
		sendMsg(message);
	}
	
	public void sendMsg(String message){
		try{
			log.debug("sending msg to system message chanle");
			JSONObject jobMsg = JSONObject.fromObject(message);
			long userId = jobMsg.getLong("userId");
			long jobId = jobMsg.getLong("jobId");
			int status = jobMsg.getInt("status");
			String resultMsg = jobMsg.getString("resultMsg");
			JSONObject resp = jobMsg.getJSONObject("response");
			String jobTypeName = jobMsg.getString("jobTypeName");
			long durationSeconds = jobMsg.getLong("durationSeconds");
			//计算完成时间
			long nd = 24*60*60;//一天的毫秒数
			long nh = 60*60;//一小时的毫秒数
			long nm = 60;//一分钟的毫秒数
			long day = durationSeconds/nd;//计算差多少天
			long hour = durationSeconds%nd/nh;//计算差多少小时
			long min = durationSeconds%nd%nh/nm;//计算差多少分钟
			long sec = durationSeconds%nd%nh%nm/1;//计算差多少秒
			StringBuilder diffTime = new StringBuilder();
			diffTime.append("用时");
			if(day != 0){
				diffTime.append(day+"天");
			}
			if(hour != 0){
				diffTime.append(hour+"小时");
			}
			if(min != 0){
				diffTime.append(min+"分钟");
			}
			diffTime.append(sec+"秒!");
			String runStatus = null;
			if(status == 3){
				runStatus = "执行成功";
			}else if(status == 4){
				runStatus = "执行失败";
			}else{
				runStatus = "执行完成";
			}
			
			//关联要素
			JSONObject msgParam = new JSONObject();
			msgParam.put("relateObject", "JOB");
			msgParam.put("relateObjectId", jobId);
			SysMsgPublisher.publishMsg(jobTypeName+"任务(ID:"+jobId+")"+runStatus+","+diffTime.toString(), resultMsg, 0, new long[]{userId}, 1, msgParam.toString(), null);
		}catch(Exception e){
			log.warn("接收到end_job消息,但处理过程中出错，消息已消费。message："+message);
			log.error(e.getMessage(),e);
		}
	}
	public void persMsg(String message){
		Connection conn = null;
		try{
			log.debug("persXing message.");
			//持久化
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			//解析message生成jobInfo
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
