package com.navinfo.dataservice.engine.man.job.operator;

import com.alibaba.fastjson.JSON;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.job.bean.JobProgress;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobStatus;
import com.navinfo.dataservice.engine.man.job.message.JobMessage;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class JobProgressOperator {
	private Logger log = LoggerRepos.getLogger(JobProgressOperator.class);

    private Connection conn;

    public JobProgressOperator(Connection conn) {
        this.conn = conn;
    }

    /**
     * 获取新的job_progress的id
     *
     * @return
     * @throws SQLException
     */
    public long getNextId() throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select job_progress_seq.nextval from dual";
        return run.queryForLong(conn, sql);
    }

    /**
     * 新增记录到数据库
     *
     * @param jobProgress
     * @throws SQLException
     */
    public void insert(JobProgress jobProgress) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "insert into job_progress values(?,?,?,?,SYSDATE,NULL,NULL,?,?,NULL)";
        run.update(conn, sql, jobProgress.getPhaseId(), jobProgress.getJobId(), jobProgress.getPhase(), jobProgress.getStatus().value(), jobProgress.getMessage(), jobProgress.getInParameter());
    }
    
    /**
     * 每个阶段执行后，发送消息
     * @param phaseId
     */
    public void pushMsg(long phaseId) {
		try {
            JobMessage jobMessage = getJobMessage(phaseId);
            String message = JSON.toJSONString(jobMessage);
            log.info("publishManJobMsg:"+message);
            SysMsgPublisher.publishManJobMsg(message, jobMessage.getOperator());
        } catch (Exception ex) {
            log.error("publishManJobMsg error:" + ExceptionUtils.getStackTrace(ex));
        }
	}

    /**
     * 读取已有的记录
     *
     * @param jobId
     * @param phase
     * @return
     * @throws SQLException
     */
    public JobProgress getByJobId(long jobId, int phase) throws SQLException {
        String sql = "select jp.* from job_progress jp where jp.job_id=? and jp.phase=?";
        ResultSetHandler<JobProgress> rsHandler = new ResultSetHandler<JobProgress>() {
            @Override
            public JobProgress handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    JobProgress jobProgress = new JobProgress();
                    jobProgress.load(rs);
                    return jobProgress;
                }
                return null;
            }
        };
        QueryRunner run = new QueryRunner();
        return run.query(conn, sql, rsHandler, jobId, phase);
    }

    /**
     * 更新步骤的执行状态
     * 1.如果成功或失败，则更新end_date
     * 2.如果开始执行，则更新start_date
     * 3.如果创建，则清空end_date,start_date
     *
     * @throws SQLException
     */
    public void updateStatus(JobProgress jobProgress) throws SQLException {
        //jobProgress.setStatus(status);

        QueryRunner run = new QueryRunner();
        String sql;
        if (jobProgress.getStatus().equals(JobProgressStatus.RUNNING)) {
            sql = "update job_progress set status=?,start_date=SYSDATE,message=?,in_parameter=? where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getMessage(), jobProgress.getInParameter(),jobProgress.getPhaseId());
        } else if (jobProgress.getStatus().equals(JobProgressStatus.CREATED)) {
            sql = "update job_progress set status=?, end_date=NULL, start_date=NULL where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getPhaseId());
        } else {
            sql = "update job_progress set status=?, end_date=SYSDATE, message=?,in_parameter=?, OUT_PARAMETER=? where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getMessage(), jobProgress.getInParameter(),jobProgress.getOutParameter(), jobProgress.getPhaseId());
        }
    }

    public void updateStatus(long phaseId, JobProgressStatus status, String outParameter) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job_progress set status=?, end_date=SYSDATE, out_parameter=? where phase_id=?";
        run.update(conn, sql, status.value(), outParameter, phaseId);
    }

    public JobMessage getJobMessage(long phaseId) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "SELECT J.OPERATOR,J.TYPE,JP.PHASE,JP.STATUS,JR.ITEM_ID,JR.ITEM_TYPE,(SELECT COUNT(1) FROM JOB_PROGRESS JP2 WHERE JP2.JOB_ID=JP.JOB_ID ) TOTAL FROM JOB_PROGRESS JP, JOB_RELATION JR, JOB J WHERE J.JOB_ID=JP.JOB_ID AND JP.JOB_ID=JR.JOB_ID AND JP.PHASE_ID=?";

        ResultSetHandler<JobMessage> rsHandler = new ResultSetHandler<JobMessage>() {
            @Override
            public JobMessage handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    JobMessage jobMessage = new JobMessage();
                    int phase = rs.getInt("phase");
                    int status = rs.getInt("status");
                    long itemId = rs.getLong("item_id");
                    int itemType = rs.getInt("item_type");
                    int total = rs.getInt("total");
                    long operator = rs.getLong("operator");
                    int jobType = rs.getInt("type");

                    jobMessage.setItemId(itemId);
                    jobMessage.setItemType(itemType);
                    jobMessage.setPhase(phase);
                    jobMessage.setStatus(status);
                    jobMessage.setOperator(operator);
                    jobMessage.setJobStatus(JobStatus.RUNNING.value());
                    jobMessage.setJobType(jobType);

                    if (status == JobProgressStatus.FAILURE.value()) {
                        jobMessage.setJobStatus(JobStatus.FAILURE.value());
                    } else if (status == JobProgressStatus.NODATA.value()) {
                        jobMessage.setJobStatus(JobStatus.SUCCESS.value());
                    } else if (status == JobProgressStatus.SUCCESS.value()) {
                        if (total == phase) {
                            jobMessage.setJobStatus(JobStatus.SUCCESS.value());
                        }
                    }

                    return jobMessage;
                }
                return null;
            }
        };
        return run.query(conn, sql, rsHandler, phaseId);
    }
}
