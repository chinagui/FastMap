package com.navinfo.dataservice.engine.man.job;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.Exception.JobRunningException;
import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.operator.JobOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobRelationOperator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public abstract class JobRunner {
    private static Logger log = LoggerRepos.getLogger(JobRunner.class);
    /**
     * 在构造函数中传入已知的参数
     */

    public Connection conn;
    public long itemId;
    public ItemType itemType;
    public List<JobPhase> phaseList;
    public long operator;
    public JobType jobType;
    public Job job;
    public JSONObject parameter;

    /**
     * 添加phase到列表
     *
     * @param phase
     */
    public void addPhase(JobPhase phase) {
        phaseList.add(phase);
    }

    /**
     * 填充phase列表
     */
    public abstract void prepare();

    public abstract void initJobType();

    /**
     * 初始化Job，JobPhase
     */
    private void init(boolean isContinue) throws SQLException {

        JobOperator jobOperator = new JobOperator(conn);
        JobRelationOperator jobRelationOperator = new JobRelationOperator(conn);
        JobRelation jobRelation = new JobRelation();
        jobRelation.setItemId(itemId);
        jobRelation.setItemType(itemType);

        if(isContinue){
            job = jobOperator.getLatestJob(itemId,itemType,jobType);
        }
        else {
            job = new Job();
            job.setJobId(jobOperator.getNextId());
            job.setOperator(operator);
            job.setType(jobType);
            job.setParameter(parameter);
            jobOperator.insert(job);
            jobRelation.setJobId(job.getJobId());
            jobRelationOperator.insert(jobRelation);
        }

        int index = 1;
        JobPhase lastJobPhase = null;
        for (JobPhase phase : phaseList) {
            phase.init(conn, job, jobRelation, lastJobPhase.jobProgress, index++, isContinue);
            lastJobPhase = phase;
        }
    }

    /**
     * 执行入口
     */
    public void run(Connection conn, long itemId, ItemType itemType, boolean isContinue, long operator, JSONObject parameter) {
        try {
            this.conn = conn;
            this.itemId = itemId;
            this.itemType = itemType;
            this.operator = operator;
            this.parameter = parameter;

            this.prepare();
            this.initJobType();
            this.init(isContinue);

            boolean finish = true;
            for (JobPhase phase : phaseList) {
                if (phase.jobProgress.getStatus().equals(JobStatus.SUCCESS)) {
                    continue;
                }else if(phase.jobProgress.getStatus().equals(JobStatus.RUNNING)){
                    throw new JobRunningException();
                }

                if(phase.getInvokeType().equals(InvokeType.ASYNC)){
                    //如果调用异步方法，停止执行
                    finish=false;
                    break;
                }
            }

            if(finish){
                JobOperator jobOperator = new JobOperator(conn);
                jobOperator.updateStatus(job.getJobId(),JobStatus.SUCCESS);
            }
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));

            try {
                JobOperator jobOperator = new JobOperator(conn);
                jobOperator.updateStatus(job.getJobId(),JobStatus.FAILURE);
            }catch (Exception e){
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
