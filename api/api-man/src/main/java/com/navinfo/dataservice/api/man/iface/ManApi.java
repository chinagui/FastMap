package com.navinfo.dataservice.api.man.iface;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.RegionMesh;
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
	 * 返回值Map<Integer,Integer> key：taskId，type：1，中线4，快线
	 * 原则：根据子任务id获取对应的任务id以及任务类型（快线/中线），任务类型和子任务类型相同
	 * 应用场景：采集（poi，tips）成果批任务号
	 * @param subtaskId
	 * @return Map<String,Integer> {taskId:12,programType:1} (programType：1，中线4，快线)
	 * @throws Exception
	 */
	public Map<String, Integer> getTaskBySubtaskId(int subtaskId) throws Exception;
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void taskUpdateCmsProgress(int phaseId,int status,String message) throws Exception;
	
	public Region queryByRegionId(Integer regionId) throws Exception ;
	
	public Subtask queryBySubtaskId(Integer subtaskId) throws Exception ;
	
	/**
	 * 根据质检任务号查询常规任务    或者     根据常规子任务号查询质检任务
	 * TASKID 任务号
	 * STAGE 作业阶段	 NUMBER(1) 0采集，1日编，2月编
	 * ISQUALITY	NUMBER(1)	0非质检子任务 1质检子任务
	 * @param qualitySubtaskId
	 * @param stage
	 * @param isQuality
	 * @return
	 * @throws Exception
	 */
	public Subtask queryBySubTaskIdAndIsQuality(Integer taskId,String stage,Integer isQuality) throws Exception ;
	
	/**
	 * 根据用户一级项，userId,查询抽检系数
	 * userId 
	 * firstWorkItem 一级作业项
	 * @param userId
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public int queryQualityLevel(Integer userId,String firstWorkItem) throws Exception ;

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
	public List<RegionMesh> queryRegionWithMeshes(Collection<String> meshes) throws Exception;
	
	public void pushMessage(Message message,Integer push) throws Exception;
	
	public List<Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception;
	
	/**
	 * 
	 * @param subtaskId
	 * @return Map<Integer,Integer> key:gridId value:1规划内2规划外
	 * @throws Exception
	 */
	public Map<Integer,Integer> getGridIdMapBySubtaskId(int subtaskId) throws Exception;
	
	public List<Map<String,Object>> getSubtaskPercentByBlockManId(int blockManId) throws Exception;
	
	//POI月编批状态，改状态，调整范围，发送消息
	//给编辑端提交接口用
	public void close(int subtaskId,long userId) throws Exception;
	
	//改状态，调整范围，发送消息
	//给凯凯写的Job调用:POI月编子任务批状态后
	public void closeSubtask(int subtaskId,long userId) throws Exception;
	
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
	 * 获取grid对应的采集taskid，若为多个返回0
	 * @param grid
	 * @return Map<Integer,Map<String,Integer>>
	 *  key：gridId
	 *  value:
	 *  	key:"quickTaskId"/"centreTaskId"
	 *  	value:taskId
	 * @throws Exception
	 */
	public Map<Integer,Map<String,Integer>> queryCollectTaskIdsByGridIdList(List<Integer> gridIdList) throws Exception;
	
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
	public Set<Integer> getCollectTaskIdByDaySubtask(int subtaskId) throws Exception;
	/**
	 * @param dbId
	 * @param i
	 * @return
	 */
	public Map<Integer, List<Integer>> getSubtaskGridMappingByDbId(int dbId, int type)  throws Exception;
	/**
	 * @param dbId
	 * @param statusList
	 * @param workKind
	 * @return
	 */
	public List<Integer> getSubtaskIdListByDbId(int dbId, List<Integer> statusList, int workKind) throws Exception;
	
	/**
	 * 更具grid获取众包子任务
	 * @param grid
	 * @return
	 * @throws Exception
	 */
	public Subtask queryCrowdSubtaskByGrid(String grid) throws Exception;
	public String getGroupNameBySubtaskId(int subtaskId) throws Exception;
	public int getFinishedRoadNumBySubtaskId(int subtaskId) throws Exception;
}

