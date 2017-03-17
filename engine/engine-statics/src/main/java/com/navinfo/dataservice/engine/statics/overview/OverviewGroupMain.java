package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/** 
 * @ClassName: OverviewGroupMain
 * @author zl
 * @date 2016年10月22日
 * @Description: OverviewGroupStat.java
 */
public class OverviewGroupMain {

	/**
	 * 
	 */
	private static Logger log = null;
	public static final String col_name_group = "fm_stat_overview_group";
	public static final String col_name_task = "fm_stat_overview_task";
	private String db_name;
	private String statDate;
	private String statTime;

	public OverviewGroupMain(String dbn, String stat_time) {
		this.db_name = dbn;
		this.statDate = stat_time.substring(0, 8);
		this.statTime = stat_time;
	}
	
	
	/**
	 * 统计结果mongo结果库初始化
	 */
	private void initMongoDb() {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();
		// 初始化 col_name_blockman
		Iterator<String> iter_group = md.listCollectionNames().iterator();
		boolean flag_group = true;
		while (iter_group.hasNext()) {
			if (iter_group.next().equalsIgnoreCase(col_name_group)) {
				flag_group = false;
				break;
			}
		}

		if (flag_group) {
			md.createCollection(col_name_group);
			md.getCollection(col_name_group).createIndex(new BasicDBObject("group_id", 1));
			md.getCollection(col_name_group).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_group + " ok");
			log.info("-- -- create mongo index on " + col_name_group + "(group_id，statDate) ok");
		}

		// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("statDate", statDate);
		mdao.deleteMany(col_name_group, query);

	}
	

	/**
	 * @Title: runStat
	 * @Description: 运行脚本的方法
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月22日 上午10:19:50 
	 */
	public void runStat() {
		log = LogManager.getLogger(PoiCollectMain.class);

		log.info("-- begin stat:" + col_name_group);

		try {
			// 初始化mongodb数据库
			initMongoDb();

			//在mongo库里查询_overview_blockMan 的集合
			//执行统计
			List<Document> groupStatList = getGroupStatList();
			MongoDao md = new MongoDao(db_name);
			md.insertMany(col_name_group, groupStatList);
			
			log.info("-- end stat:" + col_name_group);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询mongo中task统计数据
	 * @throws ServiceException 
	 */
	public Map<Long,List<Map<String,Object>>> getTaskStat() throws ServiceException{
		MongoDao mongoDao = new MongoDao(db_name);
		BasicDBObject filter = new BasicDBObject("statDate", statDate);
		FindIterable<Document> findIterable = mongoDao.find(col_name_task, filter).sort(Sorts.descending("groupId","statDate"));
		MongoCursor<Document> iterator = findIterable.iterator();
		Map<Long,List<Map<String,Object>>> taskStatMap = new HashMap<Long,List<Map<String,Object>>>();
		//处理数据
		long groupId = 0;
		long percent = 0;//进度百分比
		long poiPlanTotal = 0;//所有任务poi计划量汇总
		long roadPlanTotal = 0;//所有任务road计划量汇总
		int status = -1;
		
		String planStartDate = null;
		String planEndDate = null;
		String actualStartDate = null;
		String actualEndDate = null;
		
		while(iterator.hasNext()){
			
			Map<String,Object> taskStat = new HashMap<String,Object>();
			//获取task统计数据
			JSONObject json = JSONObject.fromObject(iterator.next());
			if(json != null){
				groupId = json.getLong("groupId");
				poiPlanTotal = json.getLong("poiPlanTotal");
				roadPlanTotal = json.getLong("roadPlanTotal");
				percent = json.getLong("percent");
				status = json.getInt("status");
				planStartDate = json.getString("planStartDate");
				planEndDate = json.getString("planEndDate");
				actualStartDate = json.getString("actualStartDate");
				actualEndDate = json.getString("actualEndDate");
			}
			//保存到map集合
			taskStat.put("groupId", groupId);
			taskStat.put("poiPlanTotal", poiPlanTotal);
			taskStat.put("roadPlanTotal", roadPlanTotal);
			taskStat.put("percent", percent);
			taskStat.put("status", status);
			taskStat.put("planStartDate", planStartDate);
			taskStat.put("planEndDate", planEndDate);
			taskStat.put("actualStartDate", actualStartDate);
			taskStat.put("actualEndDate", actualEndDate);
			if(!taskStatMap.containsKey(groupId)){taskStatMap.put(groupId, new ArrayList<Map<String,Object>>());}
			taskStatMap.get(groupId).add(taskStat);
		}
		
		return taskStatMap;
	}
	
	/**
	 * 处理统计数据
	 */
	public Document getGroupStat(List<Map<String,Object>> taskStatList){
		long percent = 0;//进度百分比
		long poiPlanTotal = 0;//所有任务poi计划量汇总
		long roadPlanTotal = 0;//所有任务road计划量汇总
		int status = 0;
		
		String planStartDate = null;
		String planEndDate = null;
		String actualStartDate = null;
		String actualEndDate = null;
		
		int planDate = 0;
		int diffDate = 0;
		
		//集合
		List<String> planStartDateList = new ArrayList<String>();
		List<String> planEndDateList = new ArrayList<String>();
		List<String> actualStartDateList = new ArrayList<String>();
		List<String> actualEndDateList = new ArrayList<String>();
		
		List<Integer> statusList = new ArrayList<Integer>();
		
		Document doc = null;
		try {
			if(taskStatList != null && taskStatList.size()>0){
				for (Map<String, Object> taskStat : taskStatList) {
					poiPlanTotal += (long)taskStat.get("poiPlanTotal");
					roadPlanTotal += (long)taskStat.get("roadPlanTotal");
					percent += (long)taskStat.get("collectPercent");
					
					statusList.add((int)taskStat.get("status"));
					if(!"null".equalsIgnoreCase((String) taskStat.get("planStartDate"))){
						planStartDateList.add((String) taskStat.get("planStartDate"));
					}
					if(!"null".equalsIgnoreCase((String) taskStat.get("planEndDate"))){
						planEndDateList.add((String) taskStat.get("planEndDate"));
					}
					if(!"null".equalsIgnoreCase((String) taskStat.get("actualStartDate"))){
						actualStartDateList.add((String) taskStat.get("collectActualStartDate"));
					}
					if(!"null".equalsIgnoreCase((String) taskStat.get("actualEndDate"))){
						actualEndDateList.add((String) taskStat.get("actualEndDate"));
					}
				}
				doc = new Document();
				//处理数据
				SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
				//进度百分比 , 所有任务完成度取平均值
				percent = percent/taskStatList.size();
				//计划开始时间
				planStartDate = startTime(planStartDateList);
				//计划结束时间
				planEndDate = endTime(planEndDateList);
				//计划天数
				try {
					if(planStartDate !=null && planEndDate != null){
					planDate = StatUtil.daysOfTwo(sf.parse(planStartDate), sf.parse(planEndDate));
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error("时间转换失败，原因为:" + e.getMessage(), e);
				}
				//实际开始时间
				actualStartDate = startTime(actualStartDateList);
				//实际结束时间
				actualEndDate = endTime(actualEndDateList);
				//距离计划结束时间天数
				String systemDate = sf.format(new Date());
				try {
					if(systemDate !=null && planEndDate != null){
					diffDate = StatUtil.daysOfTwo(sf.parse(systemDate), sf.parse(planEndDate));
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error("时间转换失败，原因为:" + e.getMessage(), e);
				}
				//判断所有任务是否关闭
				for (Integer taskStatus : statusList) {
					if(taskStatus == 1){
						status =1;
						break;
					}
				}
				//处理统计数据
				doc.put("percent", percent);
				doc.put("poiPlanTotal", poiPlanTotal);
				doc.put("roadPlanTotal", roadPlanTotal);
				
				doc.put("planStartDate", planStartDate);
				doc.put("planEndDate", planEndDate);
				doc.put("planDate", planDate);
				doc.put("actualStartDate", actualStartDate);
				if(status == 0){
					doc.put("actualEndDate", actualEndDate);
				}else if(status == 1){
					doc.put("actualEndDate", null);
				}
				doc.put("diffDate", diffDate);
				
				doc.put("statDate", statDate);
				doc.put("statTime", statTime);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("查询失败，原因为:" + e.getMessage(), e);
		}
		return doc;
	}
	
	/**
	 * 统计数据
	 * @author Han Shaoming
	 * @return
	 * @throws ParseException
	 */
	private List<Document> getGroupStatList() throws ParseException {
		List<Document> groupStatList = new ArrayList<Document>();
		try {
			//查询task统计数据
			Map<Long, List<Map<String, Object>>> taskStatMap = getTaskStat();
			if(taskStatMap != null && !taskStatMap.isEmpty()){
				for(Long key :taskStatMap.keySet()){
					List<Map<String, Object>> taskStatList = taskStatMap.get(key);
					Document doc = getGroupStat(taskStatList);
					doc.put("groupId", key);
					groupStatList.add(doc);
				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("查询失败，原因为:" + e.getMessage(), e);
		}
		return groupStatList;
	}
	
	/**
	 * 处理开始时间
	 */
	private String startTime(List<String> startTimeList){
		String time1 = null;
		String startTime = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			Calendar c1=Calendar.getInstance();     
			Calendar c2=Calendar.getInstance();     
			if(startTimeList !=null && startTimeList.size() == 1){
				startTime = startTimeList.get(0);
			}
			if(startTimeList !=null && startTimeList.size() > 1){
				startTime = startTimeList.get(0);
				for(int i=1;i<startTimeList.size();i++){
					//取最小时间
					time1 = startTimeList.get(i);
					c1.setTime(df.parse(startTime));     
					c2.setTime(df.parse(time1));     
					int result=c1.compareTo(c2);
					if(result > 0){
						startTime = time1;
					}
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("时间转换失败，原因为:" + e.getMessage(), e);
		}
		return startTime;
	}
	
	/**
	 * 处理结束时间
	 */
	private String endTime(List<String> endTimeList){
		String time2 = null;
		String endTime = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			Calendar c1=Calendar.getInstance();     
			Calendar c2=Calendar.getInstance();     
			if(endTimeList !=null && endTimeList.size() == 1){
				endTime = endTimeList.get(0);
			}
			if(endTimeList !=null && endTimeList.size() > 1){
				endTime = endTimeList.get(0);
				for(int i=1;i<endTimeList.size();i++){
					//取最大时间
					time2 = endTimeList.get(i);
					c1.setTime(df.parse(endTime));     
					c2.setTime(df.parse(time2));     
					int result=c1.compareTo(c2);
					if(result < 0){
						endTime = time2;
					}
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("时间转换失败，原因为:" + e.getMessage(), e);
		}
		return endTime;
	}
	



	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewTaskMain overviewSubtaskStat = new OverviewTaskMain("fm_stat", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//		OverviewGroupMain overviewSubtaskStat = new OverviewGroupMain("fm_stat", "201610240956");
		overviewSubtaskStat.runStat();
		
	}
}
