package com.navinfo.dataservice.engine.man;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.exception.ServiceException;

public class taskTest {

	public static void taskTestCreate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"tasks\" :[{\"descp\":\"task1\", \"planStartDate\":\"20160430000000\",\"planEndDate\":\"20160630000000\",\"cityId\":1,\"geometry\":\"Polygon((116 23, 116 24))\"}]}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		//TaskService service = new TaskService();
		//service.create(1,dataJson);			
	}
	
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
	
	public static void main(String[] agr) throws Exception{
		taskTest.taskTestUpdate();
	}
	
	@Test
	public void testGetByTask() throws ServiceException
	{
		JSONObject dataJson = JSONObject.fromObject("{\"subtaskId\":\"82\"}");
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
	}
}
