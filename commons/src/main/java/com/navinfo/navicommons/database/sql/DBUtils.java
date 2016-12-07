package com.navinfo.navicommons.database.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-10-15
 */
public class DBUtils 
{
    private static final transient Logger log = Logger.getLogger(DBUtils.class);

    public static void rollBack(Connection con)
    {
        try {
        	if(con!=null)
            con.rollback();
        } catch (SQLException e) {
            log.warn("rollBack发生异常");
        }
    }

    public static void closeConnection(Connection con)
    {
        if(con != null)
            try
            {
                con.close();
            } catch (SQLException e)
            {
                log.warn("关闭连接时发生异常");
            }
    }

    public static void closeResultSet(ResultSet rs)
    {
        if(rs != null)
            try
            {
                rs.close();
            } catch (SQLException e)
            {
                log.warn("关闭结果集时发生异常");
            }
    }

    public static void closeStatement(Statement st)
    {
        if(st != null)
            try
            {
                st.close();
            } catch (SQLException e)
            {
                log.warn("关闭语句时发生异常");
            }
    }
	

}
