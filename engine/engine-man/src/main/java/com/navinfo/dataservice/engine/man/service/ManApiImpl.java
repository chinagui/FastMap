package com.navinfo.dataservice.engine.man.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.dataservice.engine.man.config.ConfigService;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.produce.ProduceService;
import com.navinfo.dataservice.engine.man.region.CpRegionProvinceService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.statics.StaticsService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
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
	public Map getCityById(Integer cityId)throws Exception{
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

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.man.iface.ManApi#getCollectTaskIdByDaySubtask(int)
	 */
	@Override
	public Set<Integer> getCollectTaskIdByDaySubtask(int subtaskId) throws ServiceException {
		Set<Integer> taskIdSet = SubtaskService.getInstance().getCollectTaskIdByDaySubtask(subtaskId);
		return taskIdSet;
	}
	
}

