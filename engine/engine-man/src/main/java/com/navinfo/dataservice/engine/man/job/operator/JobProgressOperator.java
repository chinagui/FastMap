package com.navinfo.dataservice.engine.man.job.operator;

import com.navinfo.dataservice.engine.man.job.bean.JobProgress;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class JobProgressOperator {

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
     * 读取已有的记录
     *
     * @param jobId
     * @param phase
     * @return
     * @throws SQLException
     */
    public JobProgress getByJobId(long jobId, int phase) throws SQLException {
        String sql = "select jp.* from job_progress jp where jp.job_id=? and jr.phase=?";
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
    public void updateStatus(JobProgress jobProgress, JobProgressStatus status) throws SQLException {
        jobProgress.setStatus(status);

        QueryRunner run = new QueryRunner();
        String sql;
        if (jobProgress.getStatus().equals(JobProgressStatus.RUNNING)) {
            sql = "update job_progress set status=?,start_date=SYSDATE where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getPhaseId());
        } else if (jobProgress.getStatus().equals(JobProgressStatus.CREATED)) {
            sql = "update job_progress set status=?, end_date=NULL, start_date=NULL where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getPhaseId());
        } else {
            sql = "update job_progress set status=?, end_date=SYSDATE, message=? where phase_id=?";
            run.update(conn, sql, jobProgress.getStatus().value(), jobProgress.getMessage(), jobProgress.getPhaseId());
        }
    }

    public void updateStatus(long phaseId, JobProgressStatus status, String outParameter) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job_progress set status=?, end_date=SYSDATE, out_parameter=? where phase_id=?";
        run.update(conn, sql, status.value(), outParameter, phaseId);
    }
}
