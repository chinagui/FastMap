package com.navinfo.dataservice.engine.man.grid;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.GridSelectorApiService;

/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：engine-manGridSelectorApiServiceImpl.java
 * GridSelector 对外暴露的api实现类:所有GridSelector需要对外部项目暴露的接口，需要在这里进行包装实现；
 */
@Service("gridSelector")
public class GridSelectorApiServiceImpl implements GridSelectorApiService {
	
	
	/* 
	 * @see com.navinfo.dataservice.api.edit.iface.GridSelectorApiService#queryRegionGridMapping(java.util.List)
	 */
	@Override
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception {
		return GridService.getInstance().queryRegionGridMapping(gridList);
	}

}

