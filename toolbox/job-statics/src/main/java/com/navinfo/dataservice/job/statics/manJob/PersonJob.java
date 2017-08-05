package com.navinfo.dataservice.job.statics.manJob;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PersonJob extends AbstractStatJob {
	
	private static final String db_name = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	public PersonJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String stat() throws JobException {
		PersonJobRequest statReq = (PersonJobRequest)request;
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			String timestamp = statReq.getTimestamp();
			List<Map<String, Object>> personList = manApi.staticsPersionJob(timestamp);
			Map<String, Object> task2subTask = new HashMap<>();
			for(Map<String, Object> map : personList){
				Map<String, Object> subMap = new HashMap<>();
				String taskId =  map.get("taskId").toString();
				String userId = map.get("userId").toString();
				String key = userId + "_" + taskId + "_" + timestamp;
				Set<Long> subtaskSet = (Set<Long>) map.get("subtaskIds");
				subMap.put("key", key);
				subMap.put("subtaskSet", subtaskSet);
				task2subTask.put(taskId, subMap);
			}
			//从mango库中查询数据
			Map<String, Object> mongoMap = queryDataFromMongo(timestamp, task2subTask);
			Map<String, Map<String, Object>> dataMap = new HashMap<String, Map<String, Object>>();
			List<Map<String, Map<String, Object>>> dataMaps = new ArrayList<>();
			Map<String, List<Map<String, Map<String, Object>>>> result = new HashMap<String, List<Map<String, Map<String, Object>>>>();
			for(Entry<String, Object> entry : mongoMap.entrySet()){
				Map<String, Object> resultMap = (Map<String, Object>) entry.getValue();
				for(Map<String, Object> map : personList){
					String taskId =  map.get("taskId").toString();
					if(taskId.equals(resultMap.get("taskId").toString())){
						map.remove("subtaskIds");
						map.put("date", timestamp);
						resultMap.putAll(map);
						continue;
					}
				}
				dataMap.put(entry.getKey(), resultMap);
				dataMaps.add(dataMap);
			}
			result.put("person", dataMaps);
			log.info("persionList:" + personList);
			log.info("stats:" + JSONObject.fromObject(result).toString());
			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	/**
	 * 从mango库中查询统计数据
	 * @param String
	 * @param Map<String, Object>
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Map<String, Object> queryDataFromMongo(String timestamp, Map<String, Object> task2subTask){
		Map<String, Object> dayPlanData = new HashMap<>();
		MongoDao md = new MongoDao(db_name);

		String planTableName = "task_day_plan";
		MongoCursor<Document> plan = md.find(planTableName, null).iterator();
		while (plan.hasNext()) {
			
			JSONObject json = JSONObject.fromObject(plan.next());
			String taskId = json.getString("taskId");
			Map<String, Object> map = (Map<String, Object>) task2subTask.get(taskId);
			if(map == null){
				continue;
			}
			Map<String, Object> taskData = new HashMap<>();
			String key = map.get("key").toString();
			String linkAllLen = json.getString("linkAllLen");
			String link27AllLen = json.getString("link27AllLen");
			int poiAllNum = Integer.parseInt(json.getString("poiAllNum"));
			taskData.put("linkAllLen", linkAllLen);
			taskData.put("link27AllLen", link27AllLen);
			taskData.put("poiAllNum", poiAllNum);
			taskData.put("taskId", taskId);
			taskData.put("poiUploadNum", 0);
			taskData.put("poiFreshNum", 0);
			taskData.put("poiFinishNum", 0);
			dayPlanData.put(key, taskData);
		}
		
		String personTableName = "person_day";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp + "230000");		//TODO 
		MongoCursor<Document> person = md.find(personTableName, query).iterator();
		Map<Integer, Object> subtaskData = new HashMap<>();
		while(person.hasNext()){
			JSONObject json = JSONObject.fromObject(person.next());
			JSONArray content = json.getJSONArray("content");
			for(int i = 0; i < content.size(); i++){
				Map<String, Integer> data = (Map<String, Integer>) content.get(i);
				Map<String, Integer> map = new HashMap<>();
				int subtaskId = data.get("subtaskId");
				int poiUploadNum = data.get("uploadNum");
				int poiFreshNum = data.get("freshNum");
				int poiFinishNum = data.get("finishNum");
				map.put("poiUploadNum", poiUploadNum);
				map.put("poiFreshNum", poiFreshNum);
				map.put("poiFinishNum", poiFinishNum);
				subtaskData.put(subtaskId, map);
			}
		}
		
		for(Entry<String, Object> entry : task2subTask.entrySet()){
			Map<String, Object> subDataMap = (Map<String, Object>) entry.getValue();
			Set<Long> subtaskIds = (Set<Long>) subDataMap.get("subtaskSet");
			String key = subDataMap.get("key").toString();
			for(Entry<Integer, Object> subEntry : subtaskData.entrySet()){
				long subtaskId = Long.valueOf(subEntry.getKey());
				if(subtaskIds.contains(subtaskId)){
					Map<String, Integer> subMap = (Map<String, Integer>) subEntry.getValue();
					int poiUploadNum = subMap.get("poiUploadNum");
					int poiFreshNum = subMap.get("poiFreshNum");
					int poiFinishNum = subMap.get("poiFinishNum");
					Map<String, Object> map = (Map<String, Object>) dayPlanData.get(key);
					map.put("poiUploadNum", poiUploadNum);
					map.put("poiFreshNum", poiFreshNum);
					map.put("poiFinishNum", poiFinishNum);
					dayPlanData.put(key, map);
				}
			}
		}
		return dayPlanData;
	}

}
