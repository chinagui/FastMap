package com.navinfo.dataservice.api.edit.iface;

import java.util.List;


import java.util.Map;

/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface GridServiceApi{
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception;
}

