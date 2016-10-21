package com.navinfo.dataservice.engine.man.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.statics.StaticsService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.version.VersionService;
import com.navinfo.navicommons.exception.ServiceException;
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
		return SubtaskOperation.getGridIdsBySubtaskId(subtaskId);
	}
	@Override
	public void close(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		SubtaskOperation.closeBySubtaskId(subtaskId);
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
}

