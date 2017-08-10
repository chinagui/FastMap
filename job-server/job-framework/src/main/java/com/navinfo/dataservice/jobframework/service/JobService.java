package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStatus;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

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
	
	public Map<String,Object> getJobByTask(int taskId, long userId, String jobType) throws Exception {
		Map<String,Object> retMap = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet ret = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select a.*");
		sb.append(" from (");
		sb.append(" select t.job_id,t.job_type,t.create_time,t.status,t.job_request,t.result_msg,t.step_count,s.step_seq,s.step_msg");
		sb.append(" from job_info t,job_step s");
		sb.append(" where t.job_id=s.job_id(+)");
		sb.append(" and t.job_type=:1");
		sb.append(" and t.task_id=:2");
		sb.append(" and t.user_id=:3");
		sb.append(" and t.status in (1,2)");
		sb.append(" order by t.create_time desc");
		sb.append(" ) a");
		sb.append(" where rownum = 1");
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, jobType);
			pstmt.setInt(2, taskId);
			pstmt.setLong(3, userId);
			ret = pstmt.executeQuery();
			if (ret.next()) {
				retMap = new HashMap<String,Object>();
				retMap.put("jobId", ret.getInt("job_id"));
				retMap.put("jobType", ret.getString("job_type"));
				retMap.put("createTime", ret.getTimestamp("create_time"));
				retMap.put("status", ret.getInt("status"));
				retMap.put("jobRequest", ret.getString("job_request"));
				retMap.put("resultMsg", ret.getString("result_msg"));
				retMap.put("stepCount", ret.getInt("step_count"));
				retMap.put("stepSeq", ret.getInt("step_seq"));
				retMap.put("stepMsg",ret.getString("step_msg"));
			}
			return retMap;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		} finally {
			DbUtils.close(ret);
			DbUtils.close(pstmt);
			DbUtils.close(conn);
		}
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
	public JSONObject getLatestJob(int subtaskId,String jobType, String jobDescp)throws ServiceException{
		Connection conn = null;
		JSONObject jobObj = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = "select * from ( select j.job_id , j.job_guid  from job_info j where j.job_type = '"+jobType+"' and j.descp = '"+jobDescp+"' and j.task_id = "+subtaskId+" and j.end_time is not null  order by j.end_time desc ) where rownum=1 ";
			log.info("getLatestJob jobInfoSql: "+jobInfoSql);
			jobObj = run.query(conn, jobInfoSql, new ResultSetHandler<JSONObject>(){
				
				@Override
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jobObjInfo = new JSONObject();
					if(rs.next()){
						jobObjInfo.put("jobId", rs.getInt("job_id"));
						jobObjInfo.put("jobGuid", rs.getString("job_guid"));
					}
					return jobObjInfo;
				}
				
			});
			return jobObj;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		//return jobObj;
	}
	
	
	/**
	 * @Title: getLatestJobByDescp
	 * @Description: 根据任务描述获取最新一次检查任务的 job_guid
	 * @param descp
	 * @return
	 * @throws ServiceException  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月12日 下午5:26:55 
	 */
	public JobInfo getLatestJobByDescp(String descp)throws ServiceException{
		log.info("begin getLatestJobByDescp descp:"+descp);
		Connection conn = null;
		JobInfo jobInfo = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = "select * from ( select  j.job_id , j.job_guid  from job_info j where  1=1 ";
					if(descp != null && StringUtils.isNotEmpty(descp)){
						jobInfoSql += " and  j.descp like '%"+descp+"%'  ";
					}
					
					jobInfoSql +=  "and j.end_time is not null  order by j.end_time desc ) where rownum=1 ";
					log.info("getLatestJobByDescp: "+jobInfoSql);
			jobInfo = run.query(conn, jobInfoSql, new ResultSetHandler<JobInfo>(){
				
				@Override
				public JobInfo handle(ResultSet rs) throws SQLException {
					JobInfo jobInfo = null;
					if(rs.next()){
						long id = rs.getLong("job_id");
						String guid = rs.getString("job_guid");
						log.info("id : "+id+ " guid: "+guid);
						 jobInfo = new JobInfo(id,guid);
						
					}
					return jobInfo;
				}
				
			});
			return jobInfo;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		//return jobObj;
	}
	
	public JobInfo getJobByDescp(String descp)throws ServiceException{
		log.info("begin getJobByDescp descp:"+descp);
		Connection conn = null;
		JobInfo jobInfo = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = "select * from ( select  j.job_id , j.job_guid  from job_info j where  1=1 ";
					if(descp != null && StringUtils.isNotEmpty(descp)){
						jobInfoSql += " and  j.descp = '"+descp+"'  ";
					}
					
					jobInfoSql +=  "and j.end_time is not null  order by j.end_time desc ) where rownum=1 ";
					log.info("getJobByDescp jobInfoSql: "+jobInfoSql);
			jobInfo = run.query(conn, jobInfoSql, new ResultSetHandler<JobInfo>(){
				
				@Override
				public JobInfo handle(ResultSet rs) throws SQLException {
					JobInfo jobInfo = null;
					if(rs.next()){
						long id = rs.getLong("job_id");
						String guid = rs.getString("job_guid");
						log.info("id : "+id+ " guid: "+guid);
						 jobInfo = new JobInfo(id,guid);
						
					}
					return jobInfo;
				}
				
			});
			return jobInfo;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		//return jobObj;
	}

	/**
	 * @Title: getJobObjList
	 * @Description: 根据查询条件获取 job 相关信息集合
	 * @param parameterJson
	 * @return  JSONArray
	 * @throws ServiceException 
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月12日 上午10:44:14 
	 */
	public List<JobInfo> getJobInfoList(JSONObject parameterJson) throws ServiceException {
		System.out.println("begin getJobInfoList parameterJson :"+parameterJson);
		Connection conn = null;
		List<JobInfo> jobInfos = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String jobInfoSql = " select distinct j.job_id , j.job_guid, j.descp  from job_info j where 1=1 ";
// "j.job_type = 'checkCore' and j.descp = '元数据库检查' and j.task_id = "+subtaskId+" and j.end_time is not null  order by j.end_time desc ) where rownum=1 ";
			
			String tableName  = parameterJson.getString("tableName");
			if(tableName!=null || StringUtils.isNotEmpty(tableName)){
				jobInfoSql+=" and j.descp like '%"+tableName+"%'";
			}
			String startDate = "";
			if(parameterJson.containsKey("startDate") && parameterJson.getString("startDate") != null){
				startDate = parameterJson.getString("startDate")+" 00:00:00";
			}
			String endDate = "";
			if(parameterJson.containsKey("endDate") && parameterJson.getString("endDate") != null){
				endDate = parameterJson.getString("endDate") +" 23:59:00";
			}
			if(startDate == null || StringUtils.isEmpty(startDate) 
					|| endDate == null || StringUtils.isEmpty(endDate) ){
				//默认最近一个月内
				Timestamp curTime = DateUtilsEx.getCurTime();
				
				Timestamp beginTime = DateUtilsEx.getDayOfDelayMonths(curTime, -1);
				
				startDate=DateUtilsEx.getTimeStr(beginTime, "yyyy-MM-dd HH:mm:ss");
				endDate = DateUtilsEx.getTimeStr(curTime, "yyyy-MM-dd HH:mm:ss");
				log.info("startDate : "+startDate+"  "+" endDate :"+endDate);
			}
			jobInfoSql+=" and (j.end_time "
					+ "between to_date('"+startDate+"','yyyy-mm-dd hh24:mi:ss') "
					+ "and to_date('"+endDate+"','yyyy-mm-dd hh24:mi:ss') "
					+ ") ";
			
			String jobName = "";
			if(parameterJson.containsKey("jobName") && parameterJson.getString("jobName") != null 
					&& StringUtils.isNotEmpty(parameterJson.getString("jobName")) && !parameterJson.getString("jobName").equals("null")){
				jobName = parameterJson.getString("jobName");
				jobInfoSql+=" and j.descp like '%"+jobName+"%'";
				
			}
			
			jobInfoSql+=" order by j.end_time desc ";
			
			log.info(jobInfoSql);
			jobInfos = run.query(conn, jobInfoSql, new ResultSetHandler<List<JobInfo>>(){
				
				@Override
				public List<JobInfo> handle(ResultSet rs) throws SQLException {
					List<JobInfo> jobArr = new ArrayList<JobInfo>();
					while(rs.next()){
						log.info("job_id: "+rs.getLong("job_id")+" job_guid: "+rs.getString("job_guid")+" descp: "+rs.getString("descp"));
						long id = rs.getLong("job_id");
						String guid = rs.getString("job_guid");
						JobInfo jobInfo = new JobInfo(id,guid);
						jobInfo.setDescp(rs.getString("descp"));
						//jobInfo.setGuid(rs.getString("job_guid"));
						
						jobArr.add(jobInfo);
					}
					log.info("jobArr : "+ jobArr.size());
					return jobArr;
				}
				
			});
			return jobInfos;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("job查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
