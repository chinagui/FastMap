package com.navinfo.dataservice.engine.man.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.engine.man.grid.GridService;
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
	public Region queryByRegionId(Integer regionId) throws Exception {
		Region region = new Region();
		region.setRegionId(regionId);
		return new RegionService().query(region);
		
	}
	@Override
	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList)
			throws Exception {
		return GridService.getInstance().queryRegionGridMappingOfSubtasks(taskList);
	}
	@Override
	public Set<Integer> queryGrid(int limit) throws Exception {
		return GridService.getInstance().queryGrid(limit);
	}
	
	@Override
	public List<Grid> listGrids() throws Exception {
		return GridService.getInstance().list();
	}
	@Override
	public List<Region> queryRegionList() throws Exception {
		return new RegionService().list();
	}
	
}

