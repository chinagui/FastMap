package com.navinfo.dataservice.jobframework.service;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.ServiceException;
import com.navinfo.dataservice.api.job.iface.JobExternalService;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobInfoService 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:14:03 
* @Description: TODO
*/
@Service("jobExternalService")
public class JobExternalServiceImpl implements JobExternalService{
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	public long createJob(String jobType,JSONObject request,long projectId,long userId,String descp)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();
			long jobId = run.queryForLong(conn, "SELECT JOB_ID_SEQ.NEXTVAL FROM DUAL");
			String jobInfoSql = "INSERT INTO JOB_INFO(JOB_ID,JOB_TYPE,CREATE_TIME,STATUS,JOB_REQUEST,PROJECT_ID,USER_ID,DESCP)"
					+ " VALUES (?,?,SYSDATE,0,?,?,?,?)";
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
		return null;
	}
	public JobInfo getJobByType(String jobType)throws ServiceException{
		return null;
	}
	@Override
	public String help() {
		// TODO Auto-generated method stub
		return "Hello,Job External Service.";
	}
}
