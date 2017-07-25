package com.navinfo.dataservice.engine.man.job.medium2quick;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;

import net.sf.json.JSONObject;

public class TaskMedium2QuickPhase extends JobPhase{

	@Override
	public JobProgressStatus run() throws Exception {
		log.info("TaskMedium2QuickPhase start:phaseId " + jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();
            
            //业务
            JobApi api = (JobApi) ApplicationContextUtil.getBean("jobApi");
			JSONObject request = JSONObject.fromObject(job.getParameter());
			request.put("phaseId", jobProgress.getPhaseId());
			request.put("jobId", this.job.getJobId());
			
			long jobId = api.createJob("taskMedium2QuickJob", request,  job.getOperator(), jobRelation.getItemId(), "中转快");
			jobProgress.setMessage("jobId:"+jobId);
			jobProgressOperator.updateStatus(jobProgress, jobProgress.getStatus());
			
			return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                JSONObject out = new JSONObject();
                out.put("errmsg",ex.getMessage());
                jobProgress.setOutParameter(out.toString());
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            log.info("TaskMedium2QuickPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
	}

	@Override
	public void initInvokeType() {
		this.invokeType = InvokeType.ASYNC;
	}

}
