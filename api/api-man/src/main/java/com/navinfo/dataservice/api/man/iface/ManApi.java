package com.navinfo.dataservice.api.man.iface;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.man.model.IRegion;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	/**
	 * @param gridList  <br/>
	 * <b>注意：如果参数gridList太长(不能超过1000)，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；key:RegionId；value：grid列表<br/>
	 * @throws Exception 
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception;
	/**
	 * @param taskList subTaskId的列表
	 * <b>注意：如果参数taskList太长（不能超过1000个），会导致oracle sql太长而出现异常；</b>
	 * @return MultiValueMap key是regionId，value是大区中满足条件的grid的列表
	 * @throws Exception
	 */
	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception;
	public IRegion queryByRegionId(Integer regionId) throws Exception ;
}

