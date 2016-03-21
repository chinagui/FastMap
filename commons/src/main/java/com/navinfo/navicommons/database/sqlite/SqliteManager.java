package com.navinfo.navicommons.database.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;

/**
 * 根据sqlite数据库的管理类
 *
 * @author liuqing
 */
public class SqliteManager {
    private static Logger log = Logger.getLogger(SqliteManager.class);

    public static void main(String[] args) {
        try {
            SqliteManager.reNameTable("D:\\temp\\d82a6175-2bc5-4409-88aa-233efb0907da\\gdb_1.db", "RD_NAME", "TEMP_RD_NAME");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * 获取连接
     *
     * @param fileLocation
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(String fileLocation) throws ClassNotFoundException, SQLException {
        File file = new File(fileLocation);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Class.forName("org.sqlite.JDBC");
        Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + fileLocation);
        sqliteConn.setAutoCommit(false);
        return sqliteConn;
    }

    public static void reNameTable(String fileLocation, String oldName, String newName) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        try {
            connection = getConnection(fileLocation);
            String sql = "ALTER TABLE " + oldName + " RENAME TO " + newName;
            log.debug(sql);
            QueryRunner runner = new QueryRunner();
            runner.update(connection, sql);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(connection);
        }

    }

    public static void createSqliteFromOracle(String fileLocation,
                                              Connection oracleConn,
                                              String whereCause,
                                              boolean closeOracleConn) throws Exception {
        createSqliteFromOracle(fileLocation, oracleConn, whereCause, closeOracleConn, null, null);
    }

    public static void createSqliteFromOracle(String fileLocation,
                                              Connection oracleConn,
                                              String whereCause,
                                              boolean closeOracleConn,
                                              List<String> extDdl) throws Exception {
        createSqliteFromOracle(fileLocation, oracleConn, whereCause, closeOracleConn, extDdl, null);
    }

    /**
     * 根据Oracle的shema生成sqlite的空数据库
     *
     * @param fileLocation
     * @param oracleConn
     * @throws Exception
     */
    public static void createSqliteFromOracle(String fileLocation,
                                              Connection oracleConn,
                                              String whereCause,
                                              boolean closeOracleConn,
                                              List<String> extDdl,
                                              List<Table> exclude) throws Exception {
        Connection sqliteConn = null;
        PreparedStatement ps = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            // String ALL_TABLES =
            // "select TABLE_NAME from user_tables where TABLE_NAME='RD_LINK'";
            String ALL_TABLES = "select TABLE_NAME from user_tables t " + whereCause;
            ps = oracleConn.prepareStatement(ALL_TABLES);
            rs = ps.executeQuery();
            sqliteConn = SqliteManager.getConnection(fileLocation);
            statement = sqliteConn.createStatement();
            while (rs.next()) {
                String tableName = rs.getString(1);
                statement.executeUpdate("drop table if exists " + tableName);
                String ddl = metaData2ddl(oracleConn, tableName, exclude);
                log.debug(ddl);
                statement.executeUpdate(ddl);
            }
            if (extDdl != null) {
                for (int i = 0; i < extDdl.size(); i++) {
                    String ddl = extDdl.get(i);
                    statement.executeUpdate(ddl);
                }
            }
            sqliteConn.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(sqliteConn);
            if (closeOracleConn)
                DbUtils.closeQuietly(oracleConn);
        }
    }


    public static void executeDDL(String fileLocation, List<String> ddls) throws Exception {
        Statement statement = null;
        Connection sqliteConn = null;
        try {
            sqliteConn = SqliteManager.getConnection(fileLocation);
            statement = sqliteConn.createStatement();
            for (int i = 0; i < ddls.size(); i++) {
                String ddl = ddls.get(i);
                log.debug(ddl);
                statement.executeUpdate(ddl);
            }
            sqliteConn.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(sqliteConn);

        }
    }

    /**
     * Oracle数据字段类型到sqlite的数据字段类型转换
     *
     * @param columnType
     * @param scale
     * @return
     */
    private static String columnTypeTransform(String columnType, int scale) {
        String type = "TEXT";
        // System.out.println(columnType);
        if (columnType.indexOf("CHAR") > -1 || columnType.indexOf("XML") > -1) {
            type = "TEXT";
        } else if (columnType.equals("NUMBER")) {
            if (scale == 0)
                type = "INTEGER";
            else
                type = "REAL";
        } else if (columnType.indexOf("SDO_GEOMETRY") > -1) {
            // type = "BLOB";
            type = "TEXT";
        } else if (columnType.indexOf("RAW") > -1) {
            type = "TEXT";
        }

        return type;

    }

    /**
     * 根据Oracle表名，生成创建表的create语句
     *
     * @param conn
     * @param tableName
     * @return
     */
    private static String metaData2ddl(Connection conn, String tableName, List<Table> exclude) {
        StringBuilder builder = new StringBuilder();
        builder.append("create table ");
        builder.append(tableName);
        builder.append(" (");

        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try {


            String sql = "select * from " + tableName + " where 0=1";
            ps2 = conn.prepareStatement(sql);
            rs2 = ps2.executeQuery();
            ResultSetMetaData md = rs2.getMetaData();
            int cc = md.getColumnCount();


            List<String> excludeColumns = new ArrayList<String>();
            if (exclude != null) {
                for (Table table : exclude) {
                    if (tableName.toUpperCase().equals(table.getTableName().toUpperCase())) {
                        excludeColumns = table.getColumns();
                    }
                }

            }

            for (int i = 1; i <= cc; i++) {
                String columnName = md.getColumnName(i);
                String columnType = md.getColumnTypeName(i);
//                long precision = md.getPrecision(i);
                int scale = md.getScale(i);
                if (!excludeColumns.contains(columnName)) {
                    builder.append("\"");
                    builder.append(columnName);
                    builder.append("\" ");
                    builder.append(columnTypeTransform(columnType, scale));
                    if (i < cc) {
                        builder.append(",");
                    }
                }

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(rs2);
            DbUtils.closeQuietly(ps2);
        }
        builder.append(" )");
        String sql = builder.toString();
        /* sql = sql.replace(",TMP_PID INTEGER", "");
        if (sql.indexOf("TMP_PID") > -1) {
            System.out.println("error:" + tableName);
            System.out.println(sql);
        }*/
        //
        return sql;
    }

}
