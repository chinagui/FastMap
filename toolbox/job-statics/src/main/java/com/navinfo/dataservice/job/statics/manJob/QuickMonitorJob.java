package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ThreadExecuteException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * QuickMonitorJob
 * 快线监控统计job
 * @author zl 2017.09.04
 *
 */
public class QuickMonitorJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

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
			
			Map<String,List<Map<String,String>>> result = new HashMap<String,List<Map<String,String>>>();
			result.put("quick_monitor", getStats());

			log.debug("quick_monitor---"+JSONObject.fromObject(result).toString());
			log.debug("快线监控统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	public List<Map<String,String>> getStats() {
//		List<Map<String, Integer>> quickMonitorMapList = null;
		List<Map<String, Integer>> quickProgramMapList = null;
		List<Map<String,String>> stats = new ArrayList<>();
		try {
			quickProgramMapList = getProgramList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		
		Map<String, Integer> quickMonitorMap = new HashMap<String, Integer>();
		
		
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
			queryProgram.put("status", 0);
			quickMonitorMap.put("unproduceCloseNum",queryCountInMongo(md, "program", queryProgram));
			
			BasicDBObject queryProgram1 = new BasicDBObject();
			queryProgram.put("isProduce", 1);
			quickMonitorMap.put("produceNum", queryCountInMongo(md, "program", queryProgram1));
			
			int programTotal = queryCountInMongo(md, "program", null);
			
			BasicDBObject queryProgram2 = new BasicDBObject();
			queryProgram.put("status", 2);
			//草稿项目
			int programDraftTotal = queryCountInMongo(md, "program", queryProgram2);
			
			BasicDBObject queryProgram3 = new BasicDBObject();
			queryProgram.put("advanceClosed", 1);
			//提前关闭项目
			int programAdvanceClosedTotal = queryCountInMongo(md, "program", queryProgram3);
			
			BasicDBObject queryProgram4= new BasicDBObject();
			queryProgram.put("isOverDue", 2);
			//逾期项目
			int programOverDueTotal = queryCountInMongo(md, "program", queryProgram4);
			 
			quickMonitorMap.put("normalNum",programTotal-programDraftTotal-programAdvanceClosedTotal-programOverDueTotal);
			
			quickMonitorMap.put("advanceNum",programAdvanceClosedTotal);
			
			BasicDBObject queryProgram5 = new BasicDBObject();
			queryProgram.put("collectAdvanceClosed", 1);
			quickMonitorMap.put("collectAdvanceNum", queryCountInMongo(md, "program", queryProgram5));
			
			BasicDBObject queryProgram6 = new BasicDBObject();
			queryProgram.put("dayAdvanceClosed", 1);
			quickMonitorMap.put("dayAdvanceNum", queryCountInMongo(md, "program", queryProgram6));
			
			BasicDBObject queryProgram7 = new BasicDBObject();
			queryProgram.put("produceAdvanceClosed", 1);
			quickMonitorMap.put("produceAdvanceNum", queryCountInMongo(md, "program", queryProgram7));
			
			quickMonitorMap.put("overdueNum", programOverDueTotal);
			
			BasicDBObject queryProgram8 = new BasicDBObject();
			queryProgram.put("collectOverdue", 1);
			quickMonitorMap.put("collectOverdueNum", queryCountInMongo(md, "program", queryProgram8));
			
//			//逾期原因统计
//			Map<String,Integer> overdueResonMap = new HashMap<String,Integer>();
//			BasicDBObject queryProgram8 = new BasicDBObject();
//			queryProgram.put("collectOverdue", 1);
//			quickMonitorMap.put("collectOverdueNum", queryCountInMongo(md, "program", queryProgram8));
//			
			
			quickMonitorMap.put("", queryCountInMongo(md, "program", queryProgram));
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		
				

		
		log.debug(JSONArray.fromObject(stats));
		
		return stats;
			
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			DbUtils.commitAndClose(conn);
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
			String sql  = "select count(1) num from program p,infor i where p.infor_id = i.infor_id and  p.type = 4 and  i.plan_status = 0 ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				unplanNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.commitAndClose(conn);
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
			String sql  = "select count(1) num from program p where p.status in (1,2) ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				workNum = rs.getInt("num");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.commitAndClose(conn);
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
	
}
