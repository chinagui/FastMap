package com.navinfo.dataservice.engine.man.job;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public abstract class JobPhase {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
    public JobProgress jobProgress;
    public Job job;
    public JobProgress lastJobProgress;
    public InvokeType invokeType;
    public JobRelation jobRelation;

    /**
     * 初始化phase
     * 1.如果是新执行的任务，则在库中新增一条phase记录
     * 2.如果是重新执行的任务，则读取库中已有的记录，如果状态为失败，更新状态为已创建
     */
    public void init(Connection conn, Job job, JobRelation jobRelation, JobProgress lastJobProgress, int phase, boolean isContinue) throws Exception {
        log.info("JobPhase init start:jobId "+job.getJobId()+",phase "+phase);
        this.job = job;
        this.lastJobProgress = lastJobProgress;
        this.jobRelation = jobRelation;
        this.initInvokeType();

        JobProgressOperator jobProgressOperator = new JobProgressOperator(conn);
        if (isContinue) {
            //重复执行的，读取库中记录
            jobProgress = jobProgressOperator.getByJobId(job.getJobId(), phase);
            if (jobProgress == null) {
                throw new Exception("未找到正在执行的步骤，无法继续执行");
            }
            if (jobProgress.getStatus() == JobProgressStatus.FAILURE) {
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.CREATED);
            }
        } else {
            //新增一条记录
            jobProgress = new JobProgress();
            jobProgress.setPhaseId(jobProgressOperator.getNextId());
            jobProgress.setJobId(job.getJobId());
            jobProgress.setPhase(phase);
            jobProgressOperator.insert(jobProgress);
        }
        log.info("JobPhase init end:jobId "+job.getJobId()+",phase "+phase);
    }

    /**
     * 执行入口
     *
     * @throws Exception
     */
    public abstract JobProgressStatus run() throws Exception;

    /**
     * 设置步骤的调用类型（异步、同步）
     */
    public abstract void initInvokeType();

    public InvokeType getInvokeType() {
        return invokeType;
    }
}
