package com.navinfo.dataservice.datahub.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

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
	public DbInfo getDbById(int dbId)throws Exception{
		return DbService.getInstance().getDbById(dbId);
	}
	@Override
	public DbInfo getSuperDb(DbInfo db) throws Exception {
		return DbService.getInstance().getSuperDb(db);
	}

	@Override
	public DbInfo getOnlyDbByType(String bizType) throws Exception {
		return DbService.getInstance().getOnlyDbByType(bizType);
	}
	@Override
	public DbInfo getDailyDbByRegionId(int regionId) throws Exception {
		ManApi man = (ManApi)ApplicationContextUtil.getBean("manApi");
		int dbId = man.getDailyRegionDbId(regionId);
		return DbService.getInstance().getDbById(dbId);
	}
	@Override
	public DbInfo getMonthlyDbByRegionId(int regionId) throws Exception {
		ManApi man = (ManApi)ApplicationContextUtil.getBean("manApi");
		int dbId = man.getMonthlyRegionDbId(regionId);
		return DbService.getInstance().getDbById(dbId);
	}

}
