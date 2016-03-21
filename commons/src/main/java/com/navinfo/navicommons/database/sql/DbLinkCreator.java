package com.navinfo.navicommons.database.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.Config;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.ProcedureBase;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-12-15 Time: 下午3:24 dblink 创建
 * 关闭 销毁 注意：dblink的 创建 关闭 销毁方法使用的是连接池 dblinkContainer.getBasicDataSource()
 * 所以在使用结束后，确保调用 version.closeBasicDataSource
 */
public class DbLinkCreator {
	private Logger log = Logger.getLogger(DbLinkCreator.class);

	public DbLinkCreator() {
	}

	/**
	 * 创建DBLINK
	 * 
	 * @param dbLinkName
	 *            dblink名称
	 * @param isPublic
	 * @param dblinkContainer
	 *            DBLINK位于的数据库 
	 * @param To...
	 *            DBLINK连接的目标数据库
	 * @throws SQLException
	 */
	public void create(String dbLinkName,
			boolean isPublic,
			DataSource dblinkContainer,
			String toSchemaUserName,String toSchemaPasswd,String toIp,String toPort,String toSid) throws SQLException {
		// 判断public database是否存在，存在，则不再创建
		if (isExists(dbLinkName, isPublic, dblinkContainer))
			return;
		String sql = "create database link ";
		if (isPublic) {
			sql = "create public database link ";
		}

		sql += dbLinkName
				+ " connect to "
				+ toSchemaUserName
				+ " identified by "
				+ "\""+toSchemaPasswd+"\""
				+ " using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = "
				+ toIp + " )(PORT = " + toPort + " )))"
				+ "(CONNECT_DATA = (SERVICE_NAME = " + toSid + " )))'";

		log.debug("create dblink :" + sql);
		Connection conn = null;
		try {
			conn = dblinkContainer.getConnection();
			QueryRunner runner = new QueryRunner();
			runner.execute(conn, sql);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * @param dbLinkName
	 * @param dblinkUseConn
	 *            使用dblink的那个连接
	 */
	public void close(String dbLinkName, Connection dblinkUseConn) throws SQLException {

		String sql = "begin dbms_session.close_database_link(upper('" + dbLinkName + "')) ;end;";
//		log.debug("close dblink :" + sql);
		if (dblinkUseConn == null)
			return;
		ProcedureBase procedureBase = new ProcedureBase(dblinkUseConn);
		procedureBase.callProcedure(sql);
	}

	/**
	 * @param dbLinkName
	 * @param dblinkUseConn
	 *            使用dblink的那个连接
	 */
	public void closeQuietly(String dbLinkName, Connection dblinkUseConn) {
		try {
			close(dbLinkName, dblinkUseConn);
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
		}
	}

	public void drop(String dbLinkName,
			boolean isPublic,
			DataSource dblinkContainer) throws Exception {
		String sql = "drop public database link ";
		if (!isPublic) {
			sql = "drop database link ";
		}
		sql += dbLinkName;
		log.debug("drop dblink :" + sql);
		Connection conn = null;
		try {
			conn = dblinkContainer.getConnection();
			QueryRunner runner = new QueryRunner();
			runner.execute(conn, sql);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 判断是否存在访问 Dblink
	 * 
	 * @param dbLinkName
	 *            dblink名称
	 * @param dblinkContainer
	 *            DBLINK位于的数据库
	 * @throws SQLException
	 */
	public boolean isExists(String dbLinkName, boolean isPublic, DataSource dblinkContainer) throws SQLException {
		String sql = "";
		if (isPublic) {
			sql = "select nvl(count(1),0) from all_db_links where upper(db_link) = ? AND OWNER='PUBLIC'";
		} else {
			sql = "select nvl(count(1),0) from user_db_links where upper(db_link) = ?";
		}
		QueryRunner runner = new QueryRunner();
		Connection conn = null;
		try {
			conn = dblinkContainer.getConnection();
			Boolean query = runner.query(conn, sql, new ResultSetHandler<Boolean>() {
				boolean exists = false;

				public Boolean handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int count = rs.getInt(1);
						if (count > 0) {
							exists = true;
						}
					}
					return exists;
				}
			}, dbLinkName.toUpperCase());
			if(query)
				log.debug(dbLinkName+"已存在，无需创建");
			return query;

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

}
