package com.navinfo.dataservice.engine.man.job.mangeMesh;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.config.ConfigService;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;

import net.sf.json.JSONObject;

public class MangeMeshPhase extends JobPhase{

	@Override
	public JobProgressStatus run() throws Exception {
		log.info("MangeMeshPhase start:phaseId " + jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();
            log.info("start exe mangeMesh method");
            JSONObject parameter = JSONObject.fromObject(job.getParameter());
            ConfigService.getInstance().mangeMesh(parameter);
            log.info("end exe mangeMesh method");
            //更新状态为成功
            jobProgress.setStatus(JobProgressStatus.SUCCESS);
			jobProgressOperator.updateStatus(jobProgress);			
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
            log.info("MangeMeshPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
        return jobProgress.getStatus();
	}

	@Override
	public void initInvokeType() {
		this.invokeType = InvokeType.SYNC;
	}

}
