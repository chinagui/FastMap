package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

/** 
 * @ClassName: OverviewGroupStat
 * @author zl
 * @date 2016年10月22日
 * @Description: OverviewGroupStat.java
 */
public class OverviewGroupStat {

	/**
	 * 
	 */
	private static Logger log = null;
	public static final String col_name_group = "fm_stat_overview_group";
	public static final String col_name_blockman = "fm_stat_overview_blockman";
	private String db_name;
	private static String stat_date;
	private static String stat_time;

	public OverviewGroupStat(String dbn, String stat_time) {
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
			md.getCollection(col_name_group).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection " + col_name_group + " ok");
			log.info("-- -- create mongo index on " + col_name_group + "(group_id，stat_date) ok");
		}

		// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
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
			List<Document> groupStatList = getGroupStat();
			MongoDao md = new MongoDao(db_name);
			md.insertMany(col_name_group, groupStatList);
			
			log.info("-- end stat:" + col_name_group);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: getGroupStat
	 * @Description: 查询数据库表生产 不存在 重复 groupId 的 GroupId list
	 * @return
	 * @throws ParseException  List<Document>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月22日 下午10:42:49 
	 */
	private List<Document> getGroupStat() throws ParseException {
		MongoDao mdao = new MongoDao(db_name);
		log.info("cal OverviewMain from "+db_name+"."+col_name_blockman);
		
		//查询fm_stat_overview_blockman 表中statDate = stat_date的  采集的 数据
		FindIterable<Document> result_collect = mdao.find(col_name_blockman, Filters.eq("statDate", stat_date)).sort(Sorts.descending("collectGroupId","statDate"))
				.projection(Projections.include("status","collectGroupId","collectPercent","collectPlanStartDate","collectPlanEndDate","poiPlanTotal","roadPlanTotal","statDate"));
		//查询fm_stat_overview_blockman 表中statDate = stat_date的  日编的 数据
		FindIterable<Document> result_daily = mdao.find(col_name_blockman, Filters.eq("statDate", stat_date)).sort(Sorts.descending("dailyGroupId","statDate"))
				.projection(Projections.include("status","dailyGroupId","dailyPercent","dailyPlanStartDate","dailyPlanEndDate","poiPlanTotal","roadPlanTotal","statDate"));
		
		List<Document> collectGroupList =mergeRepeatGroupId(result_collect,0);//生产采集的groupList 
		List<Document> dailyGroupList =mergeRepeatGroupId(result_daily,1); //生产日编的groupList 
		List<Document> groupList = new ArrayList<Document>(); //总的group 集合 (无重复的group_id)
		
		groupList.addAll(collectGroupList);
		groupList.addAll(dailyGroupList);
		
		return groupList;
	}


	/**
	 * @Title: mergeRepeatGroupId
	 * @Description: 根据 数据库查询的_overview_blockman list 生产 _overview_blockman list
	 * @param result
	 * @param stage
	 * @return
	 * @throws ParseException  List<Document>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月22日 下午10:38:51 
	 */
	private List<Document> mergeRepeatGroupId(FindIterable<Document> result,int stage) throws ParseException {
		int groupId_flag = -1 ;
		Date planStartDate_flag = null;
		Date planEndDate_flag = null;
		int percent_flag = 0;
		int poiPlanTotal_flag = 0;
		int roadPlanTotal_flag = 0;
		
		List<Document> groupList = new ArrayList<Document>();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		for(Document doc: result){
			Document groupDoc=new Document();
			groupDoc.append("stage", stage);//#####stage
			//*************************
			if(stage == 0){//采集
				if(Integer.parseInt(doc.get("collectGroupId").toString()) == groupId_flag){//重复 groupId
					groupList.remove(groupList.size()-1);//删除list 上一条数据
					
					//比较collectPlanStartDate 时间
					if( StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("collectPlanStartDate").toString())) < 0){//说明doc.get("collectPlanStartDate")时间更靠前
						planStartDate_flag = df.parse(doc.get("collectPlanStartDate").toString());
					}
					//比较collectPlanEndDate 时间
					if( StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("collectPlanStartDate").toString())) > 0){//说明doc.get("collectPlanEndDate")时间更靠后
						planEndDate_flag = df.parse(doc.get("collectPlanEndDate").toString());
					}
					percent_flag=(int)Math.rint((percent_flag + (Double.parseDouble(doc.get("collectPercent").toString())))/2);//取 进度的平均值并取整
					poiPlanTotal_flag+= Integer.parseInt(doc.get("poiPlanTotal").toString());
					roadPlanTotal_flag+= Integer.parseInt(doc.get("roadPlanTotal").toString());
							
					//#####actualEndDate
					//#####diffDate
					if(Integer.parseInt(doc.get("status").toString()) == 0){ //状态为已关闭
						groupDoc.append("actualEndDate", doc.get("statDate").toString());
						groupDoc.append("diffDate", StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("statDate").toString())));
						
					}else{ //只要有一个未关闭
						groupDoc.append("actualEndDate", null);
						groupDoc.append("diffDate", null);
					}
				}else{  //不存在重复的groupId
					groupId_flag = Integer.parseInt(doc.get("collectGroupId").toString());
					planStartDate_flag = df.parse(doc.get("collectPlanStartDate").toString());
					planEndDate_flag = df.parse(doc.get("collectPlanEndDate").toString());
					percent_flag = (int)Math.rint(Double.parseDouble(doc.get("collectPercent").toString()));
					poiPlanTotal_flag = Integer.parseInt(doc.get("poiPlanTotal").toString());
					roadPlanTotal_flag = Integer.parseInt(doc.get("roadPlanTotal").toString());
							
					if(Integer.parseInt(doc.get("status").toString()) == 0){ //状态为已关闭
						groupDoc.append("actualEndDate", doc.get("statDate").toString());
						groupDoc.append("diffDate", StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("statDate").toString())));
					}else{
						groupDoc.append("actualEndDate", doc.get("statDate").toString());
						groupDoc.append("diffDate", StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("statDate").toString())));
					}
				}
			}else{ //日编
				if(Integer.parseInt(doc.get("dailyGroupId").toString()) == groupId_flag){//存在重复的 groupId
					groupList.remove(groupList.size()-1);//删除list 上一条数据
					
					//比较collectPlanStartDate 时间
					if( StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("dailyPlanStartDate").toString())) < 0){//说明doc.get("collectPlanStartDate")时间更靠前
						planStartDate_flag = df.parse(doc.get("dailyPlanStartDate").toString());
					}
					//比较collectPlanEndDate 时间
					if( StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("dailyPlanStartDate").toString())) > 0){//说明doc.get("collectPlanEndDate")时间更靠后
						planEndDate_flag = df.parse(doc.get("dailyPlanEndDate").toString());
					}
					percent_flag=(int)Math.rint((percent_flag + (Double.parseDouble(doc.get("dailyPercent").toString())))/2);//取 进度的平均值并取整

					poiPlanTotal_flag+= Integer.parseInt(doc.get("poiPlanTotal").toString());
					roadPlanTotal_flag+= Integer.parseInt(doc.get("roadPlanTotal").toString());
							
					if(Integer.parseInt(doc.get("status").toString()) == 0){ //状态为已关闭
						groupDoc.append("actualEndDate", doc.get("statDate").toString());
						groupDoc.append("diffDate", StatUtil.daysOfTwo(planStartDate_flag, df.parse(doc.get("statDate").toString())));
						
					}else{ //只要有一个未关闭
						groupDoc.append("actualEndDate", null);
						groupDoc.append("diffDate", null);
					}
					
				}else{//不存在重复的 groupId
					groupId_flag = Integer.parseInt(doc.get("dailyGroupId").toString());
					planStartDate_flag = df.parse(doc.get("dailyPlanStartDate").toString());
					planEndDate_flag = df.parse(doc.get("dailyPlanEndDate").toString());
					percent_flag = (int)Math.rint(Double.parseDouble(doc.get("dailyPercent").toString()));
					poiPlanTotal_flag = Integer.parseInt(doc.get("poiPlanTotal").toString());
					roadPlanTotal_flag = Integer.parseInt(doc.get("roadPlanTotal").toString());
					
					if(Integer.parseInt(doc.get("status").toString()) == 0){ //状态为已关闭
						groupDoc.append("actualEndDate", doc.get("statDate").toString());
						groupDoc.append("diffDate", StatUtil.daysOfTwo(df.parse(doc.get("dailyPlanStartDate").toString()), df.parse(doc.get("statDate").toString())));
					}else{
						groupDoc.append("actualEndDate", null);
						groupDoc.append("diffDate", null);
					}
				}
			}
			//*************************
			groupDoc.append("groupId", groupId_flag);//#####groupId
			groupDoc.append("percent", percent_flag);//#####percent
			groupDoc.append("planStartDate", df.format(planStartDate_flag));//#####planStartDate
			groupDoc.append("planEndDate", df.format(planEndDate_flag));//#####planEndDate
			groupDoc.append("planDate", StatUtil.daysOfTwo(planStartDate_flag,planEndDate_flag));//#####planDate
			groupDoc.append("actualStartDate", df.format(planStartDate_flag));//#####actualStartDate
			
			groupDoc.append("poiPlanTotal", poiPlanTotal_flag);//#####poiPlanTotal
			groupDoc.append("roadPlanTotal", roadPlanTotal_flag);//#####roadPlanTotal
			groupDoc.append("statDate", stat_date);//#####statDate
			groupDoc.append("statTime", stat_time);//#####statTime
			
			groupList.add(groupDoc);
		}
		
		return groupList;
	}



	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewGroupStat overviewSubtaskStat = new OverviewGroupStat("fm_stat", "201610240956");
		overviewSubtaskStat.runStat();
		
	}
}
