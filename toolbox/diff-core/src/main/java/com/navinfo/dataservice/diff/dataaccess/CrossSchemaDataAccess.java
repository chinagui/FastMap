package com.navinfo.dataservice.diff.dataaccess;

import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.OracleSchema;

import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 上午9:56
 */
public class CrossSchemaDataAccess implements DataAccess
{
    private OracleSchema schema;

    public OracleSchema getSchema() {
		return schema;
	}

	public void setSchema(OracleSchema schema) {
		this.schema = schema;
	}

	public CrossSchemaDataAccess(OracleSchema schema)
    {
        this.schema= schema;
    }

    @Override
    public String accessTable(GlmTable table)
    {
    	return schema.getConnConfig().getUserName()+"."+table.getName();
    }
}
