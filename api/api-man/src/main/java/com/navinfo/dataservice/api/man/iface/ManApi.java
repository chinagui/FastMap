package com.navinfo.dataservice.api.man.iface;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;

import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;


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
	public MultiValueMap queryRegionGridMapping(List<Integer> gridList) throws Exception;
	
	public Region queryByRegionId(Integer regionId) throws Exception ;
	
	public Subtask queryBySubtaskId(Integer subtaskId) throws Exception ;
	
	List<Grid> listGrids()throws Exception;
	/**
	
	 * @param taskList subTaskId的列表
	List<Region> listRegions()throws Exception;
	 * <b>注意：如果参数taskList太长（不能超过1000个），会导致oracle sql太长而出现异常；</b>
	 * @return MultiValueMap key是regionId，value是大区中满足条件的grid的列表
	 * @throws Exception
	 */
	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception;
	public Set<Integer> queryGrid(int limit) throws Exception;
	public List<Region> queryRegionList() throws Exception;
	Region queryRegionByDbId(int dbId)throws Exception;
	
	public int queryAdminIdBySubtask(int subtaskId) throws Exception;
}

