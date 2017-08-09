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
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			String timestamp = statReq.getTimestamp().substring(0, 8);
			log.info("timestamp:" + timestamp);
			List<Map<String, Object>> personList = manApi.staticsPersionJob(timestamp);
			Map<String, Object> task2subTask = new HashMap<>();
			for(Map<String, Object> map : personList){
				Map<String, Object> subMap = new HashMap<>();
				String taskId =  map.get("taskId").toString();
				String userId = map.get("userId").toString();
				String key = userId + "_" + taskId + "_" + timestamp;
				Set<Long> subtaskSet = (Set<Long>) map.get("subtaskIds");
				if(task2subTask.containsKey(taskId)){
					Set<Long> subtasks = (Set<Long>)((Map<String, Object>)task2subTask.get(taskId)).get("subtaskSet");
					subtaskSet.addAll(subtasks);
				}
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
				resultMap.put("date", timestamp);
				resultMap.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
				for(Map<String, Object> map : personList){
					String taskId =  map.get("taskId").toString();
					map.remove("subtaskIds");
					if(taskId.equals(resultMap.get("taskId").toString())){
						resultMap.putAll(map);
						continue;
					}
//					if(!resultMap.containsKey("userId")){
//						for(Entry<String, Object> oricalData : map.entrySet()){
//							resultMap.put(oricalData.getKey(), "");
//						}
//					}
				}
				dataMap.put(entry.getKey(), resultMap);
			}
			dataMaps.add(dataMap);
			result.put("person", dataMaps);
			log.info("result:" + result);
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
			taskData.put("tipsAddLen", 0);
			taskData.put("tipsAllLen", 0);
			//这些值需求待定，先赋值为空
			taskData.put("startDate", "");
			taskData.put("endDate", "");
			taskData.put("workTime", "");
			dayPlanData.put(key, taskData);
		}
		
		//从mongo库差personTips和personDay的数据
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
		
		//将一个task下的子任务对应的数据求和返回
		Map<String, Object> data = convertDataBySubtask(personTips, task2subTask);
		for(Entry<String, Object> entry : data.entrySet()){
			String key = entry.getKey();
			Map<String, Integer> subMap = (Map<String, Integer>) entry.getValue();
			Map<String, Integer> map = (Map<String, Integer>) dayPlanData.get(key);
			if(map == null){
				continue;
			}
			map.put("poiUploadNum", subMap.get("poiUploadNum"));
			map.put("poiFreshNum", subMap.get("poiFreshNum"));
			map.put("poiFinishNum", subMap.get("poiFinishNum"));
			dayPlanData.put(key, map);
		}
		
		return dayPlanData;
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
		query.put("timestamp", timestamp + "230000");		
		MongoCursor<Document> personTips = md.find(personTipsName, query).iterator();
		//统计一个任务下所有子任务的personTips
		while(personTips.hasNext()){
			JSONObject tipsJson = JSONObject.fromObject(personTips.next());
			JSONArray tipsContent = tipsJson.getJSONArray("content");
			double tipsAddLen = 0;
			double tipsAllLen = 0;
			int subtaskId = 0;
			for(int j = 0; j < tipsContent.size(); j++){
				Map<String, Object> map = new HashMap<>();
				Map<String, Object> tipsData = (Map<String, Object>) tipsContent.get(j);
				subtaskId = Integer.parseInt(tipsData.get("subtaskId").toString());
				tipsAddLen = Double.valueOf(tipsData.get("tipsAddLen").toString());
				tipsAllLen = Double.valueOf(tipsData.get("tipsAllLen").toString());
				if(result.containsKey(subtaskId)){
					tipsAddLen += Double.valueOf(tipsData.get("tipsAddLen").toString());
					tipsAllLen += Double.valueOf(tipsData.get("tipsAllLen").toString());
				}
				map.put("tipsAddLen", String.valueOf(tipsAddLen));
				map.put("tipsAllLen", String.valueOf(tipsAllLen));
			    result.put(subtaskId, map);
			}
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
		query.put("timestamp", timestamp + "230000");		
		MongoCursor<Document> person = md.find(personTableName, query).iterator();
		
		Map<Integer, Object> subtaskData = new HashMap<>();
		//统计一个任务下所有的子任务的数据
		while(person.hasNext()){
			JSONObject json = JSONObject.fromObject(person.next());
			JSONArray content = json.getJSONArray("content");
			int poiUploadNum = 0;
			int poiFreshNum = 0;
			int poiFinishNum = 0;
			int subtaskId = 0;
			for(int i = 0; i < content.size(); i++){
				Map<String, Integer> data = (Map<String, Integer>) content.get(i);
				Map<String, Integer> map = new HashMap<>();
				subtaskId = data.get("subtaskId");

				if(subtaskData.containsKey(subtaskId)){
					poiUploadNum += data.get("uploadNum");
					poiFreshNum += data.get("freshNum");
					poiFinishNum += data.get("finishNum");
				}else{
					poiUploadNum = data.get("uploadNum");
					poiFreshNum = data.get("freshNum");
					poiFinishNum = data.get("finishNum");
				}
				map.put("poiUploadNum", poiUploadNum);
				map.put("poiFreshNum", poiFreshNum);
				map.put("poiFinishNum", poiFinishNum);
				subtaskData.put(subtaskId, map);
			}
		}
		return subtaskData;
	}
	
	/**
	 * 根据任务id和子任务id对数据进行处理
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Map<String, Object> convertDataBySubtask(Map<Integer, Object> personTips, Map<String, Object> task2subTask){
		Map<String, Object> result = new HashMap<>();
		for(Entry<String, Object> entry : task2subTask.entrySet()){
			String taskId = entry.getKey();
			Map<String, Object> subDataMap = (Map<String, Object>) entry.getValue();
			Set<Long> subtaskIds = (Set<Long>) subDataMap.get("subtaskSet");
			String key = subDataMap.get("key").toString();
			Map<String, Object> map = new HashMap<>();
			int poiUploadNum = 0;
			int poiFreshNum = 0;
			int poiFinishNum = 0;
			for(Entry<Integer, Object> subEntry : personTips.entrySet()){
				long subtaskId = Long.valueOf(subEntry.getKey());
				if(subtaskIds.contains(subtaskId)){
					Map<String, Object> subMap = (Map<String, Object>) subEntry.getValue();
					poiUploadNum += (int) subMap.get("poiUploadNum");
					poiFreshNum += (int) subMap.get("poiFreshNum");
					poiFinishNum += (int) subMap.get("poiFinishNum");
				}
			}

			map.put("taskId", taskId);
			map.put("poiUploadNum", poiUploadNum);
			map.put("poiFreshNum", poiFreshNum);
			map.put("poiFinishNum", poiFinishNum);
			result.put(key, map);
		}
		return result;
	}

}
