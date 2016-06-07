package com.navinfo.dataservice.api.edit.iface;

import java.util.List;


import java.util.Map;

import org.springframework.stereotype.Service;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface GridSelectorApiService{
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception;
}

