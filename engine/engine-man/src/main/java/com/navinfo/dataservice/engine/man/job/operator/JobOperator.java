package com.navinfo.dataservice.engine.man.job.operator;

import com.alibaba.fastjson.JSONArray;
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
        String sql = "insert into job values(?,?,?,?,?,SYSDATE,NULL,?)";
        run.update(conn, sql, job.getJobId(), job.getType().value(), job.getStatus().value(), job.getLastest(), job.getOperator(), job.getParameter());
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
                    job.load(rs);
                    return job;
                }
                return null;
            }
        };
        return run.query(conn, sql, resultSetHandler, itemId, itemType.value(), jobType.value());
    }

    public Job getByJobId(long jobId) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select j.* from job j where j.job_id=?";
        ResultSetHandler<Job> resultSetHandler = new ResultSetHandler<Job>() {
            @Override
            public Job handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    Job job = new Job();
                    job.load(rs);
                    return job;
                }
                return null;
            }
        };
        return run.query(conn, sql, resultSetHandler, jobId);
    }

    public Job getByPhaseId(long phaseId) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select j.* from job j,job_progress jp where j.job_id=jp.job_id and jp.phase_id=?";
        ResultSetHandler<Job> resultSetHandler = new ResultSetHandler<Job>() {
            @Override
            public Job handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    Job job = new Job();
                    job.load(rs);
                    return job;
                }
                return null;
            }
        };
        return run.query(conn, sql, resultSetHandler, phaseId);
    }

    /**
     * 更新job的执行状态
     *
     * @param jobId
     * @param status
     * @throws SQLException
     */
    public void updateStatusByJobId(long jobId, JobStatus status) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job set status=?, end_date=SYSDATE where job_id=?";
        run.update(conn, sql, status.value(), jobId);
    }

    /**
     * 更新job的执行状态
     *
     * @param phaseId
     * @param status
     * @throws SQLException
     */
    public void updateStatusByPhaseId(long phaseId, JobStatus status) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job j set j.status=? where exists(select null from job_progress jp where jp.phase_id=? and jp.job_id=j.job_id)";
        run.update(conn, sql, status.value(), phaseId);
    }

    public void updateLatest(long jobId, int latest) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "update job set latest=? where job_id=?";
        run.update(conn, sql, latest, jobId);
    }

    /**
     * 根据jobId查询每个步骤的执行状态
     *
     * @param jobId
     * @return
     * @throws SQLException
     */
    public JSONArray getJobProgressStatus(long jobId) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "select phase_id,phase,status,message from job_progress where job_id=? order by phase asc";

        ResultSetHandler<JSONArray> resultSetHandler = new ResultSetHandler<JSONArray>() {
            @Override
            public JSONArray handle(ResultSet rs) throws SQLException {
                JSONArray array = new JSONArray();
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("phaseId", rs.getLong("phase_id"));
                    json.put("status", rs.getInt("status"));
                    json.put("phase", rs.getInt("phase"));
                    json.put("message", rs.getString("message"));
                    array.add(json);
                }
                return array;
            }
        };
        return run.query(conn, sql, resultSetHandler, jobId);
    }
}
