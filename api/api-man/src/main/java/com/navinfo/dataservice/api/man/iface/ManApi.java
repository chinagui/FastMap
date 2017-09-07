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
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：apiGridSelectorExternalService.java
 */
public interface ManApi{
	/**
	 * 修改task_progress的值，并发送socket
	 * @throws Exception
	 */
	public void endProgressAndSocket(int phaseId,int status,String message) throws Exception;
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

	/**
	 * 更新job步骤的执行状态
	 * @param phaseId
	 * @param status 2成功，3失败，4无数据
	 * @param message
	 * @throws Exception
	 */
	public void updateJobProgress(long phaseId,int status,String message) throws Exception;
	
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
	public Set<Integer> getCollectTaskIdByDayTask(int taskId) throws Exception;
	/**
	 * 获取开启的多源子任务与grid的map
	 * @param dbId 所在大区库
	 * @param type 1常规4快速更新
	 * @return
	 */
	public Map<Integer, List<Integer>> getOpendMultiSubtaskGridMappingByDbId(int dbId, int type)  throws Exception;
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
	/**
	 * 通过质检子任务id获取常规子任务相关信息。用于编辑过程中tips质检子任务
	 * @param qualitySubtaskId
	 * @return Map<String, String> returnMap=new HashMap<String, String>();
						returnMap.put("subtaskId", rs.getString("SUBTASK_ID"));
						returnMap.put("exeUserId", rs.getString("EXE_USER_ID"));
						returnMap.put("exeUserName", rs.getString("USER_REAL_NAME"));
						returnMap.put("groupId", rs.getString("GROUP_ID"));
						returnMap.put("groupName", rs.getString("GROUP_NAME"));
						returnMap.put("finishedRoad", rs.getString("FINISHED_ROAD"));
						returnMap.put("subtaskName", rs.getString("SUBTASK_NAME"));
						returnMap.put("taskName", rs.getString("TASK_NAME"));
	 * @throws Exception 
	 */
	public Map<String, String> getCommonSubtaskByQualitySubtask(int qualitySubtaskId)
			throws Exception;
	/**
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Integer> getProvinceRegionIdMap() throws Exception;
	/**
	 * @return
	 */
	public List<Integer> listDayDbIds() throws Exception;
	/**
	 * @return
	 * @throws Exception 
	 */
	public Map<Integer, String> getUsers() throws Exception;
	/**
	 * @return
	 * @throws Exception 
	 */
	public Map<Integer, Integer> getsubtaskUserMap() throws Exception;
	
	/**
	 * 子任务对应任务基地名，子任务省、市、对应的常规子任务作业员、子任务质检方式，当前版本
	 * key：groupName,province,city,userId,version
	 * 应用场景：（采集端）道路外业质检上传获取子任务相关信息
	 * @param qualitySubtaskId 质检子任务号
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSubtaskInfoByQuality(int qualitySubtaskId) throws Exception;

    public JSONArray getGridIdsByTaskId(int taskId) throws Exception;
    
    public JSONArray getAdminCodeAndProvince() throws Exception;
    
    /**
     * 获取子任务下同项目的区域粗编子任务Id列表
     * @param int subTaskId
     * @throws Exception
     * 
     * */
    public List<Integer> queryRudeSubTaskBySubTask(int subTaskId) throws Exception;
    
    /**
	 * 查询MAN_TIMELINE
	 * objName:program,task,subtask,infor
	 * @return	Map<Long,Map<String, Object>> key:objId
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String, Object>> queryManTimelineByObjName(String objName,int operateType) throws Exception;
	
	/**
	 * timestamp:yyyymmdd
	 * 获取按照人天任务进行统计的管理列表
	 * @return Map<String, Object>:	map.put("subtaskIds", subtaskSet);
									map.put("userId", userId);
									map.put("taskId", taskId);
									map.put("taskName", rs.getString("TASK_NAME"));
									map.put("cityName", rs.getString("CITY_NAME"));
									map.put("leaderName", rs.getString("LEADER_NAME"));
									map.put("userName", rs.getString("USER_NAME"));	
	 * @throws Exception
	 */
	public List<Map<String,Object>> staticsPersionJob(String timestamp) throws Exception;
	
	/**
	 * 查询task的grids
	 * @author Han Shaoming
	 * @return	Set<Integer>  grids
	 * @throws Exception
	 */
	public Map<Integer, Integer> queryGridIdsByTaskId(int taskId) throws Exception;
	
	/**
	 * 查询subtask详细信息
	 * @author Han Shaoming
	 * @return	List<Map<String,Object>> map key:fieldName,value:相应的值
	 * @throws Exception
	 */
	public List<Map<String,Object>> querySubtaskByTaskId(int taskId) throws Exception;
	
	/**
	 * 查询task对应的项目类型
	 * @author Han Shaoming
	 * @return	Map<Integer,Integer> key:taskId,value:programType 项目类型。1常规(中线)4快速更新(快线)9 虚拟项目
	 * @throws Exception
	 */
	public Map<Integer,Integer> queryProgramTypes() throws Exception;

	/**
	 * 根据OBJ_ID,OBJ_TYPE,OPERATE_TYPE查询MAN_TIMELINE
	 * OBJ_TYPE:program,task,subtask,infor
	 * @return	Map<Long,Map<String, Object>> key:objId
	 * @throws ServiceException
	 */
	public Map<String, Object> queryTimelineByCondition(int objId,
														String objType, int operateType) throws Exception;

    /**
     * 保存timeline
     * @param objectID
     * @param name
     * @param type
     * @param operateDate
     * @throws Exception
     */
    public void saveTimeline(int objectID, String name, int type, String operateDate) throws Exception;
    
    /**
     * 获取所有采集子任务的集合
     * @throws Exception
     */
    public Set<Integer> allCollectSubtaskId() throws Exception;
    
    /**
     * 根据taskId获取对应任务的tips转aumark数量
     * @throws Exception 
     * 
     * */
    public Map<Integer, Integer> getTips2MarkNumByTaskId() throws Exception;
    
	/**
	 * 查询所有city下的所有block对应的grid集合
	 * @return Map<Integer,Map<Integer, Set<Integer>>>>
	 * @throws Exception 
	 * 
	 * */
    public Map<Integer, Map<Integer, Set<Integer>>> queryAllCityGrids() throws Exception;
    
    /**
	 * 区县统计api，主要是为区县统计脚本提供初始查询结果，blockJob用
	 * @return
	 * @throws Exception
	 */
	public Map<Integer,Map<String, Object>> blockStatic()throws Exception;
    
    /**
     * 查询所有项目统计相关信息
     * @throws Exception 
     * 
     * */
    public List<Map<String, Object>> queryProgramStat() throws Exception;
    
    /**
	 * 城市统计api，主要是为城市统计脚本提供初始查询结果，cityJob用
	 * @return
	 * @throws Exception
	 */
	public Map<Integer,Map<String, Object>> cityStatic()throws Exception;
	
    /**
     * 查询已经分配子任务的任务集合
     * @throws Exception 
     * 
     * */
    public Set<Integer> queryTasksHasSubtask() throws Exception;
}

