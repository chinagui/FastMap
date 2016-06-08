package com.navinfo.dataservice.api.datahub.iface;

import com.navinfo.dataservice.api.datahub.model.DbInfo;

/** 
* @ClassName: DatahubApiService 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:30:06 
* @Description: TODO
*  
*/
public interface DatahubApi {
	public DbInfo getDbById(int dbId)throws Exception;
	public DbInfo getSuperDb(DbInfo db)throws Exception;
	public DbInfo getOnlyDbByType(String bizType)throws Exception;
}
