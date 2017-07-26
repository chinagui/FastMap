package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	public static final String col_name_subtask = "fm_stat_overview_subtask";
	private String db_name;
	private String statDate;
	private String statTime;

	public OverviewTaskMain(String dbn, String statTime) {
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
			md.getCollection(col_name_task).createIndex(new BasicDBObject("groupId", 1));
			md.getCollection(col_name_task).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_task + " ok");
			log.info("-- -- create mongo index on " + col_name_task + "(taskId,groupId,statDate) ok");
		}
			// 删除当天重复统计数据
			BasicDBObject query = new BasicDBObject();
			query.put("statDate", statDate);
			mongoDao.deleteMany(col_name_task, query);
		
	}
	
	/**
	 * 根据taskId查询mongo中subtask统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Object> getSubtaskStatByTaskId(long TaskId) throws ServiceException{
		MongoDao mongoDao = new MongoDao(db_name);
		BasicDBObject filter = new BasicDBObject("taskId", TaskId);
		filter.put("statDate", statDate);
		FindIterable<Document> findIterable = mongoDao.find(col_name_subtask, filter);
		MongoCursor<Document> iterator = findIterable.iterator();
		Map<String,Object> subtaskStat = new HashMap<String,Object>();
		//处理数据
		int progress = 1;
		int percent = 0;
		//集合
		List<Long> progressList = new ArrayList<Long>();
		int count = 0;
		while(iterator.hasNext()){
			//计数
			count++;
			//获取block统计数据
			JSONObject json = JSONObject.fromObject(iterator.next());
			if(json != null){
				percent += json.getLong("percent");
				
				progressList.add(json.getLong("progress"));
			}
		}
		//判断进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)
		for(int i=0; i<progressList.size();i++){
			if(progressList.get(i) == 2){
				progress = 2;
				break;
			}
		}
		//进度百分比 , 所有block采集完成度取平均值
		if(count == 0){
			count = 1;
		}
		percent = percent/count;
		//保存到map集合
		subtaskStat.put("percent", percent);
		subtaskStat.put("progress", progress);
		return subtaskStat;
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
				int status = task.getStatus();
				//任务开启
				if(status == 1){
					Document doc = getTaskStat(task);
					taskStatList.add(doc);
				}
				//任务关闭
				if(status == 0){
					//查询task统计表
					Map<String, Object> taskStat = manApi.queryTaskStatByTaskId(task.getTaskId());
					if(taskStat != null && taskStat.size() > 0 && taskStat.get("actualEndDate") != null){
						Document document = new Document();
						document.put("taskId", taskStat.get("taskId"));
						document.put("programId", taskStat.get("programId"));
						document.put("progress", taskStat.get("progress"));
						document.put("percent", taskStat.get("percent"));
						document.put("status", taskStat.get("status"));
						document.put("poiPlanTotal", taskStat.get("poiPlanTotal"));
						document.put("roadPlanTotal", taskStat.get("roadPlanTotal"));
						document.put("planStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("planStartDate")));
						document.put("planEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("planEndDate")));
						document.put("planDate", taskStat.get("planDate"));
						document.put("actualStartDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("actualStartDate")));
						document.put("actualEndDate", new SimpleDateFormat("yyyyMMdd").format(taskStat.get("actualEndDate")));
						document.put("diffDate", taskStat.get("diffDate"));
						document.put("groupId", taskStat.get("groupId"));
						document.put("type", taskStat.get("type"));
						document.put("statDate", statDate);
						document.put("statTime", statTime);
						
						taskStatList.add(document);
					}
					if(taskStat != null && taskStat.size() > 0 && taskStat.get("actualEndDate") == null){
						Document doc = getTaskStat(task);
						//添加实际结束时间
						String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
						doc.put("actualEndDate", systemDate);
						
						taskStatList.add(doc);
					}
					if(taskStat == null || taskStat.size() == 0){
						Document doc = getTaskStat(task);
						//添加实际结束时间
						String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
						doc.put("actualEndDate", systemDate);
						
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
		Integer taskId = null;
		int progress = 1;
		int percent = 0;
		
		Integer programId = 0;
		Integer status = 0;
		Integer groupId = 0;
		Integer type = 0;
		Integer poiPlanTotal = 0;
		Float roadPlanTotal = 0f;
		
		String planStartDate = null;
		String planEndDate = null;
		Integer planDate = 0;
		
		String actualStartDate = null;
		String actualEndDate = null;
		Integer diffDate = 0;
		Document doc = null;
		try {
			doc = new Document();
			//根据taskId查询subtask数据
			Map<String, Object> subtaskStatList = getSubtaskStatByTaskId(task.getTaskId());
			//taskId
			taskId = task.getTaskId();
			//所属项目id
			programId = task.getProgramId();
			//任务状态
			status = task.getStatus();
			//作业组
			groupId = task.getGroupId();
			//任务类型
			type = task.getType();
			//任务poi计划量
			poiPlanTotal = task.getPoiPlanTotal();
			//任务road计划量
			//modify by songhe修改为float数据类型，表示道路长度
			roadPlanTotal = task.getRoadPlanTotal();

			//计划开始时间
			if(task.getPlanStartDate() != null){
				planStartDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanStartDate());
			}
			//计划结束时间
			if(task.getPlanEndDate() != null){
				planEndDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanEndDate());
			}
			//计划天数
			if(task.getPlanStartDate() != null && task.getPlanEndDate() != null){
			planDate = StatUtil.daysOfTwo(task.getPlanStartDate(), task.getPlanEndDate());
			}
			//实际开始时间
			if(task.getPlanStartDate() != null){
			actualStartDate = new SimpleDateFormat("yyyyMMdd").format(task.getPlanStartDate());
			}
			//实际结束时间
			//距离计划结束时间天数
			String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			if(systemDate !=null && planEndDate != null){
				diffDate = StatUtil.daysOfTwo(new SimpleDateFormat("yyyyMMdd").parse(systemDate), new SimpleDateFormat("yyyyMMdd").parse(planEndDate));
			}
			//任务完成度
			percent = (int)subtaskStatList.get("percent");
			//进度
			progress = (int) subtaskStatList.get("progress");
			//处理统计数据
			doc.put("taskId", taskId);
			doc.put("programId", programId);
			doc.put("status", status);
			doc.put("groupId", groupId);
			doc.put("type", type);
			doc.put("progress", progress);
			doc.put("percent", percent);
			doc.put("poiPlanTotal", poiPlanTotal);
			doc.put("roadPlanTotal", roadPlanTotal);
			
			doc.put("planStartDate", planStartDate);
			doc.put("planEndDate", planEndDate);
			doc.put("planDate", planDate);
			doc.put("actualStartDate", actualStartDate);
			doc.put("actualEndDate", actualEndDate);
			doc.put("diffDate", diffDate);
			
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
			//System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
//		OverviewTaskMain overviewSubtaskStat = new OverviewTaskMain("fm_stat", "20161024144950");
		overviewSubtaskStat.runStat();
	}

}
