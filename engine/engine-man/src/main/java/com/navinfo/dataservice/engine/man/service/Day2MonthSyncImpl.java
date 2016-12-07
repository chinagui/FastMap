package com.navinfo.dataservice.engine.man.service;

import com.navinfo.dataservice.api.man.iface.Day2MonthSyncApi;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthSyncService;

public class Day2MonthSyncImpl implements Day2MonthSyncApi {

	@Override
	public Long insertSyncInfo(FmDay2MonSync info) throws Exception {
		return Day2MonthSyncService.getInstance().insertSyncInfo(info);
	}

	@Override
	public Integer updateSyncInfo(FmDay2MonSync info) throws Exception {
		return Day2MonthSyncService.getInstance().updateSyncInfo(info);
	}

	@Override
	public FmDay2MonSync queryLastedSyncInfo(Integer cityId) throws Exception {
		return Day2MonthSyncService.getInstance().queryLastedSyncInfo(cityId);
	}

}
