package com.navinfo.dataservice.engine.man.service;

import java.sql.Connection;
import java.util.*;

import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import net.sf.json.JSONArray;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.dataservice.engine.man.config.ConfigService;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.produce.ProduceService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.region.CpRegionProvinceService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.statics.StaticsOperation;
import com.navinfo.dataservice.engine.man.statics.StaticsService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskProgressOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.timeline.TimelineService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.dataservice.engine.man.version.VersionService;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;
/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：engine-manGridSelectorApiServiceImpl.java
 * GridSelector 对外暴露的api实现类:所有GridSelector需要对外部项目暴露的接口，需要在这里进行包装实现；
 */
@Service("manApi")
public class ManApiImpl implements ManApi {
	private Logger log = LoggerRepos.getLogger(ManApiImpl.class);
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	@Override
	public void taskUpdateCmsProgress(int phaseId,int status,String message) throws Exception {
		TaskService.getInstance().taskUpdateCmsProgress(phaseId, status,message);
	}

	/**
	 * 更新job步骤的执行状态, 如果是成功和无数据，继续执行job
	 * @param phaseId
	 * @param status 2成功，3失败，4无数据
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void updateJobProgress(long phaseId,int status,String message) throws Exception {
		JobService.getInstance().updateJobProgress(phaseId, JobProgressStatus.valueOf(status), message);
	}
	
	@Override
	public Region queryByRegionId(Integer regionId) throws Exception {
		Region region = new Region();
		region.setRegionId(regionId);
		return RegionService.getInstance().query(region);
		
	}
//	@Override
//	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList)
//			throws Exception {
//		return GridService.getInstance().queryRegionGridMappingOfSubtasks(taskList);
//	}
//	@Override
//	public Set<Integer> queryGrid(int limit) throws Exception {
//		return GridService.getInstance().queryGrid(limit);
//	}
//	
//	@Override
//	public List<Grid> listGrids() throws Exception {
//		return GridService.getInstance().list();
//	}
	@Override
	public List<Region> queryRegionList() throws Exception {
		return RegionService.getInstance().list();
	}
	@Override
	public Region queryRegionByDbId(int dbId) throws Exception {
		return RegionService.getInstance().queryByDbId(dbId);
	}
	@Override
	public Subtask queryBySubtaskId(Integer subtaskId)
			throws Exception {
		// TODO Auto-generated method stub
		return SubtaskService.getInstance().queryBySubtaskIdS(subtaskId);
	}
	@Override
	public int queryAdminIdBySubtask(int subtaskId) throws Exception {
		return SubtaskService.getInstance().queryAdminIdBySubtask(subtaskId);
	}
	@Override
	public int queryDbIdByAdminId(int adminId) throws Exception {
		return RegionService.getInstance().queryDbIdByAdminId(adminId);
	}
	@Override
	public String querySpecVersionByType(int type) throws Exception {
		VersionService service = new VersionService();
		
		return service.query(type);
	}
	@Override
	public List<Region> queryRegionWithGrids(List<Integer> grids) throws Exception {
		return RegionService.getInstance().queryRegionWithGrids(grids);
	}
	@Override
	public List<RegionMesh> queryRegionWithMeshes(Collection<String> meshes) throws Exception{
		return RegionService.getInstance().queryRegionWithMeshes(meshes);
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#queryCityIdByTaskId(int)
	 */
	@Override
	public int queryCityIdByTaskId(int taskId) throws Exception {
		// TODO Auto-generated method stub
		return CityService.getInstance().queryCityIdByTaskId(taskId);
	}
	@Override
	public void pushMessage(Message message,Integer push) throws Exception{
		MessageService.getInstance().push(message, push);
	}
	@Override
	public List<Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		return SubtaskOperation.getGridIdListBySubtaskId(subtaskId);
	}
	/**
	 * 
	 * @param subtaskId
	 * @return Map<Integer,Integer> key:gridId value:1规划内2规划外
	 * @throws Exception
	 */
	@Override
	public Map<Integer,Integer> getGridIdMapBySubtaskId(int subtaskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			Map<Integer,Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskIdWithConn(conn, subtaskId);
			return gridIds;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	@Override
	public void close(int subtaskId,long userId) throws Exception {
		// TODO Auto-generated method stub
//		SubtaskOperation.closeBySubtaskId(subtaskId);
		SubtaskService.getInstance().close(subtaskId, userId);
	}
	@Override
	public void updateProduceStatus(int produceId,int status) throws Exception {
		// TODO Auto-generated method stub
		ProduceService.getInstance().updateProduceStatus(produceId,status);
	}
	/* 
	 * 根据blockmanid 查询出所有相关的子任务
	 */
	@Override
	public List<Map<String,Object>> getSubtaskPercentByBlockManId(int blockManId) throws Exception {
		
		return BlockOperation.getSubtaskPercentByBlockManId(blockManId);
	}
	@Override
	public List<Task> queryTaskAll() throws Exception {
		// TODO Auto-generated method stub
		return TaskService.getInstance().queryTaskAll();
	}
	@Override
	public Map<String, Object> queryTaskStatByTaskId(long taskId) throws Exception {
		// TODO Auto-generated method stub
		return StaticsService.getInstance().queryTaskStatByTaskId(taskId);
	}
	@Override
	public UserInfo getUserInfoByUserId(long userId) throws Exception {
		// TODO Auto-generated method stub
		return UserInfoService.getInstance().getUserInfoByUserId(userId);
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#createJob(long, java.lang.String, net.sf.json.JSONObject)
	 */
	@Override
	public int createJob(long userId, String produceType, JSONObject paraJson) throws Exception {
		// TODO Auto-generated method stub
		return ProduceService.getInstance().create(userId,produceType,paraJson,0);
	}
	
	@Override
	public String queryConfValueByConfKey(String confKey) throws Exception {
		// TODO Auto-generated method stub
		return ConfigService.getInstance().query(confKey);
	}
	@Override
	public List<Map<String, Object>> queryDay2MonthList(JSONObject conditionJson)
			throws Exception {
		return Day2MonthService.getInstance().list(conditionJson);
	}
	
	@Override
	public List<CpRegionProvince> listCpRegionProvince() throws Exception {

		return CpRegionProvinceService.getInstance().list();
	}
	
	@Override
	public Map<Integer,Integer> listDayDbIdsByAdminId()throws Exception{
		return CpRegionProvinceService.getInstance().listDayDbIdsByAdminId();
	}
	@Override
	public Map<String,Object> getCityById(Integer cityId)throws Exception{
		JSONObject json = new JSONObject().element("cityId", cityId);
		return CityService.getInstance().query(json );
	}
	@Override
	public List<Integer> queryGridOfCity(Integer cityId) throws Exception {
		JSONObject condition = new JSONObject().element("cityId", cityId);
		return GridService.getInstance().queryListByCondition(condition);
	}
	/**
	 * 获取grid对应的taskid，若为多个返回0
	 * @param grid
	 * @return Map<String,Integer> key："quickTaskId"，"centreTaskId"
	 * @throws Exception
	 */
	@Override
	public Map<String,Integer> queryTaskIdsByGrid(String grid) throws Exception {
		return GridService.getInstance().queryTaskIdsByGrid(grid);
	}	
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
	@Override
	public List<Map<String, Object>> getProduceProgram() throws Exception {
		// TODO Auto-generated method stub
		return ProduceService.getInstance().getProduceProgram();
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#queryTaskIdsByGridIdList(java.util.List)
	 */
	@Override
	public Map<Integer,Map<String,Integer>> queryCollectTaskIdsByGridIdList(List<Integer> gridIdList) throws Exception {
		// TODO Auto-generated method stub
		return GridService.getInstance().queryCollectTaskIdsByGridIdList(gridIdList);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#closeSubtaskStatus(int, int)
	 */
	@Override
	public void closeSubtask(int subtaskId, long userId) throws Exception {
		SubtaskService.getInstance().closeSubtask(subtaskId,userId);
		
	}
	/**
	 * 返回值Map<Integer,Integer> key：taskId，type：1，中线4，快线
	 * 原则：根据子任务id获取对应的任务id以及任务类型（快线/中线），任务类型和子任务类型相同
	 * 应用场景：采集（poi，tips）成果批任务号
	 * @param subtaskId
	 * @return Map<String,Integer> {taskId:12,programType:1} (programType：1，中线4，快线)
	 * @throws Exception
	 */
	@Override
	public Map<String, Integer> getTaskBySubtaskId(int subtaskId)
			throws Exception {
		return SubtaskService.getInstance().getTaskBySubtaskId(subtaskId);
	}

	
	@Override
	public Set<Integer> getCollectTaskIdByDaySubtask(int subtaskId) throws ServiceException {
		Set<Integer> taskIdSet = SubtaskService.getInstance().getCollectTaskIdByDaySubtask(subtaskId);
		return taskIdSet;
	}

	@Override
	public Set<Integer> getCollectTaskIdByDayTask(int taskId) throws ServiceException {
		Set<Integer> taskIdSet = SubtaskService.getInstance().getCollectTaskIdByDayTask(taskId);
		return taskIdSet;
	}

	@Override
	public Map<Integer, List<Integer>> getOpendMultiSubtaskGridMappingByDbId(int dbId, int type) throws Exception {
		// TODO Auto-generated method stub
		return SubtaskService.getInstance().getOpendMultiSubtaskGridMappingByDbId(dbId,type);
	}

	@Override
	public List<Integer> getSubtaskIdListByDbId(int dbId, List<Integer> statusList, int workKind) throws Exception {
		return SubtaskService.getInstance().getSubtaskIdListByDbId(dbId,statusList,workKind);
	}

	@Override
	public Subtask queryBySubTaskIdAndIsQuality(Integer taskId, String stage, Integer isQuality) throws Exception {
		return SubtaskService.getInstance().queryBySubTaskIdAndIsQuality(taskId, stage, isQuality);
	}
	
	@Override
	public int queryQualityLevel(Integer userId,String firstWorkItem) throws Exception {
		return UserInfoService.getInstance().queryQualityLevel(userId,firstWorkItem);
	}
	
	@Override
	public Subtask queryCrowdSubtaskByGrid(String grid) throws Exception{
		return SubtaskService.getInstance().queryCrowdSubtaskByGrid(grid);
	}
	
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
	@Override
	public Map<String, String> getCommonSubtaskByQualitySubtask(int qualitySubtaskId) throws Exception {
		// TODO Auto-generated method stub
		return SubtaskService.getInstance().getCommonSubtaskByQualitySubtask(qualitySubtaskId);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#getProvinceRegionIdMap()
	 */
	@Override
	public Map<String, Integer> getProvinceRegionIdMap() throws Exception {
		// TODO Auto-generated method stub
		return CpRegionProvinceService.getInstance().getProvinceRegionIdMap();
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#listDayDbIds()
	 */
	@Override
	public List<Integer> listDayDbIds() throws Exception {
		// TODO Auto-generated method stub
		return RegionService.getInstance().listDayDbIds();
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#getUsers()
	 */
	@Override
	public Map<Integer, String> getUsers() throws Exception {
		// TODO Auto-generated method stub
		return UserInfoService.getInstance().getUsers();
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#getsubtaskUserMap()
	 */
	@Override
	public Map<Integer, Integer> getsubtaskUserMap() throws Exception {
		// TODO Auto-generated method stub
		return SubtaskService.getInstance().getsubtaskUserMap();
	}

    @Override
    public JSONArray getGridIdsByTaskId(int taskId) throws Exception {
        Connection conn = null;
        try{
            conn = DBConnector.getInstance().getManConnection();
            return TaskService.getInstance().getGridListByTaskId(conn, taskId);
        }catch(Exception e){
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
        }finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

	@Override
	public JSONArray getAdminCodeAndProvince() throws Exception {
		return CpRegionProvinceService.getInstance().getAdminCodeAndProvince();
	}

	/**
	 * 修改task_progress的值，并发送socket
	 */
	@Override
	public void endProgressAndSocket(int phaseId, int status, String message) throws Exception {
		Connection conn=null;
		try {
			conn=DBConnector.getInstance().getManConnection();
			TaskProgressOperation.endProgressAndSocket(conn, phaseId, status,message);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 子任务对应任务基地名，子任务省、市、对应的常规子任务作业员、子任务质检方式，当前版本
	 * key：groupName,province,city,userId,version
	 * 应用场景：（采集端）道路外业质检上传获取子任务相关信息
	 * @param qualitySubtaskId 质检子任务号
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> getSubtaskInfoByQuality(int qualitySubtaskId) throws Exception {
		// TODO Auto-generated method stub
		return SubtaskService.getInstance().getSubtaskInfoByQuality(qualitySubtaskId);
	}

	/**
	 * 根据子任务Id查询同项目下的区域粗编子任务列表
	 * @param int subTaskId
	 * @throws Exception
	 * 
	 * */
	@Override
	public List<Integer> queryRudeSubTaskBySubTask(int subTaskId) throws Exception {
		return ProgramService.getInstance().queryRudeSubTaskBySubTask(subTaskId);
	}
	
	/**
	 * 查询MAN_TIMELINE
	 * objName:program,task,subtask,infor
	 * @return	Map<Long,Map<String, Object>> key:objId
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String, Object>> queryManTimelineByObjName(String objName,int operateType) throws Exception{
		return TimelineService.queryManTimelineByObjName(objName,operateType);
	}
	
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
	@Override
	public List<Map<String, Object>> staticsPersionJob(String timestamp) throws Exception{
		return StaticsOperation.staticsPersionJob(timestamp);
	}
	
	/**
	 * 查询task的grids
	 * @author Han Shaoming
	 * @return	Set<Integer>  grids
	 * @throws ExceptionSELECT TASK_ID FROM TASK
	 */
	public Map<Integer, Integer> queryGridIdsByTaskId(int taskId) throws Exception{
		return TaskService.getInstance().getGridMapByTaskId(taskId);
	}
	
	/**
	 * 查询subtask详细信息
	 * @author Han Shaoming
	 * @return	List<Map<String,Object>> map key:fieldName,value:相应的值
	 * @throws Exception
	 */
	public List<Map<String,Object>> querySubtaskByTaskId(int taskId) throws Exception{
		return TaskService.getInstance().querySubtaskByTaskId(taskId);
	}
	
	/**
	 * 查询task对应的项目类型
	 * @author Han Shaoming
	 * @return	Map<Integer,Integer> key:taskId,value:programType 项目类型。1常规(中线)4快速更新(快线)9 虚拟项目
	 * @throws Exception
	 */
	public Map<Integer,Integer> queryProgramTypes() throws Exception{
		return TaskService.getInstance().queryProgramTypes();
	}

    /**
     * 根据OBJ_ID,OBJ_TYPE,OPERATE_TYPE查询MAN_TIMELINE
     * OBJ_TYPE:program,task,subtask,infor
     * @return	Map<Long,Map<String, Object>> key:objId
     * @throws ServiceException
     */
    public  Map<String, Object> queryTimelineByCondition(int objId,
                                                                     String objType, int operateType) throws Exception{
        return TimelineService.queryTimelineByCondition(objId, objType, operateType);
    }

    /**
     * 保存timeline
     * @param objID
     * @param objName
     * @param objType
     * @param operateDate
     * @throws Exception
     */
    public void saveTimeline(int objID, String objName, int objType, String operateDate) throws Exception {
        TimelineService.saveTimeline(objID, objName, objType, operateDate);
    }
    
    /**
     * 获取所有采集子任务的集合
     * @throws Exception
     */
    public Set<Integer> allCollectSubtaskId() throws Exception{
    	return SubtaskService.getInstance().allCollectSubtaskId();
    }
    
    /**
     * 根据taskId获取对应任务的tips转aumark数量
     * @throws Exception 
     * 
     * */
    public Map<Integer, Integer> getTips2MarkNumByTaskId() throws Exception{
    	return TaskService.getInstance().getTips2MarkNumByTaskId();
    }
    
	/**
	 * 查询所有city下的所有block对应的grid集合
	 * @return Map<Integer,Map<Integer, Set<Integer>>>>
	 * @throws Exception 
	 * 
	 * */
    public Map<Integer, Map<Integer, Set<Integer>>> queryAllCityGrids() throws Exception{
    	return CityService.getInstance().queryAllCityGrids();
    }
    
    /**
     * 查询所有项目统计相关信息
     * @throws Exception 
     * 
     * */
    public List<Map<String, Object>> queryProgramStat() throws Exception{
    	return ProgramService.getInstance().queryProgramStat();
    }
}