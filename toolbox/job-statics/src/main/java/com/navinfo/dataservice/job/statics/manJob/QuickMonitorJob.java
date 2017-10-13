package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * QuickMonitorJob
 * 快线监控统计job
 * @author zl 2017.09.04
 *
 */
public class QuickMonitorJob extends AbstractStatJob {

	protected ManApi manApi = null;

	/**
	 * @param jobInfo
	 */
	public QuickMonitorJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			long t = System.currentTimeMillis();
			QuickMonitorJobRequest statReq = (QuickMonitorJobRequest)request;
			String timestamp = statReq.getTimestamp();
			
			Map<String,Object> statsMap = getStats(timestamp);
			JSONArray stats = new JSONArray();
//			JSONObject statsjson = JSONObject.fromObject(statsMap);
			stats.add(statsMap);
			
			JSONObject result = new JSONObject();
			
			log.debug("quick_monitor---"+JSONObject.fromObject(result).toString());
			log.debug("快线监控统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			System.out.println(JSONObject.fromObject(result).toString());
			result.put("quick_monitor", stats);
			//System.out.println(result.toString());
			return result.toString();
//			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	public Map<String,Object> getStats(String timestamp) {
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
		
		Map<String, Object> quickMonitorMap = new HashMap<String, Object>();
		
		
		try {
			quickMonitorMap.put("programNum", getProgramNum());
			quickMonitorMap.put("fastTotal", getFastTotal());
			quickMonitorMap.put("fastInforNum", getFastInforNum());
			quickMonitorMap.put("fastPreNum", getFastPreNum());
			quickMonitorMap.put("fastOtherNum", getFastOtherNum());
			quickMonitorMap.put("commonTotal", getCommonTotal());
			quickMonitorMap.put("commonInforNum", getCommonInforNum());
			quickMonitorMap.put("commonPreNum", getCommonPreNum());
			quickMonitorMap.put("commonOtherNum", getCommonOtherNum());
			quickMonitorMap.put("poiTotal", getPoiTotal());
			quickMonitorMap.put("poiInforNum", getPoiInforNum());
			quickMonitorMap.put("poiPreNum", getPoiPreNum());
			quickMonitorMap.put("poiOtherNum", getPoiOtherNum());
			quickMonitorMap.put("unplanNum", getUnplanNum());
			quickMonitorMap.put("workNum", getWorkNum());
			
			BasicDBObject queryProgram = new BasicDBObject();
			queryProgram.put("timestamp", timestamp);
			queryProgram.put("status", 0);
			queryProgram.put("type", 4);			
			quickMonitorMap.put("unproduceCloseNum",queryCountInMongo(md, "program", queryProgram));
			
			BasicDBObject queryProgram1 = new BasicDBObject();
			queryProgram1.put("timestamp", timestamp);
			queryProgram1.put("isProduce", 1);
			queryProgram1.put("type", 4);
			quickMonitorMap.put("produceNum", queryCountInMongo(md, "program", queryProgram1));
			
			BasicDBObject queryProgramTotal = new BasicDBObject();
			queryProgramTotal.put("timestamp", timestamp);
			queryProgramTotal.put("type", 4);
			int programTotal = queryCountInMongo(md, "program", queryProgramTotal);
			
			BasicDBObject queryProgram2 = new BasicDBObject();
			queryProgram2.put("timestamp", timestamp);
			queryProgram2.put("status", 2);
			queryProgram2.put("type", 4);
			//草稿项目
			int programDraftTotal = queryCountInMongo(md, "program", queryProgram2);
			
			BasicDBObject queryProgram3 = new BasicDBObject();
			queryProgram3.put("timestamp", timestamp);
			queryProgram3.put("advanceClosed", 1);
			queryProgram3.put("type", 4);
			//提前关闭项目
			int programAdvanceClosedTotal = queryCountInMongo(md, "program", queryProgram3);
			
			BasicDBObject queryProgram4= new BasicDBObject();
			queryProgram4.put("timestamp", timestamp);
			queryProgram4.put("isOverDue", 2);
			queryProgram4.put("type", 4);
			//逾期项目
			int programOverDueTotal = queryCountInMongo(md, "program", queryProgram4);
			 
			quickMonitorMap.put("normalNum",programTotal-programDraftTotal-programAdvanceClosedTotal-programOverDueTotal);
			
			quickMonitorMap.put("advanceNum",programAdvanceClosedTotal);
			
			BasicDBObject queryProgram5 = new BasicDBObject();
			queryProgram5.put("timestamp", timestamp);
			queryProgram5.put("collectAdvanceClosed", 1);
			queryProgram5.put("type", 4);
			quickMonitorMap.put("collectAdvanceNum", queryCountInMongo(md, "program", queryProgram5));
			
			BasicDBObject queryProgram6 = new BasicDBObject();
			queryProgram6.put("timestamp", timestamp);
			queryProgram6.put("dayAdvanceClosed", 1);
			queryProgram6.put("type", 4);
			quickMonitorMap.put("dayAdvanceNum", queryCountInMongo(md, "program", queryProgram6));
			
			BasicDBObject queryProgram7 = new BasicDBObject();
			queryProgram7.put("timestamp", timestamp);
			queryProgram7.put("produceAdvanceClosed", 1);
			queryProgram7.put("type", 4);
			quickMonitorMap.put("produceAdvanceNum", queryCountInMongo(md, "program", queryProgram7));
			
			quickMonitorMap.put("overdueNum", programOverDueTotal);
			
			BasicDBObject queryProgram8 = new BasicDBObject();
			queryProgram8.put("timestamp", timestamp);
			queryProgram8.put("collectOverdue", 1);
			queryProgram8.put("type", 4);
			quickMonitorMap.put("collectOverdueNum", queryCountInMongo(md, "program", queryProgram8));
			
			//采集逾期原因统计
			quickMonitorMap.put("collectOverdueReasonNum", getOverdueResonMap(0));
			
			BasicDBObject queryProgram9 = new BasicDBObject();
			queryProgram9.put("timestamp", timestamp);
			queryProgram9.put("dayOverdue", 1);
			queryProgram9.put("type", 4);
			quickMonitorMap.put("dayOverdueNum", queryCountInMongo(md, "program", queryProgram9));
			//日编逾期原因统计
			quickMonitorMap.put("dayOverdueReasonNum", getOverdueResonMap(1));
			
			BasicDBObject queryProgram10 = new BasicDBObject();
			queryProgram10.put("timestamp", timestamp);
			queryProgram10.put("produceOverdue", 1);
			queryProgram10.put("type", 4);
			quickMonitorMap.put("produceOverdueNum", queryCountInMongo(md, "program", queryProgram10));
			 
			BasicDBObject queryProgram11 = new BasicDBObject();
			queryProgram11.put("timestamp", timestamp);
			queryProgram11.put("type", 4);
			Map<String,Integer> statMap = getStatDataInMongo(md, "program", queryProgram11);
			quickMonitorMap.put("roadPlanTotal",statMap.get("roadPlanTotal"));
			quickMonitorMap.put("roadActualTotal",Math.floor(statMap.get("roadActualTotal")/1000));
			quickMonitorMap.put("poiPlanTotal",statMap.get("poiPlanTotal"));
			quickMonitorMap.put("poiActualTotal",statMap.get("poiActualTotal"));
			
			int tipsPlanTotal = 0;
			String tipPlanTotalStr = manApi.queryConfValueByConfKey("tips_plan_total");
			if(tipPlanTotalStr != null && StringUtils.isNotEmpty(tipPlanTotalStr)){
				tipsPlanTotal = Integer.parseInt(tipPlanTotalStr);
			}
			quickMonitorMap.put("tipsPlanTotal", tipsPlanTotal);
			
			quickMonitorMap.put("collectTipsUploadNum", statMap.get("collectTipsUploadNum"));
			
			quickMonitorMap.put("dayEditTipsFinishNum", statMap.get("dayEditTipsFinishNum"));
			
			BasicDBObject queryProgram12 = new BasicDBObject();
			queryProgram12.put("timestamp", timestamp);
			queryProgram12.put("isDay2Month", 1);	
			queryProgram12.put("type", 4);
			quickMonitorMap.put("day2MonthNum", queryCountInMongo(md, "program", queryProgram12));
			
			BasicDBObject queryProgram13 = new BasicDBObject();
			queryProgram13.put("timestamp", timestamp);
			queryProgram13.put("isDay2Month", 0);
			queryProgram13.put("type", 4);
			quickMonitorMap.put("noday2MonthNum", queryCountInMongo(md, "program", queryProgram13));
			
			BasicDBObject queryProgram14 = new BasicDBObject();
			queryProgram14.put("timestamp", timestamp);
			queryProgram14.put("isTips2Mark", 1);
			queryProgram14.put("type", 4);
			quickMonitorMap.put("aumarkNum", queryCountInMongo(md, "program", queryProgram14));
			
			BasicDBObject queryProgram15 = new BasicDBObject();
			queryProgram15.put("timestamp", timestamp);
			queryProgram15.put("isTips2Mark", 0);	
			queryProgram15.put("type", 4);
			quickMonitorMap.put("noAumarkNum", queryCountInMongo(md, "program", queryProgram15));
			
			//获取时间平均值
			BasicDBObject queryProgram16 = new BasicDBObject();
			queryProgram16.put("timestamp", timestamp);
			queryProgram16.put("type", 0);	
			queryProgram16.put("status", 0);	
			queryProgram16.put("programType", 4);
			quickMonitorMap.put("collectAverageDate", getDateAvgInMongo(md, "task",queryProgram16,"actualStartDate","actualEndDate"));
			
			BasicDBObject queryProgram17 = new BasicDBObject();
			queryProgram17.put("timestamp", timestamp);
			queryProgram16.put("status", 0);	
			queryProgram17.put("type", 1);	
			queryProgram17.put("programType", 4);
			quickMonitorMap.put("dayAverageDate", getDateAvgInMongo(md, "task",queryProgram17,"actualStartDate","actualEndDate"));
			
			BasicDBObject queryProgram18 = new BasicDBObject();
			queryProgram18.put("timestamp", timestamp);
			queryProgram16.put("status", 0);	
			queryProgram18.put("type", 4);	
			quickMonitorMap.put("produceAverageDate", getDateAvgInMongo(md, "program",queryProgram18,"actualEndDate","produceDate"));
			
			quickMonitorMap.put("fastPercent", getFastPercent());
			quickMonitorMap.put("commonPercent", getCommonPercent());
			quickMonitorMap.put("poiPercent", getPoiPercent());
			
			BasicDBObject queryProgram19 = new BasicDBObject();
			queryProgram19.put("timestamp", timestamp);
			queryProgram19.put("type", 4);	
			Map<String,Map<String,Integer>> cityDetailMap = getCityDetailMapInMongo(md, "program", queryProgram19);
			quickMonitorMap.put("cityDetail", cityDetailMap);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		log.debug(JSONArray.fromObject(quickMonitorMap));
		
		return quickMonitorMap;
			
	 }
	

	private Map<String,Integer> getOverdueResonMap(int taskType) throws SQLException {
		//select t.overdue_reason,count(t.overdue_reason) from task t,program p where t.program_id = p.program_id  and t.type = 0 and p.type = 4 group by t.overdue_reason;
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String,Integer> overdueResonMap = null;
		try {
			overdueResonMap = new HashMap<String,Integer>();
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "select t.overdue_reason ,count(t.overdue_reason) num from task t,program p where t.program_id = p.program_id  and t.type = "+taskType+" and p.type = 4 group by t.overdue_reason";
			pstmt = conn.prepareStatement(sql);
			Map<String,Integer> resonMap = new HashMap<String,Integer>();
			rs = pstmt.executeQuery();
			int total = 0 ;
			while(rs.next()){
				total+=rs.getInt("num");
				resonMap.put(rs.getString("overdue_reason"), rs.getInt("num"));
			}
			
			if(resonMap != null && resonMap.size() > 0 && total > 0){
				for(Entry<String, Integer> entry : resonMap.entrySet()){
					String key = entry.getKey();
					Integer value = entry.getValue();
					if(key != null && StringUtils.isNotEmpty(key)){
						overdueResonMap.put(key, (value*100/total));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return overdueResonMap;
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
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and i.info_type_name like '%设施%' and (i.method <> '矢量制作' and i.method <> '预采集') ";
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
			String sql  = "select count(1) num from infor i where i.plan_status = 0 ";
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
			String sql  = "select count(1) num from program p where p.type = 4 and  p.status in (1,2) ";
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
					roadPlanTotal += jso.getInt("roadPlanTotal");
				}
				if(jso.containsKey("roadActualTotal")){
					roadActualTotal += jso.getInt("roadActualTotal");
				}
				if(jso.containsKey("poiPlanTotal")){
					poiPlanTotal += jso.getInt("poiPlanTotal");
				}
				if(jso.containsKey("poiActualTotal")){
					poiActualTotal += jso.getInt("poiActualTotal");
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
	
	private double getDateAvgInMongo(MongoDao md, String collName, BasicDBObject filter, String startDate, String endDate) throws Exception {
		try {
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Integer> stat = new HashMap<String,Integer>();
			
			double dateAvg = 0;
			
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
				dateAvg = Math.floor((double)total/count);
			}
			
			return dateAvg;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中平均时间报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中平均时间报错"+e.getMessage(),e);
		}
	}
	
	private int getFastPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int fastPercent= 0;
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
//			System.out.println(sql);
//			System.out.println((isAdopted1+isAdopted2+isAdopted3));
			if((isAdopted1+isAdopted2+isAdopted3) > 0){
				fastPercent = ((isAdopted2+isAdopted3)*100/(isAdopted1+isAdopted2+isAdopted3));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return fastPercent;
	}

	private int getCommonPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int commonPercent= 0;
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
//			System.out.println(sql);
//			System.out.println((isAdopted1+isAdopted2+isAdopted3));
			if((isAdopted1+isAdopted2+isAdopted3) > 0){
				commonPercent = ((isAdopted2+isAdopted3)*100/(isAdopted1+isAdopted2+isAdopted3));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return commonPercent;
	}
	
	private int getPoiPercent() {
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiPercent= 0;
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
//			System.out.println(sql);
//			System.out.println(isAdopted1+isAdopted2+isAdopted3);
			if((isAdopted1+isAdopted2+isAdopted3) > 0){
				poiPercent = ((isAdopted2+isAdopted3)*100/(isAdopted1+isAdopted2+isAdopted3));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiPercent;
	}
	
	private Map<String, Map<String, Integer>> getCityDetailMapInMongo(MongoDao md, String collName, BasicDBObject filter) throws Exception {
		Map<String, Map<String, Integer>> cityDetailMap = null;
		try {
			cityDetailMap = new HashMap<String, Map<String, Integer>>();
			if(filter == null ){
//				filter = new BasicDBObject("timestamp", null);
			}
			FindIterable<Document> findIterable = md.find(collName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
		
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				int roadActualTotal= 0;
				if(jso.containsKey("inforCity") && jso.getString("inforCity") != null && StringUtils.isNotEmpty(jso.getString("inforCity"))){
					if(cityDetailMap.containsKey("inforCity")){
						Map<String, Integer> cityMap =  cityDetailMap.get(jso.getString("inforCity"));
						cityMap.put("total", cityMap.get("total")+1);
						if(jso.containsKey("roadActualTotal")){
							roadActualTotal = jso.getInt("roadActualTotal");
						}
						cityMap.put("roadActualTotal", cityMap.get("roadActualTotal")+roadActualTotal);
						
						cityDetailMap.put(jso.getString("inforCity"), cityMap);
					}else{
						Map<String, Integer> cityMap = new HashMap<String, Integer>();
						cityMap.put("total", 1);
						if(jso.containsKey("roadActualTotal")){
							roadActualTotal = jso.getInt("roadActualTotal");
						}
						cityMap.put("roadActualTotal", roadActualTotal);
						cityDetailMap.put(jso.getString("inforCity"), cityMap);
					}
				}
				
			}
			return cityDetailMap;
		} catch (Exception e) {
			log.error("查询mongo "+collName+" 中各城市统计数据报错"+e.getMessage());
			throw new Exception("查询mongo "+collName+" 中城市统计数据报错"+e.getMessage(),e);
		}
	}
}
