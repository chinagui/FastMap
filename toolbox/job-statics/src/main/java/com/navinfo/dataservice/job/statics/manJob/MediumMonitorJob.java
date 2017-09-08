package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * MediumMonitorJob
 * 中线监控统计job
 * @author zl 2017.09.04
 *
 */
public class MediumMonitorJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;
	
	protected ManApi manApi = null;

	/**
	 * @param jobInfo
	 */
	public MediumMonitorJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			long t = System.currentTimeMillis();
			
//			Map<String,Map<String,Object>> result = new HashMap<String,Map<String,Object>>();
//			result.put("medium_monitor", getStats());
			Map<String,Object> statsMap = getStats();
			JSONArray stats = new JSONArray();
//			JSONObject statsjson = JSONObject.fromObject(statsMap);
			stats.add(statsMap);
			
			JSONObject result = new JSONObject();
			
			log.debug("medium_monitor---"+JSONObject.fromObject(result).toString());
			log.debug("快线监控统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			result.put("medium_monitor", stats);
			System.out.println(result.toString());
			return result.toString();
//			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	public Map<String,Object> getStats() {
		manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		/*List<Map<String, Integer>> quickProgramMapList = null;
		List<Map<String,String>> stats = new ArrayList<>();
		try {
			quickProgramMapList = getProgramList();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		
		Map<String, Object> mediumMonitorMap = new HashMap<String, Object>();
		
		
		try {
			String planStartDate ="";
			if(manApi.queryConfValueByConfKey("plan_start_date") != null && StringUtils.isNotEmpty(manApi.queryConfValueByConfKey("plan_start_date"))){
				planStartDate=manApi.queryConfValueByConfKey("plan_start_date");
			}
			String planEndDate ="";
			if(manApi.queryConfValueByConfKey("plan_end_date") != null && StringUtils.isNotEmpty(manApi.queryConfValueByConfKey("plan_start_date"))){
				planEndDate=manApi.queryConfValueByConfKey("plan_end_date");
			}
			mediumMonitorMap.put("planStartDate", planStartDate);
			mediumMonitorMap.put("planEndDate", planEndDate);
			mediumMonitorMap.put("unplanNum", getUnplanNum());
			mediumMonitorMap.put("workNum", getWorkNum());
			mediumMonitorMap.put("workOverdueNum", getWorkOverdueNum());
			mediumMonitorMap.put("closeNum", getCloseNum());
			
			BasicDBObject queryProgram = new BasicDBObject();
			queryProgram.put("normalClosed", 1);
			queryProgram.put("type", 1);		
			mediumMonitorMap.put("normalClosedNum",queryCountInMongo(md, "program", queryProgram));
			
			BasicDBObject queryProgram1 = new BasicDBObject();
			queryProgram1.put("advanceClosed", 1);
			queryProgram1.put("type", 1);		
			mediumMonitorMap.put("advanceClosedNum", queryCountInMongo(md, "program", queryProgram1));
			
			BasicDBObject queryProgram2 = new BasicDBObject();
			queryProgram2.put("overdueClosed", 1);
			queryProgram2.put("type", 1);
			mediumMonitorMap.put("overdueClosedNum", queryCountInMongo(md, "program", queryProgram2));
			
			BasicDBObject queryProgram3 = new BasicDBObject();
			queryProgram3.put("type", 0);
			mediumMonitorMap.put("roadPlanIn", queryDatasAvgInMongo(md, "task", queryProgram3,"roadPlanIn"));
			
			BasicDBObject queryProgram4 = new BasicDBObject();
			queryProgram4.put("type", 1);
			double collectLink17UpdateTotal = queryDatasSumInMongo(md, "program", queryProgram4,"collectLink17UpdateTotal");
			mediumMonitorMap.put("collectLink17UpdateTotal", collectLink17UpdateTotal);
			
			BasicDBObject queryProgram5 = new BasicDBObject();
			queryProgram5.put("type", 1);
			double link17AllLen = queryDatasSumInMongo(md, "program", queryProgram5,"link17AllLen");
			mediumMonitorMap.put("link17AllLen", link17AllLen);
			Double roadActualCoverPercent = Math.floor((collectLink17UpdateTotal/link17AllLen)*100);
			mediumMonitorMap.put("roadActualCoverPercent", roadActualCoverPercent.intValue());
			
			BasicDBObject queryProgram6 = new BasicDBObject();
			queryProgram6.put("type", 0);
			mediumMonitorMap.put("poiPlanIn", queryDatasAvgInMongo(md, "task", queryProgram6,"poiPlanIn"));
			
			 
			BasicDBObject queryProgram7 = new BasicDBObject();
			queryProgram7.put("type", 0);
			double poiActualTotal = queryDatasSumInMongo(md, "task", queryProgram7,"poiFinishNum");
			mediumMonitorMap.put("poiActualTotal", poiActualTotal);
			
			 
			BasicDBObject queryProgram8 = new BasicDBObject();
			queryProgram8.put("type", 0);
			double poiAllNum = queryDatasSumInMongo(md, "task", queryProgram8,"poiAllNum");
			mediumMonitorMap.put("poiAllNum", poiAllNum);
			
			Double poiActualCoverPercent = Math.floor((poiActualTotal/poiAllNum)*100);
			mediumMonitorMap.put("poiActualCoverPercent", poiActualCoverPercent.intValue());
			
			BasicDBObject queryProgram9 = new BasicDBObject();
//			queryProgram9.put("type", 0);
//			?????????????????有没有查询条件
			Double collectWorkDate = queryDatasSumInMongo(md, "task", queryProgram9,"workDate");
			mediumMonitorMap.put("collectWorkDate", collectWorkDate.intValue());
			
			BasicDBObject queryProgram10 = new BasicDBObject();
//			queryProgram10.put("type", 0);
//			?????????????????有没有查询条件
			Double collectPlanDate = queryDatasSumInMongo(md, "task", queryProgram10,"collectPlanDate");
			mediumMonitorMap.put("collectPlanDate", collectPlanDate);
			
			double planPercent = 0;
			//???????????要不要 *100
			if(collectPlanDate != 0){
				planPercent = collectWorkDate*100/collectPlanDate;
			}
			mediumMonitorMap.put("planPercent", planPercent);
			
			BasicDBObject queryProgram11 = new BasicDBObject();
			queryProgram11.put("type", 1);
			double collectLinkUpdateTotal = queryDatasSumInMongo(md, "program", queryProgram11,"collectLinkUpdateTotal");
			mediumMonitorMap.put("collectLinkUpdateTotal", collectLinkUpdateTotal);
			
			BasicDBObject queryProgram12 = new BasicDBObject();
			queryProgram12.put("type", 0);
			queryProgram12.put("isassign", 0);
			double unassignRoadPlanNum = queryDatasSumInMongo(md, "program", queryProgram11,"link17AllLen","roadPlanIn");
			mediumMonitorMap.put("unassignRoadPlanNum", unassignRoadPlanNum);
			
			
			/*BasicDBObject queryProgram13 = new BasicDBObject();
			queryProgram13.put("type", 1);
			double workRoadPlanTotal = queryDatasSumInMongo(md, "task", queryProgram13,"roadPlanTotal");*/
			int workRoadPlanTotal = getColumnSumInTask("road_plan_total", "0", "1,2");
			mediumMonitorMap.put("workRoadPlanTotal", workRoadPlanTotal);
			
			BasicDBObject queryProgram14 = new BasicDBObject();
			queryProgram14.put("type", 0);
			queryProgram14.put("status", 0);
			double closeCollectLinkUpdateTotal = queryDatasSumInMongo(md, "task", queryProgram14,"collectLinkUpdateTotal");
			mediumMonitorMap.put("closeCollectLinkUpdateTotal", closeCollectLinkUpdateTotal);
			
			//?????????????进度要不要 *100 
			Double roadActualPercent = (double) 0;
			if((unassignRoadPlanNum + workRoadPlanTotal + closeCollectLinkUpdateTotal) != 0){
				roadActualPercent =collectLinkUpdateTotal*100/(unassignRoadPlanNum + workRoadPlanTotal + closeCollectLinkUpdateTotal);
			}
			mediumMonitorMap.put("roadActualPercent", roadActualPercent);
			
			BasicDBObject queryProgram16 = new BasicDBObject();
			queryProgram16.put("type", 0);
			queryProgram16.put("isassign", 0);
			double unassignPoiPlanNum = queryDatasSumInMongo(md, "task", queryProgram16,"poiAllNum","poiPlanIn");
			mediumMonitorMap.put("unassignPoiPlanNum", unassignPoiPlanNum);
			
			/*BasicDBObject queryProgram17 = new BasicDBObject();
			queryProgram17.put("type", 0);
			double workPoiPlanTotal = queryDatasSumInMongo(md, "task", queryProgram17,"poiPlanTotal");*/
			int workPoiPlanTotal = getColumnSumInTask("poi_plan_total", "0", "1,2");
			mediumMonitorMap.put("workPoiPlanTotal", workPoiPlanTotal);
			
			BasicDBObject queryProgram18 = new BasicDBObject();
			queryProgram18.put("type", 0);
			queryProgram18.put("status", 0);
			double closePoiFinishNum = queryDatasSumInMongo(md, "task", queryProgram18,"poiFinishNum");
			mediumMonitorMap.put("closePoiFinishNum", closePoiFinishNum);
			
			//?????????????进度要不要 *100 
			Double poiActualPercent = (double) 0 ;
			if((unassignPoiPlanNum + workPoiPlanTotal+closePoiFinishNum) != 0){
				poiActualPercent = poiActualTotal*100/(unassignPoiPlanNum + workPoiPlanTotal+closePoiFinishNum);
			}
			mediumMonitorMap.put("poiActualPercent", poiActualPercent);
			
			mediumMonitorMap.put("collectPlanPercent", planPercent);
			mediumMonitorMap.put("collectActualPercent", (roadActualPercent*0.33+poiActualPercent*0.67)*100);
			
			BasicDBObject queryProgram19 = new BasicDBObject();
			queryProgram19.put("type", 2);
			double monthPoiPlanOut = queryDatasSumInMongo(md, "task", queryProgram19,"poiPlanOut");
			mediumMonitorMap.put("monthPoiPlanOut", monthPoiPlanOut);
			
			BasicDBObject queryProgram20 = new BasicDBObject();
			queryProgram20.put("type", 0);
			double poiPlanTotal = queryDatasSumInMongo(md, "task", queryProgram20,"poiPlanOut");
			mediumMonitorMap.put("poiPlanTotal", poiPlanTotal);
			
			BasicDBObject queryProgram21 = new BasicDBObject();
			queryProgram21.put("type", 2);
			String monthPlanStartDate = getStartOrEndDate("task","plan_start_date",2,"");
			mediumMonitorMap.put("monthPlanStartDate", monthPlanStartDate);
			
			String monthPlanEndDateStr = getStartOrEndDate("task","plan_end_date",2," desc");
			String monthPlanEndDate ="";
			if(monthPlanEndDateStr != null && StringUtils.isNotEmpty(monthPlanEndDateStr)){
				monthPlanEndDate=monthPlanEndDateStr;
			}
			mediumMonitorMap.put("monthPlanEndDate", monthPlanEndDate);
			
			int monthPlanDate = StatUtil.daysOfTwo(monthPlanStartDate, monthPlanEndDate);
			mediumMonitorMap.put("monthPlanDate", monthPlanDate);
			
			String curDate = DateUtils.getCurYmd();
			int monthWorkDate = StatUtil.daysOfTwo(monthPlanStartDate, curDate);
			mediumMonitorMap.put("monthWorkDate", monthWorkDate);
			
			Double a = (double) 0 ;
			if(monthPlanDate !=0 && poiPlanTotal != 0){
				a =Math.floor(((monthPoiPlanOut/monthPlanDate)*(monthWorkDate/poiPlanTotal)*1.1)*100);
			}
			mediumMonitorMap.put("monthPlanPercent", a.intValue());
			
			BasicDBObject queryProgram22 = new BasicDBObject();
			queryProgram22.put("type", 2);
			Double monthPoiFinishNum = queryDatasSumInMongo(md, "task", queryProgram22,"monthPoiFinishNum");
			mediumMonitorMap.put("monthPoiFinishNum", monthPoiFinishNum.intValue());
			
			int monthWorkPoiPlanOut = getColumnSumInTask("poi_plan_out", "2", "1,2");
			mediumMonitorMap.put("monthWorkPoiPlanOut", monthWorkPoiPlanOut);
			
			
			BasicDBObject queryProgram23 = new BasicDBObject();
			queryProgram23.put("type", 2);
			queryProgram23.put("status", 0);
			Double monthCloseDay2MonthNum = queryDatasSumInMongo(md, "task", queryProgram23,"day2MonthNum");
			mediumMonitorMap.put("monthCloseDay2MonthNum", monthCloseDay2MonthNum.intValue());
			
			Double monthActualPercent = (double) 0;
			if((monthCloseDay2MonthNum + monthWorkPoiPlanOut) != 0){
				monthActualPercent = monthPoiFinishNum*100/(monthCloseDay2MonthNum + monthWorkPoiPlanOut);
			}
			mediumMonitorMap.put("monthActualPercent", monthActualPercent.intValue());
			
			BasicDBObject queryProgram24 = new BasicDBObject();
			queryProgram24.put("type", 0);
			Double roadPlanTotal = queryDatasSumInMongo(md, "task", queryProgram24,"roadPlanTotal");
			mediumMonitorMap.put("roadPlanTotal", roadPlanTotal.intValue());
			
			BasicDBObject queryProgram25 = new BasicDBObject();
			queryProgram25.put("type", 0);
			Double roadPlanOut = queryDatasSumInMongo(md, "task", queryProgram25,"roadPlanOut");
			mediumMonitorMap.put("roadPlanOut", roadPlanOut.intValue());
			
			Double tipsPlanNum = (double) 0;
			if(collectPlanDate > 0){
				tipsPlanNum =roadPlanOut/collectPlanDate*collectWorkDate;
			}
			mediumMonitorMap.put("tipsPlanNum", tipsPlanNum);
			
			BasicDBObject queryProgram26 = new BasicDBObject();
			queryProgram26.put("type", 0);
			Double tipsActualNum = queryDatasSumInMongo(md, "task", queryProgram26,"collectTipsUploadNum");
			mediumMonitorMap.put("tipsActualNum", tipsActualNum);
			
			BasicDBObject queryProgram27 = new BasicDBObject();
			queryProgram27.put("type", 0);
			Double tips2MarkNum = queryDatasSumInMongo(md, "task", queryProgram27,"tips2MarkNum");
			mediumMonitorMap.put("tips2MarkNum", tips2MarkNum);
			
			BasicDBObject queryProgram28 = new BasicDBObject();
			queryProgram28.put("type", 2);
			Double monthDay2MonthNum = queryDatasSumInMongo(md, "task", queryProgram28,"day2MonthNum");
			mediumMonitorMap.put("monthDay2MonthNum", monthDay2MonthNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mediumMonitorMap;
			
	 }
	
	public int getColumnSumInTask(String column ,String taskType,String taskStatus) throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int total = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = " select sum(t."+column+") total from task t where t.type = "+taskType+" and t.status in ("+taskStatus+") ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				total = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return total;
		
	}
	private String getStartOrEndDate(String tableName, String column,int type, String orderFlag) {
		//select to_char(plan_start_date,'yyyyMMdd') from (select t.plan_start_date from task t where t.type = 2 order by t.plan_start_date ) where rownum=1
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String startDate = "";
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select to_char("+column+",'yyyyMMdd') col from (select t."+column+" from "+tableName+" t where t.type = "+type+" order by t."+column+" "+orderFlag+" ) where rownum=1";
			pstmt = conn.prepareStatement(sql);
			System.out.println(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				startDate = rs.getString("col");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
//		return programNum;
		return startDate;
	}

	private double queryDatasSumInMongo(MongoDao md, String collName, BasicDBObject filter, String cloumn1,String cloumn2) throws Exception {
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			
			double total = 0;
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				double d1 = 0;
				double d2 = 0;
				if(jso.containsKey(cloumn1)){
					d1 = jso.getDouble(cloumn1);
				}
				if(jso.containsKey(cloumn2)){
					d2 = jso.getDouble(cloumn2);
				}
				total += d1*d2;
			}
			
			return total;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中"+cloumn1+","+cloumn2+" 数据求和报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中"+cloumn1+","+cloumn2+" 数据求和报错"+e.getMessage(),e);
		}
	}

	private double queryDatasSumInMongo(MongoDao md, String collName, BasicDBObject filter, String cloumn) throws Exception {
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			
			double total = 0;
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				
				if(jso.containsKey(cloumn)){
					total += jso.getDouble(cloumn);
				}
				
			}
			
			return total;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中数据求和报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中数据求和报错"+e.getMessage(),e);
		}
	}

	private double queryDatasAvgInMongo(MongoDao md, String collName, BasicDBObject filter, String cloumn) throws Exception {
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			
			int count = 0;
			int total = 0;
			double dataAvg = 0;
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				
				if(jso.containsKey(cloumn)){
					count++;
					total += jso.getInt(cloumn);
				}
				
			}
			if(count > 0){
				dataAvg = Math.floor((double)total/count);
			}
			
			return dataAvg;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中数据平均值报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中数据平均值报错"+e.getMessage(),e);
		}
	}

	private int getCloseNum() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int programNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p where p.type = 1 and p.status = 0";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				programNum = rs.getInt("num");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return programNum;
	}


	public List<Map<String,Integer>> getProgramList() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String,Integer>> programList = new ArrayList<>();
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select * from program p where p.type = 4";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				Map<String,Integer> map = new HashMap<>();
				map.put("programId", rs.getInt("program_id"));
//				map.put("dbId", rs.getInt(2));
				programList.add(map);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return programList;
		
	}
	
	public int getProgramNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int programNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p where p.type = 4";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				programNum = rs.getInt("num");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return programNum;
		
	}
	
	public int getFastTotal() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int fastTotal = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|高速公路%' or i.info_type_name like '%道路|城市高速公路%') ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				fastTotal = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastTotal;
		
	}
	
	public int getFastInforNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int fastInforNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|高速公路%' or i.info_type_name like '%道路|城市高速公路%') and  i.method = '矢量制作' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				fastInforNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastInforNum;
		
	}
	
	public int getFastPreNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int fastPreNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|高速公路%' or i.info_type_name like '%道路|城市高速公路%') and  i.method = '预采集' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				fastPreNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastPreNum;
		
	}
	public int getFastOtherNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int fastOtherNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|高速公路%' or i.info_type_name like '%道路|城市高速公路%') and ( i.method <> '预采集' and i.method <> '矢量制作') ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				fastOtherNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastOtherNum;
		
	}
	public int getCommonTotal() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int commonTotal = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%道路|重要一般道%' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				commonTotal = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonTotal;
		
	}
	public int getCommonInforNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int commonInforNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%道路|重要一般道%' and i.method = '矢量制作' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				commonInforNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonInforNum;
		
	}
	public int getCommonPreNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int commonPreNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%道路|重要一般道%' and i.method = '预采集' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				commonPreNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonPreNum;
		
	}
	
	public int getCommonOtherNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int commonOtherNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%道路|重要一般道%' and ( i.method <> '矢量制作' and  i.method <> '预采集') ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				commonOtherNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonOtherNum;
		
	}
	public int getPoiTotal() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiTotal = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%设施%' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				poiTotal = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiTotal;
	}
	public int getPoiInforNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiInforNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%设施%' and i.method = '矢量制作'";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				poiInforNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiInforNum;
	}
	public int getPoiPreNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiPreNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%设施%' and i.method = '预采集'";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				poiPreNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiPreNum;
	}
	public int getPoiOtherNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiOtherNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%设施%' and (i.method <> '矢量制作' and i.method <> '预采集' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				poiOtherNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiOtherNum;
	}
	
	public int getUnplanNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int unplanNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p,city c where p.city_id = c.city_id and p.type = 1 and c.plan_status = 0  ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				unplanNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return unplanNum;
	}
	
	public int getWorkNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int workNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from program p where p.type = 1 and  p.status in (1,2) ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				workNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return workNum;
	}
	public int getWorkOverdueNum() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int workNum = 0;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select count(1) num from  program p,fm_stat_overview_task t where p.program_id = t.program_id and  p.type = 1 and  p.status in (1,2)  and t.task_id in (select a.task_id from fm_stat_overview_task a where a.diff_date < 0 ) ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				workNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return workNum;
	}
	
	public int queryCountInMongo(MongoDao md,String collName,BasicDBObject query){
		int count = 0;
		String personFccName = collName;
		long countL = md.count(personFccName, query);
		count = new Long(countL).intValue();  
		return count;
	}
	
	public Map<String,Integer> getStatDataInMongo(MongoDao md,String collName,BasicDBObject filter) throws Exception{
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Integer> stat = new HashMap<String,Integer>();
			
			int roadPlanTotal = 0;
			int roadActualTotal = 0;
			int poiPlanTotal = 0;
			int poiActualTotal = 0;
			
			int collectTipsUploadNum = 0;
			int dayEditTipsFinishNum = 0;
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				
				if(jso.containsKey("roadPlanTotal")){
					poiPlanTotal += jso.getInt("roadPlanTotal");
				}
				if(jso.containsKey("roadActualTotal")){
					poiPlanTotal += jso.getInt("roadActualTotal");
				}
				if(jso.containsKey("poiPlanTotal")){
					poiPlanTotal += jso.getInt("poiPlanTotal");
				}
				if(jso.containsKey("poiActualTotal")){
					poiPlanTotal += jso.getInt("poiActualTotal");
				}
				if(jso.containsKey("collectTipsUploadNum")){
					collectTipsUploadNum += jso.getInt("collectTipsUploadNum");
				}
				if(jso.containsKey("dayEditTipsFinishNum")){
					dayEditTipsFinishNum += jso.getInt("dayEditTipsFinishNum");
				}
				
			}
			stat.put("roadPlanTotal", roadPlanTotal);
			stat.put("roadActualTotal", roadActualTotal);
			stat.put("poiPlanTotal",poiPlanTotal );
			stat.put("poiActualTotal", poiActualTotal);
			stat.put("collectTipsUploadNum", collectTipsUploadNum);
			stat.put("dayEditTipsFinishNum", dayEditTipsFinishNum);

			
			
			return stat;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中统计数据报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中统计数据报错"+e.getMessage(),e);
		}
	}
	
	private int getDateAvgInMongo(MongoDao md, String collName, BasicDBObject filter, String startDate, String endDate) throws Exception {
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Integer> stat = new HashMap<String,Integer>();
			
			int dateAvg = 0;
			
			int count = 0;
			int total = 0;
			//处理数据
			while(iterator.hasNext()){
				count++;
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				String strStart = null;
				String strEnd = null;
				if(jso.containsKey("startDate")){
					strStart = jso.getString("startDate");
				}
				if(jso.containsKey("endDate")){
					strEnd = jso.getString("endDate");
				}
				
				total += StatUtil.daysOfTwo(strStart, strEnd);
			}

			if(count > 0){
				dateAvg = (int) Math.floor((total/count+1)) ;
			}
			
			return dateAvg;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中平均时间报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中平均时间报错"+e.getMessage(),e);
		}
	}
	
	private double getFastPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double fastPercent= 0;
		int isAdopted1 = 0;//未采纳
		int isAdopted2 = 0;//采纳
		int isAdopted3 = 0;//部分采纳
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = " select i.is_adopted,count(i.is_adopted) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|高速公路%' or i.info_type_name like '%道路|城市高速公路%')  group by i.is_adopted  ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				if(rs.getInt("is_adopted") == 1){
					isAdopted1 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 2){
					isAdopted2 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 3){
					isAdopted3 = rs.getInt("num");
				}
			}
			fastPercent = ((isAdopted2+isAdopted3)/(isAdopted1+isAdopted2+isAdopted3));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastPercent;
	}

	private double getCommonPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double commonPercent= 0;
		int isAdopted1 = 0;//未采纳
		int isAdopted2 = 0;//采纳
		int isAdopted3 = 0;//部分采纳
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = " select i.is_adopted,count(i.is_adopted) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%道路|重要一般道%')  group by i.is_adopted  ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				if(rs.getInt("is_adopted") == 1){
					isAdopted1 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 2){
					isAdopted2 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 3){
					isAdopted3 = rs.getInt("num");
				}
			}
			commonPercent = ((isAdopted2+isAdopted3)/(isAdopted1+isAdopted2+isAdopted3));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonPercent;
	}
	
	private double getPoiPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		double poiPercent= 0;
		int isAdopted1 = 0;//未采纳
		int isAdopted2 = 0;//采纳
		int isAdopted3 = 0;//部分采纳
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = " select i.is_adopted,count(i.is_adopted) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and (i.info_type_name like '%设施%')  group by i.is_adopted  ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				if(rs.getInt("is_adopted") == 1){
					isAdopted1 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 2){
					isAdopted2 = rs.getInt("num");
				}else if(rs.getInt("is_adopted") == 3){
					isAdopted3 = rs.getInt("num");
				}
			}
			poiPercent = ((isAdopted2+isAdopted3)/(isAdopted1+isAdopted2+isAdopted3));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiPercent;
	}
	
	private Map<String, Map<String, Integer>> getCityDetailLMapInMongo(MongoDao md, String collName, BasicDBObject filter) throws Exception {
		Map<String, Map<String, Integer>> cityDetailLMap = null;
		try {
			cityDetailLMap = new HashMap<String, Map<String, Integer>>();
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Integer> stat = new HashMap<String,Integer>();
		
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				int total = 0;
				int roadActualTotal= 0;
				if(jso.containsKey("inforCity")){
					if(cityDetailLMap.containsKey("inforCity")){
						Map<String, Integer> cityMap =  cityDetailLMap.get(jso.getString("inforCity"));
						cityMap.put("total", cityMap.get("total")+1);
						if(jso.containsKey("roadActualTotal")){
							roadActualTotal = jso.getInt("roadActualTotal");
						}
						cityMap.put("roadActualTotal", cityMap.get("roadActualTotal")+roadActualTotal);
						
						cityDetailLMap.put(jso.getString("inforCity"), cityMap);
					}else{
						Map<String, Integer> cityMap = new HashMap<String, Integer>();
						cityMap.put("total", 1);
						if(jso.containsKey("roadActualTotal")){
							roadActualTotal = jso.getInt("roadActualTotal");
						}
						cityMap.put("roadActualTotal", roadActualTotal);
						cityDetailLMap.put(jso.getString("inforCity"), cityMap);
					}
				}
				
			}
			return cityDetailLMap;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中各城市统计数据报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中城市统计数据报错"+e.getMessage(),e);
		}
	}
	
	/*public static void main(String[] args) {
		System.out.println((float)4/3);
		System.out.println((float)5/6);
		System.out.println((float)4/3*5/6);
		System.out.println((float)4/3*5/6*1.1);
//		Double a = Math.floor(((double)4/3*(double)5/6*1.1)*100);
		Double a = 9.8;
		System.out.println(a.intValue());
	}*/
}
