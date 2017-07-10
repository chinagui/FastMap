package com.navinfo.dataservice.engine.man.job.operator;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
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
        run.update(conn, sql, jobProgress.getPhaseId(), jobProgress.getJobId(), jobProgress.getPhase(), jobProgress.getStatus().value(), jobProgress.getMessage(), jobProgress.getInParameter().toJSONString());
    }

    /**
     * 读取已有的记录
     *
     * @param itemId
     * @param itemType
     * @param phase
     * @return
     * @throws SQLException
     */
    public JobProgress load(long itemId, ItemType itemType, int phase) throws SQLException {
        String sql = "select * from job_relation jr,job_progress jp,job j where jr.job_id=jp.job_id and j.latest=1 and jr.item_id=? and jr.item_type=? and jr.phase=?";
        ResultSetHandler<JobProgress> rsHandler = new ResultSetHandler<JobProgress>() {
            @Override
            public JobProgress handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    JobProgress jobProgress = new JobProgress();
                    jobProgress.setPhaseId(rs.getLong("phase_id"));
                    jobProgress.setJobId(rs.getInt("phase"));
                    jobProgress.setPhase(rs.getInt("phase"));
                    jobProgress.setStatus(JobProgressStatus.valueOf(rs.getInt("status")));
                    jobProgress.setMessage(rs.getString("message"));
                    jobProgress.setInParameter(JSONObject.parseObject(rs.getString("in_parameter")));
                    jobProgress.setOutParameter(JSONObject.parseObject(rs.getString("out_parameter")));
                    return jobProgress;
                }
                return null;
            }
        };
        QueryRunner run = new QueryRunner();
        return run.query(conn, sql, rsHandler, itemId, itemType.value(), phase);
    }

    /**
     * 更新步骤的执行状态
     * 1.如果成功或失败，则更新end_date
     * 2.如果开始执行，则更新start_date
     * 3.如果创建，则清空end_date,start_date
     *
     * @param phaseId
     * @param status
     * @throws SQLException
     */
    public void updateStatus(long phaseId, JobProgressStatus status) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql;
        if (status.equals(JobProgressStatus.RUNNING)) {
            sql = "update job_progress set status=? and start_date=SYSDATE where phase_id=?";

        } else if (status.equals(JobProgressStatus.CREATED)) {
            sql = "update job_progress set status=? and end_date=NULL and start_date=NULL where phase_id=?";
        } else {
            sql = "update job_progress set status=? and end_date=SYSDATE where phase_id=?";
        }
        run.update(conn, sql, status.value(), phaseId);
    }
}
