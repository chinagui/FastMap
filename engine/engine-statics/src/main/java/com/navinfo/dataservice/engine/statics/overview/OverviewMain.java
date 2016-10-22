package com.navinfo.dataservice.engine.statics.overview;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

public class OverviewMain {	
	private static Logger log = LogManager.getLogger(OverviewMain.class);
	public static final String col_name_overview_main = "fm_stat_overview";
	public static final String col_name_blockman = "fm_stat_overview_blockman";
	public static final String col_name_task="fm_stat_overview_task";
	public static final String col_name_subtask = "fm_stat_overview_subtask";
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddhhmmss");
	/**
	 * @param args 第一个参数是统计日期(yyyyMMdd),第二个参数是统计的目标数据库名字，比如fm_stat
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException{
		String statDate = dateFormat.format(new Date());
		if(args!=null&&args.length>1){
			statDate = args[1];//外部传入的统计时间
		}
		String dbName = StatMain.db_name;
		if(args!=null&&args.length>2){
			dbName = args[2];//外部传入的dbname
		}
		log.info("参数 statDate:"+statDate+",dbName:"+dbName);
		exeCalculate(statDate, dbName);
	}

	private static void exeCalculate(String statDate, String dbName) throws ParseException {
		MongoDao mdao = new MongoDao(dbName);
		log.info("db_name:"+dbName);
		Document statInfo = calCollectPercent(mdao,statDate);
		log.info(statInfo.toJson().toString());
		mdao.createIndex(col_name_overview_main,new Document("statDate", 1));
		log.info("index created");
		mdao.updateOne(col_name_overview_main,
				Filters.eq("statDate", statInfo.getString("stateInfo")),
				statInfo,
				true
				);
		log.info("new stat info upsert into db");
	}

	private static Document calCollectPercent(MongoDao mdao, String statDate) throws ParseException {
		log.info("cal from "+col_name_blockman);
		
		log.info("根据当前日期取block的统计信息");
		FindIterable<Document> result = mdao.find(col_name_blockman, Filters.eq("statDate", statDate))
				.projection(Projections.include(
						"blockManId",
						"collectPercent",
						"collectPlanStartDate",
						"collectPlanEndDate",
						"workDetail",
						"collectActualStartDate",
						"collectActualEndDate",
						"dailyPercent",
						"dailyPlanStartDate",
						"dailyPlanEndDate",
						"dailyActualStartDate", 
						"dailytActualEndDate"
						));
		
		List<Integer> dailyPercentSet = new ArrayList<Integer>();
		List<Integer> colPercentSet = new ArrayList<Integer>();
		Set<String> collectStartDateSet=new HashSet<String>();
		Set<String> collectEndDateSet = new HashSet<String>();
		Set<String> collectActualStartDateSet = new HashSet<String>();
		Set<String> collectActualEndDateSet = new HashSet<String>();
		Set<String> dailyPlanStartDateSet = new HashSet<String>();
		Set<String> dailyPlanEndDateSet = new HashSet<String>();
		Set<String> dailyActualStartDateSet = new HashSet<String>();
		Set<String> dailytActualEndDateSet = new HashSet<String>();
		int poiPlanTotalSum = 0;
		int roadPlanTotalSum = 0;
		log.info("根据mongo查询结果进行计算");
		for (Document doc: result){
			log.info("blockManId:"+doc.getInteger("blockManId"));
			//计算 collectPercent
			log.info("calCollectPercent");
			Object collectPercent = doc.get("collectPercent");
			if (collectPercent !=null){
				colPercentSet.add(Integer.parseInt(collectPercent.toString()));
			}
			log.info("计算 collectPlanStartDate");
			collectStartDateSet.add(doc.getString("collectPlanStartDate"));
			log.info("计算 collectPlanEndDate");
			collectEndDateSet.add(doc.getString("collectPlanEndDate"));
			log.info("计算collectActualStartDate");
			String colActStartDate = doc.getString("collectActualStartDate");
			if (StringUtils.isNotEmpty(colActStartDate)){
				collectActualStartDateSet.add(colActStartDate);
			}
			log.info("计算collectActualEndDate");
			String colActEndDate = doc.getString("collectActualEndDate");
			if (StringUtils.isNotEmpty(colActEndDate)){
				collectActualEndDateSet.add(colActEndDate);
			}
			log.info("计算dailyPercent");
			Object dailyPercent = doc.get("dailyPercent");
			if (dailyPercent !=null){
				dailyPercentSet.add(Integer.parseInt(dailyPercent.toString()));
			}
			log.info("计算dailyPlanStartDateSet");
			String dailyPlanStartDate = doc.getString("dailyPlanStartDate");
			if (StringUtils.isNotEmpty(dailyPlanStartDate)){
				dailyPlanStartDateSet.add(dailyPlanStartDate);
			}
			log.info("计算dailyPlanEndDateSet");
			String dailyPlanEndDate = doc.getString("dailyPlanEndDate");
			if (StringUtils.isNotEmpty(dailyPlanEndDate)){
				dailyPlanEndDateSet.add(dailyPlanEndDate);
			}
			log.info("计算dailyActualStartDateSet");
			String dailyActualStartDate = doc.getString("dailyActualStartDate");
			if (StringUtils.isNotEmpty(dailyActualStartDate)){
				dailyActualStartDateSet.add(dailyActualStartDate);
			}
			log.info("计算dailytActualEndDate");
			String dailytActualEndDate = doc.getString("dailytActualEndDate");
			if (StringUtils.isNotEmpty(dailytActualEndDate)){
				dailytActualEndDateSet.add(dailytActualEndDate);
			}
			log.info("计算poiPlanTotal,roadPlanTotal");
			Object workDetailJson = doc.get("workDetail");
			if(workDetailJson!=null){
				Document workDetailDoc = (Document)workDetailJson;
				Object poiPlanTotal = workDetailDoc.get("poiPlanTotal");
				if(poiPlanTotal!=null){
					poiPlanTotalSum+=Integer.parseInt(poiPlanTotal.toString());
				}
				Object roadPlanTotal = workDetailDoc.get("roadPlanTotal");
				if(roadPlanTotal!=null){
					roadPlanTotalSum+=Integer.parseInt(roadPlanTotal.toString());
				}
			}
			
		}
		Document statResult = new Document();
		statResult.put("collectPercent", avg(colPercentSet)) ;
		String minStartDate = calMin(collectStartDateSet);
		String maxEndDate = calMax(collectStartDateSet);
		statResult.put("collectPlanStartDate", minStartDate) ;
		statResult.put("collectPlanEndDate", maxEndDate) ;
		statResult.put("collectPlanDate", daysBetween(minStartDate,maxEndDate)) ;
		String collectActualStartDate = calMin(collectActualStartDateSet);
		statResult.put("collectActualStartDate", collectActualStartDate) ;
		String  collectActualEndDate= calMax(collectActualEndDateSet);
		statResult.put("collectActualStartDate", collectActualEndDate) ;
		statResult.put("collectDiffDate", daysBetween(collectActualEndDate,maxEndDate));
		statResult.put("dailyPercent", avg(dailyPercentSet));
		String dailyPlanStartDate = calMin(dailyPlanStartDateSet);
		statResult.put("dailyPlanStartDate", dailyPlanStartDate);
		String dailyPlanEndDate = calMax(dailyPlanEndDateSet);
		statResult.put("dailyPlanEndDate", dailyPlanEndDate);
		//dailyPlanDate
		statResult.put("dailyPlanDate", daysBetween(dailyPlanStartDate,dailyPlanEndDate));
		//dailyActualStartDate
		String dailyActualStartDate = calMin(dailyActualStartDateSet);
		statResult.put("dailyActualStartDate", dailyActualStartDate);
		//dailytActualEndDate
		String dailytActualEndDate = calMax(dailytActualEndDateSet);
		statResult.put("dailytActualEndDate", dailytActualEndDate);
		statResult.put("dailyDiffDate", daysBetween(statDate,dailytActualEndDate));
		statResult.put("poiPlanTotal", poiPlanTotalSum);
		statResult.put("roadPlanTotal", roadPlanTotalSum);
		statResult.put("statDate", statDate);
		statResult.put("statTime", dateTimeFormat.format(new Date()));
		return statResult;
	}

	private static String calMax(Collection<String> collection) {
		if(collection.isEmpty())return "";
		return Collections.max(collection);
	}

	private static String calMin(Collection<String> collection) {
		if(collection.isEmpty())return "";
		return Collections.min(collection);
	}
	/**
	 * @param dailyPercentSet
	 * @return int 数组的平均值BigDecimal
	 */
	private static double avg(List<Integer> dailyPercentSet){
		int sum = 0;
		int size=dailyPercentSet.size();
		if(size==0) return 0;
		for(Integer pct:dailyPercentSet){
			sum+=pct.intValue();
		}	
		BigDecimal collectPercent = new BigDecimal(sum).divide(new BigDecimal(size));
		return collectPercent.doubleValue();
		
	}
	
	/**
	 * @param smdate开始日期
	 * @param bdate结束日期
	 * @return 两个日期之间的天数
	 * @throws ParseException
	 */
	private static int daysBetween(String smdate,String bdate) throws ParseException{
		if (StringUtils.isEmpty(smdate)||StringUtils.isEmpty(bdate)){
			return 0;
		}
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");  
        Calendar cal = Calendar.getInstance();    
        cal.setTime(sdf.parse(smdate));    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(sdf.parse(bdate));    
        long time2 = cal.getTimeInMillis();         
        long between_days=(time2-time1)/(1000*3600*24);  
            
       return Integer.parseInt(String.valueOf(between_days));     
    }  

	
}
