package com.navinfo.dataservice.diff.dataaccess;

import com.navinfo.dataservice.datahub.glm.GlmTable;


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
    public String accessTable(GlmTable table)
    {
        return table.getName();
    }
}
