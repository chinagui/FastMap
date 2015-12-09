package com.navinfo.navicommons.database.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-5-7
 */
public class DatastoreImpl implements Datastore
{
    private Connection con;
    private PersistenceManager persistenceManager;


    public void close() throws PersistenceException
    {
        try
        {
            con.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void flush() throws PersistenceException
    {
        //do nouthing
    }

    public void refresh(Object obj, int lockmode) throws PersistenceException
    {
        //do nouthoing
    }

    public void commit() throws PersistenceException
    {
        try
        {
            con.commit();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void rollback() throws PersistenceException
    {
        try
        {
            con.rollback();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public PersistenceManager getPersistenceManager() throws PersistenceException
    {
        return persistenceManager;
    }

    public void init() throws PersistenceException
    {
        if(con == null)
        {
            con = ConnectionManagerFactory.getConnectionManager().getConnection();
            persistenceManager = new JdbcPersistence(con);
        }
    }

    public Connection getConnection() throws PersistenceException
    {
        return con;
    }
}
