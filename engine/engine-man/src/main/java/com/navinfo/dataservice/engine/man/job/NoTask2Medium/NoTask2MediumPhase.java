package com.navinfo.dataservice.engine.man.job.NoTask2Medium;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;

import net.sf.json.JSONObject;

public class NoTask2MediumPhase extends JobPhase {

	@Override
	public JobProgressStatus run() throws Exception {
		log.info("NoTask2MediumPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();
            
            
            JobApi api=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JSONObject request=new JSONObject();
			request.put("phaseId", jobProgress.getPhaseId());
			request.put("taskId", jobRelation.getItemId());
			long jobId=api.createJob("taskOther2MediumJob", request,  job.getOperator(), jobRelation.getItemId(), "无任务采集成果入中");
			
			jobProgress.setMessage("jobId:" + jobId);
            jobProgressOperator.updateStatus(jobProgress);
            //return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            jobProgress.setStatus(JobProgressStatus.FAILURE);
            if (jobProgressOperator != null && jobProgress != null) {
                JSONObject out = new JSONObject();
                out.put("errmsg",ex.getMessage());
                jobProgress.setOutParameter(out.toString());
                jobProgressOperator.updateStatus(jobProgress);
            }
            //throw ex;
        } finally {
            log.info("NoTask2MediumPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
        return jobProgress.getStatus();
	}

	@Override
	public void initInvokeType() {
		this.invokeType = InvokeType.ASYNC;
	}

}
