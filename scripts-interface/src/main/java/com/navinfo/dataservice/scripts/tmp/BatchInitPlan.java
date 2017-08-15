package com.navinfo.dataservice.scripts.tmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;

import net.sf.json.JSONObject;

public class BatchInitPlan {
	private static Logger log = LogManager.getLogger(BatchInitPlan.class);
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		log.info("start");
		JobScriptsInterface.initContext();
		List<Map<String, Object>> allTask = TaskService.getInstance().unPlanlist(new JSONObject());
		Set<Integer> errorTaskIds=new HashSet<Integer>();
//		//按照大区分组并行跑
//		Map<Integer,Set<Integer>> regionTaskMap=new HashMap<>();
//		for(Map<String, Object> task:allTask){
//			int taskId=(int) task.get("taskId");
//			int regionId=(int) task.get("regionId");
//			if(!regionTaskMap.containsKey(regionId)){
//				regionTaskMap.put(regionId, new HashSet<Integer>());
//			}
//			regionTaskMap.get(regionId).add(taskId);
//		}
		for(Map<String, Object> task:allTask){
			int taskId=0;
			try{
				taskId=(int) task.get("taskId");
				log.info("start init "+taskId);
				TaskService.getInstance().initPlanData(taskId);
				log.info("end init "+taskId);
			}catch (Exception e) {
				errorTaskIds.add(taskId);
				log.warn("init "+taskId+" error", e);
			}
		}
		log.info("success "+(allTask.size()-errorTaskIds.size())+",error "+errorTaskIds.size()+";error list "+errorTaskIds.toString());
		log.info("end");
		System.exit(0);
	}
}
