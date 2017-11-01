package com.navinfo.dataservice.job.statics.manJob;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
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

public class PersonJob extends AbstractStatJob {
	
	private static final String db_name = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
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
			String workDay = statReq.getWorkDay();
			log.info("timestamp:" + timestamp+",workDay:"+workDay);
			List<Map<String, Object>> personList = manApi.staticsPersionJob(workDay);
			//从mango库中查询数据
			Map<Integer, Map<String, Object>> tasks = queryTaskData(timestamp, md);
			Map<Integer, Object> personFcc = queryPersonFcc(timestamp,workDay, md);
//			Map<Integer, Object> subTasks = queryDataFromMongo(md, timestamp);
			//从mongo库差personTips和personDay和task的数据
			Map<Integer, Object> personTips = queryPersonTips(timestamp,workDay, md);
			Map<Integer, Object> personDay = queryPersonDay(timestamp,workDay, md);
			
			Map<String, Object> result = new HashMap<>();
			List<Map<String, Object>> keyMaps = new ArrayList<>();
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
				double tipsAddLen = 0d;
				long tipsAllNum = 0;
				double fccUpdateLen = 0d;
				int poiAllNum = 0;
				int poiUploadNum = 0;
				int poiFreshNum = 0;
				int poiFinishNum = 0;
				int deleteCount = 0;
	    		int increaseAndAlterCount = 0;
	    		String effectiveTime="";
	    		
	    		//添加了很多统计字段
	    		int aPoiNum = 0;
	    		int aPoiFreshNum = 0;
	    		int aPoiAddNum = 0;
	    		int aPoiDelNum = 0;
	    		int aPoiUpdateNum = 0;
	    		int aCommitNum = 0;
	    		int aCommitFreshNum = 0;
	    		int aCommitAddNum = 0;
	    		int aCommitDelNum = 0;
	    		int aCommitUpdateNum = 0;

	    		int b1PoiNum = 0;
	    		int b1PoiFreshNum = 0;
	    		int b1PoiAddNum = 0;
	    		int b1PoiDelNum = 0;
	    		int b1PoiUpdateNum = 0;
	    		int b1CommitNum = 0;
	    		int b1CommitFreshNum = 0;
	    		int b1CommitAddNum = 0;
	    		int b1CommitDelNum = 0;
	    		int b1CommitUpdateNum = 0;

	    		int b2PoiNum = 0;
	    		int b2PoiFreshNum = 0;
	    		int b2PoiAddNum = 0;
	    		int b2PoiDelNum = 0;
	    		int b2PoiUpdateNum = 0;
	    		int b2CommitNum = 0;
	    		int b2CommitFreshNum = 0;
	    		int b2CommitAddNum = 0;
	    		int b2CommitDelNum = 0;
	    		int b2CommitUpdateNum = 0;

	    		int b3PoiNum = 0;
	    		int b3PoiFreshNum = 0;
	    		int b3PoiAddNum = 0;
	    		int b3PoiDelNum = 0;
	    		int b3PoiUpdateNum = 0;
	    		int b3CommitNum = 0;
	    		int b3CommitFreshNum = 0;
	    		int b3CommitAddNum = 0;
	    		int b3CommitDelNum = 0;
	    		int b3CommitUpdateNum = 0;

	    		int b4PoiNum = 0;
	    		int b4PoiFreshNum = 0;
	    		int b4PoiAddNum = 0;
	    		int b4PoiDelNum = 0;
	    		int b4PoiUpdateNum = 0;
	    		int b4CommitNum = 0;
	    		int b4CommitFreshNum = 0;
	    		int b4CommitAddNum = 0;
	    		int b4CommitDelNum = 0;
	    		int b4CommitUpdateNum = 0;

	    		int cPoiNum = 0;
	    		int cPoiFreshNum = 0;
	    		int cPoiAddNum = 0;
	    		int cPoiDelNum = 0;
	    		int cPoiUpdateNum = 0;
	    		int cCommitNum = 0;
	    		int cCommitFreshNum = 0;
	    		int cCommitAddNum = 0;
	    		int cCommitDelNum = 0;
	    		int cCommitUpdateNum = 0;
	    		
	    		double taskFc1len = 0d;
	    		double taskFc2len = 0d;
	    		double taskFc3len = 0d;
	    		double taskFc4len = 0d;
	    		double taskFc5len = 0d;
	    		
	    		double track1UpLen = 0d;
	    		double track2UpLen = 0d;
	    		double track3UpLen = 0d;
	    		double track4UpLen = 0d;
	    		double track5UpLen = 0d;
	    		double track6UpLen = 0d;
	    		double track7UpLen = 0d;
	    		double track8UpLen = 0d;
	    		double track9UpLen = 0d;
	    		double track10UpLen = 0d;
	    		
	    		double trackFc1Uplen = 0d;
	    		double trackFc2Uplen = 0d;
	    		double trackFc3Uplen = 0d;
	    		double trackFc4Uplen = 0d;
	    		double trackFc5Uplen = 0d;
	    		
	    		int tips1Len = 0;
	    		int tips2Len = 0;
	    		int tips3Len = 0;
	    		int tips4Len = 0;
	    		int tips5Len = 0;
	    		int tips6Len = 0;
	    		int tips7Len = 0;
	    		int tips8Len = 0;
	    		int tips9Len = 0;
	    		int tips10Len = 0;
	    		
	    		int addNum = 0;
	    		int upNum = 0;
	    		int delNum = 0;
	    		
				String startDate = "";
				String endDate = "";
				String workTime = "";
				if(tasks.containsKey(taskId)){
					Map<String, Object> taskMap = tasks.get(taskId);
					linkAllLen =  (double) taskMap.get("linkAllLen");
					link27AllLen =  (double) taskMap.get("link27AllLen");
					poiAllNum = (int) taskMap.get("poiAllNum");
					
					taskFc1len =  (double) taskMap.get("taskFc1len");
					taskFc2len =  (double) taskMap.get("taskFc2len");
					taskFc3len = (double) taskMap.get("taskFc3len");
					taskFc4len =  (double) taskMap.get("taskFc4len");
					taskFc5len =  (double) taskMap.get("taskFc5len");
				}

				if(personFcc.containsKey(taskId)){
					List<Map<String, Object>> fccs = (List<Map<String, Object>>) personFcc.get(taskId);
					for(Map<String, Object> fccMap : fccs){
						if(userId == (int) fccMap.get("userId")){
							startDate = fccMap.get("startDate").toString();
							endDate = fccMap.get("endDate").toString();
							workTime = fccMap.get("workTime").toString();
							effectiveTime = fccMap.get("effectiveTime").toString();
							fccUpdateLen = Double.valueOf(fccMap.get("fccUpdateLen").toString());
							//modiby by songhe 2017/11/01  fcc添加统计字段
							track1UpLen = (double) fccMap.get("track1UpLen");
							track2UpLen = (double) fccMap.get("track2UpLen");
							track3UpLen = (double) fccMap.get("track3UpLen");
							track4UpLen = (double) fccMap.get("track4UpLen");
							track5UpLen = (double) fccMap.get("track5UpLen");
							track6UpLen = (double) fccMap.get("track6UpLen");
							track7UpLen = (double) fccMap.get("track7UpLen");
							track8UpLen = (double) fccMap.get("track8UpLen");
							track9UpLen = (double) fccMap.get("track9UpLen");
							track10UpLen = (double) fccMap.get("track10UpLen");
							
							trackFc1Uplen = (double) fccMap.get("trackFc1Uplen");
							trackFc2Uplen = (double) fccMap.get("trackFc2Uplen");
							trackFc3Uplen = (double) fccMap.get("trackFc3Uplen");
							trackFc4Uplen = (double) fccMap.get("trackFc4Uplen");
							trackFc5Uplen = (double) fccMap.get("trackFc5Uplen");
						}
					}
				}
				for(long subtaskId : subtaskSet){
					int id = (int) subtaskId;
					if(personTips.containsKey(id)){
						Map<String, Object> subData = (Map<String, Object>) personTips.get(id);
						tipsAddLen += (double)subData.get("tipsAddLen");
						tipsAllNum += (long)subData.get("tipsAllNum");
						tips1Len += (int) subData.get("tips1Len");
						tips2Len += (int) subData.get("tips2Len");
						tips3Len += (int) subData.get("tips3Len");
						tips4Len += (int) subData.get("tips4Len");
						tips5Len += (int) subData.get("tips5Len");
						tips6Len += (int) subData.get("tips6Len");
						tips7Len += (int) subData.get("tips7Len");
						tips8Len += (int) subData.get("tips8Len");
						tips9Len += (int) subData.get("tips9Len");
						tips10Len += (int) subData.get("tips10Len");
						
						addNum += (int) subData.get("addNum");
						upNum += (int) subData.get("upNum");
						delNum += (int) subData.get("delNum");
					}
					if(personDay.containsKey(id)){
						Map<String, Object> subData = (Map<String, Object>) personDay.get(id);
						poiUploadNum += (int)subData.get("poiUploadNum");
						poiFreshNum += (int)subData.get("poiFreshNum");
						poiFinishNum += (int)subData.get("poiFinishNum");
						deleteCount += (int)subData.get("deleteCount");
						increaseAndAlterCount += (int)subData.get("increaseAndAlterCount");
						
						aPoiNum += (int)subData.get("aPoiNum");
						aPoiFreshNum += (int)subData.get("aPoiFreshNum");
						aPoiAddNum += (int)subData.get("aPoiAddNum");
						aPoiDelNum += (int)subData.get("aPoiDelNum");
						aPoiUpdateNum += (int)subData.get("aPoiUpdateNum");
						aCommitNum += (int)subData.get("aCommitNum");
						aCommitFreshNum += (int)subData.get("aCommitFreshNum");
						aCommitAddNum += (int)subData.get("aCommitAddNum");
						aCommitDelNum += (int)subData.get("aCommitDelNum");
						aCommitUpdateNum += (int)subData.get("aCommitUpdateNum");

						b1PoiNum += (int)subData.get("b1PoiNum");
						b1PoiFreshNum += (int)subData.get("b1PoiFreshNum");
						b1PoiAddNum += (int)subData.get("b1PoiAddNum");
						b1PoiDelNum += (int)subData.get("b1PoiDelNum");
						b1PoiUpdateNum += (int)subData.get("b1PoiUpdateNum");
						b1CommitNum += (int)subData.get("b1CommitNum");
						b1CommitFreshNum += (int)subData.get("b1CommitFreshNum");
						b1CommitAddNum += (int)subData.get("b1CommitAddNum");
						b1CommitDelNum += (int)subData.get("b1CommitDelNum");
						b1CommitUpdateNum += (int)subData.get("b1CommitUpdateNum");

						b2PoiNum += (int)subData.get("b2PoiNum");
						b2PoiFreshNum += (int)subData.get("b2PoiFreshNum");
						b2PoiAddNum += (int)subData.get("b2PoiAddNum");
						b2PoiDelNum += (int)subData.get("b2PoiDelNum");
						b2PoiUpdateNum += (int)subData.get("b2PoiUpdateNum");
						b2CommitNum += (int)subData.get("b2CommitNum");
						b2CommitFreshNum += (int)subData.get("b2CommitFreshNum");
						b2CommitAddNum += (int)subData.get("b2CommitAddNum");
						b2CommitDelNum += (int)subData.get("b2CommitDelNum");
						b2CommitUpdateNum += (int)subData.get("b2CommitUpdateNum");

						b3PoiNum += (int)subData.get("b3PoiNum");
						b3PoiFreshNum += (int)subData.get("b3PoiFreshNum");
						b3PoiAddNum += (int)subData.get("b3PoiAddNum");
						b3PoiDelNum += (int)subData.get("b3PoiDelNum");
						b3PoiUpdateNum += (int)subData.get("b3PoiUpdateNum");
						b3CommitNum += (int)subData.get("b3CommitNum");
						b3CommitFreshNum += (int)subData.get("b3CommitFreshNum");
						b3CommitAddNum += (int)subData.get("b3CommitAddNum");
						b3CommitDelNum += (int)subData.get("b3CommitDelNum");
						b3CommitUpdateNum += (int)subData.get("b3CommitUpdateNum");

						b4PoiNum += (int)subData.get("b4PoiNum");
						b4PoiFreshNum += (int)subData.get("b4PoiFreshNum");
						b4PoiAddNum += (int)subData.get("b4PoiAddNum");
						b4PoiDelNum += (int)subData.get("b4PoiDelNum");
						b4PoiUpdateNum += (int)subData.get("b4PoiUpdateNum");
						b4CommitNum += (int)subData.get("b4CommitNum");
						b4CommitFreshNum += (int)subData.get("b4CommitFreshNum");
						b4CommitAddNum += (int)subData.get("b4CommitAddNum");
						b4CommitDelNum += (int)subData.get("b4CommitDelNum");
						b4CommitUpdateNum += (int)subData.get("b4CommitUpdateNum");

						cPoiNum += (int)subData.get("cPoiNum");
						cPoiFreshNum += (int)subData.get("cPoiFreshNum");
						cPoiAddNum += (int)subData.get("cPoiAddNum");
						cPoiDelNum += (int)subData.get("cPoiDelNum");
						cPoiUpdateNum += (int)subData.get("cPoiUpdateNum");
						cCommitNum += (int)subData.get("cCommitNum");
						cCommitFreshNum += (int)subData.get("cCommitFreshNum");
						cCommitAddNum += (int)subData.get("cCommitAddNum");
						cCommitDelNum += (int)subData.get("cCommitDelNum");
						cCommitUpdateNum += (int)subData.get("cCommitUpdateNum");

					}
				}
				//汇总数据放入map中
				Map<String, Object> dataMap = new HashMap<>(128);
				dataMap.put("key", key);
				dataMap.put("taskId", taskId);
				dataMap.put("userId", userId);
				dataMap.put("cityName", cityName);
				dataMap.put("userName", userName);
				dataMap.put("taskName", taskName);
				dataMap.put("leaderName", leaderName);
				dataMap.put("linkAllLen", linkAllLen);
				dataMap.put("link27AllLen", link27AllLen);
				dataMap.put("tipsAddLen", tipsAddLen);
				dataMap.put("tipsAllNum", tipsAllNum);
				dataMap.put("poiAllNum", poiAllNum);
				dataMap.put("poiUploadNum", poiUploadNum);
				dataMap.put("poiFreshNum", poiFreshNum);
				dataMap.put("poiFinishNum", poiFinishNum);
				dataMap.put("deleteCount", deleteCount);
				dataMap.put("increaseAndAlterCount", increaseAndAlterCount);
				
				dataMap.put("startDate", startDate);
				dataMap.put("endDate", endDate);
				dataMap.put("workTime", workTime);
				dataMap.put("effectiveTime", effectiveTime);
				
				dataMap.put("fccUpdateLen", fccUpdateLen);
				dataMap.put("workDay", workDay);
				dataMap.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
				
				dataMap.put("aPoiNum", aPoiNum);
				dataMap.put("aPoiFreshNum", aPoiFreshNum);
				dataMap.put("aPoiAddNum", aPoiAddNum);
				dataMap.put("aPoiDelNum", aPoiDelNum);
				dataMap.put("aPoiUpdateNum", aPoiUpdateNum);
				dataMap.put("aCommitNum", aCommitNum);
				dataMap.put("aCommitFreshNum", aCommitFreshNum);
				dataMap.put("aCommitAddNum", aCommitAddNum);
				dataMap.put("aCommitDelNum", aCommitDelNum);
				dataMap.put("aCommitUpdateNum", aCommitUpdateNum);
				
				dataMap.put("b1PoiNum", b1PoiNum);
				dataMap.put("b1PoiFreshNum", b1PoiFreshNum);
				dataMap.put("b1PoiAddNum", b1PoiAddNum);
				dataMap.put("b1PoiDelNum", b1PoiDelNum);
				dataMap.put("b1PoiUpdateNum", b1PoiUpdateNum);
				dataMap.put("b1CommitNum", b1CommitNum);
				dataMap.put("b1CommitFreshNum", b1CommitFreshNum);
				dataMap.put("b1CommitAddNum", b1CommitAddNum);
				dataMap.put("b1CommitDelNum", b1CommitDelNum);
				dataMap.put("b1CommitUpdateNum", b1CommitUpdateNum);
				
				dataMap.put("b2PoiNum", b2PoiNum);
				dataMap.put("b2PoiFreshNum", b2PoiFreshNum);
				dataMap.put("b2PoiAddNum", b2PoiAddNum);
				dataMap.put("b2PoiDelNum", b2PoiDelNum);
				dataMap.put("b2PoiUpdateNum", b2PoiUpdateNum);
				dataMap.put("b2CommitNum", b2CommitNum);
				dataMap.put("b2CommitFreshNum", b2CommitFreshNum);
				dataMap.put("b2CommitAddNum", b2CommitAddNum);
				dataMap.put("b2CommitDelNum", b2CommitDelNum);
				dataMap.put("b2CommitUpdateNum", b2CommitUpdateNum);
				
				dataMap.put("b3PoiNum", b3PoiNum);
				dataMap.put("b3PoiFreshNum", b3PoiFreshNum);
				dataMap.put("b3PoiAddNum", b3PoiAddNum);
				dataMap.put("b3PoiDelNum", b3PoiDelNum);
				dataMap.put("b3PoiUpdateNum", b3PoiUpdateNum);
				dataMap.put("b3CommitNum", b3CommitNum);
				dataMap.put("b3CommitFreshNum", b3CommitFreshNum);
				dataMap.put("b3CommitAddNum", b3CommitAddNum);
				dataMap.put("b3CommitDelNum", b3CommitDelNum);
				dataMap.put("b3CommitUpdateNum", b3CommitUpdateNum);
				
				dataMap.put("b4PoiNum", b4PoiNum);
				dataMap.put("b4PoiFreshNum", b4PoiFreshNum);
				dataMap.put("b4PoiAddNum", b4PoiAddNum);
				dataMap.put("b4PoiDelNum", b4PoiDelNum);
				dataMap.put("b4PoiUpdateNum", b4PoiUpdateNum);
				dataMap.put("b4CommitNum", b4CommitNum);
				dataMap.put("b4CommitFreshNum", b4CommitFreshNum);
				dataMap.put("b4CommitAddNum", b4CommitAddNum);
				dataMap.put("b4CommitDelNum", b4CommitDelNum);
				dataMap.put("b4CommitUpdateNum", b4CommitUpdateNum);
				
				dataMap.put("cPoiNum", cPoiNum);
				dataMap.put("cPoiFreshNum", cPoiFreshNum);
				dataMap.put("cPoiAddNum", cPoiAddNum);
				dataMap.put("cPoiDelNum", cPoiDelNum);
				dataMap.put("cPoiUpdateNum", cPoiUpdateNum);
				dataMap.put("cCommitNum", cCommitNum);
				dataMap.put("cCommitFreshNum", cCommitFreshNum);
				dataMap.put("cCommitAddNum", cCommitAddNum);
				dataMap.put("cCommitDelNum", cCommitDelNum);
				dataMap.put("cCommitUpdateNum", cCommitUpdateNum);
				
				dataMap.put("taskFc1len", taskFc1len);
				dataMap.put("taskFc2len", taskFc2len);
				dataMap.put("taskFc3len", taskFc3len);
				dataMap.put("taskFc4len", taskFc4len);
				dataMap.put("taskFc5len", taskFc5len);
				
				dataMap.put("track1UpLen", track1UpLen);
				dataMap.put("track2UpLen", track2UpLen);
				dataMap.put("track3UpLen", track3UpLen);
				dataMap.put("track4UpLen", track4UpLen);
				dataMap.put("track5UpLen", track5UpLen);
				dataMap.put("track6UpLen", track6UpLen);
				dataMap.put("track7UpLen", track7UpLen);
				dataMap.put("track8UpLen", track8UpLen);
				dataMap.put("track9UpLen", track9UpLen);
				dataMap.put("track10UpLen", track10UpLen);
				
				dataMap.put("trackFc1Uplen", trackFc1Uplen);
				dataMap.put("trackFc2Uplen", trackFc2Uplen);
				dataMap.put("trackFc3Uplen", trackFc3Uplen);
				dataMap.put("trackFc4Uplen", trackFc4Uplen);
				dataMap.put("trackFc5Uplen", trackFc5Uplen);
				
				dataMap.put("tips1Len", tips1Len);
				dataMap.put("tips2Len", tips2Len);
				dataMap.put("tips3Len", tips3Len);
				dataMap.put("tips4Len", tips4Len);
				dataMap.put("tips5Len", tips5Len);
				dataMap.put("tips6Len", tips6Len);
				dataMap.put("tips7Len", tips7Len);
				dataMap.put("tips8Len", tips8Len);
				dataMap.put("tips9Len", tips9Len);
				dataMap.put("tips10Len", tips10Len);
				
				dataMap.put("addNum", addNum);
				dataMap.put("upNum", upNum);
				dataMap.put("delNum", delNum);
				
				keyMaps.add(dataMap);
			}
			result.put("person", keyMaps);
			//log.info("result:" + result);
			JSONObject identifyJson=new JSONObject();
			identifyJson.put("timestamp", statReq.getTimestamp());
			identifyJson.put("workDay", statReq.getWorkDay());
			statReq.setIdentifyJson(identifyJson);
			statReq.setIdentify("timestamp:"+statReq.getTimestamp()+",workDay:"+statReq.getWorkDay());
			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	

//	/**
//	 * 从mango库中查询统计数据
//	 * @param String
//	 * @param Map<String, Object>
//	 * 
//	 * */
//	@SuppressWarnings("unchecked")
//	public Map<Integer, Object> queryDataFromMongo(MongoDao md, String timestamp){
//		
//		//从mongo库差personTips和personDay和task的数据
//		Map<Integer, Object> personTips = queryPersonTips(timestamp, md);
//		Map<Integer, Object> personDay = queryPersonDay(timestamp, md);
//		
//		//personTips和personDay的数据放在一个map中
//		for(Entry<Integer, Object> entry : personTips.entrySet()){
//			int subtaskId = entry.getKey();
//			Map<String, Object> subMap = (Map<String, Object>) entry.getValue();
//			int poiUploadNum = 0;
//			int poiFreshNum = 0;
//			int poiFinishNum = 0;
//			double tipsAddLen = (double) subMap.get("tipsAddLen");
//			double tipsAllLen = (double) subMap.get("tipsAllLen");
//			
//    		int deleteCount = 0;
//    		int increaseAndAlterCount = 0;
//			if(personDay.containsKey(subtaskId)){
//				Map<String, Integer> daySubMap = (Map<String, Integer>) personDay.get(subtaskId);
//				poiUploadNum = daySubMap.get("poiUploadNum");
//				poiFreshNum = daySubMap.get("poiFreshNum");
//				poiFinishNum = daySubMap.get("poiFinishNum");
//				deleteCount = daySubMap.get("deleteCount");
//				increaseAndAlterCount = daySubMap.get("increaseAndAlterCount");
//			}
//			subMap.put("poiUploadNum", poiUploadNum);
//			subMap.put("poiFreshNum", poiFreshNum);
//			subMap.put("poiFinishNum", poiFinishNum);
//			subMap.put("tipsAddLen", tipsAddLen);
//			subMap.put("tipsAllLen", tipsAllLen);
//			subMap.put("deleteCount", deleteCount);
//			subMap.put("increaseAndAlterCount", increaseAndAlterCount);
//			personTips.put(subtaskId, subMap);
//		}
//		return personTips;
//	}
	
	/**
	 * 查询personTips中的数据放入对应map中
	 * @param String
	 * @param MongoDao
	 * @return  Map<Integer, Object>
	 * 
	 * */
	public Map<Integer, Object> queryPersonTips(String timestamp,String workDay, MongoDao md){
		Map<Integer, Object> result = new HashMap<>();
		String personTipsName = "person_tips";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);		
		query.put("workDay", workDay);
		MongoCursor<Document> personTips = md.find(personTipsName, query).iterator();
		//统计一个任务下所有子任务的personTips
		while(personTips.hasNext()){
			JSONObject tipsJson = JSONObject.fromObject(personTips.next());
			double tipsAddLen = 0;
			long tipsAllNum = 0;
			int subtaskId = 0;
			Map<String, Object> map = new HashMap<>(32);
			subtaskId = Integer.parseInt(tipsJson.get("subtaskId").toString());
			tipsAddLen = Double.valueOf(tipsJson.get("tipsAddLen").toString());
			tipsAllNum = Long.valueOf(tipsJson.get("tipsAllNum").toString());
			//modiby by songhe 2017/11/01  添加了十个统计项
			int tips1Len = tipsJson.containsKey("tips1Len") ? (int) tipsJson.get("tips1Len") : 0;
			int tips2Len = tipsJson.containsKey("tips2Len") ? (int) tipsJson.get("tips2Len") : 0;
			int tips3Len = tipsJson.containsKey("tips3Len") ? (int) tipsJson.get("tips3Len") : 0;
			int tips4Len = tipsJson.containsKey("tips4Len") ? (int) tipsJson.get("tips4Len") : 0;
			int tips5Len = tipsJson.containsKey("tips5Len") ? (int) tipsJson.get("tips5Len") : 0;
			int tips6Len = tipsJson.containsKey("tips6Len") ? (int) tipsJson.get("tips6Len") : 0;
			int tips7Len = tipsJson.containsKey("tips7Len") ? (int) tipsJson.get("tips7Len") : 0;
			int tips8Len = tipsJson.containsKey("tips8Len") ? (int) tipsJson.get("tips8Len") : 0;
			int tips9Len = tipsJson.containsKey("tips9Len") ? (int) tipsJson.get("tips9Len") : 0;
			int tips10Len = tipsJson.containsKey("tips10Len") ? (int) tipsJson.get("tips10Len") : 0;
			
			int addNum = tipsJson.containsKey("addNum") ? (int) tipsJson.get("addNum") : 0;
			int upNum = tipsJson.containsKey("upNum") ? (int) tipsJson.get("upNum") : 0;
			int delNum = tipsJson.containsKey("delNum") ? (int) tipsJson.get("delNum") : 0;
			
			map.put("tips1Len", tips1Len);
			map.put("tips2Len", tips2Len);
			map.put("tips3Len", tips3Len);
			map.put("tips4Len", tips4Len);
			map.put("tips5Len", tips5Len);
			map.put("tips6Len", tips6Len);
			map.put("tips7Len", tips7Len);
			map.put("tips8Len", tips8Len);
			map.put("tips9Len", tips9Len);
			map.put("tips10Len", tips10Len);
			
			map.put("tipsAddLen", tipsAddLen);
			map.put("tipsAllNum", tipsAllNum);
			
			map.put("addNum", addNum);
			map.put("upNum", upNum);
			map.put("delNum", delNum);
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
	public Map<Integer, Object> queryPersonDay(String timestamp,String workDay, MongoDao md){
		String personTableName = "person_day";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);		
		query.put("workDay", workDay);
		MongoCursor<Document> person = md.find(personTableName, query).iterator();
		
		Map<Integer, Object> subtaskData = new HashMap<>(64);
		//统计所有的子任务的数据
		while(person.hasNext()){
			JSONObject json = JSONObject.fromObject(person.next());
			
			Map<String, Integer> map = new HashMap<>();
			int subtaskId = (int) json.get("subtaskId");
			int poiUploadNum = (int) json.get("uploadNum");
			int poiFreshNum = (int) json.get("freshNum");
			int poiFinishNum = (int) json.get("finishNum");
    		int deleteCount = (int) json.get("deleteCount");
    		int increaseAndAlterCount = (int) json.get("increaseAndAlterCount");
    		//modiby by songhe 2017/11/01   添加了很多统计字段
			int aPoiNum = Integer.parseInt(json.get("aPoiNum").toString());
			int aPoiFreshNum = Integer.parseInt(json.get("aPoiFreshNum").toString());
			int aPoiAddNum = Integer.parseInt(json.get("aPoiAddNum").toString());
			int aPoiDelNum = Integer.parseInt(json.get("aPoiDelNum").toString());
			int aPoiUpdateNum = Integer.parseInt(json.get("aPoiUpdateNum").toString());
			int aCommitNum = Integer.parseInt(json.get("aCommitNum").toString());
			int aCommitFreshNum = Integer.parseInt(json.get("aCommitFreshNum").toString());
			int aCommitAddNum = Integer.parseInt(json.get("aCommitAddNum").toString());
			int aCommitDelNum = Integer.parseInt(json.get("aCommitDelNum").toString());
			int aCommitUpdateNum = Integer.parseInt(json.get("aCommitUpdateNum").toString());
			
			int b1PoiNum = Integer.parseInt(json.get("b1PoiNum").toString());
			int b1PoiFreshNum = Integer.parseInt(json.get("b1PoiFreshNum").toString());
			int b1PoiAddNum = Integer.parseInt(json.get("b1PoiAddNum").toString());
			int b1PoiDelNum = Integer.parseInt(json.get("b1PoiDelNum").toString());
			int b1PoiUpdateNum = Integer.parseInt(json.get("b1PoiUpdateNum").toString());
			int b1CommitNum = Integer.parseInt(json.get("b1CommitNum").toString());
			int b1CommitFreshNum = Integer.parseInt(json.get("b1CommitFreshNum").toString());
			int b1CommitAddNum = Integer.parseInt(json.get("b1CommitAddNum").toString());
			int b1CommitDelNum = Integer.parseInt(json.get("b1CommitDelNum").toString());
			int b1CommitUpdateNum = Integer.parseInt(json.get("b1CommitUpdateNum").toString());
			
			int b2PoiNum = Integer.parseInt(json.get("b2PoiNum").toString());
			int b2PoiFreshNum = Integer.parseInt(json.get("b2PoiFreshNum").toString());
			int b2PoiAddNum = Integer.parseInt(json.get("b2PoiAddNum").toString());
			int b2PoiDelNum = Integer.parseInt(json.get("b2PoiDelNum").toString());
			int b2PoiUpdateNum = Integer.parseInt(json.get("b2PoiUpdateNum").toString());
			int b2CommitNum = Integer.parseInt(json.get("b2CommitNum").toString());
			int b2CommitFreshNum = Integer.parseInt(json.get("b2CommitFreshNum").toString());
			int b2CommitAddNum = Integer.parseInt(json.get("b2CommitAddNum").toString());
			int b2CommitDelNum = Integer.parseInt(json.get("b2CommitDelNum").toString());
			int b2CommitUpdateNum = Integer.parseInt(json.get("b2CommitUpdateNum").toString());
			
			int b3PoiNum = Integer.parseInt(json.get("b3PoiNum").toString());
			int b3PoiFreshNum = Integer.parseInt(json.get("b3PoiFreshNum").toString());
			int b3PoiAddNum = Integer.parseInt(json.get("b3PoiAddNum").toString());
			int b3PoiDelNum = Integer.parseInt(json.get("b3PoiDelNum").toString());
			int b3PoiUpdateNum = Integer.parseInt(json.get("b3PoiUpdateNum").toString());
			int b3CommitNum = Integer.parseInt(json.get("b3CommitNum").toString());
			int b3CommitFreshNum = Integer.parseInt(json.get("b3CommitFreshNum").toString());
			int b3CommitAddNum = Integer.parseInt(json.get("b3CommitAddNum").toString());
			int b3CommitDelNum = Integer.parseInt(json.get("b3CommitDelNum").toString());
			int b3CommitUpdateNum = Integer.parseInt(json.get("b3CommitUpdateNum").toString());
			
			int b4PoiNum = Integer.parseInt(json.get("b4PoiNum").toString());
			int b4PoiFreshNum = Integer.parseInt(json.get("b4PoiFreshNum").toString());
			int b4PoiAddNum = Integer.parseInt(json.get("b4PoiAddNum").toString());
			int b4PoiDelNum = Integer.parseInt(json.get("b4PoiDelNum").toString());
			int b4PoiUpdateNum = Integer.parseInt(json.get("b4PoiUpdateNum").toString());
			int b4CommitNum = Integer.parseInt(json.get("b4CommitNum").toString());
			int b4CommitFreshNum = Integer.parseInt(json.get("b4CommitFreshNum").toString());
			int b4CommitAddNum = Integer.parseInt(json.get("b4CommitAddNum").toString());
			int b4CommitDelNum = Integer.parseInt(json.get("b4CommitDelNum").toString());
			int b4CommitUpdateNum = Integer.parseInt(json.get("b4CommitUpdateNum").toString());
			
			int cPoiNum = Integer.parseInt(json.get("cPoiNum").toString());
			int cPoiFreshNum = Integer.parseInt(json.get("cPoiFreshNum").toString());
			int cPoiAddNum = Integer.parseInt(json.get("cPoiAddNum").toString());
			int cPoiDelNum = Integer.parseInt(json.get("cPoiDelNum").toString());
			int cPoiUpdateNum = Integer.parseInt(json.get("cPoiUpdateNum").toString());
			int cCommitNum = Integer.parseInt(json.get("cCommitNum").toString());
			int cCommitFreshNum = Integer.parseInt(json.get("cCommitFreshNum").toString());
			int cCommitAddNum = Integer.parseInt(json.get("cCommitAddNum").toString());
			int cCommitDelNum = Integer.parseInt(json.get("cCommitDelNum").toString());
			int cCommitUpdateNum = Integer.parseInt(json.get("cCommitUpdateNum").toString());
			
			map.put("aPoiNum", aPoiNum);
			map.put("aPoiFreshNum", aPoiFreshNum);
			map.put("aPoiAddNum", aPoiAddNum);
			map.put("aPoiDelNum", aPoiDelNum);
			map.put("aPoiUpdateNum", aPoiUpdateNum);
			map.put("aCommitNum", aCommitNum);
			map.put("aCommitFreshNum", aCommitFreshNum);
			map.put("aCommitAddNum", aCommitAddNum);
			map.put("aCommitDelNum", aCommitDelNum);
			map.put("aCommitUpdateNum", aCommitUpdateNum);
			
			map.put("b1PoiNum", b1PoiNum);
			map.put("b1PoiFreshNum", b1PoiFreshNum);
			map.put("b1PoiAddNum", b1PoiAddNum);
			map.put("b1PoiDelNum", b1PoiDelNum);
			map.put("b1PoiUpdateNum", b1PoiUpdateNum);
			map.put("b1CommitNum", b1CommitNum);
			map.put("b1CommitFreshNum", b1CommitFreshNum);
			map.put("b1CommitAddNum", b1CommitAddNum);
			map.put("b1CommitDelNum", b1CommitDelNum);
			map.put("b1CommitUpdateNum", b1CommitUpdateNum);
			
			map.put("b2PoiNum", b2PoiNum);
			map.put("b2PoiFreshNum", b2PoiFreshNum);
			map.put("b2PoiAddNum", b2PoiAddNum);
			map.put("b2PoiDelNum", b2PoiDelNum);
			map.put("b2PoiUpdateNum", b2PoiUpdateNum);
			map.put("b2CommitNum", b2CommitNum);
			map.put("b2CommitFreshNum", b2CommitFreshNum);
			map.put("b2CommitAddNum", b2CommitAddNum);
			map.put("b2CommitDelNum", b2CommitDelNum);
			map.put("b2CommitUpdateNum", b2CommitUpdateNum);
			
			map.put("b3PoiNum", b3PoiNum);
			map.put("b3PoiFreshNum", b3PoiFreshNum);
			map.put("b3PoiAddNum", b3PoiAddNum);
			map.put("b3PoiDelNum", b3PoiDelNum);
			map.put("b3PoiUpdateNum", b3PoiUpdateNum);
			map.put("b3CommitNum", b3CommitNum);
			map.put("b3CommitFreshNum", b3CommitFreshNum);
			map.put("b3CommitAddNum", b3CommitAddNum);
			map.put("b3CommitDelNum", b3CommitDelNum);
			map.put("b3CommitUpdateNum", b3CommitUpdateNum);
			
			map.put("b4PoiNum", b4PoiNum);
			map.put("b4PoiFreshNum", b4PoiFreshNum);
			map.put("b4PoiAddNum", b4PoiAddNum);
			map.put("b4PoiDelNum", b4PoiDelNum);
			map.put("b4PoiUpdateNum", b4PoiUpdateNum);
			map.put("b4CommitNum", b4CommitNum);
			map.put("b4CommitFreshNum", b4CommitFreshNum);
			map.put("b4CommitAddNum", b4CommitAddNum);
			map.put("b4CommitDelNum", b4CommitDelNum);
			map.put("b4CommitUpdateNum", b4CommitUpdateNum);
			
			map.put("cPoiNum", cPoiNum);
			map.put("cPoiFreshNum", cPoiFreshNum);
			map.put("cPoiAddNum", cPoiAddNum);
			map.put("cPoiDelNum", cPoiDelNum);
			map.put("cPoiUpdateNum", cPoiUpdateNum);
			map.put("cCommitNum", cCommitNum);
			map.put("cCommitFreshNum", cCommitFreshNum);
			map.put("cCommitAddNum", cCommitAddNum);
			map.put("cCommitDelNum", cCommitDelNum);
			map.put("cCommitUpdateNum", cCommitUpdateNum);
			
			map.put("poiUploadNum", poiUploadNum);
			map.put("poiFreshNum", poiFreshNum);
			map.put("poiFinishNum", poiFinishNum);
			map.put("deleteCount", deleteCount);
			map.put("increaseAndAlterCount", increaseAndAlterCount);
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
			//modiby by songhe 2017/11/01  添加了五个统计字段
			double taskFc1len = Double.valueOf(json.get("taskFc1len").toString());
			double taskFc2len = Double.valueOf(json.get("taskFc2len").toString());
			double taskFc3len = Double.valueOf(json.get("taskFc3len").toString());
			double taskFc4len = Double.valueOf(json.get("taskFc4len").toString());
			double taskFc5len = Double.valueOf(json.get("taskFc5len").toString());
			
			taskData.put("taskFc1len", taskFc1len);
			taskData.put("taskFc2len", taskFc2len);
			taskData.put("taskFc3len", taskFc3len);
			taskData.put("taskFc4len", taskFc4len);
			taskData.put("taskFc5len", taskFc5len);
			
			taskData.put("linkAllLen", linkAllLen);
			taskData.put("link27AllLen", link27AllLen);
			taskData.put("poiAllNum", poiAllNum);
			
			tasks.put(taskId, taskData);
		}
		return tasks;
	}
	
	/**
	 * 查询queryPersonFcc中的数据放入对应map中
	 * @param String
	 * @param MongoDao
	 * @return  Map<Integer, Object>
	 * 
	 * */
	public Map<Integer, Object> queryPersonFcc(String timestamp,String workDay, MongoDao md){
		Map<Integer, Object> result = new HashMap<>();
		String personFccName = "person_fcc";
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);	
		query.put("workDay", workDay);
		MongoCursor<Document> personFcc = md.find(personFccName, query).iterator();
		while(personFcc.hasNext()){
			JSONObject fccJson = JSONObject.fromObject(personFcc.next());
			Map<String, Object> map = new HashMap<>(32);
			List<Map<String, Object>> tasks = new ArrayList<>();
			int userId = Integer.parseInt(fccJson.get("userId").toString());
			int taskId = Integer.parseInt(fccJson.get("taskId").toString());
			double fccUpdateLen = Double.valueOf(fccJson.get("linkLen").toString());
			double effectiveTime=Double.valueOf(fccJson.get("effectiveTime").toString());
			String startCollectTime = (StringUtils.isBlank(fccJson.get("startCollectTime").toString()) ? df.format(new Date()) : fccJson.get("startCollectTime").toString());
			String endCollectTime = (StringUtils.isBlank(fccJson.get("endCollectTime").toString()) ? df.format(new Date()) : fccJson.get("endCollectTime").toString());
			String workTime = "";
			log.warn("taskId="+taskId+",startCollectTime="+startCollectTime+",endCollectTime="+endCollectTime);
			try {
				String reg = "(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})";
				Date begin = df.parse(startCollectTime.replaceAll(reg, "$1-$2-$3 $4:$5:$6"));
				Date end = df.parse(endCollectTime.replaceAll(reg, "$1-$2-$3 $4:$5:$6"));
				long between = (end.getTime() - begin.getTime())/1000;//除以1000是为了转换成秒   
				//int day = (int) (between/(24*3600));   
				//int hour = (int) (between%(24*3600)/3600);   
				//int minute = (int) (between%3600/60);   
				//int second = (int) (between%60);
				double hour = (double) (between/3600);
				workTime = String.valueOf(hour);
			}catch(ParseException e){
				log.error("taskId="+taskId+",startCollectTime="+startCollectTime+",endCollectTime="+endCollectTime, e);
			}
			
			//modiby by songhe 2017/11/01  添加了很多统计项
			double track1UpLen = fccJson.containsKey("track1UpLen") ? Double.valueOf(fccJson.get("track1UpLen").toString()) : 0;
			double track2UpLen = fccJson.containsKey("track2UpLen") ? Double.valueOf(fccJson.get("track2UpLen").toString()) : 0;
			double track3UpLen = fccJson.containsKey("track3UpLen") ? Double.valueOf(fccJson.get("track3UpLen").toString()) : 0;
			double track4UpLen = fccJson.containsKey("track4UpLen") ? Double.valueOf(fccJson.get("track4UpLen").toString()) : 0;
			double track5UpLen = fccJson.containsKey("track5UpLen") ? Double.valueOf(fccJson.get("track5UpLen").toString()) : 0;
			double track6UpLen = fccJson.containsKey("track6UpLen") ? Double.valueOf(fccJson.get("track6UpLen").toString()) : 0;
			double track7UpLen = fccJson.containsKey("track7UpLen") ? Double.valueOf(fccJson.get("track7UpLen").toString()) : 0;
			double track8UpLen = fccJson.containsKey("track8UpLen") ? Double.valueOf(fccJson.get("track8UpLen").toString()) : 0;
			double track9UpLen = fccJson.containsKey("track9UpLen") ? Double.valueOf(fccJson.get("track9UpLen").toString()) : 0;
			double track10UpLen = fccJson.containsKey("track10UpLen") ? Double.valueOf(fccJson.get("track10UpLen").toString()) : 0;
			
			double trackFc1Uplen = fccJson.containsKey("trackFc1Uplen") ? Double.valueOf(fccJson.get("trackFc1Uplen").toString()) : 0;
			double trackFc2Uplen = fccJson.containsKey("trackFc2Uplen") ? Double.valueOf(fccJson.get("trackFc2Uplen").toString()) : 0;
			double trackFc3Uplen = fccJson.containsKey("trackFc3Uplen") ? Double.valueOf(fccJson.get("trackFc3Uplen").toString()) : 0;
			double trackFc4Uplen = fccJson.containsKey("trackFc4Uplen") ? Double.valueOf(fccJson.get("trackFc4Uplen").toString()) : 0;
			double trackFc5Uplen = fccJson.containsKey("trackFc5Uplen") ? Double.valueOf(fccJson.get("trackFc5Uplen").toString()) : 0;
			
			map.put("effectiveTime", effectiveTime);
			map.put("startDate", startCollectTime);
			map.put("endDate", endCollectTime);
			map.put("workTime", workTime);
			map.put("fccUpdateLen", fccUpdateLen);
			map.put("userId", userId);
			
			map.put("track1UpLen", track1UpLen);
			map.put("track2UpLen", track2UpLen);
			map.put("track3UpLen", track3UpLen);
			map.put("track4UpLen", track4UpLen);
			map.put("track5UpLen", track5UpLen);
			map.put("track6UpLen", track6UpLen);
			map.put("track7UpLen", track7UpLen);
			map.put("track8UpLen", track8UpLen);
			map.put("track9UpLen", track9UpLen);
			map.put("track10UpLen", track10UpLen);
			map.put("trackFc1Uplen", trackFc1Uplen);
			map.put("trackFc2Uplen", trackFc2Uplen);
			map.put("trackFc3Uplen", trackFc3Uplen);
			map.put("trackFc4Uplen", trackFc4Uplen);
			map.put("trackFc5Uplen", trackFc5Uplen);
			
			if(result.containsKey(taskId)){
				tasks = (List<Map<String, Object>>) result.get(taskId);
			}
			tasks.add(map);
		    result.put(taskId, tasks);
		}
		return result;
	}
	
}
