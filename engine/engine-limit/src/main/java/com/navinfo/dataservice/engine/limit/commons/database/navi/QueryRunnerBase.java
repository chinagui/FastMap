package com.navinfo.dataservice.engine.limit.commons.database.navi;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: liuqing Date: 2010-10-18 Time: 17:52:51
 */
public class QueryRunnerBase extends org.apache.commons.dbutils.QueryRunner {
	public QueryRunnerBase() {
		super();
	}

	public QueryRunnerBase(DataSource ds) {
		super(ds);
	}

	public <T> T query(Connection conn, String sql, int fetchSize,
			ResultSetHandler<T> rsh, Object... params) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		T result = null;
		try {
			stmt = this.prepareStatement(conn, sql);
			stmt.setFetchSize(fetchSize);
			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			result = rsh.handle(rs);
		} catch (SQLException e) {
			this.rethrow(e, sql, params);

		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
		return result;
	}

	public <T> T query(DataSource ds, String sql, int fetchSize,
			ResultSetHandler<T> rsh, Object... params) throws SQLException {
		Connection conn = ds.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		T result = null;
		try {
			stmt = this.prepareStatement(conn, sql);
			stmt.setFetchSize(fetchSize);
			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			result = rsh.handle(rs);
		} catch (SQLException e) {
			this.rethrow(e, sql, params);

		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return result;
	}

	public void execute(DataSource ds, String sql) throws SQLException {
		PreparedStatement stmt = null;
		Connection conn = ds.getConnection();
		try {
			stmt = this.prepareStatement(conn, sql);
			stmt.execute();
			conn.commit();
		} catch (SQLException e) {
			this.rethrow(e, sql);

		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}

	}

	/**
	 * 查询个数的时候用,如果查到的结果集为空,则返回-1
	 * 
	 * @param ds
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int queryForInt(DataSource ds, String sql, Object... params)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// 如果没查询到数据,则返回的是-1
		int num = -1;
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql);

			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
				num = rs.getInt(1);
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return num;
	}

	/**
	 * 查询个数的时候用,如果查到的结果集为空,则返回-1
	 * 
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int queryForInt(Connection conn, String sql, Object... params)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// 如果没查询到数据,则返回的是-1
		int num = -1;
		try {
			stmt = conn.prepareStatement(sql);

			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
				num = rs.getInt(1);
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
		return num;
	}

	/**
	 * 查询个数的时候用,如果查到的结果集为空,则返回-1
	 * 
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public long queryForLong(Connection conn, String sql, Object... params)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// 如果没查询到数据,则返回的是-1
		long num = -1L;
		try {
			stmt = conn.prepareStatement(sql);

			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
				num = rs.getLong(1);
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
		return num;
	}

	/**
	 * 查询单条某字段的时候用
	 * 
	 * 如果没查询到数据,则返回的是空字符串,如果查到多个,则取最后一条的记录
	 * 
	 * @param ds
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public String queryForString(DataSource ds, String sql, Object... params)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// 如果没查询到数据,则返回的是空字符串
		String value = "";
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql);

			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
				value = rs.getString(1);
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return value;
	}

	public String queryForString(Connection conn, String sql, Object... params)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// 如果没查询到数据,则返回的是空字符串
		String value = "";
		try {
			stmt = conn.prepareStatement(sql);
			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
				value = rs.getString(1);
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
		return value;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List queryForList(DataSource ds, String sql, Object... params)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List list = new ArrayList();
		Connection conn = ds.getConnection();
		try {
			stmt = conn.prepareStatement(sql);
			this.fillStatement(stmt, params);
			rs = this.wrap(stmt.executeQuery());
			while (rs.next()) {
			    list.add( rs.getObject(1));
			}
		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return list;
	}


	public void execute(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = this.prepareStatement(conn, sql);
			stmt.execute();
			// conn.commit();
		} catch (SQLException e) {
			this.rethrow(e, sql);

		} finally {
			DbUtils.closeQuietly(stmt);
		}

	}

	public int update(DataSource ds, String sql, Object... params)
			throws SQLException {

		PreparedStatement stmt = null;
		int rows = 0;
		Connection conn = ds.getConnection();
		try {
			stmt = this.prepareStatement(conn, sql);
			this.fillStatement(stmt, params);
			rows = stmt.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			this.rethrow(e, sql, params);

		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}

		return rows;
	}

	public final static String TOTAL_RECORD_NUM = "TOTAL_RECORD_NUM_";

	/**
	 * <pre>
	 * 分页查询：
	 * 传统分页查询需要查两次才能完成一次分页查询。
	 * 在此，采用 WITH 语法重用SQL 改进了分页查询，查一次即可，这样可免去查两次带来的性能损失。
	 * 记录总数以列（列名为TOTAL_RECORD_NUM_）的形式添加到返回的结果集中
	 * </pre>
	 * 
	 * @param <T>
	 * @param pageNum
	 *            起始页号为1
	 * @param pageSize
	 * @param conn
	 * @param sql
	 * @param rsh
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(long pageNum, long pageSize, Connection conn,
			String sql, ResultSetHandler<T> rsh, Object... params)
			throws SQLException {
		if (pageNum == 0) {
			pageNum = 1;
		}
		if (pageSize == 0) {
			pageSize = 20;
		}
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;
		String pageSql = "WITH query AS "
				+ //
				" (" + sql
				+ ") "
				+ //
				"SELECT /*+FIRST_ROWS ORDERED*/ t.*,( SELECT COUNT(1) FROM query) AS "
				+ TOTAL_RECORD_NUM
				+ //
				"  FROM (SELECT t.*, rownum AS rownum_ FROM query t WHERE rownum <= ?) t "
				+ //
				" WHERE t.rownum_ >= ? ";
		Object[] newParams = new Object[params.length + 2];
		System.arraycopy(params, 0, newParams, 0, params.length);
		newParams[params.length] = pageEndNum;
		newParams[params.length + 1] = pageStartNum;
		return super.query(conn, pageSql, rsh, newParams);
	}

	public Page query(Page page, Connection conn, String sql,
			RowMapper<?> rowMapper, Object... params) throws SQLException {
		int pageNum = page.thePageNum();
		int pageSize = page.getPageSize();
		if (pageNum == 0) {
			page.setPageNum(pageNum = 1);
		}
		if (pageSize == 0) {
			page.setPageNum(pageSize = 20);
		}
		long pageStartNum = (pageNum - 1) * pageSize + 1;
		long pageEndNum = pageNum * pageSize;
		String pageSql = "WITH query AS "
				+ //
				" (" + sql
				+ ") "
				+ //
				"SELECT /*+FIRST_ROWS ORDERED*/ t.*,( SELECT COUNT(1) FROM query) AS "
				+ TOTAL_RECORD_NUM
				+ //
				"  FROM (SELECT t.*, rownum AS rownum_ FROM query t WHERE rownum <= ?) t "
				+ //
				" WHERE t.rownum_ >= ? ";
		Object[] newParams = new Object[params.length + 2];
		System.arraycopy(params, 0, newParams, 0, params.length);
		newParams[params.length] = pageEndNum;
		newParams[params.length + 1] = pageStartNum;
		return super.query(conn, pageSql, toResultSetHandler(rowMapper, page),
				newParams);
	}

	public ResultSetHandler<Page> toResultSetHandler(
			final RowMapper<?> rowMapper, final Page page) {
		return new ResultSetHandler<Page>() {

			public Page handle(ResultSet rs) throws SQLException {
				List<Object> list = new ArrayList<Object>();
				int totalCount = 0;
				for (int i = 0; rs.next(); i++) {
					Object mapRow = rowMapper.mapRow(rs, i);
					list.add(mapRow);
					totalCount = rs.getInt(TOTAL_RECORD_NUM);
				}
				page.setTotalCount(totalCount);
				page.setResult(list);
				return page;
			}
		};
	}

	public <T> T query(long pageNum, long pageSize, DataSource ds, String sql,
			ResultSetHandler<T> rsh, Object... params) throws Exception {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return query(pageNum, pageSize, conn, sql, rsh, params);
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
