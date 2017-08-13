package com.navinfo.dataservice.scripts.tmp;

import java.util.List;
import java.util.Map;

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
		for(Map<String, Object> task:allTask){
			int taskId=0;
			try{
				taskId=(int) task.get("taskId");
				log.info("start init "+taskId);
				TaskService.getInstance().initPlanData(taskId);
				log.info("end init "+taskId);
			}catch (Exception e) {
				log.warn("init "+taskId+" error", e);
			}
		}
		log.info("end");
		System.exit(0);
	}
}
