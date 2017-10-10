package com.navinfo.dataservice.job.statics.manJob;

import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 任务统计
 * @ClassName DayProduce
 * @author zl
 * @date 2017年9月23日 
 * @Description TODO
 */
public class DayProduceJob extends AbstractStatJob {

	
	private static final String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	protected ManApi manApi = null;
	
	public DayProduceJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			//获取统计时间
			DayProduceJobRequest statReq = (DayProduceJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			long t = System.currentTimeMillis();
			//日出品统计数据
//			Map<String, Object> dayProduceStatMap = new HashMap<String, Object>();
			
			Map<String, Object> dayProduceStatByUrlMap = getDataByDayProduceUrl();
			Map<String,Object>  dataSumMap = getSumInMongo();
			//处理具体数据
			Map<String, Object> dayProduceStatMap = getDayProduceStat(dayProduceStatByUrlMap,dataSumMap);
			
			dayProduceStatByUrlMap.put("dpAverage", dayProduceStatMap);	
			JSONArray produceList=new JSONArray();
			produceList.add(dayProduceStatByUrlMap);
			//处理数据
			JSONObject result = new JSONObject();
			result.put("day_produce",produceList);

			log.info("end stat "+statReq.getJobType());
			log.info(result.toString());
			log.debug("所有日出品数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result.toString();
			
		} catch (Exception e) {
			log.error("日出品统计:"+e.getMessage(), e);
			throw new JobException("日出品统计:"+e.getMessage(),e);
		}
	}
	
	private Map<String, Object> getDayProduceStat(Map<String, Object> dayProduceStatByUrlMap,
			Map<String, Object> dataSumMap) {
		Map<String, Object> getAvgMap = new HashMap<String, Object>();
		double dpUpdateRoadSum = 0;
		double dpAddRoadSum = 0;
		int dpUpdatePoiSum = 0;
		int dpAddPoiSum = 0;
		int total = 0;
		double dpUpdateRoad = 0;
		double dpAddRoad = 0;
		double dpUpdatePoi = 0;
		double dpAddPoi = 0;
		
		//平均值
		double updateRoad = 0;
		double addRoad = 0;
		float updatePoi = 0;
		float addPoi = 0;
		if(dayProduceStatByUrlMap != null && dayProduceStatByUrlMap.size() > 0){
			dpUpdateRoad = (double) dayProduceStatByUrlMap.get("dpUpdateRoad");
			dpAddRoad = (double) dayProduceStatByUrlMap.get("dpAddRoad");
			dpUpdatePoi = (int) dayProduceStatByUrlMap.get("dpUpdatePoi");
			dpAddPoi = (int) dayProduceStatByUrlMap.get("dpAddPoi");
			if(dataSumMap != null && dataSumMap.size()>0){
				dpUpdateRoadSum = (double) dataSumMap.get("dpUpdateRoadSum");
				dpAddRoadSum = (double) dataSumMap.get("dpAddRoadSum");
				dpUpdatePoiSum = (int) dataSumMap.get("dpUpdatePoiSum");
				dpAddPoiSum = (int) dataSumMap.get("dpAddPoiSum");
				total=(int) dataSumMap.get("total");
			}
			updateRoad = (dpUpdateRoadSum+dpUpdateRoad)/(total+1);
			addRoad = (dpAddRoadSum+dpAddRoad)/(total+1);
			updatePoi= (float)(dpUpdatePoiSum+dpUpdatePoi)/(total+1);
			addPoi= (float)(dpAddPoiSum+dpAddPoi)/(total+1);
			
		}

		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00"); 
		getAvgMap.put("updateRoad", Double.parseDouble(df.format(updateRoad)));
		getAvgMap.put("addRoad", Double.parseDouble(df.format(addRoad)));
		getAvgMap.put("updatePoi", Double.parseDouble(df.format(updatePoi)));
		getAvgMap.put("addPoi", Double.parseDouble(df.format(addPoi)));
		return getAvgMap;
	}

	//查询mongo库中所有记录的条数及每列求和
	private Map<String,Object> getSumInMongo() throws Exception{
		Map<String,Object>  dataSumMap = null;
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			//计算总条数
//			total = queryCountInMongo(mongoDao, "day_produce", null);
			//计算每列和
			dataSumMap = queryDatasSumInMongo(mongoDao, "day_produce", null);
			
			return dataSumMap;
		} catch (Exception e) {
			log.error("查询mongo中上一次的任务统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中上一次的任务统计数据报错"+e.getMessage(),e);
		}
	}
	
	public int queryCountInMongo(MongoDao md,String collName,BasicDBObject query){
		int count = 0;
		String personFccName = collName;
		long countL = md.count(personFccName, query);
		count = new Long(countL).intValue();  
		return count;
	}
	
	private Map<String,Object> queryDatasSumInMongo(MongoDao md, String collName, BasicDBObject filter) throws Exception {
		Map<String,Object> datasSumMap = null;
		try {
			datasSumMap = new HashMap<String,Object>();
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			
			double dpUpdateRoadSum = 0;
			double dpAddRoadSum = 0;
			int dpUpdatePoiSum = 0;
			int dpAddPoiSum = 0;
			
			int total = 0;
			//处理数据
			while(iterator.hasNext()){
				total+=1;
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				
				if(jso.containsKey("dpUpdateRoad")){
					dpUpdateRoadSum += jso.getDouble("dpUpdateRoad");
				}
				if(jso.containsKey("dpAddRoad")){
					dpAddRoadSum += jso.getDouble("dpAddRoad");
				}
				if(jso.containsKey("dpUpdatePoi")){
					dpUpdatePoiSum += jso.getDouble("dpUpdatePoi");
				}
				if(jso.containsKey("dpAddPoi")){
					dpAddPoiSum += jso.getDouble("dpAddPoi");
				}
			}
			datasSumMap.put("dpUpdateRoadSum", dpUpdateRoadSum);
			datasSumMap.put("dpAddRoadSum", dpAddRoadSum);
			datasSumMap.put("dpUpdatePoiSum", dpUpdatePoiSum);
			datasSumMap.put("dpAddPoiSum", dpAddPoiSum);
			datasSumMap.put("total", total);
			return datasSumMap;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中数据求和报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中数据求和报错"+e.getMessage(),e);
		}
	}
	
	//调用日出品接口
	private Map<String,Object> getDataByDayProduceUrl(){
			Map<String,Object> dayProduceMap = null;
		try{
			dayProduceMap = new HashMap<String,Object>();
			Map<String,String> parMap = new HashMap<String,String>();
			parMap.put("parameter", null);
			parMap.put("operate", "getDayProduceData");
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.dayProduceStatUrl);
			log.debug(" DayProduceUrl: "+msUrl);
			String result = ServiceInvokeUtil.invoke(msUrl, parMap, 10000);
			JSONObject resJson = JSONObject.fromObject(result);
			JSONObject dataJson = resJson.getJSONObject("msg");
			double dpUpdateRoad = 0;
			double dpAddRoad = 0;
			int dpUpdatePoi = 0;
			int dpAddPoi = 0;
//			double dpAverage = 0;
			if(dataJson.containsKey("dpUpdateRoad")){
				dpUpdateRoad = dataJson.getDouble("dpUpdateRoad");
			}
			if(dataJson.containsKey("dpAddRoad")){
				dpAddRoad = dataJson.getDouble("dpAddRoad");
			}
			if(dataJson.containsKey("dpUpdatePoi")){
				dpUpdatePoi = dataJson.getInt("dpUpdatePoi");
			}
			if(dataJson.containsKey("dpAddPoi")){
				dpAddPoi = dataJson.getInt("dpAddPoi");
			}
			dayProduceMap.put("dpUpdateRoad", dpUpdateRoad);
			dayProduceMap.put("dpAddRoad", dpAddRoad);
			dayProduceMap.put("dpUpdatePoi", dpUpdatePoi);
			dayProduceMap.put("dpAddPoi", dpAddPoi);
			
			log.debug("调用日出品统计接口成功 result:"+result);
		}catch(Exception e){
			try{
			}catch(Exception ex){
				log.error(ex.getMessage(),ex);
			}
			log.warn("调用日出品统计接口出错");
			log.error(e.getMessage(),e);
		}
		return dayProduceMap;
	}

	
}
