package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

/** 
 * @ClassName: OverviewBlockStat
 * @author zl
 * @date 2016年10月20日
 * @Description: OverviewBlockStat.java
 */
public class OverviewBlockStat {

	/**
	 * 
	 */
	private static Logger log = null;
	public static final String col_name_blockman = "fm_stat_overview_blockman";
	private String db_name;
	private static String stat_date;
	private static String stat_time;

	public OverviewBlockStat(String dbn, String stat_time) {
		this.db_name = dbn;
		this.stat_date = stat_time.substring(0, 8);
		this.stat_time = stat_time;
	}
	
	
	/**
	 * 统计结果mongo结果库初始化
	 */
	private void initMongoDb() {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();
		// 初始化 col_name_blockman
		Iterator<String> iter_blockman = md.listCollectionNames().iterator();
		boolean flag_blockman = true;
		while (iter_blockman.hasNext()) {
			if (iter_blockman.next().equalsIgnoreCase(col_name_blockman)) {
				flag_blockman = false;
				break;
			}
		}

		if (flag_blockman) {
			md.createCollection(col_name_blockman);
			md.getCollection(col_name_blockman).createIndex(new BasicDBObject("block_man_id", 1));
			md.getCollection(col_name_blockman).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection " + col_name_blockman + " ok");
			log.info("-- -- create mongo index on " + col_name_blockman + "(block_man_id，stat_date) ok");
		}

		// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name_blockman, query);

	}
	

	/**
	 * @Title: runStat
	 * @Description: 运行脚本的方法
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月21日 下午2:55:50 
	 */
	public void runStat() {
		log = LogManager.getLogger(PoiCollectMain.class);

		log.info("-- begin stat:" + col_name_blockman);

		try {
			// 初始化mongodb数据库
			initMongoDb();

			//执行统计
			List<Map<String,Object>> blockManMapList = OracleDao.getBlockManListWithStat();
			
			MongoDao md = new MongoDao(db_name);
			List<Document> blockManStatList = new ArrayList<Document>();
			Iterator<Map<String,Object>> blockManMapItr = blockManMapList.iterator();
			while(blockManMapItr.hasNext()){
				Document blockMan = getBlockManStat(blockManMapItr.next());
				blockManStatList.add(blockMan);
			}
			md.insertMany(col_name_blockman, blockManStatList);
			
			log.info("-- end stat:" + col_name_blockman);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: getBlockManStat
	 * @Description: 获取想mongo库里存入的完整数据
	 * @param blockManMap
	 * @return
	 * @throws ParseException  Document
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月21日 下午2:56:42 
	 */
	public Document getBlockManStat(Map<String,Object> blockManMap) throws ParseException{
		Document doc = new Document();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		//SimpleDateFormat dft = new SimpleDateFormat("yyyymmddhhmmss");
		Date d = df.parse(stat_date);
		if(blockManMap.get("blockManId") != null){
			
		//System.out.println("blockManId :  "+blockManMap.get("blockManId"));
		//根据blockManId 去查询所有子任务,计算子任务的完成度(采集/日编)
		Map<String,Object> subtaskPercentMap =getSubtaskPercentThroughBlockManId(Integer.parseInt(blockManMap.get("blockManId").toString()));
		//ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
		doc.put("blockManId", blockManMap.get("blockManId"));
		doc.put("taskId", blockManMap.get("taskId"));
		doc.put("collectGroupId", blockManMap.get("collectGroupId"));
		doc.put("dailyGroupId", blockManMap.get("dailyGroupId"));
		doc.put("progress", subtaskPercentMap.get("progress"));//进度
		
		doc.put("percent", ((int)subtaskPercentMap.get("collectPercent")+(int)subtaskPercentMap.get("dailyPercent"))/2);//完成度
		doc.put("status", blockManMap.get("status"));
		doc.put("planStartDate", df.format(blockManMap.get("collectPlanStartDate")));
		doc.put("planEndDate",  df.format(blockManMap.get("dailyPlanEndDate")));
		doc.put("diffDate", StatUtil.daysOfTwo(d,(Date)blockManMap.get("dailyPlanEndDate")));
		doc.put("poiPlanTotal", blockManMap.get("poiPlanTotal"));
		doc.put("roadPlanTotal", blockManMap.get("roadPlanTotal"));
		//采集部分
		doc.put("collectProgress", subtaskPercentMap.get("collectProgress"));
		doc.put("collectPercent", subtaskPercentMap.get("collectPercent"));
		doc.put("collectPlanStartDate", df.format(blockManMap.get("collectPlanStartDate")));
		doc.put("collectPlanEndDate", df.format(blockManMap.get("collectPlanEndDate")));
		doc.put("collectPlanDate", StatUtil.daysOfTwo((Date)blockManMap.get("collectPlanStartDate"),(Date)blockManMap.get("collectPlanEndDate")));
		doc.put("collectActualStartDate", df.format(blockManMap.get("collectPlanStartDate")));		
		doc.put("collectDiffDate", StatUtil.daysOfTwo(d,(Date)blockManMap.get("collectPlanEndDate")));
		//日编部分
		doc.put("dailyProgress", subtaskPercentMap.get("dailyProgress"));
		doc.put("dailyPercent", subtaskPercentMap.get("dailyPercent"));
		doc.put("dailyPlanStartDate", df.format(blockManMap.get("dailyPlanStartDate")));
		doc.put("dailyPlanEndDate", df.format(blockManMap.get("dailyPlanEndDate")));
		doc.put("dailyPlanDate", StatUtil.daysOfTwo((Date)blockManMap.get("dailyPlanStartDate"),(Date)blockManMap.get("dailyPlanEndDate")));
		doc.put("dailyActualStartDate", df.format(blockManMap.get("dailyPlanStartDate")));
		doc.put("dailyDiffDate", StatUtil.daysOfTwo(d,(Date)blockManMap.get("dailyPlanEndDate")));
		
		doc.put("statDate",stat_date);
		doc.put("statTime",stat_time);
		
		//如果 oracle BLOCK_MAN 表里的status =0 说明blockman已经关闭
		if(blockManMap.get("status") != null && StringUtils.isNotEmpty(blockManMap.get("status").toString()) 
				&& blockManMap.get("status").equals("0")){
			//如果 oracle  FM_STAT_OVERVIEW_BLOCKMAN 表里的status存在且 等于0 
			if(blockManMap.get("fStatus") != null && StringUtils.isNotEmpty((String) blockManMap.get("fStatus")) 
					&& blockManMap.get("fStatus").equals("0")){
				doc.put("collectActualEndDate", df.format(blockManMap.get("fCollectActualEndDate")));
				doc.put("dailyActualEndDate", df.format(blockManMap.get("fDailyActualEndDate")));
			}else{
				doc.put("collectActualEndDate", stat_date);
				doc.put("dailyActualEndDate", stat_time);
			}
		}else{
			doc.put("collectActualEndDate", null);
			doc.put("dailyActualEndDate", null);
		}
		
		}
		return doc;
	}

	
	
	/**
	 * @Title: getSubtaskPercentThroughBlockManId
	 * @Description: 根据subtask及其统计表,来统计blockman需要额字段
	 * @param blockManId
	 * @return  Map<String,Object>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月21日 下午2:37:36 
	 */
	private Map<String, Object> getSubtaskPercentThroughBlockManId(int blockManId) {
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
		Map<String,Object> blockManMap = new HashMap<String,Object>();
		try {
			List<Map<String,Object>> subtaskList = api.getSubtaskPercentByBlockManId(blockManId);
			int progress = 1 ;//blockman 的进度异常情况
			int collectProgress = 1;//blockman 采集部分 的进度异常情况
			int dailyProgress = 1;//blockman 日编部分  的进度异常情况
			
			int collectPercent =0;//采集部分,进度的平均值
			int dailyPercent = 0;//日编部分,进度的平均值
			
			int collectCount= 0;//采集子任务的总数
			int dailyCount= 0;//日编子任务的总数
			//计算 采集  和 日编子任务的完成度
			for(Map<String,Object> subtaskMap : subtaskList){
				int stage = (int) subtaskMap.get("stage");
				int sub_percent = (int) subtaskMap.get("percent");
				int sub_progress= (int) subtaskMap.get("progress");
				
				if(stage == 0){//采集
					collectPercent+=sub_percent;
					collectCount+=1;
					if(sub_progress == 2){
						collectProgress = 2;
					}
				}else if(stage == 1){//日编
					dailyPercent+=sub_percent;
					dailyCount+=1;
					if(sub_progress == 2){
						dailyProgress = 2;
					}
				}
				
			}
			if(collectCount != 0){
				collectPercent = collectPercent/collectCount;
			}
			if(dailyCount != 0){
				dailyPercent = dailyPercent/dailyCount;
			}
			if(collectProgress == 2 || dailyProgress == 2 ){
				progress = 2;
			}
			blockManMap.put("progress",progress );
			blockManMap.put("collectProgress", collectProgress);
			blockManMap.put("dailyProgress", dailyProgress);
			blockManMap.put("collectPercent", collectPercent);
			blockManMap.put("dailyPercent", dailyPercent);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return blockManMap;
	}


	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewBlockStat overviewSubtaskStat = new OverviewBlockStat("fm_stat", "201610240956");
		overviewSubtaskStat.runStat();
		
	}
}
