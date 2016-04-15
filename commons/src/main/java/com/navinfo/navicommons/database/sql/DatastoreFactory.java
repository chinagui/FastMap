package com.navinfo.navicommons.database.sql;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-5-7
 */
public class DatastoreFactory
{
    public static Datastore createDatastore()
    {
        Datastore datastore = new DatastoreImpl();
        datastore.init();
        return datastore;
    }
}
