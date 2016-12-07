package com.navinfo.navicommons.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import oracle.sql.CLOB;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author liuqing
 */
public abstract class DataBaseUtils {

    private static Logger log = Logger.getLogger(DataBaseUtils.class);

    /**
     * 获取表的MetaData
     *
     * @param conn
     * @param tableName
     * @return
     */
    public static List<ColumnMetaData> getTableMetaData(Connection conn, String tableName, boolean closeConn) throws Exception {
        long t1 = System.currentTimeMillis();
        List<ColumnMetaData> columns = new ArrayList<ColumnMetaData>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select * from " + tableName + " where 0=1";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            columns = getTableMetaData(tableName, md);
            long t3 = System.currentTimeMillis();
            // log.debug("getTableMetaData["+tableName+"] time ="+(t3-t1)+"ms");
        } catch (Exception ex) {
//            log.error(tableName, ex);
            throw ex;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (closeConn) {
                    if (conn != null)
                        conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return columns;

    }

    /**
     * 获取表的MetaData
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static List<ColumnMetaData> getTableMetaData(String tableName, ResultSetMetaData md) throws SQLException {
        List<ColumnMetaData> columns = new ArrayList<ColumnMetaData>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String columnName = md.getColumnName(i);
            int columnType = md.getColumnType(i);
            ColumnMetaData tmd = new ColumnMetaData();
            tmd.setColumnCount(md.getColumnCount());
            tmd.setColumnName(columnName);
            tmd.setColumnType(columnType);
            tmd.setTableName(tableName);
            tmd.setPrecision(md.getPrecision(i));
            tmd.setScale(md.getScale(i));
            tmd.setColumnTypeName(md.getColumnTypeName(i));
            /*if (!columnName.equals("TMP_PID")) {

            }*/
            columns.add(tmd);
        }

        return columns;

    }

    /**
     * 将字CLOB转成STRING类型
     *
     * @param clob
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static String clob2String(CLOB clob) throws SQLException {
        String reString = (clob != null ? clob.getSubString(1, (int) clob.length()) : null);
        return reString;
    }

    /**
     * 根据Oracle的元数据信息生成Insert 语句
     *
     * @param tableName
     * @param tmdList
     * @return
     */
    public static String generateInsertSql(String tableName,
                                           List<ColumnMetaData> tmdList,
                                           List<String> excludeColumnList,
                                           boolean noValue) {
        tmdList=removeIgnoreColumn(tmdList,excludeColumnList);
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(tableName);
        builder.append(" (");
        int columnSize = tmdList.size();

        String parameters[] = new String[columnSize];
        String columns[] = new String[columnSize];
        for (int i = 0; i < tmdList.size(); i++) {
            parameters[i] = "?";
            columns[i] = "\""+tmdList.get(i).getColumnName()+"\"";
        }

        builder.append(StringUtils.join(columns, ","));
        builder.append(") values(");
        if (!noValue) {
            builder.append(StringUtils.join(parameters, ","));
            builder.append(")");
        }

        return builder.toString();
    }

    public static String generateInsertSql(String tableName, List<ColumnMetaData> tmdList) {
        return generateInsertSql(tableName, tmdList, null, false);
    }

    public static String  generateInsertSql(String tableName, List<ColumnMetaData> tmdList, List<String> excludeColumnList) {
        return generateInsertSql(tableName, tmdList, excludeColumnList, false);
    }


    public static StringBuilder generateInsertNoValuesSql(String tableName,
                                                          List<ColumnMetaData> tmdList,
                                                          List<String> excludeColumnList) {
        return new StringBuilder(generateInsertSql(tableName, tmdList, excludeColumnList, true));
    }

    public static List<ColumnMetaData> removeIgnoreColumn(List<ColumnMetaData> tmdList, List<String> ignoreColumns) {
        List<ColumnMetaData> newTmdList = new ArrayList<ColumnMetaData>();
        if (ignoreColumns != null&&ignoreColumns.size()>0) {
            for (int i = 0; i < tmdList.size(); i++) {
                ColumnMetaData columnMetaData = tmdList.get(i);
                String columnName = columnMetaData.getColumnName();
                for (int j = 0; j < ignoreColumns.size(); j++) {
                    String ignoreName = ignoreColumns.get(j);
                    if (!ignoreName.toUpperCase().equals(columnName)) {
//                        log.debug("add column:"+columnName);
                        newTmdList.add(columnMetaData);
                    }
                }
            }
            return newTmdList;
        }
        return tmdList;
    }

    /**
     * 返回单表查询sql
     *
     * @param tableName
     * @param tmdList
     * @return
     */
    public static String generateSimpleQueryString(String tableName, List<ColumnMetaData> tmdList) {
        long t1 = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        builder.append("select ");
        boolean hasMeshColumn = false;
        for (int i = 0; i < tmdList.size(); i++) {
            ColumnMetaData tmd = tmdList.get(i);
            String columnName = tmd.getColumnName();
//            if (!columnName.equals("TMP_PID")) {
            builder.append("l." + columnName);
//            }

            if (i < tmdList.size() - 1) {
                builder.append(",");
            }
            if (columnName.equals("MESH_ID")) {
                hasMeshColumn = true;
            }
        }
        builder.append(" from ");
        builder.append(tableName);
        builder.append(" l ");
        /*if (hasMeshColumn) {
            builder.append(" where mesh_id =595673");
        }*/

        String sql = builder.toString();
        if (sql.indexOf("TMP_PID") > -1) {
            System.out.println("error:" + tableName);
        }
        // log.debug(sql);
        long t3 = System.currentTimeMillis();
        // log.debug("generateSimpleQueryString["+tableName+"] time ="+(t3-t1)+"ms");
        return sql;
    }

    /**
     * @param tableNameLike
     * @param conn
     * @param closeConn
     * @return
     * @throws Exception
     */
    public static List<String> getTables(String tableNameLike, Connection conn, boolean closeConn) throws Exception {
        long t1 = System.currentTimeMillis();
        List<String> tables = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select table_name from user_tables where table_name like '" + tableNameLike + "'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
            long t3 = System.currentTimeMillis();
            // log.debug("getTableMetaData["+tableName+"] time ="+(t3-t1)+"ms");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (closeConn) {
                    if (conn != null)
                        conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return tables;
    }

    public static String getReturnTable(String sql) throws Exception {

        String name = "";
        // String regEx = "(\\s+.+\\s+P) ";
        String regEx = "FROM\\s+(\\S+)\\s+P";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            name = matcher.group(1);
        }
        if (name.equals("")) {
            log.debug(sql);
            throw new Exception("can't find tablename");
        }

        return name;
    }

    /**
     * 获取数据源
     *
     * @param jdbcTemplate
     * @param key
     * @return
     */
    public static DataSource getSpecifiedDataSource(JdbcTemplate jdbcTemplate, String key) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource instanceof DynamicDataSource) {
            dataSource = ((DynamicDataSource) dataSource).getSpecifiedDataSource(key);
        }

        return dataSource;
    }

    /**
     * 获取数据源
     *
     * @param jdbcTemplate
     * @param key
     * @return
     */
    public static Connection getSpecifiedConnection(JdbcTemplate jdbcTemplate, String key) throws SQLException {
        Connection conn = getSpecifiedDataSource(jdbcTemplate, key).getConnection();
        conn.setAutoCommit(false);
        return conn;
    }


    public static List<String> getPrimaryKeys(Connection connection, String tableName) throws SQLException {
        List<String> keyList = new ArrayList<String>();
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rsKey = meta.getPrimaryKeys(null, null, tableName); //获取制定表的主键列信息
        while (rsKey.next())
            keyList.add(rsKey.getString("COLUMN_NAME"));
        return keyList;
    }

    public static List<String> getForeignKeys(Connection connection, String tableName) throws SQLException {
        List<String> keyList = new ArrayList<String>();
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rsKey = meta.getImportedKeys(null, null, tableName); //获取制定表的主键列信息
        while (rsKey.next())
            keyList.add(rsKey.getString("FKCOLUMN_NAME"));
        return keyList;
    }
    
	public  static void turnOnPkConstraint(Connection conn, Set<String> tableNames) throws SQLException {
		switchConstraint(conn, true,"P",tableNames);

	}
	public  static void turnOffPkConstraint(Connection conn, Set<String> tableNames) throws SQLException {
		switchConstraint(conn, false,"P",tableNames);

	}
	public  static void turnOnFkConstraint(Connection conn, Set<String> tableNames) throws SQLException {
		switchConstraint(conn, true,"F",tableNames);

	}
	public  static void turnOffFkConstraint(Connection conn, Set<String> tableNames) throws SQLException {
		switchConstraint(conn, true,"F",tableNames);

	}
    
    private static void switchConstraint(Connection conn, final boolean onOff,String constraintType,Set<String> targetTables)
            throws SQLException {

		if(targetTables==null||targetTables.size()==0){
			return;
		}
		
		String tableSql = "select table_name,constraint_name from user_constraints c where c.constraint_type='"+constraintType+"' AND C.TABLE_NAME NOT LIKE 'BIN%' ";

		tableSql += " AND table_name in ('" + StringUtils.join(targetTables,"','")+"')";

		QueryRunner runner = new QueryRunner();
		//KEY=CONSTRAINT_NAME,VALUE=TABLE_NAME
		Map<String,String> tables = runner.query(conn, tableSql, new ResultSetHandler<Map<String,String>>() {
			@Override
			public Map<String,String> handle(ResultSet rs) throws SQLException {
				
				Map<String,String> tables = new HashMap<String,String>();
				while (rs.next()) {
					tables.put(new String(rs.getString("CONSTRAINT_NAME")),new String(rs.getString("TABLE_NAME")));	
				}
				return tables;
			}

		});
		
		String disable = "DISABLE";
		if (onOff) {
			disable = "ENABLE";
		}
		for (String constraintName : tables.keySet()) {
			String primaryKeySql = "ALTER TABLE " + tables.get(constraintName) + " " + disable + " constraint "+constraintName;
			runner.execute(conn, primaryKeySql);
		}
    }
    /**
     * @param conn 传入的链接，本方法不关闭；调用放负责关闭conn
     * @param seqName ORACLE sequence 的名称
     * @return sequnce.NEXTVAL
     * @throws Exception
     */
    public static long fetchSequence(Connection conn,String seqName) throws Exception{
    	QueryRunner runner = new QueryRunner();
    	ResultSetHandler<Long> rsh = new ResultSetHandler<Long>(){

			@Override
			public Long handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					return rs.getLong(1);
				}
				return 0L;
			}};
		return runner.query(conn,"select "+seqName+".NEXTVAL  from dual", rsh );
    }

    public static void main(String args[]) {
        try {
            String name = DataBaseUtils.getReturnTable("SELECT P.* FROM POI_TELE P,TEMP_POI_INFO T WHERE P.POI_PID=T.PID");
            System.out.println(name);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
