package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStatus;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午7:09:35 
* @Description: TODO
*/
public class JobService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private JobService(){}
	private static class SingletonHolder{
		private static final JobService INSTANCE=new JobService();
	}
	public static final JobService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	public long create(String jobType,JSONObject request,long userId,long taskId,String descp)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			long jobId = run.queryForLong(conn, "SELECT JOB_ID_SEQ.NEXTVAL FROM DUAL");
			String jobGuid = UuidUtils.genUuid();
			String jobInfoSql = "INSERT INTO JOB_INFO(JOB_ID,JOB_TYPE,CREATE_TIME,STATUS,JOB_REQUEST,JOB_GUID,USER_ID,TASK_ID,DESCP)"
					+ " VALUES (?,?,SYSDATE,?,?,?,?,?,?)";
			run.update(conn, jobInfoSql, jobId,jobType,JobStatus.STATUS_CREATE,request.toString(),jobGuid,userId,taskId,descp);
			//发送run_job消息
			JobMsgPublisher.runJob(jobId,jobGuid,jobType,request,userId,taskId);
			return jobId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public List<JobInfo> getAllJob()throws ServiceException{
		return null;
	}
	public JobInfo getJobById(long jobId)throws ServiceException{
		Connection conn = null;
		JobInfo jobInfo = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = "SELECT T.JOB_ID,T.JOB_TYPE,T.CREATE_TIME,T.BEGIN_TIME,T.END_TIME,T.STEP_COUNT,T.STATUS,T.JOB_REQUEST,T.JOB_RESPONSE,T.RESULT_MSG,T.JOB_GUID,T.USER_ID,T.DESCP"
					+ ",S.STEP_SEQ,S.STEP_MSG,S.BEGIN_TIME,S.END_TIME,S.STATUS AS STEP_STATUS,T.TASK_ID FROM JOB_INFO T,JOB_STEP S WHERE T.JOB_ID=S.JOB_ID(+) AND T.JOB_ID=? ORDER BY S.STEP_SEQ";
			jobInfo = run.query(conn, jobInfoSql, new FullHandler(), jobId);
			return jobInfo;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public JobInfo getJobByGuid(String jobGuid)throws ServiceException{
		Connection conn = null;
		JobInfo jobInfo = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = "SELECT T.JOB_ID,T.JOB_TYPE,T.CREATE_TIME,T.BEGIN_TIME,T.END_TIME,T.STEP_COUNT,T.STATUS,T.JOB_REQUEST,T.JOB_RESPONSE,T.JOB_GUID,T.USER_ID,T.DESCP"
					+ ",S.STEP_SEQ,S.STEP_MSG,S.BEGIN_TIME,S.END_TIME,S.STATUS AS STEP_STATUS FROM JOB_INFO T,JOB_STEP S WHERE T.JOB_ID=S.JOB_ID(+) AND T.JOB_GUID=? ORDER BY S.STEP_SEQ";
			jobInfo = run.query(conn, jobInfoSql, new FullHandler(), jobGuid);
			return jobInfo;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public JobInfo getJobByType(String jobType)throws ServiceException{
		return null;
	}
	
	/* resultset handler */
	class FullHandler implements ResultSetHandler<JobInfo>{
		@Override
		public JobInfo handle(ResultSet rs) throws SQLException {
			JobInfo jobInfo = null;
			if(rs.next()){
				long id = rs.getLong("JOB_ID");
				String guid = rs.getString("JOB_GUID");
				jobInfo = new JobInfo(id,guid);
				jobInfo.setType(rs.getString("JOB_TYPE"));
				jobInfo.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				jobInfo.setBeginTime(rs.getTimestamp("BEGIN_TIME"));
				jobInfo.setEndTime(rs.getTimestamp("END_TIME"));
				jobInfo.setStatus(rs.getInt("STATUS"));
				jobInfo.setRequest(JSONObject.fromObject(rs.getString("JOB_REQUEST")));
				jobInfo.setResponse(JSONObject.fromObject(rs.getString("JOB_RESPONSE")));
				jobInfo.setResultMsg(rs.getString("RESULT_MSG"));
				jobInfo.setUserId(rs.getLong("USER_ID"));
				jobInfo.setDescp(rs.getString("DESCP"));
				jobInfo.setStepCount(rs.getInt("STEP_COUNT"));
				jobInfo.setTaskId(rs.getLong("TASK_ID"));
				List<JobStep> steps = new ArrayList<JobStep>();
				do{
					if(rs.getObject("STEP_SEQ")!=null){
						JobStep step = new JobStep(id);
						step.setStepSeq(rs.getInt("STEP_SEQ"));
						step.setStepMsg(rs.getString("STEP_MSG"));
						step.setBeginTime(rs.getTimestamp("BEGIN_TIME"));
						step.setEndTime(rs.getTimestamp("END_TIME"));
						step.setStatus(rs.getInt("STEP_STATUS"));
						steps.add(step);
					}
				}while(rs.next());
				jobInfo.setSteps(steps);
			}
			return jobInfo;
		}
	}
	public String hello()throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return "conn:"+conn.toString();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
