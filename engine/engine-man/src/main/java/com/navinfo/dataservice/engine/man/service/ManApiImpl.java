package com.navinfo.dataservice.engine.man.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserInfo;
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

import net.sf.json.JSONObject;
/*
 * @author mayunfei
 * 2016年6月7日
 * 描述：engine-manGridSelectorApiServiceImpl.java
 * GridSelector 对外暴露的api实现类:所有GridSelector需要对外部项目暴露的接口，需要在这里进行包装实现；
 */
@Service("manApi")
public class ManApiImpl implements ManApi {
	
	
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
	@Override
	public void close(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		SubtaskOperation.closeBySubtaskId(subtaskId);
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
	@Override
	public Map<String,Integer> queryTaskIdsByGrid(String grid) throws Exception {
		Map<String,Integer> map = new HashMap<String, Integer>();
		map.put("quickTaskId", 1);
		map.put("centreTaskId", 1);
		return map;
	}
}

