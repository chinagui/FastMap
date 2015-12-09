package com.navinfo.navicommons.database.sql;

import java.sql.Connection;

public interface Datastore
{
    public void close()  throws PersistenceException;

    public void flush()  throws PersistenceException;

    public void refresh(Object obj, int lockmode)  throws PersistenceException;

    public void commit()  throws PersistenceException;

    public void rollback()  throws PersistenceException;

    public PersistenceManager getPersistenceManager()  throws PersistenceException;

    public void init()  throws PersistenceException;

    public Connection getConnection() throws PersistenceException;

}
