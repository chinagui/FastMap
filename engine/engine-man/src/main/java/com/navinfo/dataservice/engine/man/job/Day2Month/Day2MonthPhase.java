package com.navinfo.dataservice.engine.man.job.Day2Month;

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
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;

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
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();

            int lot = 0;
            int type = 0;
            if (jobRelation.getItemType() == ItemType.LOT) {
                lot = (int) jobRelation.getItemId();
            } else {
                String parameter = job.getParameter();
                JSONObject json = JSONObject.fromObject(parameter);
                type = 1;
                lot = json.getInt("lot");
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
            jobProgressOperator.updateStatus(jobProgress, jobProgress.getStatus());
            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgress.setOutParameter(ex.getMessage());
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            log.info("Day2MonthPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
