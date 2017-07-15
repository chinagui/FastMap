package com.navinfo.dataservice.engine.man.job;

import com.alibaba.fastjson.JSON;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.exception.JobRunningException;
import com.navinfo.dataservice.engine.man.job.message.JobMessage;
import com.navinfo.dataservice.engine.man.job.operator.JobOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobRelationOperator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public abstract class JobRunner {
    private static Logger log = LoggerRepos.getLogger(JobRunner.class);
    /**
     * 在构造函数中传入已知的参数
     */
    public long itemId;
    public ItemType itemType;
    public List<JobPhase> phaseList = new ArrayList<>();
    public long operator;
    public JobType jobType;
    public Job job;
    public String parameter;
    public JobRelation jobRelation;
    public Connection conn;

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
    private void init(boolean isContinue) throws Exception {

        JobOperator jobOperator = new JobOperator(conn);
        JobRelationOperator jobRelationOperator = new JobRelationOperator(conn);
        jobRelation = new JobRelation();
        jobRelation.setItemId(itemId);
        jobRelation.setItemType(itemType);

        job = jobOperator.getLatestJob(itemId, itemType, jobType);

        //如果有正在执行的job，则停止执行，抛出异常
        if (job != null && job.getStatus() == JobStatus.RUNNING) {
            throw new JobRunningException();
        }

        if(job != null && job.getStatus() == JobStatus.SUCCESS && jobType == JobType.TiPS2MARK){
           throw new Exception("Tips转mark不能重复执行!");
        }

        if (isContinue) {
            if (job == null) {
                throw new Exception("未找到正在执行的任务，无法继续执行！");
            }
            jobRelation.setJobId(job.getJobId());
            jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.RUNNING);
        } else {
            if (job != null) {
                jobOperator.updateLatest(job.getJobId(), 0);
            }
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
            JobProgress lastProgress = null;
            if (lastJobPhase != null) {
                lastProgress = lastJobPhase.jobProgress;
            }
            phase.init(conn, job, jobRelation, lastProgress, index++, isContinue);
            lastJobPhase = phase;
        }
    }

    /**
     * 加载job信息
     */
    private void loadPhases() throws Exception {

        int index = 1;
        JobPhase lastJobPhase = null;

        JobRelationOperator jobRelationOperator = new JobRelationOperator(conn);
        jobRelation = jobRelationOperator.getByJobId(job.getJobId());

        for (JobPhase phase : phaseList) {
            JobProgress lastProgress = null;
            if (lastJobPhase != null) {
                lastProgress = lastJobPhase.jobProgress;
            }
            phase.init(conn, job, jobRelation, lastProgress, index++, true);
            lastJobPhase = phase;
        }
    }

    /**
     * 依次执行所有步骤
     *
     * @throws Exception
     */
    public void runPhases() throws Exception {
        boolean finish = true;
        for (JobPhase phase : phaseList) {
            if (phase.jobProgress.getStatus() == JobProgressStatus.SUCCESS) {
                continue;
            } else if (phase.jobProgress.getStatus() == JobProgressStatus.RUNNING) {
                throw new JobRunningException();
            } else if (phase.jobProgress.getStatus() == JobProgressStatus.NODATA) {
                break;
            }

            JobProgressStatus status = phase.run();

            //发送步骤状态消息
            try {
                JobProgressOperator jobProgressOperator = new JobProgressOperator(conn);
                JobMessage jobMessage = jobProgressOperator.getJobMessage(phase.jobProgress.getPhaseId());
                if (status != JobProgressStatus.RUNNING && status != JobProgressStatus.CREATED) {
                    SysMsgPublisher.publishManJobMsg(JSON.toJSONString(jobMessage), jobMessage.getOperator());
                }
            } catch (Exception ex) {
                log.error("public_msg_error:" + ExceptionUtils.getStackTrace(ex));
            }

            if (status == JobProgressStatus.FAILURE) {
                job.setStatus(JobStatus.FAILURE);
                finish = false;
                break;
            }

            if (phase.getInvokeType() == InvokeType.ASYNC) {
                //如果调用异步方法，停止执行
                finish = false;
                break;
            }
        }

        if (finish) {
            job.setStatus(JobStatus.SUCCESS);
        }

        if (job.getStatus() == JobStatus.FAILURE ||
                job.getStatus() == JobStatus.SUCCESS) {
            JobOperator jobOperator = new JobOperator(conn);
            jobOperator.updateStatusByJobId(job.getJobId(), job.getStatus());
        }
    }

    /**
     * 执行入口
     */
    public long run(long itemId, ItemType itemType, boolean isContinue, long operator, String parameter) throws Exception {
        try {
            conn = DBConnector.getInstance().getManConnection();
            this.itemId = itemId;
            this.itemType = itemType;
            this.operator = operator;
            this.parameter = parameter;

            this.prepare();
            this.initJobType();
            this.init(isContinue);
            conn.commit();

            runPhases();

            return job.getJobId();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            DbUtils.rollbackAndCloseQuietly(conn);
            try {
                if (job != null) {
                    JobOperator jobOperator = new JobOperator(conn);
                    jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.FAILURE);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
                DbUtils.rollbackAndCloseQuietly(conn);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 继续执行入口
     */
    public long resume(Job job) throws Exception {
        try {
            this.job = job;
            conn = DBConnector.getInstance().getManConnection();
            this.prepare();
            this.initJobType();
            this.loadPhases();
            conn.commit();

            runPhases();

            return job.getJobId();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            DbUtils.rollback(conn);
            try {
                if (job != null) {
                    JobOperator jobOperator = new JobOperator(conn);
                    jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.FAILURE);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
                DbUtils.rollback(conn);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

}
