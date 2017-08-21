package com.navinfo.dataservice.job.statics.manJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	
	public PersonJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String stat() throws JobException {
		PersonJobRequest statReq = (PersonJobRequest)request;
		MongoDao md = new MongoDao(db_name);
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			String timestamp = statReq.getTimestamp();
			String timeForOrical = timestamp.substring(0, 8);
			log.info("timestamp:" + timestamp);
			List<Map<String, Object>> personList = manApi.staticsPersionJob(timeForOrical);
			//从mango库中查询数据
			Map<Integer, Map<String, Object>> tasks = queryTaskData(timestamp, md);
			Map<Integer, Object> subTasks = queryDataFromMongo(md, timestamp);
			
			Map<String, Object> result = new HashMap<>();
			List<Map<String, Object>> keyMaps = new ArrayList<>();
			Map<String, Object> keyMap = new HashMap<>();
			for(Map<String, Object> map : personList){
				int taskId =  Integer.parseInt(map.get("taskId").toString());
				int userId = Integer.parseInt(map.get("userId").toString());
				String key = userId + "_" + taskId + "_" + timestamp;
				Set<Long> subtaskSet = (Set<Long>) map.get("subtaskIds");
				String cityName = map.get("cityName").toString();
				String userName = map.get("userName").toString();
				String taskName = map.get("taskName").toString();
				String leaderName = map.get("leaderName").toString();
				double linkAllLen = 0d;
				double link27AllLen = 0d;
				int poiAllNum = 0;
				int poiUploadNum = 0;
				int poiFreshNum = 0;
				int poiFinishNum = 0;
				//这些值需求待定，先赋值为空
				String startDate = "";
				String endDate = "";
				String workTime = "";
				if(tasks.containsKey(taskId)){
					Map<String, Object> taskMap = tasks.get(taskId);
					linkAllLen =  (double) taskMap.get("linkAllLen");
					link27AllLen =  (double) taskMap.get("link27AllLen");
					poiAllNum = (int) taskMap.get("poiAllNum");
				}
				for(Entry<Integer, Object> entry : subTasks.entrySet()){
					long subtaskId = entry.getKey();
					if(subtaskSet.contains(subtaskId)){
						Map<String, Integer> subData = (Map<String, Integer>) entry.getValue();
						poiUploadNum += subData.get("poiUploadNum");
						poiFreshNum += subData.get("poiFreshNum");
						poiFinishNum += subData.get("poiFinishNum");
					}
				}
				//汇总数据放入map中
				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put("taskId", taskId);
				dataMap.put("userId", userId);
				dataMap.put("cityName", cityName);
				dataMap.put("userName", userName);
				dataMap.put("taskName", taskName);
				dataMap.put("leaderName", leaderName);
				dataMap.put("linkAllLen", linkAllLen);
				dataMap.put("link27AllLen", link27AllLen);
				dataMap.put("poiAllNum", poiAllNum);
				dataMap.put("poiUploadNum", poiUploadNum);
				dataMap.put("poiFreshNum", poiFreshNum);
				dataMap.put("poiFinishNum", poiFinishNum);
				
				dataMap.put("startDate", startDate);
				dataMap.put("endDate", endDate);
				dataMap.put("workTime", workTime);
				dataMap.put("poiFinishNum", poiFinishNum);
				dataMap.put("date", timestamp);
				dataMap.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
				keyMap.put(key, dataMap);
				keyMaps.add(keyMap);
			}
			result.put("person", keyMaps);
			log.info("result:" + result);
			
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
	public Map<Integer, Object> queryDataFromMongo(MongoDao md, String timestamp){
		
		//从mongo库差personTips和personDay和task的数据
		Map<Integer, Object> personTips = queryPersonTips(timestamp, md);
		Map<Integer, Object> personDay = queryPersonDay(timestamp, md);
		
		//personTips和personDay的数据放在一个map中
		for(Entry<Integer, Object> entry : personTips.entrySet()){
			int subtaskId = entry.getKey();
			Map<String, Integer> subMap = (Map<String, Integer>) entry.getValue();
			int poiUploadNum = 0;
			int poiFreshNum = 0;
			int poiFinishNum = 0;
			if(personDay.containsKey(subtaskId)){
				Map<String, Integer> daySubMap = (Map<String, Integer>) personDay.get(subtaskId);
				poiUploadNum = daySubMap.get("poiUploadNum");
				poiFreshNum = daySubMap.get("poiFreshNum");
				poiFinishNum = daySubMap.get("poiFinishNum");
			}
			subMap.put("poiUploadNum", poiUploadNum);
			subMap.put("poiFreshNum", poiFreshNum);
			subMap.put("poiFinishNum", poiFinishNum);
			personTips.put(subtaskId, subMap);
		}
		return personTips;
	}
	
	/**
	 * 查询personTips中的数据放入对应map中
	 * @param String
	 * @param MongoDao
	 * @return  Map<Integer, Object>
	 * 
	 * */
	public Map<Integer, Object> queryPersonTips(String timestamp, MongoDao md){
		Map<Integer, Object> result = new HashMap<>();
		String personTipsName = "person_tips";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);		
		MongoCursor<Document> personTips = md.find(personTipsName, query).iterator();
		//统计一个任务下所有子任务的personTips
		while(personTips.hasNext()){
			JSONObject tipsJson = JSONObject.fromObject(personTips.next());
			double tipsAddLen = 0;
			double tipsAllLen = 0;
			int subtaskId = 0;
			Map<String, Object> map = new HashMap<>();
			subtaskId = Integer.parseInt(tipsJson.get("subtaskId").toString());
			tipsAddLen = Double.valueOf(tipsJson.get("tipsAddLen").toString());
			tipsAllLen = Double.valueOf(tipsJson.get("tipsAllLen").toString());
			map.put("tipsAddLen", String.valueOf(tipsAddLen));
			map.put("tipsAllLen", String.valueOf(tipsAllLen));
		    result.put(subtaskId, map);
		}
		return result;
	}
	
	/**
	 * 查询personDay中的数据放入对应map中
	 * @param String
	 * @param MongoDao
	 * @return  Map<Integer, Object>
	 * 
	 */
	public Map<Integer, Object> queryPersonDay(String timestamp, MongoDao md){
		String personTableName = "person_day";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);		
		MongoCursor<Document> person = md.find(personTableName, query).iterator();
		
		Map<Integer, Object> subtaskData = new HashMap<>();
		//统计所有的子任务的数据
		while(person.hasNext()){
			JSONObject json = JSONObject.fromObject(person.next());
			int poiUploadNum = 0;
			int poiFreshNum = 0;
			int poiFinishNum = 0;
			int subtaskId = 0;
			Map<String, Integer> map = new HashMap<>();
			subtaskId = (int) json.get("subtaskId");
			poiUploadNum = (int) json.get("uploadNum");
			poiFreshNum = (int) json.get("freshNum");
			poiFinishNum = (int) json.get("finishNum");
			
			map.put("poiUploadNum", poiUploadNum);
			map.put("poiFreshNum", poiFreshNum);
			map.put("poiFinishNum", poiFinishNum);
			subtaskData.put(subtaskId, map);
		}
		return subtaskData;
	}
	
	/**
	 * 从mango库中查询任务统计数据
	 * @param String
	 * @param Map<Integer, Object>
	 * 
	 * */
	public Map<Integer, Map<String, Object>> queryTaskData(String timestamp, MongoDao md){
		
		Map<Integer, Map<String, Object>> tasks = new HashMap<>();

		String planTableName = "task_day_plan";
		MongoCursor<Document> plan = md.find(planTableName, null).iterator();
		while (plan.hasNext()) {
			
			JSONObject json = JSONObject.fromObject(plan.next());
			int taskId = json.getInt("taskId");
			
			Map<String, Object> taskData = new HashMap<>();
			double linkAllLen = Double.valueOf(json.getString("linkAllLen"));
			double link27AllLen = Double.valueOf(json.getString("link27AllLen"));
			int poiAllNum = Integer.parseInt(json.getString("poiAllNum"));
			taskData.put("linkAllLen", linkAllLen);
			taskData.put("link27AllLen", link27AllLen);
			taskData.put("poiAllNum", poiAllNum);
			
			tasks.put(taskId, taskData);
		}
		return tasks;
	}
	
}
