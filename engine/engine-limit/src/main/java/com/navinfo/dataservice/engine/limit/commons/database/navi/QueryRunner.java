package com.navinfo.dataservice.engine.limit.commons.database.navi;

import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * User: liuqing Date: 2010-10-18 Time: 17:52:51
 */
public class QueryRunner extends QueryRunnerBase {
    
    public static QueryRunnerBase TEST_MOCK_QUERY_RUNNER = null;
    
    private QueryRunnerBase runner = null;
    
    public QueryRunner() {
        runner = new QueryRunnerBase();
        if(TEST_MOCK_QUERY_RUNNER != null)
        {
            runner = TEST_MOCK_QUERY_RUNNER;
        }
    }

    public QueryRunner(DataSource ds) {
        runner = new QueryRunnerBase(ds);
        
        if(TEST_MOCK_QUERY_RUNNER != null)
        {
            runner = TEST_MOCK_QUERY_RUNNER;
        }
    }

    @Override
    public <T> T query(Connection conn, String sql, int fetchSize, ResultSetHandler<T> rsh, Object... params)
        throws SQLException {
        
        return runner.query(conn, sql, fetchSize, rsh, params);
    }

    @Override
    public <T> T query(DataSource ds, String sql, int fetchSize, ResultSetHandler<T> rsh, Object... params)
        throws SQLException {
        
        return runner.query(ds, sql, fetchSize, rsh, params);
    }

    @Override
    public void execute(DataSource ds, String sql)
        throws SQLException {
        
        runner.execute(ds, sql);
    }

    @Override
    public int queryForInt(DataSource ds, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForInt(ds, sql, params);
    }
    @Override
    public int queryForInt(Connection conn, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForInt(conn, sql, params);
    }
    @Override
    public long queryForLong(Connection conn, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForLong(conn, sql, params);
    }

    @Override
    public String queryForString(DataSource ds, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForString(ds, sql, params);
    }

    @Override
    public String queryForString(Connection conn, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForString(conn, sql, params);
    }

    @Override
    public List queryForList(DataSource ds, String sql, Object... params)
        throws SQLException {
        
        return runner.queryForList(ds, sql, params);
    }

    @Override
    public void execute(Connection conn, String sql)
        throws SQLException {
        
        runner.execute(conn, sql);
    }

    @Override
    public int update(DataSource ds, String sql, Object... params)
        throws SQLException {
        
        return runner.update(ds, sql, params);
    }

    @Override
    public <T> T query(long pageNum, long pageSize, Connection conn, String sql, ResultSetHandler<T> rsh,
        Object... params)
        throws SQLException {
        
        return runner.query(pageNum, pageSize, conn, sql, rsh, params);
    }

    @Override
    public Page query(Page page, Connection conn, String sql, RowMapper<?> rowMapper, Object... params)
        throws SQLException {
        
        return runner.query(page, conn, sql, rowMapper, params);
    }

    @Override
    public ResultSetHandler<Page> toResultSetHandler(RowMapper<?> rowMapper, Page page) {
        
        return runner.toResultSetHandler(rowMapper, page);
    }

    @Override
    public <T> T query(long pageNum, long pageSize, DataSource ds, String sql, ResultSetHandler<T> rsh,
        Object... params)
        throws Exception {
        
        return runner.query(pageNum, pageSize, ds, sql, rsh, params);
    }

    @Override
    public int[] batch(Connection conn, String sql, Object[][] params)
        throws SQLException {
        
        return runner.batch(conn, sql, params);
    }

    @Override
    public int[] batch(String sql, Object[][] params)
        throws SQLException {
        
        return runner.batch(sql, params);
    }

    @Override
    public void fillStatement(PreparedStatement stmt, Object... params)
        throws SQLException {
        
        runner.fillStatement(stmt, params);
    }

    @Override
    public void fillStatementWithBean(PreparedStatement stmt, Object bean, PropertyDescriptor[] properties)
        throws SQLException {
        
        runner.fillStatementWithBean(stmt, bean, properties);
    }

    @Override
    public void fillStatementWithBean(PreparedStatement stmt, Object bean, String... propertyNames)
        throws SQLException {
        
        runner.fillStatementWithBean(stmt, bean, propertyNames);
    }

    @Override
    public DataSource getDataSource() {
        
        return runner.getDataSource();
    }


    @Override
    public <T> T query(Connection conn, String sql, Object param, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(conn, sql, param, rsh);
    }

    @Override
    public <T> T query(Connection conn, String sql, Object[] params, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(conn, sql, params, rsh);
    }

    @Override
    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params)
        throws SQLException {
        
        return runner.query(conn, sql, rsh, params);
    }

    @Override
    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(conn, sql, rsh);
    }

    @Override
    public <T> T query(String sql, Object param, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(sql, param, rsh);
    }

    @Override
    public <T> T query(String sql, Object[] params, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(sql, params, rsh);
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
        throws SQLException {
        
        return runner.query(sql, rsh, params);
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh)
        throws SQLException {
        
        return runner.query(sql, rsh);
    }


    @Override
    public int update(Connection conn, String sql)
        throws SQLException {
        
        return runner.update(conn, sql);
    }

    @Override
    public int update(Connection conn, String sql, Object param)
        throws SQLException {
        
        return runner.update(conn, sql, param);
    }

    @Override
    public int update(Connection conn, String sql, Object... params)
        throws SQLException {
        
        return runner.update(conn, sql, params);
    }

    @Override
    public int update(String sql)
        throws SQLException {
        
        return runner.update(sql);
    }

    @Override
    public int update(String sql, Object param)
        throws SQLException {
        
        return runner.update(sql, param);
    }

    @Override
    public int update(String sql, Object... params)
        throws SQLException {
        
        return runner.update(sql, params);
    }

    
}
