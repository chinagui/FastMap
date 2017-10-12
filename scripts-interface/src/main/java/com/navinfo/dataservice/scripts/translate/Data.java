package com.navinfo.dataservice.scripts.translate;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Title: Data
 * @Package: com.navinfo.dataservice.scripts.translate
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/9/2017
 * @Version: V1.0
 */
public class Data {


    public static void main(String[] args) {
        File file = new File("D:\\ALL_KIND_MAP_10WIN.mdb");

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;

        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Class.forName("com.hxtt.sql.access.AccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String dbURL = "jdbc:access:///" + file.getAbsolutePath();
        //String dbURL = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ=" + file.getAbsolutePath();

        try {
            Properties prop = new Properties();
            prop.setProperty("charSet", "gb2312");
            prop.setProperty("user", "");
            prop.setProperty("password", "");

            conn = DriverManager.getConnection(dbURL, prop);

            stmt = conn.createStatement();

            result = stmt.executeQuery("SELECT * FROM ALL_KIND_MAP");

            while (result.next()) {
                System.out.print(StringUtils.rightPad(result.getString("MIF_KIND"), 8));
                System.out.print(StringUtils.rightPad(result.getString("DB_KIND"), 7));
                System.out.print(StringUtils.rightPad(result.getString("CLASS"), 16));
                System.out.println(StringUtils.rightPad(result.getString("描述"), 8));
            }

            stmt.addBatch("UPDATE ALL_KIND_MAP SET DB_KIND = 11 WHERE MIF_KIND = 1301");
            System.out.println(Arrays.toString(stmt.executeBatch()));
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(result);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }
}
