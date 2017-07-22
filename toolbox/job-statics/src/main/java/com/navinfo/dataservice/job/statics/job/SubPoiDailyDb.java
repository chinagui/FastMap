package com.navinfo.dataservice.job.statics.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.job.statics.job.model.PoiDailyDbObj;


public class SubPoiDailyDb implements Callable<Map<String, List<JSONObject>>> {
	
	public SubPoiDailyDb(int dbId,String dbName,String statTime){
		
	}

	@Override
	public Map<String, List<JSONObject>> call() throws Exception {
		String subtaskStr="{ subtaskId:1,collectUploadNum:12, commitNum:2 }";
		String taskStr="{ taskId:1,collectUploadNum:12, commitNum:2 }";
		List<JSONObject> subtask=new ArrayList<JSONObject>();
		subtask.add(JSONObject.fromObject(subtaskStr));
		List<JSONObject> task=new ArrayList<JSONObject>();
		task.add(JSONObject.fromObject(taskStr));
		
		Map<String, List<JSONObject>> staticsResult=new HashMap<String, List<JSONObject>>();
		staticsResult.put("daily", subtask);
		staticsResult.put("task", task);
		return staticsResult;
	}

}
