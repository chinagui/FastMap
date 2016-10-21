package com.navinfo.dataservice.engine.statics.overview;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONObject;

public class OverviewMain {	
	private static Logger log = LogManager.getLogger(OverviewMain.class);
	public static final String col_name_task = "fm_stat_overview_task";
	public static final String col_name_blockman = "fm_stat_overview_blockman";
	public static final String col_name_subtask = "fm_stat_overview_subtask";
	static final SimpleDateFormat f = new SimpleDateFormat("yyyy-mm-dd");
	
	public static void main(String[] args) throws ParseException{
		JSONObject statInfo = JSONObject.fromObject("{\"collectPercent\":0,\"collectPlanStartDate\"}");
		String curDate = f.format(new Date());
		calCollectPercent(statInfo,StatMain.db_name,curDate);
		System.out.println(statInfo.get("collectPercent"));
	}

	private static void calCollectPercent(JSONObject statResult,String db_name,String date) throws ParseException {
		MongoDao mdao = new MongoDao(db_name);
		log.info("cal OverviewMain from "+db_name+"."+col_name_blockman);
		FindIterable<Document> result = mdao.find(col_name_blockman, Filters.eq("statDate", date))
				.projection(Projections.include("collectPercent","collectPlanStartDate","collectPlanEndDate"));
		
		int sum = 0;
		int blockCount = 0;
		Set<String> collectStartDateSet=new HashSet<String>();
		Set<String> collectEndDateSet = new HashSet<String>();
		Set<String> collectActualStartDateSet = new HashSet<String>();
		Set<String> collectActualEndDateSet = new HashSet<String>();
		for (Document doc: result){
			//计算 collectPercent
			log.info("calCollectPercent");
			int val = 0;
			Object collectPercent = doc.get("collectPercent");
			if (collectPercent !=null){
				val=Integer.parseInt(collectPercent.toString());
			}
			sum+=val;
			blockCount++;
			//计算 collectPlanStartDate
			collectStartDateSet.add(doc.getString("collectPlanStartDate"));
			//计算 collectPlanEndDate
			collectEndDateSet.add(doc.getString("collectPlanEndDate"));
			//计算collectActualStartDate
			collectActualStartDateSet.add(doc.getString("collectActualStartDate"));
			//计算collectActualEndDate
			collectActualEndDateSet.add(doc.getString("collectActualEndDate"));
			//
		}
		BigDecimal collectPercent = new BigDecimal(sum).divide(new BigDecimal(blockCount));
		statResult.put("collectPercent", collectPercent) ;
		String minStartDate = Collections.min(collectStartDateSet);
		String minEndDate = Collections.max(collectStartDateSet);
		statResult.put("collectPlanStartDate", minStartDate) ;
		statResult.put("collectPlanEndDate", minEndDate) ;
		statResult.put("collectPlanDate", daysBetween(minStartDate,minEndDate)) ;
		Collections.max(collectStartDateSet);
//		statResult.put("collectActualStartDate", collectActualStartDate) ;
	}
	private static int daysBetween(String smdate,String bdate) throws ParseException{  
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
