package com.navinfo.dataservice.datahub.manager;

import com.navinfo.dataservice.api.datahub.iface.DbManagerApiService;
import com.navinfo.dataservice.datahub.model.OracleSchema;

/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：datahubDbManagerApiServiceImpl.java
 */
public class DbManagerApiServiceImpl implements DbManagerApiService {

	@Override
	public Object getOnlyDbByName(String dbName) throws Exception {
		// TODO Auto-generated method stub
		OracleSchema schema = (OracleSchema)new DbManager().getOnlyDbByName(dbName);
		return schema;
	}

}

