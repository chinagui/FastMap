package com.navinfo.dataservice.diff.dataaccess;

import com.navinfo.dataservice.datahub.glm.GlmTable;
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
    	return schema.getDbUserName()+"."+table.getName();
    }
}
