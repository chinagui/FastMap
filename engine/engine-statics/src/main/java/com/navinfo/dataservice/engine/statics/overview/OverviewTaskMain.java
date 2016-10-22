package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName overviewStatTask
 * @author Han Shaoming
 * @date 2016年10月20日 上午11:17:54
 * @Description TODO
 */
public class OverviewTaskMain {
	
	private static Logger log = null;
	public static final String col_name_task = "fm_stat_overview_task";
	public static final String col_name_block = "fm_stat_overview_blockman";
	private String db_name;
	private String statDate;
	private String statTime;

	public OverviewTaskMain(String dbn, String statTime) {
		StatInit.initDatahubDb();
		this.db_name = dbn;
		this.statDate = statTime.substring(0, 8);
		this.statTime = statTime;
	}
	
	/**
	 * 初始化mongo结果库
	 */
	public void initMongoDb(){
		//查询mongo库中所有的collection集合名称
		MongoDao mongoDao = new MongoDao(db_name);
		MongoDatabase md = mongoDao.getDatabase();
		MongoCursor<String> iterator = md.listCollectionNames().iterator();
		Boolean flag_task = true;
		//判断是否存在当天记录
		while(iterator.hasNext()){
			if(iterator.next().equalsIgnoreCase(col_name_task)){
				flag_task = false;
				break;
			}
		}
		if(flag_task){
			//创建collection集合
			md.createCollection(col_name_task);
			md.getCollection(col_name_task).createIndex(new BasicDBObject("taskId", 1));
			md.getCollection(col_name_task).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_task + " ok");
			log.info("-- -- create mongo index on " + col_name_task + "(taskId，statDate) ok");
		}
			// 删除当天重复统计数据
			BasicDBObject query = new BasicDBObject();
		query.put("statDate", statDate);
			mongoDao.deleteMany(col_name_task, query);
		
	}
	
	/**
	 * 根据taskId查询mongo中block统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Object> getBlockStatByTaskId(long TaskId) throws ServiceException{
		MongoDao mongoDao = new MongoDao(db_name);
		BasicDBObject filter = new BasicDBObject("taskId", TaskId);
		filter.put("statDate", statDate);
		FindIterable<Document> findIterable = mongoDao.find(col_name_block, filter);
		MongoCursor<Document> iterator = findIterable.iterator();
		Map<String,Object> blockStat = new HashMap<String,Object>();
		//处理数据
		long poiPlanTotal = 0;
		long roadPlanTotal = 0;
		long collectProgress = 1;
		long collectPercent = 0;
		
		String collectPlanStartDate = null;
		String collectPlanEndDate = null;
		long collectPlanDate = 0;
		
		String collectActualStartDate = null;
		String collectActualEndDate = null;
		long collectDiffDate = 0;
		
		long dailyProgress = 1;
		long dailyPercent = 0;
		
		String dailyPlanStartDate = null;
		String dailyPlanEndDate = null;
		long dailyPlanDate = 0;
		
		String dailyActualStartDate = null;
		String dailyActualEndDate = null;
		long dailyDiffDate = 0;
		
		//集合
		List<String> collectPlanStartDateList = new ArrayList<String>();
		List<String> collectPlanEndDateList = new ArrayList<String>();
		List<String> collectActualStartDateList = new ArrayList<String>();
		List<String> collectActualEndDateList = new ArrayList<String>();
		
		List<String> dailyPlanStartDateList = new ArrayList<String>();
		List<String> dailyPlanEndDateList = new ArrayList<String>();
		List<String> dailyActualStartDateList = new ArrayList<String>();
		List<String> dailyActualEndDateList = new ArrayList<String>();
		
		List<Long> collectProgressList = new ArrayList<Long>();
		List<Long> dailyProgressList = new ArrayList<Long>();
		int count = 0;
		while(iterator.hasNext()){
			//计数
			count++;
			//获取block统计数据
			JSONObject json = JSONObject.fromObject(iterator.next());
			if(json != null){
				poiPlanTotal += json.getLong("poiPlanTotal");
				roadPlanTotal += json.getLong("roadPlanTotal");
				collectPercent += json.getLong("collectPercent");
				dailyPercent += json.getLong("dailyPercent");
				
				collectProgressList.add(json.getLong("collectProgress"));
				dailyProgressList.add(json.getLong("dailyProgress"));
				//采集
				if(!"null".equalsIgnoreCase(json.getString("collectPlanStartDate"))){
					collectPlanStartDateList.add(json.getString("collectPlanStartDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("collectPlanEndDate"))){
					collectPlanEndDateList.add(json.getString("collectPlanEndDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("collectActualStartDate"))){
					collectActualStartDateList.add(json.getString("collectActualStartDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("collectActualEndDate"))){
					collectActualEndDateList.add(json.getString("collectActualEndDate"));
				}
				//日编
				if(!"null".equalsIgnoreCase(json.getString("dailyPlanStartDate"))){
					dailyPlanStartDateList.add(json.getString("dailyPlanStartDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("dailyPlanEndDate"))){
					dailyPlanEndDateList.add(json.getString("dailyPlanEndDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("dailyActualStartDate"))){
					dailyActualStartDateList.add(json.getString("dailyActualStartDate"));
				}
				if(!"null".equalsIgnoreCase(json.getString("dailyActualEndDate"))){
					dailyActualEndDateList.add(json.getString("dailyActualEndDate"));
				}
			}
		}
		//判断进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)
		for(int i=0; i<collectProgressList.size();i++){
			if(collectProgressList.get(i) == 2){
				collectProgress = 2;
				break;
			}
		}
		for(int i=0; i<dailyProgressList.size();i++){
			if(dailyProgressList.get(i) == 2){
				dailyProgress = 2;
				break;
			}
		}
		
		//进度百分比 , 所有block采集完成度取平均值
		if(count == 0){
			count = 1;
		}
		collectPercent = collectPercent/count;
		dailyPercent = dailyPercent/count;
		//计划开始时间
		collectPlanStartDate = startTime(collectPlanStartDateList);
		dailyPlanStartDate = startTime(dailyPlanStartDateList);
		//计划结束时间
		collectPlanEndDate = endTime(collectPlanEndDateList);
		dailyPlanEndDate = endTime(dailyPlanEndDateList);
		//计划天数
		try {
			if(collectPlanStartDate !=null && collectPlanEndDate != null){
			collectPlanDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(collectPlanStartDate), new SimpleDateFormat("yyyyMMdd").parse(collectPlanEndDate));
			}
			if(dailyPlanStartDate !=null && dailyPlanEndDate != null){
			dailyPlanDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(dailyPlanStartDate), new SimpleDateFormat("yyyyMMdd").parse(dailyPlanEndDate));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("时间转换失败，原因为:" + e.getMessage(), e);
		}
		//实际开始时间
		collectActualStartDate = startTime(collectActualStartDateList);
		dailyActualStartDate = startTime(dailyActualStartDateList);
		//实际结束时间
		collectActualEndDate = endTime(collectActualEndDateList);
		dailyActualEndDate = endTime(dailyActualEndDateList);
		//距离计划结束时间天数
		String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
		try {
			if(systemDate !=null && collectPlanEndDate != null){
			collectDiffDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(systemDate), new SimpleDateFormat("yyyyMMdd").parse(collectPlanEndDate));
			}
			if(systemDate !=null && collectPlanEndDate != null){
			dailyDiffDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(systemDate), new SimpleDateFormat("yyyyMMdd").parse(collectPlanEndDate));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("时间转换失败，原因为:" + e.getMessage(), e);
		}
		//保存到map集合
		blockStat.put("poiPlanTotal", poiPlanTotal);
		blockStat.put("roadPlanTotal", roadPlanTotal);
		blockStat.put("collectProgress", collectProgress);
		blockStat.put("collectPercent", collectPercent);
		blockStat.put("collectPlanStartDate", collectPlanStartDate);
		blockStat.put("collectPlanEndDate", collectPlanEndDate);
		blockStat.put("collectPlanDate", collectPlanDate);
		blockStat.put("collectActualStartDate", collectActualStartDate);
		blockStat.put("collectActualEndDate", collectActualEndDate);
		blockStat.put("collectDiffDate", collectDiffDate);
		blockStat.put("dailyProgress", dailyProgress);
		blockStat.put("dailyPercent", dailyPercent);
		blockStat.put("dailyPlanStartDate", dailyPlanStartDate);
		blockStat.put("dailyPlanEndDate", dailyPlanEndDate);
		blockStat.put("dailyPlanDate", dailyPlanDate);
		blockStat.put("dailyActualStartDate", dailyActualStartDate);
		blockStat.put("dailyActualEndDate", dailyActualEndDate);
		blockStat.put("dailyDiffDate", dailyDiffDate);
		return blockStat;
	}
	/**
	 * 统计数据
	 * @param subtask
	 * @return
	 * @throws ParseException
	 */
	public List<Document> getTaskStatList() throws ParseException{
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Document> taskStatList = new ArrayList<Document>();
		try {
			List<Task> taskAll = manApi.queryTaskAll();
			for (Task task : taskAll) {
				//任务开启
				if(task.getTaskStatus() == 1){
					Document doc = getTaskStat(task);
					taskStatList.add(doc);
				}
				//任务关闭
				if(task.getTaskStatus() == 0){
					//查询task统计表
					Map<String, Object> taskStat = manApi.queryTaskStatByTaskId(task.getTaskId());
					if(taskStat != null && taskStat.size() > 0 && taskStat.get("actualEndDate") != null){
						Document document = new Document();
						document.put("taskId", taskStat.get("taskId")); 
						document.put("progress", taskStat.get("progress"));
						document.put("percent", taskStat.get("percent"));
						document.put("poiPlanTotal", taskStat.get("poiPlanTotal"));
						document.put("roadPlanTotal", taskStat.get("roadPlanTotal"));
						document.put("planStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("planStartDate")));
						document.put("planEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("planEndDate")));
						document.put("planDate", taskStat.get("planDate"));
						document.put("actualStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("actualStartDate")));
						document.put("actualEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("actualEndDate")));
						document.put("diffDate", taskStat.get("diffDate"));
						document.put("collectProgress", taskStat.get("collectProgress"));
						document.put("collectPercent", taskStat.get("collectPercent"));
						document.put("collectPlanStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("collectPlanStartDate")));
						document.put("collectPlanEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("collectPlanEndDate")));
						document.put("collectPlanDate", taskStat.get("collectPlanDate"));
						document.put("collectActualStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("collectActualStartDate")));
						document.put("collectActualEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("collectActualEndDate")));
						document.put("collectDiffDate", taskStat.get("collectDiffDate"));
						document.put("dailyProgress", taskStat.get("dailyProgress"));
						document.put("dailyPercent", taskStat.get("dailyPercent"));
						document.put("dailyPlanStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("dailyPlanStartDate")));
						document.put("dailyPlanEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("dailyPlanEndDate")));
						document.put("dailyPlanDate", taskStat.get("dailyPlanDate"));
						document.put("dailyActualStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("dailyActualStartDate")));
						document.put("dailyActualEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("dailyActualEndDate")));
						document.put("dailyDiffDate", taskStat.get("dailyDiffDate"));
						document.put("statDate", statDate);
						document.put("statTime", statTime);
						
						taskStatList.add(document);
					}
					if(taskStat != null && taskStat.size() > 0 && taskStat.get("actualEndDate") == null){
						Document doc = getTaskStat(task);
						//根据taskId查询block数据
						Map<String, Object> blockStatList = getBlockStatByTaskId(task.getTaskId());
						String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
						doc.put("actualEndDate", systemDate);
						doc.put("collectActualEndDate", blockStatList.get("collectActualEndDate"));
						doc.put("dailyActualEndDate", blockStatList.get("dailyActualEndDate"));
						
						taskStatList.add(doc);
					}
					if(taskStat == null || taskStat.size() == 0){
						Document doc = getTaskStat(task);
						//根据taskId查询block数据
						Map<String, Object> blockStatList = getBlockStatByTaskId(task.getTaskId());
						String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
						doc.put("actualEndDate", systemDate);
						doc.put("collectActualEndDate", blockStatList.get("collectActualEndDate"));
						doc.put("dailyActualEndDate", blockStatList.get("dailyActualEndDate"));
						
						taskStatList.add(doc);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("查询失败，原因为:" + e.getMessage(), e);
		}
		return taskStatList;
	}
	
	/**
	 * 处理任务数据
	 */
	public Document getTaskStat(Task task){
		long taskId = 0;
		long progress = 1;
		long percent = 0;
		
		String planStartDate = null;
		String planEndDate = null;
		long planDate = 0;
		
		String actualStartDate = null;
		String actualEndDate = null;
		long diffDate = 0;
		Document doc = null;
		try {
			doc = new Document();
			//根据taskId查询block数据
			Map<String, Object> blockStatList = getBlockStatByTaskId(task.getTaskId());
			//taskId
			taskId = task.getTaskId();
			//计划开始时间
			planStartDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanStartDate());
			//计划结束时间
			planEndDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanEndDate());
			//计划天数
			if(task.getPlanStartDate() != null && task.getPlanEndDate() != null){
			planDate = StatUtil.daysOfTwo(task.getPlanStartDate(), task.getPlanEndDate());
			}
			//实际开始时间
			if(task.getPlanStartDate() != null){
			actualStartDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanStartDate());
			}
			//实际结束时
			//距离计划结束时间天数
			String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			if(systemDate !=null && planStartDate != null){
				diffDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(systemDate), new SimpleDateFormat("yyyyMMdd").parse(planStartDate));
			}
			//任务完成度
			percent = ((long)blockStatList.get("collectPercent") + (long)blockStatList.get("dailyPercent"))/2;
			//进度
			int diff = 0;
			if(actualStartDate != null && systemDate != null){
				diff = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(actualStartDate),new SimpleDateFormat("yyyyMMdd").parse(systemDate));
			}
			long planDiff = planDate*percent;
			if(diff > planDiff){
				progress = 2;
			}
			//处理统计数据
			doc.put("taskId", taskId);
			doc.put("progress", progress);
			doc.put("percent", percent);
			doc.put("poiPlanTotal", blockStatList.get("poiPlanTotal"));
			doc.put("roadPlanTotal", blockStatList.get("roadPlanTotal"));
			
			doc.put("planStartDate", planStartDate);
			doc.put("planEndDate", planEndDate);
			doc.put("planDate", planDate);
			doc.put("actualStartDate", actualStartDate);
			doc.put("actualEndDate", actualEndDate);
			doc.put("diffDate", diffDate);
			
			doc.put("collectProgress", blockStatList.get("collectProgress"));
			doc.put("collectPercent", blockStatList.get("collectPercent"));
			
			doc.put("collectPlanStartDate", blockStatList.get("collectPlanStartDate"));
			doc.put("collectPlanEndDate", blockStatList.get("collectPlanEndDate"));
			doc.put("collectPlanDate", blockStatList.get("collectPlanDate"));
			doc.put("collectActualStartDate", blockStatList.get("collectActualStartDate"));
			doc.put("collectActualEndDate", null);
			doc.put("collectDiffDate", blockStatList.get("collectDiffDate"));
			
			doc.put("dailyProgress", blockStatList.get("dailyProgress"));
			doc.put("dailyPercent", blockStatList.get("dailyPercent"));
			
			doc.put("dailyPlanStartDate", blockStatList.get("dailyPlanStartDate"));
			doc.put("dailyPlanEndDate", blockStatList.get("dailyPlanEndDate"));
			doc.put("dailyPlanDate", blockStatList.get("dailyPlanDate"));
			doc.put("dailyActualStartDate", blockStatList.get("dailyActualStartDate"));
			doc.put("dailyActualEndDate", null);
			doc.put("dailyDiffDate", blockStatList.get("dailyDiffDate"));
			
			doc.put("statDate", statDate);
			doc.put("statTime", statTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("查询失败，原因为:" + e.getMessage(), e);
		}
		return doc;
	}
	
	public void runStat() {
		log = LogManager.getLogger(OverviewTaskMain.class);

		log.info("-- begin stat:" + col_name_task);

		try {
			// 初始化mongodb数据库
			initMongoDb();

			//执行统计
			List<Document> taskList = getTaskStatList();
			
			MongoDao md = new MongoDao(db_name);
			md.insertMany(col_name_task, taskList);
			
			log.info("-- end stat:" + col_name_task);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewTaskMain overviewSubtaskStat = new OverviewTaskMain("fm_stat", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		overviewSubtaskStat.runStat();
	}

}
