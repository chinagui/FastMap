package com.navinfo.dataservice.engine.statics.overview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;

/**
 * 
 * @ClassName overviewStatProgram
 * @author Han Shaoming
 * @date 2016年10月20日 上午11:17:54
 * @Description TODO
 */
public class OverviewProgramMain {
	
	private static Logger log = null;
	public static final String col_name_program = "fm_stat_overview_program";
	private String db_name;
	private String statDate;
	private String statTime;

	public OverviewProgramMain(String dbn, String statTime) {
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
			if(iterator.next().equalsIgnoreCase(col_name_program)){
				flag_task = false;
				break;
			}
		}
		if(flag_task){
			//创建collection集合
			md.createCollection(col_name_program);
			md.getCollection(col_name_program).createIndex(new BasicDBObject("programId", 1));
			md.getCollection(col_name_program).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_program + " ok");
			log.info("-- -- create mongo index on " + col_name_program + "(programId，statDate) ok");
		}
			// 删除当天重复统计数据
			BasicDBObject query = new BasicDBObject();
			query.put("statDate", statDate);
			mongoDao.deleteMany(col_name_program, query);
		
	}
	
	public void runStat() {
		log = LogManager.getLogger(OverviewProgramMain.class);
		log.info("-- begin stat:" + col_name_program);
		try {
			// 初始化mongodb数据库
			initMongoDb();

			//执行统计
			List<FmStatOverviewProgram> programStatList = OracleDao.getProgramListWithStat();
			
			MongoDao md = new MongoDao(db_name);
			List<Document> programList = new ArrayList<Document>();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmmss");
			for(FmStatOverviewProgram program:programStatList){
				Document doc = new Document();
				doc.append("programId", program.getProgramId());
				doc.append("status", program.getStatus());
				doc.append("type", program.getType());
				doc.append("percent", program.getPercent());
				doc.append("progress", program.getProgress());
				doc.append("planStartDate", df.format(program.getPlanStartDate()));
				doc.append("planEndDate", df.format(program.getPlanEndDate()));
				doc.append("planDate", program.getPlanDate());
				doc.append("actualStartDate", df.format(program.getActualStartDate()));
				doc.append("actualEndDate", df.format(program.getActualEndDate()));
				doc.append("diffDate", program.getDiffDate());
				doc.append("poiPlanTotal", program.getPoiPlanTotal());
				doc.append("roadPlanTotal", program.getRoadPlanTotal());
				
				doc.append("collectProgress", program.getCollectProgress());
				doc.append("collectPercent", program.getCollectPercent());
				doc.append("collectPlanStartDate", df.format(program.getCollectPlanStartDate()));
				doc.append("collectPlanEndDate", df.format(program.getCollectPlanEndDate()));
				doc.append("collectPlanDate", program.getCollectPlanDate());
				doc.append("collectActualStartDate", df.format(program.getCollectActualStartDate()));
				doc.append("collectActualEndDate", df.format(program.getCollectActualEndDate()));
				doc.append("collectDiffDate", program.getCollectDiffDate());
				
				doc.append("dailyProgress", program.getDailyProgress());
				doc.append("dailyPercent", program.getDailyPercent());
				doc.append("dailyPlanStartDate", df.format(program.getDailyPlanStartDate()));
				doc.append("dailyPlanEndDate", df.format(program.getDailyPlanEndDate()));
				doc.append("dailyPlanDate", program.getDailyPlanDate());
				doc.append("dailyActualStartDate", df.format(program.getDailyActualStartDate()));
				doc.append("dailyActualEndDate", df.format(program.getDailyActualEndDate()));
				doc.append("dailyDiffDate", program.getDailyDiffDate());
				
				doc.append("monthlyProgress", program.getMonthlyProgress());
				doc.append("monthlyPercent", program.getMonthlyPercent());
				doc.append("monthlyPlanStartDate", df.format(program.getMonthlyPlanStartDate()));
				doc.append("monthlyPlanEndDate", df.format(program.getMonthlyPlanEndDate()));
				doc.append("monthlyPlanDate", program.getMonthlyPlanDate());
				doc.append("monthlyActualStartDate", df.format(program.getMonthlyActualStartDate()));
				doc.append("monthlyActualEndDate", df.format(program.getMonthlyActualEndDate()));
				doc.append("monthlyDiffDate", program.getMonthlyDiffDate());
				
				doc.append("statDate", statDate);
				doc.append("statTime", statTime);
				
				programList.add(doc);
			}
			md.insertMany(col_name_program, programList);
			
			log.info("-- end stat:" + col_name_program);
			System.exit(0);
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
		OverviewProgramMain overviewSubtaskStat = new OverviewProgramMain("fm_stat", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		//OverviewTaskMain overviewSubtaskStat = new OverviewTaskMain("fm_stat", "20161024144950");
		overviewSubtaskStat.runStat();
	}

}
