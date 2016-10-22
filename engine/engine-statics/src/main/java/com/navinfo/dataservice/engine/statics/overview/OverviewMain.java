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
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONObject;

public class OverviewMain {	
	private static Logger log = LogManager.getLogger(OverviewMain.class);
	public static final String col_name_overview_main = "fm_stat_overview";
	public static final String col_name_blockman = "fm_stat_overview_blockman";
	public static final String col_name_task="fm_stat_overview_task";
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
	static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyymmddhhMMss");
	private static final String stateInfoTemplate = "{\"collectPercent\":0,"
			+ "\"collectPlanStartDate\":\"\","
			+ "\"collectPlanEndDate\":\"\","
			+ "\"collectPlanDate\":0,"
			+ "\"collectActualStartDate\":\"\","
			+ "\"collectActualEndDate\":\"\","
			+ "\"collectDiffDate\":0,"
			+ "\"dailyPercent\":0,"
			+ "\"dailyPlanStartDate\":\"\","
			+ "\"dailyPlanEndDate\":\"\","
			+ "\"dailyPlanDate\":0,"
			+ "\"dailyActualStartDate\":\"\","
			+ "\"dailytActualEndDate\":\"\","
			+ "\"dailyDiffDate\":0,"
			+ "\"poiPlanTotal\":0," //block_man记录的poi计划量汇总
			+ "\"roadPlanTotal\":0,"//block_man记录的road计划量汇总
			+ "\"statDate\":\"\","//统计日期
			+ "\"statTime\":\"\","//统计详细时间
			+ "}";
	public static void main(String[] args) throws ParseException{
		
		MongoDao mdao = new MongoDao(StatMain.db_name);
		log.info("db_name:"+StatMain.db_name);
		Document statInfo = calCollectPercent(mdao);
		log.debug(statInfo);
		mdao.insertOne(col_name_overview_main,statInfo);
	}

	private static Document calCollectPercent(MongoDao mdao) throws ParseException {
		log.info("cal from "+col_name_blockman);
		Date now = new Date();
		String curDate = dateFormat.format(now);
		FindIterable<Document> result = mdao.find(col_name_blockman, Filters.eq("statDate", curDate))
				.projection(Projections.include(
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
		for (Document doc: result){
			//计算 collectPercent
			log.info("calCollectPercent");
			Object collectPercent = doc.get("collectPercent");
			if (collectPercent !=null){
				colPercentSet.add(Integer.parseInt(collectPercent.toString()));
			}
			//计算 collectPlanStartDate
			collectStartDateSet.add(doc.getString("collectPlanStartDate"));
			//计算 collectPlanEndDate
			collectEndDateSet.add(doc.getString("collectPlanEndDate"));
			//计算collectActualStartDate
			String colActStartDate = doc.getString("collectActualStartDate");
			if (StringUtils.isNotEmpty(colActStartDate)){
				collectActualStartDateSet.add(colActStartDate);
			}
			//计算collectActualEndDate
			String colActEndDate = doc.getString("collectActualEndDate");
			if (StringUtils.isNotEmpty(colActEndDate)){
				collectActualEndDateSet.add(colActEndDate);
			}
			//计算dailyPercent
			log.info("cal dailyPercent");
			Object dailyPercent = doc.get("dailyPercent");
			if (dailyPercent !=null){
				dailyPercentSet.add(Integer.parseInt(dailyPercent.toString()));
			}
			//计算dailyPlanStartDateSet
			String dailyPlanStartDate = doc.getString("dailyPlanStartDate");
			if (StringUtils.isNotEmpty(dailyPlanStartDate)){
				dailyPlanStartDateSet.add(dailyPlanStartDate);
			}
			//计算dailyPlanEndDateSet
			String dailyPlanEndDate = doc.getString("dailyPlanEndDate");
			if (StringUtils.isNotEmpty(dailyPlanEndDate)){
				dailyPlanEndDateSet.add(dailyPlanEndDate);
			}
			//计算dailyActualStartDateSet
			String dailyActualStartDate = doc.getString("dailyActualStartDate");
			if (StringUtils.isNotEmpty(dailyActualStartDate)){
				dailyActualStartDateSet.add(dailyActualStartDate);
			}
			//计算dailytActualEndDate
			String dailytActualEndDate = doc.getString("dailytActualEndDate");
			if (StringUtils.isNotEmpty(dailytActualEndDate)){
				dailytActualEndDateSet.add(dailytActualEndDate);
			}
			//poiPlanTotal
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
		Document statResult = Document.parse(stateInfoTemplate);
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
		statResult.put("dailyDiffDate", daysBetween(curDate,dailytActualEndDate));
		statResult.put("poiPlanTotal", poiPlanTotalSum);
		statResult.put("roadPlanTotal", roadPlanTotalSum);
		statResult.put("statDate", curDate);
		statResult.put("statTime", dateTimeFormat.format(now));
		
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
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
        Calendar cal = Calendar.getInstance();    
        cal.setTime(sdf.parse(smdate));    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(sdf.parse(bdate));    
        long time2 = cal.getTimeInMillis();         
        long between_days=(time2-time1)/(1000*3600*24);  
            
       return Integer.parseInt(String.valueOf(between_days));     
    }  

	
}
