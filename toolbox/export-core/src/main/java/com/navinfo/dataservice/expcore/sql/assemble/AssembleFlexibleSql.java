package com.navinfo.dataservice.expcore.sql.assemble;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.expcore.sql.ExpSQL;


public class AssembleFlexibleSql{
	protected Logger log = Logger.getLogger(this.getClass());

	public AssembleFlexibleSql() {
	}

	public List<ExpSQL>  assemble(List<String> flexTables,Map<String,String> flexConditions) throws Exception {
		try {
			List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
			if(flexTables==null||flexConditions==null){
				return expSQLs;
			}
			String where= generateWhere(flexTables.get(0),flexConditions);
			for(String table:flexTables){
				ExpSQL expSQL = new ExpSQL();
				String sql = "SELECT P.* FROM "+table+" P "+where;
				expSQL.setSql(sql);
				expSQLs.add(expSQL);
			}
			return expSQLs;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}
	private String generateWhere(String table,Map<String,String> flexConditions){

//		StringBuilder sb = new StringBuilder();
//		Set<Entry<String,String>> entrySet = flexConditions.entrySet();
//		Iterator<Entry<String,String>> iterator = entrySet.iterator();
//		while(iterator.hasNext()){
//			Entry<String,String> entry =  iterator.next();
//			String col = entry.getKey();
//			String value = entry.getValue();
//		}
//		for(String key:flexConditions.keySet()){
//			
//		}
		//临时实现
		return "WHERE P.MESH_ID IN IN (select to_number(column_value) from table(varchar_to_table('"+flexConditions.get("MESH_ID")+"')))";
	}

	/**
	 * 自动构造所有表复制的sql
	 * 
	 * @param dbLinkName
	 * @param parentDataSource
	 * @return
	 * @throws java.sql.SQLException
	 */
	protected List<ExpSQL> assembleFlexibleSql(final String dbLinkName, OracleSchema sourceSchema,
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
