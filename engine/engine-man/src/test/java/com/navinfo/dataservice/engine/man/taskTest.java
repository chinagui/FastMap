package com.navinfo.dataservice.engine.man;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class taskTest extends InitApplication{

	@Test
//	public void taskTestCreate() throws Exception {
//		try {
//			// TODO Auto-generated constructor stub
//			String parameter = "{\"blockId\":151,\"stage\":1,\"type\":2,\"descp\":\"开发sp6开发sp6_2\",\"planStartDate\":\"20160905\",\"planEndDate\":\"20160927\",\"exeUserId\":\"1573\",\"gridIds\":[60566232,60566231,60566230,60566222,60566221,60566220],\"name\":\"开发sp6_2\"}";
//			JSONObject dataJson = JSONObject.fromObject(parameter);
//			if(dataJson==null){
//				throw new IllegalArgumentException("parameter参数不能为空。");
//			}
//			
//			JSONArray gridIds = new JSONArray();
//			
//			//创建区域专项子任务
//			if(dataJson.containsKey("taskId")){
//				List<Integer> gridIdList = GridService.getInstance().getGridListByTaskId(dataJson.getInt("taskId"));
//				gridIds.addAll(gridIdList);
//			}else{
//				gridIds = dataJson.getJSONArray("gridIds");
//			}
//			//根据gridIds获取wkt
//			String wkt = GridUtils.grids2Wkt(gridIds);
//			if(wkt.contains("MULTIPOLYGON")){
//				throw new IllegalArgumentException("请输入符合条件的grids");
//			}
//			
//			Object[] gridIdList = gridIds.toArray();
//			dataJson.put("gridIds",gridIdList);
//			
//			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
//			bean.setGeometry(wkt);
//				
//			SubtaskService.getInstance().create(bean);	
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//				
//	}
	
	public static void taskTestUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		//TaskService service = new TaskService();
		//service.update(dataJson);			
	}
	
	@Test
	public void testGetByTask() throws ServiceException
	{
		JSONObject dataJson = JSONObject.fromObject("{\"subtaskId\":\"22\"}");
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		
		Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
		
		Subtask subtask = SubtaskService.getInstance().query(bean);	
		
		//根据需要的返回字段拼装结果
		HashMap<String, Object> data = new HashMap<String, Object>();
		if(subtask!=null&&subtask.getSubtaskId()!=null){
			data.put("subtaskId", subtask.getSubtaskId());
			data.put("stage", subtask.getStage());
			data.put("type", subtask.getType());
			data.put("planStartDate", subtask.getPlanStartDate());
			data.put("planEndDate", subtask.getPlanEndDate());
			data.put("descp", subtask.getDescp());
			data.put("name", subtask.getName());
			data.put("gridIds", subtask.getGridIds());
			data.put("dbId", subtask.getDbId());
			data.put("geometry", subtask.getGeometry());
		}
		else{
		}
		
		JSONObject result = JsonOperation.beanToJson(data);
		
		System.out.println(result);
	}

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	public void testQueryAdmin() throws ServiceException
	{
		SubtaskService subService = SubtaskService.getInstance(); 
		
		subService.queryAdminIdBySubtask(162);
	}
}
