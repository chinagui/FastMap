package com.navinfo.dataservice.api.man.iface;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;

import net.sf.json.JSONObject;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	
	public Region queryByRegionId(Integer regionId) throws Exception ;
	
	public Subtask queryBySubtaskId(Integer subtaskId) throws Exception ;
	
//	List<Grid> listGrids()throws Exception;
	/**
	
	 * @param taskList subTaskId的列表
	List<Region> listRegions()throws Exception;
	 * <b>注意：如果参数taskList太长（不能超过1000个），会导致oracle sql太长而出现异常；</b>
	 * @return MultiValueMap key是regionId，value是大区中满足条件的grid的列表
	 * @throws Exception
	 */
//	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception;
//	public Set<Integer> queryGrid(int limit) throws Exception;
	public List<Region> queryRegionList() throws Exception;
	Region queryRegionByDbId(int dbId)throws Exception;
	
	public int queryAdminIdBySubtask(int subtaskId) throws Exception;
	
	public int queryDbIdByAdminId(int adminId) throws Exception;
	public int queryCityIdByTaskId(int taskId) throws Exception;

	public String querySpecVersionByType(int type) throws Exception;
	
	public List<Region> queryRegionWithGrids(List<Integer> grids) throws Exception;
	
	public void pushMessage(Message message,Integer push) throws Exception;
	
	public List<Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception;
	
	public List<Map<String,Object>> getSubtaskPercentByBlockManId(int blockManId) throws Exception;
	
	public void close(int subtaskId) throws Exception;
	
	public void updateProduceStatus(int produceId,int status) throws Exception;
	
	public List<Task> queryTaskAll() throws Exception;
	
	public Map<String, Object> queryTaskStatByTaskId(long taskId) throws Exception;
	
	public Map<String, Object> getUserInfoByUserId(long userId) throws Exception;
	
	public int createJob(long userId,String produceType, JSONObject paraJson) throws Exception;

	public String queryConfValueByConfKey(String confKey) throws Exception;
	
}

