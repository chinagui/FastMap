package com.navinfo.dataservice.expcore.sql.assemble;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;


public class AssembleFullCopySql{
	protected static Logger log = Logger.getLogger(AssembleFullCopySql.class);

	private static List<String> getAssembleTables(String gdbVersion,String featureType,List<String> specificTables,List<String> excludedTables)throws Exception{
		List<String> tables = null;
		if(specificTables!=null){
			tables = specificTables;
		}else{
			Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
			tables = new ArrayList<String>();
			tables.addAll(glm.getEditTableNames(featureType));
			tables.addAll(glm.getExtendTableNames(featureType));
			if(excludedTables!=null){
				tables.removeAll(excludedTables);
			}
		}
		return tables;
	}

	public static List<ExpSQL> assembleFastCopySql(String dbLinkName, OracleSchema sourceSchema, OracleSchema targetSchema, String gdbVersion,String featureType,List<String> specificTables,List<String> excludedTables,Map<String,String> tableRenames) throws Exception {
		List<String> tables = getAssembleTables(gdbVersion, featureType, specificTables, excludedTables);
		List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
		Connection conn=null;
		try {
			// 自动构造所有表复制的sql
			conn=sourceSchema.getPoolDataSource().getConnection();
			for (String tableName : tables) {
				String rename = (tableRenames!=null&&tableRenames.containsKey(tableName))?tableRenames.get(tableName):tableName;
				String columnSelect=getSelectColumnString(conn,tableName);
				if(StringUtils.isEmpty(columnSelect))continue;
				ExpSQL expSQL = new ExpSQL();
				// expSQL.setSql("create table " + tableName +
				// " as select * from " +
				// tableName + "@" + dbLinkName);
				expSQL.setSql("INSERT /*+ append */ INTO " + rename + " select "+columnSelect+" from " + tableName + "@" + dbLinkName);
				expSQLs.add(expSQL);
			}
			return expSQLs;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

	private static String getSelectColumnString(Connection conn, String tableName) throws SQLException {
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
	public static List<ExpSQL> assembleTruncateSql(String gdbVersion,String featureType, List<String> specificTables,List<String> excludedTables) throws Exception {
		List<String> tables = getAssembleTables(gdbVersion,featureType, specificTables, excludedTables);
		List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
		for (String tableName : tables) {
			ExpSQL expSQL = new ExpSQL();
			expSQL.setSql("TRUNCATE TABLE " + tableName);
			expSQLs.add(expSQL);
		}
		return expSQLs;
	}

}
