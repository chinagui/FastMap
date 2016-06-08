package com.navinfo.dataservice.api.man.iface;

/**
 * @author wangshishuai3966
 *
 */
public interface ManApi {
	public int getDailyRegionDbId(int regionId) throws Exception;
	
	public int getMonthlyRegionDbId(int regionId) throws Exception;
}
