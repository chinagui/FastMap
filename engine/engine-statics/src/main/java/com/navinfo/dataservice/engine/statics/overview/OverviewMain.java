package com.navinfo.dataservice.engine.statics.overview;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class OverviewMain {	
	private static Logger log = LogManager.getLogger(OverviewMain.class);
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddhhmmss");
	public static final String col_name_overview_main = "fm_stat_overview";
	private String db_name;
	private static String stat_date;
	private static String stat_time;

	public OverviewMain(String dbn,String stat_time) {
		this.db_name = dbn;
		this.stat_date = stat_time.substring(0, 8);
		this.stat_time = stat_time;
	}
		
	/**
	 * @Title: runStat
	 * @Description: 运行脚本的方法
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月21日 下午2:55:50 
	 */
	public void runStat() {
		log.info("-- begin stat:"+col_name_overview_main);
		try {
			initMongoDb();
			exeCalculate(stat_date,db_name);
			log.info("-- end stat:"+col_name_overview_main);
			//System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void exeCalculate(String statDate, String dbName) throws ParseException, ServiceException {
		MongoDao mdao = new MongoDao(dbName);
		log.info("db_name:"+dbName);
		Document statInfo = calCollectPercent();
		log.info(statInfo.toString());
		mdao.updateOne(col_name_overview_main,
				Filters.eq("statDate", statInfo.getString("stateInfo")),
				statInfo,
				true
				);
		log.info("new stat info upsert into db");
}

	/**
	 * @return 
	 * @throws ServiceException 
	 * 
	 */
	private static Document calCollectPercent() throws ServiceException {
		Connection conn = null;
		try{	
			conn =  DBConnector.getInstance().getManConnection();
			Document statResult = new Document();
			
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT AVG(P.PERCENT) PERCENT,");
			sb.append("       MIN(P.COLLECT_PLAN_START_DATE) COLLECT_PLAN_START_DATE,");
			sb.append("       MAX(P.COLLECT_PLAN_END_DATE) COLLECT_PLAN_END_DATE,");
			sb.append("       MIN(P.COLLECT_ACTUAL_START_DATE) COLLECT_ACTUAL_START_DATE,");
			sb.append("       MIN(P.COLLECT_ACTUAL_END_DATE) COLLECT_ACTUAL_END_DATE,");
			sb.append("       AVG(P.COLLECT_PERCENT) COLLECT_PERCENT,");
			sb.append("       MIN(P.DAILY_PLAN_START_DATE) DAILY_PLAN_START_DATE,");
			sb.append("       MAX(P.DAILY_PLAN_END_DATE) DAILY_PLAN_END_DATE,");
			sb.append("       MIN(P.DAILY_ACTUAL_START_DATE) DAILY_ACTUAL_START_DATE,");
			sb.append("       MIN(P.DAILY_ACTUAL_END_DATE) DAILY_ACTUAL_END_DATE,");
			sb.append("       AVG(P.DAILY_PERCENT) DAILY_PERCENT,");
			sb.append("       MIN(P.MONTHLY_PLAN_START_DATE) MONTHLY_PLAN_START_DATE,");
			sb.append("       MAX(P.MONTHLY_PLAN_END_DATE) MONTHLY_PLAN_END_DATE,");
			sb.append("       MIN(P.MONTHLY_ACTUAL_START_DATE) MONTHLY_ACTUAL_START_DATE,");
			sb.append("       MIN(P.MONTHLY_ACTUAL_END_DATE) MONTHLY_ACTUAL_END_DATE,");
			sb.append("       AVG(P.MONTHLY_PERCENT) MONTHLY_PERCENT,");
			sb.append("       SUM(P.POI_PLAN_TOTAL) POI_PLAN_TOTAL,");
			sb.append("       SUM(P.ROAD_PLAN_TOTAL) ROAD_PLAN_TOTAL");
			sb.append("  FROM FM_STAT_OVERVIEW_PROGRAM P");
			sb.append(" WHERE P.TYPE = 4");
			
			String sql = sb.toString();

			statResult = queryRunner.query(conn, sql, new ResultSetHandler<Document>() {
				@Override
				public Document handle(ResultSet rs) throws SQLException {
					Document statResult = new Document();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						int planDate = 0;
						int diffDate = 0;

						statResult.put("percent", rs.getInt("PERCENT"));
						statResult.put("collectPercent", rs.getInt("COLLECT_PERCENT")); 
						statResult.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
						statResult.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT")); 
						
						Timestamp planStartDate = rs.getTimestamp("COLLECT_PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("COLLECT_PLAN_END_DATE");
						if((planStartDate!=null)&&(planEndDate!=null)){
							planDate = StatUtil.daysOfTwo(planEndDate,planStartDate);
							diffDate = StatUtil.daysOfTwo(planEndDate,new Date());;
						}

						statResult.put("collectPlanStartDate", StatUtil.formatDate(rs.getTimestamp("COLLECT_PLAN_START_DATE"))); 
						statResult.put("collectPlanEndDate", StatUtil.formatDate(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
						statResult.put("collectActualStartDate", StatUtil.formatDate(rs.getTimestamp("COLLECT_ACTUAL_START_DATE"))); 
						statResult.put("collectActualEndDate", StatUtil.formatDate(rs.getTimestamp("COLLECT_ACTUAL_END_DATE"))); 
						statResult.put("collectPlanDate", planDate);
						statResult.put("collectDiffDate", diffDate);
						
						planStartDate = rs.getTimestamp("DAILY_PLAN_START_DATE");
						planEndDate = rs.getTimestamp("DAILY_PLAN_END_DATE");
						if((planStartDate!=null)&&(planEndDate!=null)){
							planDate = StatUtil.daysOfTwo(planEndDate,planStartDate);
							diffDate = StatUtil.daysOfTwo(planEndDate,new Date());;
						}
						statResult.put("dailyPlanStartDate",  StatUtil.formatDate(rs.getTimestamp("DAILY_PLAN_START_DATE"))); 
						statResult.put("dailyPlanEndDate", StatUtil.formatDate(rs.getTimestamp("DAILY_PLAN_END_DATE"))); 
						statResult.put("dailyActualStartDate", StatUtil.formatDate(rs.getTimestamp("DAILY_ACTUAL_START_DATE"))); 
						statResult.put("dailyActualEndDate", StatUtil.formatDate(rs.getTimestamp("DAILY_ACTUAL_END_DATE"))); 
						statResult.put("dailyPlanDate", planDate);
						statResult.put("dailyDiffDate", diffDate);
						
						planStartDate = rs.getTimestamp("MONTHLY_PLAN_START_DATE");
						planEndDate = rs.getTimestamp("MONTHLY_PLAN_END_DATE");
						if((planStartDate!=null)&&(planEndDate!=null)){
							planDate = StatUtil.daysOfTwo(planEndDate,planStartDate);
							diffDate = StatUtil.daysOfTwo(planEndDate,new Date());;
						}
						statResult.put("monthlyPlanStartDate", StatUtil.formatDate(rs.getTimestamp("MONTHLY_PLAN_START_DATE"))); 
						statResult.put("monthlyPlanEndDate", StatUtil.formatDate(rs.getTimestamp("MONTHLY_PLAN_END_DATE"))); 
						statResult.put("monthlyActualStartDate", StatUtil.formatDate(rs.getTimestamp("MONTHLY_ACTUAL_START_DATE"))); 
						statResult.put("monthlyActualEndDate", StatUtil.formatDate(rs.getTimestamp("MONTHLY_ACTUAL_END_DATE"))); 
						statResult.put("monthlyPlanDate", planDate);
						statResult.put("monthlyDiffDate",  diffDate);
						
						statResult.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						statResult.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
					}
					return statResult;
				}
			});		
			statResult.put("statDate", stat_date);
			statResult.put("statTime", stat_time);
			return statResult;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询子任务进展详情失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
			if(iterator.next().equalsIgnoreCase(col_name_overview_main)){
				flag_task = false;
				break;
			}
		}
		if(flag_task){
			//创建collection集合
			md.createCollection(col_name_overview_main);
			md.getCollection(col_name_overview_main).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_overview_main + " ok");
			log.info("-- -- create mongo index on " + col_name_overview_main + "(statDate) ok");
		}
			// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("statDate", stat_date);
		mongoDao.deleteMany(col_name_overview_main, query);
		
	}
	
	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewMain overviewSubtaskStat = new OverviewMain("fm_stat", "201703171828");
		overviewSubtaskStat.runStat();
	}

//	private static void exeCalculate(String statDate, String dbName) throws ParseException {
//		MongoDao mdao = new MongoDao(dbName);
//		log.info("db_name:"+dbName);
//		Document statInfo = calCollectPercent(mdao,statDate);
//		log.info(statInfo.toJson().toString());
//		mdao.updateOne(col_name_overview_main,
//				Filters.eq("statDate", statInfo.getString("stateInfo")),
//				statInfo,
//				true
//				);
//		log.info("new stat info upsert into db");
//	}

//	private static Document calCollectPercent(MongoDao mdao, String statDate) throws ParseException {
//		log.info("cal from "+col_name_blockman);
//		
//		log.info("根据当前日期取block的统计信息");
//		FindIterable<Document> result = mdao.find(col_name_blockman, Filters.eq("statDate", statDate))
//				.projection(Projections.include(
//						"blockManId",
//						"collectPercent",
//						"collectPlanStartDate",
//						"collectPlanEndDate",
//						"workDetail",
//						"collectActualStartDate",
//						"collectActualEndDate",
//						"dailyPercent",
//						"dailyPlanStartDate",
//						"dailyPlanEndDate",
//						"dailyActualStartDate", 
//						"dailyActualEndDate"
//						));
//		
//		List<Integer> dailyPercentSet = new ArrayList<Integer>();
//		List<Integer> colPercentSet = new ArrayList<Integer>();
//		Set<String> collectStartDateSet=new HashSet<String>();
//		Set<String> collectEndDateSet = new HashSet<String>();
//		Set<String> collectActualStartDateSet = new HashSet<String>();
//		Set<String> collectActualEndDateSet = new HashSet<String>();
//		Set<String> dailyPlanStartDateSet = new HashSet<String>();
//		Set<String> dailyPlanEndDateSet = new HashSet<String>();
//		Set<String> dailyActualStartDateSet = new HashSet<String>();
//		Set<String> dailytActualEndDateSet = new HashSet<String>();
//		int poiPlanTotalSum = 0;
//		int roadPlanTotalSum = 0;
//		log.info("根据mongo查询结果进行计算");
//		for (Document doc: result){
//			log.info("blockManId:"+doc.getInteger("blockManId"));
//			//计算 collectPercent
//			log.info("calCollectPercent");
//			Object collectPercent = doc.get("collectPercent");
//			if (collectPercent !=null){
//				colPercentSet.add(Integer.parseInt(collectPercent.toString()));
//			}
//			log.info("计算 collectPlanStartDate");
//			collectStartDateSet.add(doc.getString("collectPlanStartDate"));
//			log.info("计算 collectPlanEndDate");
//			collectEndDateSet.add(doc.getString("collectPlanEndDate"));
//			log.info("计算collectActualStartDate");
//			String colActStartDate = doc.getString("collectActualStartDate");
//			if (StringUtils.isNotEmpty(colActStartDate)){
//				collectActualStartDateSet.add(colActStartDate);
//			}
//			log.info("计算collectActualEndDate");
//			String colActEndDate = doc.getString("collectActualEndDate");
//			if (StringUtils.isNotEmpty(colActEndDate)){
//				collectActualEndDateSet.add(colActEndDate);
//			}
//			log.info("计算dailyPercent");
//			Object dailyPercent = doc.get("dailyPercent");
//			if (dailyPercent !=null){
//				dailyPercentSet.add(Integer.parseInt(dailyPercent.toString()));
//			}
//			log.info("计算dailyPlanStartDateSet");
//			String dailyPlanStartDate = doc.getString("dailyPlanStartDate");
//			if (StringUtils.isNotEmpty(dailyPlanStartDate)){
//				dailyPlanStartDateSet.add(dailyPlanStartDate);
//			}
//			log.info("计算dailyPlanEndDateSet");
//			String dailyPlanEndDate = doc.getString("dailyPlanEndDate");
//			if (StringUtils.isNotEmpty(dailyPlanEndDate)){
//				dailyPlanEndDateSet.add(dailyPlanEndDate);
//			}
//			log.info("计算dailyActualStartDateSet");
//			String dailyActualStartDate = doc.getString("dailyActualStartDate");
//			if (StringUtils.isNotEmpty(dailyActualStartDate)){
//				dailyActualStartDateSet.add(dailyActualStartDate);
//			}
//			log.info("计算dailyActualEndDate");
//			String dailytActualEndDate = doc.getString("dailyActualEndDate");
//			if (StringUtils.isNotEmpty(dailytActualEndDate)){
//				dailytActualEndDateSet.add(dailytActualEndDate);
//			}
//			log.info("计算poiPlanTotal,roadPlanTotal");
//			Object workDetailJson = doc.get("workDetail");
//			if(workDetailJson!=null){
//				Document workDetailDoc = (Document)workDetailJson;
//				Object poiPlanTotal = workDetailDoc.get("poiPlanTotal");
//				if(poiPlanTotal!=null){
//					poiPlanTotalSum+=Integer.parseInt(poiPlanTotal.toString());
//				}
//				Object roadPlanTotal = workDetailDoc.get("roadPlanTotal");
//				if(roadPlanTotal!=null){
//					roadPlanTotalSum+=Integer.parseInt(roadPlanTotal.toString());
//				}
//			}
//			
//		}
//		Document statResult = new Document();
//		statResult.put("collectPercent", avg(colPercentSet)) ;
//		String minStartDate = calMin(collectStartDateSet);
//		String maxEndDate = calMax(collectStartDateSet);
//		statResult.put("collectPlanStartDate", minStartDate) ;
//		statResult.put("collectPlanEndDate", maxEndDate) ;
//		statResult.put("collectPlanDate", daysBetween(minStartDate,maxEndDate)) ;
//		String collectActualStartDate = calMin(collectActualStartDateSet);
//		statResult.put("collectActualStartDate", collectActualStartDate) ;
//		String  collectActualEndDate= calMax(collectActualEndDateSet);
//		statResult.put("collectActualEndDate", collectActualEndDate) ;
//		statResult.put("collectDiffDate", daysBetween(collectActualEndDate,maxEndDate));
//		statResult.put("dailyPercent", avg(dailyPercentSet));
//		String dailyPlanStartDate = calMin(dailyPlanStartDateSet);
//		statResult.put("dailyPlanStartDate", dailyPlanStartDate);
//		String dailyPlanEndDate = calMax(dailyPlanEndDateSet);
//		statResult.put("dailyPlanEndDate", dailyPlanEndDate);
//		//dailyPlanDate
//		statResult.put("dailyPlanDate", daysBetween(dailyPlanStartDate,dailyPlanEndDate));
//		//dailyActualStartDate
//		String dailyActualStartDate = calMin(dailyActualStartDateSet);
//		statResult.put("dailyActualStartDate", dailyActualStartDate);
//		//dailytActualEndDate
//		String dailytActualEndDate = calMax(dailytActualEndDateSet);
//		statResult.put("dailyActualEndDate", dailytActualEndDate);
//		statResult.put("dailyDiffDate", daysBetween(statDate,dailytActualEndDate));
//		statResult.put("poiPlanTotal", poiPlanTotalSum);
//		statResult.put("roadPlanTotal", roadPlanTotalSum);
//		statResult.put("statDate", statDate);
//		statResult.put("statTime", dateTimeFormat.format(new Date()));
//		return statResult;
//	}

//	private static String calMax(Collection<String> collection) {
//		if(collection.isEmpty())return "";
//		return Collections.max(collection);
//	}
//
//	private static String calMin(Collection<String> collection) {
//		if(collection.isEmpty())return "";
//		return Collections.min(collection);
//	}
//	/**
//	 * @param dailyPercentSet
//	 * @return int 数组的平均值BigDecimal
//	 */
//	private static double avg(List<Integer> dailyPercentSet){
//		int sum = 0;
//		int size=dailyPercentSet.size();
//		if(size==0) return 0;
//		for(Integer pct:dailyPercentSet){
//			sum+=pct.intValue();
//		}	
//		MathContext mc = new MathContext(3);
//		BigDecimal collectPercent = new BigDecimal(sum,mc).divide(new BigDecimal(size,mc),3);
//		return collectPercent.doubleValue();
//		
//	}
	
//	/**
//	 * @param smdate开始日期
//	 * @param bdate结束日期
//	 * @return 两个日期之间的天数
//	 * @throws ParseException
//	 */
//	private static int daysBetween(String smdate,String bdate) throws ParseException{
//		if (StringUtils.isEmpty(smdate)||StringUtils.isEmpty(bdate)){
//			return 0;
//		}
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");  
//        Calendar cal = Calendar.getInstance();    
//        cal.setTime(sdf.parse(smdate));    
//        long time1 = cal.getTimeInMillis();                 
//        cal.setTime(sdf.parse(bdate));    
//        long time2 = cal.getTimeInMillis();         
//        long between_days=(time2-time1)/(1000*3600*24);  
//            
//       return Integer.parseInt(String.valueOf(between_days));     
//    }  

	
}
