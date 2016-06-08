package com.navinfo.dataservice.diff.dataaccess;

import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.OracleSchema;

import java.util.List;

/**
 * 
 * 本地访问，和差分服务器是一个用户，因为差分服务器必须是oracle，所有使用本地访问的必须是oracle的schema
 * @version $Id:Exp$
 * @since 12-3-9 上午9:58
 */
public class LocalDataAccess implements DataAccess
{
	protected OracleSchema schema;
	public LocalDataAccess(OracleSchema schema){
		this.schema=schema;
	}

    public OracleSchema getSchema() {
		return schema;
	}

	public void setSchema(OracleSchema schema) {
		this.schema = schema;
	}

	@Override
    public String accessTable(GlmTable table)
    {
        return table.getName();
    }
}
