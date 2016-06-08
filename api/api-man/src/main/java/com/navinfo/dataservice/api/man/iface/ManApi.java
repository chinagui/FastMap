package com.navinfo.dataservice.api.man.iface;

import java.util.List;


import java.util.Map;

/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception;
}

