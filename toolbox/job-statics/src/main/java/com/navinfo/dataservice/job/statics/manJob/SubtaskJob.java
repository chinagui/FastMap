package com.navinfo.dataservice.job.statics.manJob;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 子任务统计
 * @ClassName SubtaskJob
 * @author Han Shaoming
 * @date 2017年8月1日 上午11:29:36
 * @Description TODO
 */
public class SubtaskJob extends AbstractStatJob {
	private static final String subtask = "subtask";
	private static final String grid_tips = "grid_tips";
//	private static final String subtask_tips = "subtask_tips";
	private static final String subtask_day_poi = "subtask_day_poi";
	private static final String grid_month_poi = "grid_month_poi";
	private static final String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	protected ManApi manApi = null;
	 
	public SubtaskJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			//获取统计时间
			SubtaskJobRequest statReq = (SubtaskJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			String timestamp = statReq.getTimestamp();
			
			manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			//查询MAN_TIMELINE表获取相应的数据
			String objName = "subtask";
			Map<Integer, Map<String, Object>> manTimelineStart = manApi.queryManTimelineByObjName(objName,1);
			Map<Integer, Map<String, Object>> manTimelineEnd = manApi.queryManTimelineByObjName(objName,0);
			//执行统计
			List<Subtask> subtaskListNeedStat = OracleDao.getSubtaskListNeedStatistics();
			//获取已关闭的统计
			List<Map<String, Object>> subtaskStatList = new ArrayList<Map<String, Object>>();
			List<Document> subtaskListWithStat = OracleDao.getSubtaskListWithStatistics();
			Map<Integer, Map<String, Object>> subtaskStatData = getSubtaskStatData(timestamp);
			for (Document document : subtaskListWithStat) {
				int subtaskId = (int) document.get("subtaskId");
				if(subtaskStatData.containsKey(subtaskId)){
					subtaskStatList.add(subtaskStatData.get(subtaskId));
				}
			}
			//查询mongo库处理数据
			Map<Integer, Map<String, Object>> dayPoiStatData = getDayPoiStatData(timestamp);
			Map<Integer, Map<String, Integer>> monthPoiStatData = getMonthPoiStatData(timestamp);
			Map<Integer, Map<String, Integer>> tipsStatData = getTipsStatData(timestamp);
//			Map<Integer, Map<String, Object>> subTipsStatData = getSubTipsStatData(timestamp);
			//统计子任务数据
			Iterator<Subtask> subtaskItr = subtaskListNeedStat.iterator();
			while(subtaskItr.hasNext()){
				Subtask subtask = subtaskItr.next();
				int subtaskId = subtask.getSubtaskId();
				//获取grid
				Map<Integer, Integer> gridIds = manApi.getGridIdMapBySubtaskId(subtaskId);
				subtask.setGridIds(gridIds);
				//获取相应的统计数据
				Map<String, Object> subManTimelineStart = null;
				Map<String, Object> subManTimelineEnd = null;
				Map<String, Object> subDayPoiStat = null;
				Map<String, Object> subTipsStat = null;
				if(manTimelineStart.containsKey(subtaskId)){
					subManTimelineStart = manTimelineStart.get(subtaskId);
				}
				if(manTimelineEnd.containsKey(subtaskId)){
					subManTimelineEnd = manTimelineEnd.get(subtaskId);
				}
				if(dayPoiStatData.containsKey(subtaskId)){
					subDayPoiStat = dayPoiStatData.get(subtaskId);
				}
//				if(subTipsStatData.containsKey(subtaskId)){
//					subTipsStat = subTipsStatData.get(subtaskId);
//				}
				Map<String, Integer> subMonthPoiStat = handleMonthPoiStatData(subtask, monthPoiStatData);
				Map<String, Integer> tipsStat = handleTipsStatData(subtask, tipsStatData);
				//处理具体统计数据
				Map<String, Object> subtaskMap = getSubtaskStat(subtask,subManTimelineStart,subManTimelineEnd,tipsStat,subMonthPoiStat,subDayPoiStat,subTipsStat);
				
				subtaskStatList.add(subtaskMap);
			}
			
			
			//处理数据
			JSONObject result = new JSONObject();
			result.put("subtask",subtaskStatList);

			log.info("end stat "+statReq.getJobType());
			long t = System.currentTimeMillis();
			log.debug("所有日库子任务数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result.toString();
			
		} catch (Exception e) {
			log.error("子任务统计:"+e.getMessage(), e);
			throw new JobException("子任务统计:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中已关闭的子任务的统计数据
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer,Map<String,Object>> getSubtaskStatData(String timestamp) throws Exception{
		try {
			//获取上一次的统计时间
			String lastTime = DateUtils.addSeconds(timestamp,-60*60);
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", lastTime);
			FindIterable<Document> findIterable = mongoDao.find(subtask, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				if(json.containsKey("content")){
					JSONArray content = json.getJSONArray("content");
					for(int i=0;i<content.size();i++){
						JSONObject jso = content.getJSONObject(i);
						int subtaskId = (int) jso.get("subtaskId");
						Map<String,Object> map = jso;
						stat.put(subtaskId, map);
					}
				}
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中已关闭的子任务的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中已关闭的子任务的统计数据报错"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中poi日编相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getDayPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(subtask_day_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				if(json.containsKey("content")){
					JSONArray content = json.getJSONArray("content");
					for(int i=0;i<content.size();i++){
						Map<String,Object> subtask = new HashMap<String,Object>();
						JSONObject jso = content.getJSONObject(i);
						int subtaskId = (int) jso.get("subtaskId");
						int poiUploadNum = (int) jso.get("poiUploadNum");
						int poiFinishNum = (int) jso.get("poiFinishNum");
						String firstEditDate = (String) jso.get("firstEditDate");
						String firstCollectDate = (String) jso.get("firstCollectDate");
						subtask.put("poiCollectUploadNum", poiUploadNum);
						subtask.put("poiFinishNum", poiFinishNum);
						subtask.put("firstEditDate", firstEditDate);
						subtask.put("firstCollectDate", firstCollectDate);
						stat.put(subtaskId, subtask);
					}
				}
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo库中subtask_day_poi统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中subtask_day_poi统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中poi月编相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Integer>> getMonthPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_month_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Integer>> monthPoiStat = new HashMap<Integer,Map<String,Integer>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				if(json.containsKey("content")){
					JSONArray content = json.getJSONArray("content");
					for(int i=0;i<content.size();i++){
						Map<String,Integer> subtask = new HashMap<String,Integer>();
						JSONObject jso = content.getJSONObject(i);
						int gridId = (int) jso.get("gridId");
						int logAllNum = (int) jso.get("logAllNum");
						int logFinishNum = (int) jso.get("logFinishNum");
						subtask.put("logAllNum", logAllNum);
						subtask.put("logFinishNum", logFinishNum);
						monthPoiStat.put(gridId, subtask);
					}
				}
			}
			return monthPoiStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_month_poi统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_month_poi统计数据报错"+e.getMessage(),e);
		}
	}
	

	/**
	 * 处理subtaskId中poi月编相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleMonthPoiStatData(Subtask subtask,Map<Integer,Map<String,Integer>> monthPoiStat) throws Exception{
		try {
			//处理子任务与grid的关系
			List<Integer> gridIds = subtask.getGridIds();
			int monthPoiLogTotalNum = 0;
			int monthPoiLogFinishNum = 0;
			for (Integer gridId : gridIds) {
				if(monthPoiStat.containsKey(gridId)){
					Map<String, Integer> map = monthPoiStat.get(gridId);
					monthPoiLogTotalNum += map.get("logAllNum");
					monthPoiLogFinishNum += map.get("logFinishNum");
				}
			}
			Map<String,Integer> subtaskStat = new HashMap<String,Integer>();
			subtaskStat.put("monthPoiLogTotalNum", monthPoiLogTotalNum);
			subtaskStat.put("monthPoiLogFinishNum", monthPoiLogFinishNum);
			return subtaskStat;
		} catch (Exception e) {
			log.error("处理subtaskId("+subtask.getSubtaskId()+")月编poi统计数据报错,"+e.getMessage());
			throw new Exception("处理subtaskId("+subtask.getSubtaskId()+")月编poi统计数据报错"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Integer>> getTipsStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_tips, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Integer>> tipsStat = new HashMap<Integer,Map<String,Integer>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				if(json.containsKey("content")){
					JSONArray content = json.getJSONArray("content");
					for(int i=0;i<content.size();i++){
						Map<String,Integer> subtask = new HashMap<String,Integer>();
						JSONObject jso = content.getJSONObject(i);
						int gridId = (int) jso.get("gridId");
						int subtaskEditAllNum = (int) jso.get("subtaskEditAllNum");
						int subtaskEditFinishNum = (int) jso.get("subtaskEditFinishNum");
						subtask.put("subtaskEditAllNum", subtaskEditAllNum);
						subtask.put("subtaskEditFinishNum", subtaskEditFinishNum);
						tipsStat.put(gridId, subtask);
					}
				}
			}
			return tipsStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_tips统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_tips统计数据报错"+e.getMessage(),e);
		}
	}
	

	/**
	 * 处理subtaskId中tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleTipsStatData(Subtask subtask,Map<Integer,Map<String,Integer>> tipsStatData) throws Exception{
		try {
			//处理子任务与grid的关系
			List<Integer> gridIds = subtask.getGridIds();
			int tipsAllNum = 0;
			int tipsFinishNum = 0;
			for (Integer gridId : gridIds) {
				if(tipsStatData.containsKey(gridId)){
					Map<String, Integer> map = tipsStatData.get(gridId);
					tipsAllNum += map.get("subtaskEditAllNum");
					tipsFinishNum += map.get("subtaskEditFinishNum");
				}
			}
			Map<String,Integer> subtaskStat = new HashMap<String,Integer>();
			subtaskStat.put("tipsAllNum", tipsAllNum);
			subtaskStat.put("tipsFinishNum", tipsFinishNum);
			return subtaskStat;
		} catch (Exception e) {
			log.error("处理subtaskId("+subtask.getSubtaskId()+")tips统计数据报错,"+e.getMessage());
			throw new Exception("处理subtaskId("+subtask.getSubtaskId()+")tips统计数据报错"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中subtask_tips相应的统计数据
	 * @throws ServiceException 
	 */
//	public Map<Integer,Map<String,Object>> getSubTipsStatData(String timestamp) throws Exception{
//		try {
//			MongoDao mongoDao = new MongoDao(dbName);
//			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
//			FindIterable<Document> findIterable = mongoDao.find(subtask_tips, filter);
//			MongoCursor<Document> iterator = findIterable.iterator();
//			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
//			//处理数据
//			while(iterator.hasNext()){
//				//获取统计数据
//				JSONObject json = JSONObject.fromObject(iterator.next());
//				if(json.containsKey("content")){
//					JSONArray content = json.getJSONArray("content");
//					for(int i=0;i<content.size();i++){
//						Map<String,Object> subtask = new HashMap<String,Object>();
//						JSONObject jso = content.getJSONObject(i);
//						int subtaskId = (int) jso.get("subtaskId");
//						String firstCollectDate = (String) jso.get("firstCollectDate");
//						subtask.put("firstCollectDate", firstCollectDate);
//						stat.put(subtaskId, subtask);
//					}
//				}
//			}
//			return stat;
//		} catch (Exception e) {
//			log.error("查询mongo库中subtask_tips统计数据报错"+e.getMessage());
//			throw new Exception("查询mongo库中subtask_tips统计数据报错"+e.getMessage(),e);
//		}
//	}
	
	
	/**
	 * 统计数据
	 * @author Han Shaoming
	 * @param subtask
	 * @param subManTimeline 
	 * @param subManTimelineEnd 
	 * @param subDayPoiStat 
	 * @param subMonthPoiStat 
	 * @param tipsStat 
	 * @param subTipsStat 
	 * @return
	 * @throws ParseException
	 */
	public Map<String, Object> getSubtaskStat(Subtask subtask, Map<String, Object> subManTimelineStart, Map<String, Object> subManTimelineEnd, Map<String, Integer> tipsStat,
			Map<String, Integer> subMonthPoiStat, Map<String, Object> subDayPoiStat, Map<String, Object> subTipsStat) throws Exception{
		
		int subtaskId = 0;
		int type = 0;
		int status = 0;
		
		int diffDate = 0;
		int planDate = 0;
		
		int tipsAllNum = 0;
		int tipsFinishNum = 0;
		int monthPoiLogTotalNum = 0;
		int monthPoiLogFinishNum = 0;
		int poiCollectUploadNum = 0;
		int poiFinishNum = 0;
		
		int poiCollectPercent = 0;
		int poiDayPercent = 0;
		int poiPercent = 0;
		int roadPercent = 0;
		
		String actualStartDate = "";
		String actualEndDate = "";
		
		int progress = 1;
		int percent = 0;
		int programType=0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			//当前时间
			String systemDate = sdf.format(new Date());
			
			Map<String, Object> subtaskMap = new HashMap<String, Object>();
			//子任务id
			subtaskId = subtask.getSubtaskId();
			//子任务类型
			type = subtask.getType();
			//子任务状态
			status = subtask.getStatus();
			
			//日编tips总量
			tipsAllNum = tipsStat.get("tipsAllNum");
			//日编tips完成个数
			tipsFinishNum = tipsStat.get("tipsFinishNum");
			//月编log总量
			monthPoiLogTotalNum = subMonthPoiStat.get("monthPoiLogTotalNum");
			//月编log完成个数
			monthPoiLogFinishNum = subMonthPoiStat.get("monthPoiLogFinishNum");
			
			if(subDayPoiStat != null && subDayPoiStat.size() > 0){
				//采集上传POI个数
				poiCollectUploadNum = (int) subDayPoiStat.get("poiCollectUploadNum");
				//POI提交个数
				poiFinishNum = (int) subDayPoiStat.get("poiFinishNum");
			}
			//计划天数
			if(subtask.getPlanStartDate() != null && subtask.getPlanEndDate() != null){
				planDate = StatUtil.daysOfTwo(subtask.getPlanStartDate(), subtask.getPlanEndDate());
			}
			//实际开始时间
			if(subtask.getType()==0 || subtask.getType()==1 || subtask.getType()==2){
				//采集
				String firstCollectDate = "";
				String tipsFirstCollectDate = "";
				if(subDayPoiStat != null && subDayPoiStat.size() > 0){
					firstCollectDate = (String) subDayPoiStat.get("firstCollectDate");
				}
				if(subManTimelineStart != null && subManTimelineStart.size() > 0){
					int operateType = (int) subManTimelineStart.get("operateType");
					if(operateType == 1){
						tipsFirstCollectDate = (String) subManTimelineStart.get("operateDate");
					}
				}
				if(subTipsStat != null && subTipsStat.size() > 0){
				}
				//处理具体时间
				if(StringUtils.isNotEmpty(firstCollectDate) && StringUtils.isEmpty(tipsFirstCollectDate)){
					actualStartDate = firstCollectDate;
				}else if(StringUtils.isNotEmpty(tipsFirstCollectDate) && StringUtils.isEmpty(firstCollectDate)){
					actualStartDate = tipsFirstCollectDate;
				}else if(StringUtils.isNotEmpty(firstCollectDate) && StringUtils.isNotEmpty(tipsFirstCollectDate)){
					if(Long.parseLong(firstCollectDate) < Long.parseLong(tipsFirstCollectDate)){
						actualStartDate = firstCollectDate;
					}else{
						actualStartDate = tipsFirstCollectDate;
					}
				}
			}else if(subtask.getType()==3 || subtask.getType()==4 || subtask.getType()==5 
					|| subtask.getType()==8 || subtask.getType()==9 || subtask.getType()==10){
				//日编
				if(subDayPoiStat != null && subDayPoiStat.size() > 0){
					actualStartDate = (String) subDayPoiStat.get("firstEditDate");
				}
			}else{
				if(subtask.getPlanStartDate() != null){
					actualStartDate = sdf.format(subtask.getPlanStartDate());
				}
			}
			//实际结束时间
			if(subtask.getStatus() == 0){
				actualEndDate = sdf.format(new Date());
				if(subManTimelineEnd != null && subManTimelineEnd.size() > 0){
					int operateType = (int) subManTimelineEnd.get("operateType");
					if(operateType == 0){
						actualEndDate = (String) subManTimelineEnd.get("operateDate");
					}
				}
			}
			//距离计划结束时间天数
			//计划结束时间
			String planEndDate = "" ;
			if(subtask.getPlanEndDate() != null){
				planEndDate = sdf.format(subtask.getPlanEndDate());
			}
			if(subtask.getStatus() == 0){
				//子任务为关闭状态：计划结束时间-实际结束时间
				if(StringUtils.isNotEmpty(actualEndDate) && StringUtils.isNotEmpty(planEndDate)){
					diffDate = StatUtil.daysOfTwo(sdf.parse(actualEndDate), sdf.parse(planEndDate));
				}
			}else if(subtask.getStatus() == 1){
				//子任务为非关闭状态：计划结束时间-当前时间
				if(StringUtils.isNotEmpty(systemDate) && StringUtils.isNotEmpty(planEndDate)){
					diffDate = StatUtil.daysOfTwo(sdf.parse(systemDate), sdf.parse(planEndDate));
				}
			}
			//先获取任务信息(快线或者中线)
			Map<String,Integer> taskMap = manApi.getTaskBySubtaskId(subtaskId);
			
			if(taskMap!=null&&taskMap.size()>0){
				programType=taskMap.get("programType");
			}
			//中线
			if(programType == 1){
				//POI_采集
				if(subtask.getType()==0 || subtask.getType()==2){
					//poi采集完成度
					if(subtask.getStatus() == 0){
						//关闭
						poiCollectPercent = 100;
					}else if(subtask.getStatus() == 1){
						//开启
						poiCollectPercent = 0;
					}
					//poi粗编完成度
					if(poiFinishNum != 0 && poiCollectUploadNum == 0){
						poiDayPercent = 100;
					}else if(poiCollectUploadNum != 0){
						poiDayPercent = poiFinishNum*100/poiCollectUploadNum;
					}
					//poi完成度
					poiPercent = (int) (poiCollectPercent*0.5 + poiDayPercent*0.5);
					//子任务完成度
					percent = poiPercent;
				}
				//道路_采集
				else if(subtask.getType()==1 || subtask.getType()==2){
					if(subtask.getStatus() == 0){
						//关闭
						roadPercent = 100;
						percent = 100;
					}else if(subtask.getStatus() == 1){
						//开启
						roadPercent = 0;
						percent = 0;
					}
				}
				//一体化_采集(需要用到上述两项,所以也要计算上述两项)
				else if(subtask.getType()==2){
					percent = (int) (poiPercent*0.5 + roadPercent*0.5);
				}
			}
			//快线
			else if(programType == 4){
				//采集任务
				if(subtask.getType()==0 ||subtask.getType()==1 || subtask.getType()==2){
					if(subtask.getStatus() == 0){
						//关闭
						poiPercent = 100;
						roadPercent = 100;
						percent = 100;
					}else if(subtask.getStatus() == 1){
						//开启
						poiPercent = 0;
						roadPercent = 0;
						percent = 0;
					}
				}
				//一体化_grid粗编_日编
				else if(subtask.getType()==3){
					if(tipsFinishNum != 0 && tipsAllNum == 0){
						percent = 100;
					}else if(tipsAllNum != 0){
						percent = tipsFinishNum*100/tipsAllNum;
					}
				}
				//一体化_区域粗编_日编
				else if(subtask.getType()==4){
					if(subtask.getStatus() == 0){
						//关闭
						percent = 100;
					}else if(subtask.getStatus() == 1){
						//开启
						percent = 0;
					}
				}
			}
			//POI专项_月编(中线,快线相同)
			if(subtask.getType()==7){
				if(monthPoiLogFinishNum != 0 && monthPoiLogTotalNum == 0){
					percent = 100;
				}else if(monthPoiLogTotalNum != 0){
					percent = monthPoiLogFinishNum*100/monthPoiLogTotalNum;
				}
			}
			//进度
			if(diffDate < 0){
				progress = 2;
			}else{
				if(planDate == 0){
					if(percent == 100){
						progress = 1;
					}else{
						progress = 2;
					}
				}else{
					int percentSchedule = 100 - diffDate*100/planDate;
					if(percent >= percentSchedule){
						progress = 1;
					}else{
						progress = 2;
					}
				}
			}
			//保存数据
			subtaskMap.put("subtaskId",subtaskId );
			subtaskMap.put("type",type );
			subtaskMap.put("status",status );
			subtaskMap.put("diffDate",diffDate );
			subtaskMap.put("planDate",planDate );
			subtaskMap.put("tipsAllNum",tipsAllNum );
			subtaskMap.put("tipsFinishNum",tipsFinishNum );
			subtaskMap.put("monthPoiLogTotalNum",monthPoiLogTotalNum );
			subtaskMap.put("monthPoiLogFinishNum",monthPoiLogFinishNum);
			subtaskMap.put("poiCollectUploadNum",poiCollectUploadNum );
			subtaskMap.put("poiFinishNum",poiFinishNum );
			subtaskMap.put("poiCollectPercent",poiCollectPercent );
			subtaskMap.put("poiDayPercent",poiDayPercent );
			subtaskMap.put("poiPercent",poiPercent );
			subtaskMap.put("roadPercent",roadPercent );
			subtaskMap.put("actualStartDate",actualStartDate );
			subtaskMap.put("actualEndDate",actualEndDate );
			subtaskMap.put("progress",progress );
			subtaskMap.put("percent",percent );
			subtaskMap.put("programType",programType );
			
			return subtaskMap;
		} catch (Exception e) {
			log.error("处理数据出错:" + e.getMessage(), e);
			throw new Exception("处理数据出错:" + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		String timestamp = "20170802180000";
		String lastTime = DateUtils.addSeconds(timestamp,-60*60);
		System.out.println(lastTime);
	}

}
