package com.navinfo.dataservice.engine.man.job.operator;

import com.navinfo.dataservice.engine.man.job.bean.JobRelation;
import com.navinfo.navicommons.database.QueryRunner;

import java.sql.Connection;
import java.sql.SQLException;

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

}
