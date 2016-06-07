package com.navinfo.dataservice.datahub.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;

/** 
* @ClassName: DatahubApiImpl 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午5:25:55 
* @Description: TODO
*  
*/
@Service("datahubApi")
public class DatahubApiImpl implements DatahubApi {

	@Override
	public DbInfo getSuperDb(DbInfo db) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbInfo getOnlyDbByType(String bizType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
