package com.navinfo.dataservice.engine.man.job.operator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;
import com.navinfo.dataservice.engine.man.job.bean.JobRelation;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Created by wangshishuai3966 on 2017/7/7.
 */
public class JobRelationOperator {
    private Connection conn;

    public JobRelationOperator(Connection conn) {
        this.conn = conn;
    }

    public void insert(JobRelation jobRelation) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "insert into job_relation values(?,?,?)";
        run.update(conn, sql, jobRelation.getJobId(), jobRelation.getItemId(), jobRelation.getItemType().value());
    }

    public JobRelation getByJobId(final long jobId) throws Exception {
        String sql = "select jr.* from job_relation jr where jr.job_id=?";
        ResultSetHandler<JobRelation> rsHandler = new ResultSetHandler<JobRelation>() {
            @Override
            public JobRelation handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    JobRelation jobRelation = new JobRelation();
                    jobRelation.load(rs);
                    return jobRelation;
                }
                return null;
            }
        };
        QueryRunner run = new QueryRunner();
        JobRelation jobRelation = run.query(conn, sql, rsHandler, jobId);
        if (jobRelation == null) {
            throw new Exception("未找到jobId" + jobId + "对应的job_relation");
        }
        return jobRelation;
    }
}
