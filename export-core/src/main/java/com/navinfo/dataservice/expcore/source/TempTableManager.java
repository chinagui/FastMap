package com.navinfo.dataservice.expcore.source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dms.commons.database.QueryRunner;
import com.navinfo.dms.tools.vm.log.VMTaskLogger;
import com.navinfo.dms.tools.vm.model.Version;

public class TempTableManager {
	private DataSource vmDataSource;
	private Logger log = Logger.getLogger(ExportSource.class);

	public TempTableManager(DataSource vmDataSource) {
		super();
		this.vmDataSource = vmDataSource;
		log = VMTaskLogger.getLogger(log);
	}

	private Integer getSchemaResourceCount(Version version) throws SQLException {
		String querySql = "select count(1) from SCHEMA_RESOURCES where INSTANCE_ID=? and SCHEMA_NAME=?";
		QueryRunner runner = new QueryRunner();
		Connection vmConn = vmDataSource.getConnection();
		try {
			return runner.query(vmConn, querySql, new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						return rs.getInt(1);
					}
					return 0;
				}
			}, version.getDataBase().getInstanceId(),
					version.getSchemaUserName());
		} finally {
			DbUtils.closeQuietly(vmConn);
		}
	}

	/**
	 * 内业：当临时表和SchemaResource为空时，删除临时表
	 * 
	 * @throws Exception
	 */
	public  void  syncTempTable(Version version) throws Exception {

		Integer resourceCount = getSchemaResourceCount(version);
		if (resourceCount == 0) {
			// 如果临时表的信息被删除，则删除临时表
			cleanExpTempTable(version);
		}
	}

	/**
	 * 删除内业临时表
	 * 
	 * @param version
	 * @throws Exception
	 */
	public synchronized static void cleanExpTempTable(Version version) throws Exception {

		final Logger log = Logger.getLogger(ExportSource.class);
		log.debug("clean exp temp table in gdb");
		// TEMP 开头，数字结尾
		String sql = "select table_name  from user_tables t where regexp_like(t.table_name, '^TEMP.+[[:digit:]]$')";
		log.debug("find exp table sql :" + sql);
		final Connection conn = version.getPoolDataSource().getConnection();
		try {
			final QueryRunner runner = new QueryRunner();
			runner.query(conn, sql, new ResultSetHandler<Object>() {
				@Override
				public Object handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						String tableName = rs.getString(1);
						String dropSql = "drop table " + tableName + " cascade constraints";
						log.debug(dropSql);
						runner.execute(conn, dropSql);
					}
					return null;
				}
			});
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}
	

	


	/**
	 * 外业临时表是否存在
	 * 
	 * @throws Exception
	 */
	public boolean isAuExpTempTableExist(Version version) throws Exception {

		String sql = "select table_name  from user_tables t  where regexp_like(t.table_name, '^TMAU_EXP.+[^[:digit:]]$')";
		final Connection conn = version.getPoolDataSource().getConnection();
		try {
			final QueryRunner runner = new QueryRunner();
			List<String> tables = runner.query(conn, sql, new ResultSetHandler<List<String>>() {
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> tables = new ArrayList<String>();
					while (rs.next()) {
						tables.add(rs.getString(1));

					}
					return tables;
				}
			});
			if (tables.size() != 22) {
				log.error("外业库中的临时表数量不对，请参考au_temp_table_create.sql表进行升级外业临时表");
			}
			/*
			 * if (tables.size() != 16) {
			 * log.debug("外业库导出临时表数量 !=16,删除已有临时表，重建"); for (int i = 0; i <
			 * tables.size(); i++) { String tableName = tables.get(i); String
			 * dropSql = "drop table " + tableName + " cascade constraints";
			 * log.debug(dropSql); runner.execute(conn, dropSql); } return
			 * false; }
			 */
			return tables.size() > 0;

		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

}
