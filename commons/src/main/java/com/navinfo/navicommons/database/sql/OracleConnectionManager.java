package com.navinfo.navicommons.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-11
 */
public class OracleConnectionManager
{
    public static Connection getConnection(String ip,String sid,String user,String password)
            throws ClassNotFoundException, SQLException
    {
        String url = "jdbc:oracle:thin:@" + ip + ":1521:" + sid;
        return getConnection(url,user,password);
    }

    public static Connection getConnection(String url,String user,String password)
            throws ClassNotFoundException, SQLException
    {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);
        return conn;
    }

}
