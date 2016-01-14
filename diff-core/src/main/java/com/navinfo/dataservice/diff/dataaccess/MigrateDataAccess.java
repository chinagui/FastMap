package com.navinfo.dataservice.diff.dataaccess;


/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-12 上午9:02
 */
public class MigrateDataAccess implements DataAccess
{

    public MigrateDataAccess()
    {
    }

    @Override
    public String accessTable(Table table)
    {
        return table.getTableName();
    }
}
