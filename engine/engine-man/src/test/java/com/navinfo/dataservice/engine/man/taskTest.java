package com.navinfo.dataservice.engine.man;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.man.task.TaskService;

public class taskTest {

	public static void taskTestUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"tasks\" :[{\"descp\":\"task1\", \"planStartDate\":\"20160430\",\"planEndDate\":\"20160630\",\"cityId\":1,\"geometry\":\"Polygon((116 23, 116 24))\"}]}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		TaskService service = new TaskService();
		service.create(dataJson);			
	}
	
	public static void main(String[] agr) throws Exception{
		taskTest.taskTestUpdate();
	}

}
