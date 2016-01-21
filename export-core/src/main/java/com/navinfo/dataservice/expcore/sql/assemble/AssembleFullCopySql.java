package com.navinfo.dataservice.expcore.sql.assemble;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.sql.ExpSQL;


public class AssembleFullCopySql{
	protected Logger log = Logger.getLogger(this.getClass());

	public AssembleFullCopySql() {
	}

	public List<ExpSQL> assemble(String dbLinkName, OracleSchema sourceSchema, OracleSchema targetSchema, List<String> specificTables,List<String> excludedTables) throws Exception {
		try {
			List<String> tables = null;
			if(specificTables!=null){
				tables = specificTables;
			}else{
				tables = getCopyTables(sourceSchema,excludedTables);
			}
			

			// 自动构造所有表复制的sql
			List<ExpSQL> expSQLs = assembleFastCopySql(dbLinkName, sourceSchema,tables);
			return expSQLs;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}
	

	protected List<String> getCopyTables(OracleSchema schema,List<String> excludedTables) throws Exception {
		String gdbCause = "where 1=1";
		
		String ignoreTablePrefixConfig = SystemConfig.getSystemConfig().getValue("glm.ignore.table.prefix");
		if(StringUtils.isNotEmpty(ignoreTablePrefixConfig)){
			String ignoreTablePrefix[] = ignoreTablePrefixConfig.split(",");
			for (String prefix : ignoreTablePrefix) {
				gdbCause += " AND table_name not like '" + prefix + "%' and table_name !='OPERATE_LOG'";
			}
		}
		if(excludedTables!=null){
			for(String table:excludedTables){
				gdbCause += " AND table_name != '" + table + "'";
			}
		}

		String sql = "select table_name from user_tables " + gdbCause;
		QueryRunner runner = new QueryRunner();
		Connection conn = null;
		try {
			conn = schema.getPoolDataSource().getConnection();
			List<String> tables = runner.query(conn, sql, new ResultSetHandler<List<String>>() {

				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> tables = new ArrayList<String>();
					while (rs.next()) {
						String tableName = rs.getString(1);
						tables.add(tableName);
					}
					return tables;
				}
			});
			return tables;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

	/**
	 * 自动构造所有表复制的sql
	 * 
	 * @param dbLinkName
	 * @param parentDataSource
	 * @return
	 * @throws java.sql.SQLException
	 */
	protected List<ExpSQL> assembleFastCopySql(final String dbLinkName, OracleSchema sourceSchema,
			List<String> tables) throws Exception {
		Connection conn=null;
		List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
		try {
			conn=sourceSchema.getPoolDataSource().getConnection();
			for (String tableName : tables) {
				String columnSelect=getSelectColumnString(conn,tableName);
				if(StringUtils.isEmpty(columnSelect))continue;
				ExpSQL expSQL = new ExpSQL();
				// expSQL.setSql("create table " + tableName +
				// " as select * from " +
				// tableName + "@" + dbLinkName);
				expSQL.setSql("INSERT /*+ append */ INTO " + tableName + " select "+columnSelect+" from " + tableName + "@" + dbLinkName);
				expSQLs.add(expSQL);
			}
		} catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
		

		return expSQLs;
	}

	private String getSelectColumnString(Connection conn, String tableName) throws SQLException {
		String sql = "select COLUMN_NAME from USER_TAB_COLUMNS where table_name = ? ORDER BY COLUMN_ID";
		QueryRunner runner = new QueryRunner();
		return runner.query(conn, sql, new ResultSetHandler<String>() {
			StringBuilder builder = new StringBuilder();

			@Override
			public String handle(ResultSet rs) throws SQLException {
				int i = 0;
				while (rs.next()) {
					if (i > 0) {
						builder.append(",");
					}
					builder.append("\"");
					builder.append(rs.getString(1));
					builder.append("\"");
					i++;

				}
				return builder.toString();
			}
		}, tableName);

	}
}
