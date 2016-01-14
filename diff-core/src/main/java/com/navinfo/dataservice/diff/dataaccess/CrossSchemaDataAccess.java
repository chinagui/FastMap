package com.navinfo.dataservice.diff.dataaccess;

import com.navinfo.dataservice.datahub.model.OracleSchema;

import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 上午9:56
 */
public class CrossSchemaDataAccess implements DataAccess
{
    private OracleSchema schema;

    public CrossSchemaDataAccess(OracleSchema schema)
    {
        this.schema= schema;
    }

    @Override
    public String accessTable(Table table)
    {
    	return schema.getDbUserName()+"."+table.getTableName();
    }
}
