package com.navinfo.dataservice.engine.man.job.operator;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.Job;
import com.navinfo.dataservice.engine.man.job.bean.JobStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class JobOperator {
    private Connection conn;

    public JobOperator(Connection conn) {
        this.conn = conn;
    }

    /**
     * 获取新的JOBID
     *
     * @return
     * @throws SQLException
     */
    public long getNextId() throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select job_seq.nextval from dual";
        return run.queryForLong(conn, sql);
    }

    /**
     * 创建一条job记录
     *
     * @param job
     * @throws SQLException
     */
    public void insert(Job job) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "insert into job values(?,?,?,?,SYSDATE,NULL,?,?)";
        run.update(conn, sql, job.getJobId(), job.getType(), job.getOperator(), job.getStatus().value(), job.getLastest(), job.getParameter().toJSONString());
    }

    /**
     * 获取最新执行的job
     *
     * @param itemId
     * @param itemType
     * @param jobType
     * @return
     * @throws SQLException
     */
    public Job getLatestJob(long itemId, ItemType itemType, final JobType jobType) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select j.* from job j,job_relation jr where j.job_id=jr.job_id and j.latest=1 and jr.item_id=? and jr.item_type=? and j.type=?";
        ResultSetHandler<Job> resultSetHandler = new ResultSetHandler<Job>() {
            @Override
            public Job handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    Job job = new Job();
                    job.setJobId(rs.getLong("job_id"));
                    job.setType(jobType);
                    job.setParameter(JSONObject.parseObject(rs.getString("parameter")));
                    return job;
                }
                return null;
            }
        };
        return run.query(conn, sql, resultSetHandler, itemId, itemType.value(), jobType.value());
    }

    /**
     * 更新job的执行状态
     *
     * @param jobId
     * @param status
     * @throws SQLException
     */
    public void updateStatus(long jobId, JobStatus status) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job set status=? and end_date=SYSDATE where job_id=?";
        run.update(conn, sql, status.value(), jobId);
    }

    public void updateLatest(long jobId, int latest) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job set latest=? where job_id=?";
        run.update(conn, sql, latest, jobId);
    }
}
