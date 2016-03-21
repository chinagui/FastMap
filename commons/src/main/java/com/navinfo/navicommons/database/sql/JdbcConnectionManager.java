package com.navinfo.navicommons.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.navinfo.navicommons.config.SystemGlobals;


/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-04-20
 */
public class JdbcConnectionManager implements ConnectionManager
{
    private static String userName;
    private static String password;
    private static String url;
    private static String driver;
    private static String jndi;
    static
    {
        jndi = SystemGlobals.getValue("datasource.jndi");
       
    }

    public  Connection getConnection() throws PersistenceException
    {
        Connection con = null;
        if(jndi != null)
        {
            con = gerConnectionFromJndi();
            System.out.println("try to get connection from jndi!");
        }
        if(con == null)
        {
            System.out.println("get connection error please check SystemGlobals.properties!");
        }
        return con;
    }

    private Connection gerConnectionFromJndi()
    {
        Connection con = null;
        try
        {
            Context context  = new InitialContext();//todo different server has different naming context implement  
            con = ((DataSource)context.lookup(jndi)).getConnection();
        } catch (NamingException e)
        {
            e.printStackTrace();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return con;
    }

    private Connection gerConnectionFromNative()
    {
        try
        {
            Connection con = DriverManager.getConnection(url,userName,password);
            con.setAutoCommit(false);
            return con;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public  boolean closeConnection(Connection con)
    {
        try
        {
            con.close();
        } catch (Exception e) 
        {
            //con.close();
        }
        return true;
    }
}
