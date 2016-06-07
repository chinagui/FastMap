package com.navinfo.dataservice.engine.man;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.man.task.TaskService;

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
		TaskService service = new TaskService();
		service.create(1,dataJson);			
	}
	
	public static void taskTestUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"tasks\" :[{\"taskId\":5, \"collectPlanStartDate\":\"2016-04-30 00:00:00\",\"collectPlanEndDate\":\"2016-04-29 00:00:00\"}]}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		TaskService service = new TaskService();
		service.update(dataJson);			
	}
	
	public static void main(String[] agr) throws Exception{
		taskTest.taskTestUpdate();
	}

}
