package com.navinfo.dataservice.engine.man.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.job.Day2Month.Day2MonthJobRunner;
import com.navinfo.dataservice.engine.man.job.Tips2Mark.Tips2MarkJobRunner;
import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.message.JobMessage;
import com.navinfo.dataservice.engine.man.job.operator.JobOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class JobService {
    private Logger log = LoggerRepos.getLogger(JobService.class);

    private JobService() {
    }

    public static JobService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 执行tips转mark
     *
     * @param itemId     目标对象ID
     * @param itemType   目标对象类型（项目、任务、子任务、批次）
     * @param operator   执行人
     * @param isContinue 是否继续
     * @return jobId
     * @throws Exception
     */
    public long tips2Mark(long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception {
        try {
            if(itemType == ItemType.LOT){
                throw new Exception("不支持的对象类型 "+itemType);
            }
            Tips2MarkJobRunner runner = new Tips2MarkJobRunner();
            return runner.run(itemId, itemType, isContinue, operator, parameter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("执行tips转mark失败，原因为:" + e.getMessage(), e);
        }
    }

    /**
     * 执行日落月
     *
     * @param itemId     目标对象ID
     * @param itemType   目标对象类型（项目、任务、子任务、批次）
     * @param operator   执行人
     * @param isContinue 是否继续
     * @return jobId
     * @throws Exception
     */
    public long day2month(long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception {
        try {
            if(itemType != ItemType.LOT && itemType != ItemType.PROJECT){
                throw new Exception("不支持的对象类型 "+itemType);
            }
            Day2MonthJobRunner runner = new Day2MonthJobRunner();
            return runner.run(itemId, itemType, isContinue, operator, parameter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("执行日落月失败，原因为:" + e.getMessage(), e);
        }
    }

    /**
     * 获取JOB每个步骤的执行状态
     *
     * @param itemId
     * @param itemType
     * @return
     * @throws Exception
     */
    public JSONArray getJobProgress(long itemId, ItemType itemType, JobType jobType) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            JobOperator jobOperator = new JobOperator(conn);
            return jobOperator.getJobProgressStatus(itemId, itemType, jobType);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("获取JOB状态失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 更新Job步骤的状态，并继续执行Job
     *
     * @param phaseId
     * @param status
     * @param outParameter
     * @throws Exception
     */
    public void updateJobProgress(long phaseId, JobProgressStatus status, String outParameter) throws Exception {
        Connection conn = null;
        try {
            log.info("updateJobProgress:phaseId:" + phaseId + ",status:" + status.value() + ",message:" + outParameter);
            conn = DBConnector.getInstance().getManConnection();
            JobProgressOperator jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(phaseId, status, outParameter);
            conn.commit();

            try {
                JobMessage jobMessage = jobProgressOperator.getJobMessage(phaseId);
                SysMsgPublisher.publishManJobMsg(JSON.toJSONString(jobMessage), jobMessage.getOperator());
            } catch (Exception ex) {
                log.error("public_msg_error:" + ExceptionUtils.getStackTrace(ex));
            }

            if (status == JobProgressStatus.FAILURE) {
                //步骤失败，更新job状态为失败，停止执行
                JobOperator jobOperator = new JobOperator(conn);
                jobOperator.updateStatusByPhaseId(phaseId, JobStatus.FAILURE);
            } else {
                //步骤成功，继续执行job
                JobOperator jobOperator = new JobOperator(conn);
                Job job = jobOperator.getByPhaseId(phaseId);
                if (job == null) {
                    throw new Exception("phaseId:" + phaseId + "对应的job不存在！");
                }
                JobRunner runner = null;
                switch (job.getType()) {
                    case TiPS2MARK:
                        runner = new Tips2MarkJobRunner();
                        break;
                    case DAY2MONTH:
                        runner = new Day2MonthJobRunner();
                        break;
                }

                if (runner == null) {
                    throw new Exception("不支持的任务类型：jobid " + job.getJobId() + ",type " + job.getType().value());
                }
                runner.resume(job);
            }
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("更新JOB步骤状态失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private static class SingletonHolder {
        private static final JobService INSTANCE = new JobService();
    }
}
