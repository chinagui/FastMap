package com.navinfo.navicommons.database.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.navicommons.database.ColumnMetaData;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-3-11
 * Time: 下午1:57
 * 合并两个sqlite的内容到一个文件中
 */
public class MergeSqlite {
    public String merge(String source, String dest) throws Exception {

        Connection descConn = null;
        Connection sourceConn = null;
        try {
            sourceConn = SqliteManager.getConnection(source);
            descConn = SqliteManager.getConnection(dest);
            List<String> tables = getTables(sourceConn);
            for (int i = 0; i < tables.size(); i++) {
                String tableName = tables.get(i);
                mergeTableData(descConn, sourceConn, tableName);


            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(descConn);
            DbUtils.commitAndCloseQuietly(sourceConn);
        }


        return dest;
    }

    private void mergeTableData(Connection descConn, Connection sourceConn, String tableName) throws Exception {
        PreparedStatement sourceStatement = null;
        ResultSet rs = null;
        try {
            sourceStatement = sourceConn.prepareStatement("select * from " + tableName);
            rs = sourceStatement.executeQuery();
            insertTableValues(sourceConn, descConn, tableName, rs);
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.close(rs);
            DbUtils.close(sourceStatement);
        }
    }

    private void insertTableValues(Connection sourceConn, Connection descConn, String tableName, ResultSet rs) throws Exception {

        PreparedStatement destStatement = null;
        try {
            List<ColumnMetaData> metaDatas = DataBaseUtils.getTableMetaData(sourceConn, tableName, false);
            String insertSql = DataBaseUtils.generateInsertSql(tableName, metaDatas);
            destStatement = descConn.prepareStatement(insertSql);
            int count = 0;
            while (rs.next()) {
                for (int j = 0; j < metaDatas.size(); j++) {
                    ColumnMetaData columnMetaData = metaDatas.get(j);
                    destStatement.setObject(j + 1, rs.getObject(j + 1));
                }
                destStatement.addBatch();
                if (count % 100 == 0) {
                    destStatement.executeBatch();
                    destStatement.clearBatch();
                }

                count++;
            }
            destStatement.executeBatch();
        } catch (Exception e) {
            throw e;

        } finally {
            DbUtils.close(destStatement);
        }
    }


    /**
     * @param sourceConn
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private List<String> getTables(Connection sourceConn) throws Exception {
        String tablesSql = "select name from sqlite_master where type = 'table'";
        try {
            QueryRunner runner = new QueryRunner();
            return runner.query(sourceConn, tablesSql, new ResultSetHandler<List<String>>() {
                public List<String> handle(ResultSet rs) throws SQLException {
                    List<String> tables = new ArrayList<String>();
                    while (rs.next()) {
                        tables.add(rs.getString(1));
                    }
                    return tables;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        MergeSqlite mergeSqlite = new MergeSqlite();
        try {
            mergeSqlite.merge("D:\\temp\\source.db", "D:\\temp\\dest.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
