package com.navinfo.dataservice.api.man.iface;

/**
 * @author wangshishuai3966
 *
 */
public interface ManApi {
	public int getDailyDbByRegion(int regionId) throws Exception;
	
	public int getMonthlyDbByRegion(int regionId) throws Exception;
	
}
