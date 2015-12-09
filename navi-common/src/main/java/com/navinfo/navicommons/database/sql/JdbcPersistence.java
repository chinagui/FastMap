package com.navinfo.navicommons.database.sql;

import java.sql.Connection;

public class JdbcPersistence implements PersistenceManager
{
    private Connection con;

    public JdbcPersistence(Connection con)
    {
        this.con = con;
    }

    public StoredProcedure getProcedure(String name, boolean withReturn)
    {
        return new StoredProcedureImpl(con,name, withReturn);
    }


    public PageQuery getPageQuery(int start,int limit)
    {
        return new PageQuery(con,start,limit);
    }

    public SQLQuery getSQLQuery()
    {
        return new SQLQuery(con);
    }


}
