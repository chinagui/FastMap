package com.navinfo.dataservice.engine.man.impl;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.engine.man.region.Region;
import com.navinfo.dataservice.engine.man.region.RegionService;

@Service("manApi")
public class ManApiImpl implements ManApi{

	@Override
	public int getDailyDbByRegion(int regionId) throws Exception {
		
		JSONObject json = new JSONObject();
		
		json.put("regionId", regionId);
		
		RegionService service = new RegionService();
		
		Region region = service.query(json);
		
		return region.getDailyDbId();
	}

	@Override
	public int getMonthlyDbByRegion(int regionId) throws Exception {
		JSONObject json = new JSONObject();
		
		json.put("regionId", regionId);
		
		RegionService service = new RegionService();
		
		Region region = service.query(json);
		
		return region.getMonthlyDbId();
	}

}
