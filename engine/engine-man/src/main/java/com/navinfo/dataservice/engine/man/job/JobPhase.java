package com.navinfo.dataservice.engine.man.job;

import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public abstract class JobPhase {

    /**
     * 在构造函数中传入已知的参数
     */
    public Connection conn;
    public JobProgress jobProgress;
    public JobProgressOperator jobProgressOperator;
    public Job job;
    public JobProgress lastJobProgress;
    public InvokeType invokeType;

    /**
     * 初始化phase
     * 1.如果是新执行的任务，则在库中新增一条phase记录
     * 2.如果是重新执行的任务，则读取库中已有的记录，如果状态为失败，更新状态为已创建
     */
    public void init(Connection conn, Job job, JobRelation jobRelation, JobProgress lastJobProgress, int phase, boolean isContinue) throws SQLException {
        this.conn = conn;
        this.job = job;
        this.lastJobProgress=lastJobProgress;

        jobProgressOperator = new JobProgressOperator(conn);
        if (isContinue) {
            //重复执行的，读取库中记录
            jobProgress = jobProgressOperator.load(jobRelation.getItemId(), jobRelation.getItemType(), phase);
            if(jobProgress.getStatus().equals(JobProgressStatus.FAILURE)){
                this.updateStatus(JobProgressStatus.CREATED);
            }
        } else {
            //新增一条记录
            jobProgress = new JobProgress();
            jobProgress.setJobId(job.getJobId());
            jobProgress.setPhase(phase);
            jobProgressOperator.insert(jobProgress);
        }
    }

    /**
     * 执行入口
     * @return 0异步调用 1同步调用
     * @throws Exception
     */
    public abstract void run() throws Exception;

    /**
     * 更新步骤的执行状态
     * @param status
     * @throws SQLException
     */
    public void updateStatus(JobProgressStatus status) throws SQLException{
        jobProgressOperator.updateStatus(jobProgress.getPhaseId(),status);
    }

    public abstract void initInvokeType();

    public InvokeType getInvokeType(){
        return invokeType;
    }
}
