package com.navinfo.navicommons.database.sql;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-5-7
 */
public class ConnectionManagerFactory 
{
    public static ConnectionManager getConnectionManager()
    {
        return new JdbcConnectionManager();
    }
}
