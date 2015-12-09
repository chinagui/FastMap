package com.navinfo.navicommons.database.sql;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-5-7
 */
public interface PersistenceManager
{
    public StoredProcedure getProcedure(String name, boolean withReturn);


    public PageQuery getPageQuery(int start,int limit);

    public SQLQuery getSQLQuery();
}
