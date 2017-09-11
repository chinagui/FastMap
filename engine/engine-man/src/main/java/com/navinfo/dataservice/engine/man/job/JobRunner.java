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
        log.info("job init start");
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

        if(job != null){
            if(job.getStatus() == JobStatus.SUCCESS) {
                if (jobType == JobType.TiPS2MARK) {
                    throw new Exception("Tips转mark不能重复执行!");
                }

                if (jobType == JobType.DAY2MONTH && itemType == ItemType.PROJECT) {
                    throw new Exception("快线项目的日落月不能重复执行!");
                }
            }
        }

        if (isContinue) {
            if (job == null) {
                throw new Exception("未找到正在执行的任务，无法继续执行！");
            }
            jobRelation.setJobId(job.getJobId());
            jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.RUNNING);
        } else {
            if(jobType == JobType.DAY2MONTH && itemType == ItemType.LOT){
                //按批次日落月，清空所有相关job的latest
                jobOperator.clearLatestJobs();
            }else {
                if (job != null) {
                    jobOperator.clearLatestJob(job.getJobId());
                }
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
        log.info("job init end: jobId "+job.getJobId());
    }

    /**
     * 加载job信息
     */
    private void loadPhases() throws Exception {
        log.info("loadPhases start: jobId "+job.getJobId());
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
        log.info("loadPhases end: jobId "+job.getJobId());
    }

    /**
     * 依次执行所有步骤
     *
     * @throws Exception
     */
    public void runPhases() throws Exception {
        log.info("runPhases start: jobId "+job.getJobId());
        boolean finish = true;
        for (JobPhase phase : phaseList) {
            if (phase.jobProgress.getStatus() == JobProgressStatus.SUCCESS) {
                continue;
            } else if (phase.jobProgress.getStatus() == JobProgressStatus.RUNNING) {
                throw new JobRunningException();
            } else if (phase.jobProgress.getStatus() == JobProgressStatus.NODATA) {
            	job.setStatus(JobStatus.NODATA);
            	finish=false;
                //如果第一步的状态是无数据，不需要执行创建CMS任务
                break;
            }

            //如果按批次日落月，不需要执行关闸
            if(job.getType()==JobType.DAY2MONTH &&
                    jobRelation.getItemType()==ItemType.LOT &&
                    phase.jobProgress.getPhase()==2){
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
                log.error("runPhasesPublishMsgError:" + ExceptionUtils.getStackTrace(ex));
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
                job.getStatus() == JobStatus.SUCCESS||
                job.getStatus() == JobStatus.NODATA) {
            JobOperator jobOperator = new JobOperator(conn);
            jobOperator.updateStatusByJobId(job.getJobId(), job.getStatus());
        }
        log.info("runPhases end: jobId "+job.getJobId());
    }

    /**
     * 执行入口
     */
    public long run(long itemId, ItemType itemType, boolean isContinue, long operator, String parameter) throws Exception {
        try {
            log.info("run job start: itemId:"+itemId+",itemType:"+itemType+",isContinue:"+isContinue+",operator:"+operator+",parameter:"+parameter);
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

            log.info("run job end: jobId "+job.getJobId());
            return job.getJobId();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            DbUtils.rollbackAndCloseQuietly(conn);
            try {
                if (job != null) {
                    JobOperator jobOperator = new JobOperator(conn);
                    jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.FAILURE);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
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
            log.info("resume job start:"+job.getJobId());
            this.job = job;
            conn = DBConnector.getInstance().getManConnection();
            this.prepare();
            this.initJobType();
            this.loadPhases();
            conn.commit();

            runPhases();

            log.info("resume job end:"+job.getJobId());
            return job.getJobId();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            try {
                if (job != null) {
                    JobOperator jobOperator = new JobOperator(conn);
                    jobOperator.updateStatusByJobId(job.getJobId(), JobStatus.FAILURE);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                DbUtils.rollback(conn);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

}
