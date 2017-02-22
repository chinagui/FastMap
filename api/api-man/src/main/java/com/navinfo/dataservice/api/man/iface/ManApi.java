package com.navinfo.dataservice.api.man.iface;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserInfo;

import net.sf.json.JSONObject;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void taskUpdateCmsProgress(int phaseId,int status) throws Exception;
	
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
	
	public UserInfo getUserInfoByUserId(long userId) throws Exception;
	
	public int createJob(long userId,String produceType, JSONObject paraJson) throws Exception;

	public String queryConfValueByConfKey(String confKey) throws Exception;
	
	public List<Map<String, Object>> queryDay2MonthList(JSONObject conditionJson) throws Exception;
	
	public List<CpRegionProvince> listCpRegionProvince()throws Exception;
	
	public Map<Integer,Integer> listDayDbIdsByAdminId()throws Exception;

	/**
	 * @param cityId 城市代码
	 * @return 对应的城市信息字段map<br/>
	 * keys包含 cityId，cityName、provinceName、geometry、regionId、planStatus
	 * @throws Exception
	 */
	public Map<String,Object> getCityById(Integer cityId) throws Exception;
	/**
	 * @param cityId 
	 * @return 城市对应的grid的list
	 * @throws Exception
	 */
	public List<Integer> queryGridOfCity(Integer cityId) throws Exception;
	
	/**
	 * 获取grid对应的taskid，若为多个返回0
	 * @param grid
	 * @return Map<String,Integer> key："quickTaskId"，"centreTaskId"
	 * @throws Exception
	 */
	public Map<String,Integer> queryTaskIdsByGrid(String grid) throws Exception;
	/**
	 * 获取待出品的情报项目list
	 * 应用场景：定时日出品（一体化）脚本
	 * @return List<Map<String, Object>>：Map<String, Object> 
	 * 			key:"produceId":int,
	 * 				"programId":int,
	 * 				"gridIds":Map<Integer, Set<Integer>> key:dbId value:grid的集合
	 * 例如：{"produceId":1,"programId":1,"gridIds":{17:[59567301,59567302],18:[59567801]}}
	 * @throws Exception
	 */
	public List<Map<String, Object>> getProduceProgram() throws Exception;
	
}

