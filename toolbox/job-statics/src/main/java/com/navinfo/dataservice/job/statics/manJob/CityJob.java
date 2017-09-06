package com.navinfo.dataservice.job.statics.manJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import net.sf.json.JSONObject;

public class CityJob extends AbstractStatJob {
	private static final String db_name = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);

	public CityJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		PersonJobRequest statReq = (PersonJobRequest)request;
		MongoDao md = new MongoDao(db_name);
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			String timestamp = statReq.getTimestamp();
			log.info("获取全部区县信息");
			Map<Integer, Map<String, Object>> cityMap = manApi.cityStatic();
			//从mango库中查询数据
			log.info("获取无数据区县信息");
			Map<Integer, Map<String, Object>> notaskMap = queryCityNotaskData(timestamp, md);
			Map<Integer, Map<String, Object>> programMap = queryProgramData(timestamp, md);
			
			List<Map<String, Object>> keyMaps = new ArrayList<>();
			Set<Integer> idIter = cityMap.keySet();
			for(int cityId:idIter){
				Map<String, Object> cityTmp=cityMap.get(cityId);
				if(notaskMap.containsKey(cityId)){
					cityTmp.put("tipsTotal",notaskMap.get(cityId).get("tipsTotal"));
					cityTmp.put("poiTotal",notaskMap.get(cityId).get("poiTotal"));
					cityTmp.put("dealershipTotal",notaskMap.get(cityId).get("dealershipTotal"));
					cityTmp.put("noDealershipTotal",notaskMap.get(cityId).get("noDealershipTotal"));
				}
				if(programMap.containsKey(cityId)){
					cityTmp.put("lot1Poi",notaskMap.get(cityId).get("lot1Poi"));
					cityTmp.put("lot2Poi",notaskMap.get(cityId).get("lot2Poi"));
					cityTmp.put("lot3Poi",notaskMap.get(cityId).get("lot3Poi"));
					cityTmp.put("lot1Tips",notaskMap.get(cityId).get("lot1Tips"));
					cityTmp.put("lot2Tips",notaskMap.get(cityId).get("lot2Tips"));
					cityTmp.put("lot3Tips",notaskMap.get(cityId).get("lot3Tips"));
				}
				keyMaps.add(cityTmp);
			}			
			Map<String, Object> result = new HashMap<>();
			result.put("city", keyMaps);
			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
		
		/**
		 * 从mango库中查询任务统计数据
		 * @param String
		 * @param Map<Integer, Object>
		 * 
		 * */
		public Map<Integer, Map<String, Object>> queryCityNotaskData(String timestamp, MongoDao md){
			
			Map<Integer, Map<String, Object>> result = new HashMap<>();
			String blockNotaskName = "city_notask";
			BasicDBObject query = new BasicDBObject();
			query.put("timestamp", timestamp);		
			MongoCursor<Document> blockNotask = md.find(blockNotaskName, query).iterator();
			//统计一个任务下所有子任务的personTips
			while(blockNotask.hasNext()){
				JSONObject notaskJson = JSONObject.fromObject(blockNotask.next());
				Map<String, Object> map = new HashMap<>();
				map.putAll(notaskJson);
				result.put(notaskJson.getInt("cityId"), map);
			}
			return result;
		}
		
		/**
		 * 从mango库中查询任务统计数据
		 * @param String
		 * @param Map<Integer, Object>
		 * 
		 * */
		public Map<Integer, Map<String, Object>> queryProgramData(String timestamp, MongoDao md){
			
			Map<Integer, Map<String, Object>> result = new HashMap<>();
			String blockNotaskName = "program";
			BasicDBObject query = new BasicDBObject();
			query.put("timestamp", timestamp);		
			query.put("type", 1);	
			MongoCursor<Document> blockNotask = md.find(blockNotaskName, query).iterator();
			//统计一个任务下所有子任务的personTips
			while(blockNotask.hasNext()){
				JSONObject programJson = JSONObject.fromObject(blockNotask.next());
				Map<String, Object> map = new HashMap<>();
				map.putAll(programJson);
				result.put(programJson.getInt("cityId"), map);
			}
			return result;
		}

}
