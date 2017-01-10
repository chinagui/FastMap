package com.navinfo.dataservice.api.man.iface;

import com.navinfo.dataservice.api.man.model.FmDay2MonSync;

/** 
 * @ClassName: Day2MonthSyncApi
 * @author MaYunFei
 * @date 下午5:35:19
 * @Description: 日落月同步记录信息api
 */
public interface Day2MonthSyncApi {
	public Long insertSyncInfo(FmDay2MonSync info) throws Exception;
	public Integer updateSyncInfo(FmDay2MonSync info) throws Exception;
	public FmDay2MonSync queryLastedSyncInfo(Integer cityId)throws Exception;
}
