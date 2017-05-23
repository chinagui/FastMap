package com.navinfo.dataservice.engine.statics.job;

import java.util.List;
import java.util.concurrent.Callable;

import com.navinfo.dataservice.engine.statics.job.model.PoiDailyDbObj;

public class SubPoiDailyDb implements Callable<List<PoiDailyDbObj>> {
	
	public SubPoiDailyDb(int dbId,String dbName,String statTime){
		
	}

	@Override
	public List<PoiDailyDbObj> call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
