package com.navinfo.dataservice.engine.man.service;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionDbInfo;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.region.Region;
import com.navinfo.dataservice.engine.man.region.RegionService;
/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：engine-manGridSelectorApiServiceImpl.java
 * GridSelector 对外暴露的api实现类:所有GridSelector需要对外部项目暴露的接口，需要在这里进行包装实现；
 */
@Service("manApi")
public class ManApiImpl implements ManApi {
	
	/* 
	 * @see com.navinfo.dataservice.api.edit.iface.GridSelectorApiService#queryRegionGridMapping(java.util.List)
	 */
	@Override
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception {
		return GridService.getInstance().queryRegionGridMapping(gridList);
	}
	@Override
	public RegionDbInfo queryByRegionId(Integer regionId) throws Exception {
		Region region =  new RegionService().query(JSONObject.fromObject("{\"regionId\":"+regionId+"}"));
		if (region==null) return null;
		RegionDbInfo regionInfo = new RegionDbInfo();
		regionInfo.setDailyDbId(region.getDailyDbId());
		regionInfo.setMonthlyDbId(region.getMonthlyDbId());
		regionInfo.setRegionId(regionId);
		return regionInfo;
	}
}

