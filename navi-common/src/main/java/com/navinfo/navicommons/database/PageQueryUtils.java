package com.navinfo.navicommons.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.navinfo.navicommons.exception.DAOException;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-3-15 Time: 下午1:39
 */
public class PageQueryUtils {
	private static final transient Logger log = Logger
			.getLogger(PageQueryUtils.class);

	/**
	 * 为给定的sql加上分页的sql支持
	 * 
	 * @param sql
	 * @return
	 */
	public static String decorateOraclePageSql(String sql) {
		String pageSql = "select * from (select a.*, rownum r from (" + sql
				+ ") a where rownum <= ?) b where r > ?";
		// log.debug(pageSql);
		return pageSql;
	}

	/**
	 * 将查询内容的sql转换成查询总数的sql
	 * 
	 * @param sql
	 * @return
	 */
	public static String decorateOracleCountSql(String sql) {
		// String regEx = "(\\s+.+\\s+P) ";
//		String regEx = "select[^from]+from";
		//上面的正则匹配有bug，修改为现在的select((?!from)[\\s\\S])+from
		String regEx = "select((?!from)[\\s\\S])+from";
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			sql = matcher.replaceFirst("select count(1) from");
		} else {
			sql = "select count(1) from (" + sql + ")";
		}
		// log.debug(sql);
		return sql;
	}

	public static String decorateOracleCountSqlForComplexSql(String sql) {
			sql = "select count(1) from (" + sql + ")";
		return sql;
	}
	
	public static Page pageQuery(int pageNum,
			PageQueryUtils.ExecuteArgs executeArgs, JdbcTemplate jdbcTemplate) {
		return pageQuery(pageNum, executeArgs, jdbcTemplate,
				new ColumnMapRowMapper());

	}

	public static Page pageQuery(int pageNum,
			PageQueryUtils.ExecuteArgs executeArgs, JdbcTemplate jdbcTemplate,
			RowMapper rowMapper) {
		Page page = new Page(pageNum);

		executeArgs.addPagerArgs(page);
		List result = jdbcTemplate.query(
				PageQueryUtils.decorateOraclePageSql(executeArgs.getSql()),
				executeArgs.getArgs(), executeArgs.getArgsType(), rowMapper);
		executeArgs.removePagerArgs();
		int count = jdbcTemplate.queryForObject(PageQueryUtils
				.decorateOracleCountSql(executeArgs.getSql()),executeArgs.getArgs(), executeArgs.getArgsType(),Integer.class);
		page.setResult(result);
		page.setTotalCount(count);
		return page;
	}

	/**
	 * 用于复杂SQL的分页查询，防止正则匹配出错
	 * 
	 * @param pageNum
	 * @param executeArgs
	 * @param jdbcTemplate
	 * @param rowMapper
	 * @return
	 */
	public static Page pageQueryForComplexSql(int pageNum,
			PageQueryUtils.ExecuteArgs executeArgs, DataSource ds,
			ResultSetHandler resultSetHandler) {
		Page page = new Page(pageNum);
		Connection conn = null;
		try {
			conn = ds.getConnection();
			executeArgs.addPagerArgs(page);
			QueryRunner runner = new QueryRunner();
			Object result = runner.query(conn,
					PageQueryUtils.decorateOraclePageSql(executeArgs.getSql()),
					resultSetHandler, executeArgs.getArgs());
			executeArgs.removePagerArgs();
			int count = runner
					.query(conn, PageQueryUtils
							.decorateOracleCountSqlForComplexSql(executeArgs.getSql()),
							new ResultSetHandler<Integer>() {
								public Integer handle(ResultSet rs)
										throws SQLException {
									if (rs.next()) {
										return rs.getInt(1);
									}
									return null;
								}
							}, executeArgs.getArgs());

			page.setResult(result);
			page.setTotalCount(count);
		} catch (SQLException e) {
			throw new DAOException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return page;
	}
	
	
	public static Page pageQuery(int pageNum,
			PageQueryUtils.ExecuteArgs executeArgs, DataSource ds,
			ResultSetHandler resultSetHandler) {
		Page page = new Page(pageNum);
		
		return pageQuery(page,executeArgs,ds,resultSetHandler);
	}
	
	public static Page pageQuery(int pageNum,int pageSize,
			PageQueryUtils.ExecuteArgs executeArgs, DataSource ds,
			ResultSetHandler resultSetHandler) {
		Page page = new Page();
		page.setPageNum(pageNum);
		page.setPageSize(pageSize);
		
		return pageQuery(page,executeArgs,ds,resultSetHandler);
	}


	public static Page pageQuery(Page page,
			PageQueryUtils.ExecuteArgs executeArgs, DataSource ds,
			ResultSetHandler resultSetHandler) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			executeArgs.addPagerArgs(page);
			QueryRunner runner = new QueryRunner();
			Object result = runner.query(conn,
					PageQueryUtils.decorateOraclePageSql(executeArgs.getSql()),
					resultSetHandler, executeArgs.getArgs());
			executeArgs.removePagerArgs();
			int count = runner
					.query(conn, PageQueryUtils
							.decorateOracleCountSql(executeArgs.getSql()),
							new ResultSetHandler<Integer>() {
								public Integer handle(ResultSet rs)
										throws SQLException {
									if (rs.next()) {
										return rs.getInt(1);
									}
									return  0;
								}
							}, executeArgs.getArgs());

			page.setResult(result);
			page.setTotalCount(count);
		} catch (SQLException e) {
			throw new DAOException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return page;
	}

	public static List queryForList(PageQueryUtils.ExecuteArgs executeArgs,
			DataSource ds, ResultSetHandler resultSetHandler) {
		Connection conn = null;
		List result = null;
		try {
			conn = ds.getConnection();

			QueryRunner runner = new QueryRunner();
			result = (List) runner.query(conn, executeArgs.getSql(),
					resultSetHandler, executeArgs.getArgs());
		} catch (SQLException e) {
			throw new DAOException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return result;
	}

	public static ExecuteArgs constructSearchSql(String sql,
			SearchParam... searchParams) {
		return constructSearchSql(sql, null, searchParams);
	}

	/**
	 * 给定查询参数，构造查询sql
	 * 
	 * @param sql
	 * @return
	 */
	public static ExecuteArgs constructSearchSql(String sql, String orderBy,
			SearchParam... searchParams) {
		if (orderBy == null)
			orderBy = "";
		ExecuteArgs executeArgs = new ExecuteArgs(sql);
		if (searchParams == null)
			return new ExecuteArgs(sql + " " + orderBy);
		for (int i = 0; i < searchParams.length; i++) {
			SearchParam searchParam = searchParams[i];
			Object param = searchParam.getParam();
			if (param != null) {
				String searchSql = searchParam.getSql();
				int paramType = searchParam.getParamType();
				if (sql.toLowerCase().indexOf("where") > -1) {
					sql += " and ";
				} else {
					sql += " where ";
				}
				if (paramType == Types.STRUCT) {
					String[] rounds = (String[]) param;
					searchSql = discardUnUsefulChar(searchSql);
					sql += " sdo_filter(" + searchParam.getSql()
							+ ", sdo_geometry(2003,8307,null,"
							+ " mdsys.sdo_elem_info_array(1,1003,3),"
							+ " mdsys.sdo_ordinate_array(" + rounds[0] + ","
							+ rounds[1] + "," + rounds[2] + "," + rounds[3]
							+ ")" + " ))='TRUE'";
				} else {
					sql += searchSql;
					executeArgs.addArgs(param);
					executeArgs.addArgsType(paramType);

				}

			}

		}
		executeArgs.setSql(sql + " " + orderBy);
		return executeArgs;
	}

	private static String discardUnUsefulChar(String searchSql) {
		if (searchSql.indexOf("=") > -1) {
			searchSql = searchSql.substring(0, searchSql.indexOf("="));
		}
		return searchSql;
	}

	public static void main(String[] args) {
		System.out
				.println(PageQueryUtils
						.decorateOracleCountSql("select p.* from test where id in (1select * from aa)"));
		System.out
				.println(PageQueryUtils.decorateOracleCountSql("select p.* "));
		System.out.println(PageQueryUtils.discardUnUsefulChar("asdf.ww=?"));

	}

	public static class ExecuteArgs {

		String sql;
		List args = new ArrayList();
		List<Integer> argsType = new ArrayList<Integer>();

		public ExecuteArgs(String sql) {
			this.sql = sql;
		}

		public void addArgs(Object value) {
			args.add(value);
		}

		public void addArgsType(Integer value) {
			argsType.add(value);
		}

		public Object[] getArgs() {
			return args.toArray();
		}

		public int[] getArgsType() {
			int[] types = new int[argsType.size()];
			for (int i = 0; i < argsType.size(); i++) {
				int type = argsType.get(i);
				types[i] = type;
			}
			return types;
		}

		public void removePagerArgs() {
			args.remove(args.size() - 1);
			args.remove(args.size() - 1);
			argsType.remove(argsType.size() - 1);// args改为argsType
			argsType.remove(argsType.size() - 1);
		}

		public void addPagerArgs(Page page) {
			addArgsType(Types.INTEGER);
			addArgsType(Types.INTEGER);
			addArgs(page.getEnd());
			addArgs(page.getStart());

		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public String getSql() {
			return sql;
		}

	}

	public static class SearchParam {
		String sql;
		int paramType;
		Object param;

		public SearchParam(String sql, Object param, int paramType) {
			setSql(sql);
			this.paramType = paramType;
			this.param = param;
		}

		public SearchParam(String sql, Object param) {
			setSql(sql);
			this.param = param;
			if (param != null) {
				if (param instanceof Integer) {
					this.paramType = Types.INTEGER;
				} else if (param instanceof Long) {
					this.paramType = Types.BIGINT;
				} else if (param instanceof Double) {
					this.paramType = Types.DOUBLE;
				} else if (param instanceof Float) {
					this.paramType = Types.FLOAT;
				} else if (param instanceof String) {
					this.paramType = Types.VARCHAR;
				} else if (param instanceof Date) {
					this.paramType = Types.DATE;
				} else {
					throw new IllegalArgumentException("请设置参数类型:");
				}

			}

		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = " " + sql + " ";
		}

		public int getParamType() {
			return paramType;
		}

		public void setParamType(int paramType) {
			this.paramType = paramType;
		}

		public Object getParam() {
			return param;
		}

		public void setParam(Object param) {
			this.param = param;
		}
	}

	public static Page pageQueryForPhoto(int pageNum,
			PageQueryUtils.ExecuteArgs executeArgs, JdbcTemplate jdbcTemplate,
			RowMapper rowMapper) {
		Page page = new Page(pageNum);

		executeArgs.addPagerArgs(page);
		List result = jdbcTemplate.query(
				PageQueryUtils.decorateOraclePageSql(executeArgs.getSql()),
				executeArgs.getArgs(), executeArgs.getArgsType(), rowMapper);
		executeArgs.removePagerArgs();
		int count = jdbcTemplate.queryForObject(
				PageQueryUtils.decorateOracleCountSql(executeArgs.getSql()),
				executeArgs.getArgs(),Integer.class);
		page.setResult(result);
		page.setTotalCount(count);
		return page;
	}

}
