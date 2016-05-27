package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.ServiceException;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午7:09:35 
* @Description: TODO
*/
@Service("jobService")
public class JobService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	public String hello()throws ServiceException{
		return "Hello, Job Service!";
	}
	
	public long create(String jobType,JSONObject request,long projectId,long userId,String descp)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();
			long jobId = run.queryForLong(conn, "SELECT JOB_ID_SEQ.NEXTVAL FROM DUAL");
			String jobInfoSql = "INSERT INTO JOB_INFO(JOB_ID,JOB_TYPE,CREATE_TIME,STATUS,JOB_REQUEST,PROJECT_ID,USER_ID,DESCP)"
					+ " VALUES (?,?,SYSDATE,1,?,?,?,?)";
			run.update(conn, jobInfoSql, jobId,jobType,request.toString(),projectId,userId,descp);
			//发送run_job消息
			JobMsgPublisher.runJob(jobId, jobType, request);
			//
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
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();
			String jobInfoSql = "SELECT T.JOB_ID,T.JOB_TYPE,T.CREATE_TIME,T.RUN_TIME,T.STATUS,T.JOB_REQUEST,T.JOB_RESPONSE,T.PROJECT_ID,T.USER_ID,T.DESCP,T.STEP_COUNT"
					+ ",S.STEP_SEQ,S.STEP_MSG,S.BEGIN_TIME,S.END_TIME,S.STATUS AS STEP_STATUS FROM JOB_INFO T,JOB_STEP S WHERE T.JOB_ID=S.JOB_ID(+) AND T.JOB_ID=?";
			jobInfo = run.query(conn, jobInfoSql, new ResultSetHandler<JobInfo>(){

				@Override
				public JobInfo handle(ResultSet rs) throws SQLException {
					JobInfo jobInfo = null;
					if(rs.next()){
						long id = rs.getLong("JOB_ID");
						jobInfo = new JobInfo(id);
						jobInfo.setType(rs.getString("JOB_TYPE"));
						jobInfo.setCreateTime(rs.getTimestamp("CREATE_TIME"));
						jobInfo.setBeginTime(rs.getTimestamp("BEGIN_TIME"));
						jobInfo.setEndTime(rs.getTimestamp("END_TIME"));
						jobInfo.setStatus(rs.getInt("STATUS"));
						jobInfo.setRequest(JSONObject.fromObject(rs.getString("JOB_REQUEST")));
						jobInfo.setResponse(JSONObject.fromObject(rs.getString("JOB_RESPONSE")));
						jobInfo.setProjectId(rs.getLong("PROJECT_ID"));
						jobInfo.setUserId(rs.getLong("USER_ID"));
						jobInfo.setDescp(rs.getString("DESCP"));
						jobInfo.setStepCount(rs.getInt("STEP_COUNT"));
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
				
			}, jobId);
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
}
