package com.navinfo.dataservice.engine.man.job.Day2Month;

import java.sql.Connection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import net.sf.json.JSONObject;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class Day2MonthPhase extends JobPhase {

    private static Logger log = LoggerRepos.getLogger(Day2MonthPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.ASYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        log.info("Day2MonthPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();

            int lot = 0;
            int type = 0;
            if (jobRelation.getItemType() == ItemType.LOT) {
                lot = (int) jobRelation.getItemId();
            } else {
                type = 1;
            }
            JSONObject jobDataJson = new JSONObject();
            jobDataJson.put("type", type);
            jobDataJson.put("taskInfo", Day2MonthUtils.getTaskInfo(conn, jobRelation.getItemId(), jobRelation.getItemType()));
            jobDataJson.put("lot", lot);
            jobDataJson.put("phaseId", jobProgress.getPhaseId());
            log.info("phaseId:"+jobProgress.getPhaseId()+",day2MonSync:"+jobDataJson.toString());
            JobApi jobApi = (JobApi) ApplicationContextUtil.getBean("jobApi");
            long jobId = jobApi.createJob("day2MonSync", jobDataJson, job.getOperator(), jobRelation.getItemId(), "日落月");
            jobProgress.setMessage("jobId:" + jobId);
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
            log.info("Day2MonthPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
        return jobProgress.getStatus();
    }
}
