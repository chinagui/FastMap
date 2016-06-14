package com.navinfo.dataservice.api.man.iface;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Region;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception;
	public Region queryByRegionId(Integer regionId) throws Exception ;
	
	List<Grid> listGrids()throws Exception;
}

