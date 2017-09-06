package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

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
 * DayPlanJob
 * Day_规划量统计job
 * @author sjw
 *
 */
public class DayPlanJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public DayPlanJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.job.statics.AbstractStatJob#stat()
	 */
	@Override
	public String stat() throws JobException {
		try {
			long t = System.currentTimeMillis();
			log.debug("所有Day_规划量数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			Map<String,List<Map<String,String>>> result = new HashMap<String,List<Map<String,String>>>();
			result.put("task_day_plan", getStats());

			//log.debug("task_day_plan---"+JSONObject.fromObject(result).toString());
			
			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	public List<Map<String,String>> getStats() {
		List<Map<String, Integer>> taskIdMapList = null;
		List<Map<String,String>> stats = new ArrayList<>();
		try {
			taskIdMapList = getTaskIdList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		
		for (Map<String, Integer> taskIdMap : taskIdMapList) {
			Integer dbId = taskIdMap.get("dbId");
			Integer taskId = taskIdMap.get("taskId");
			
			if(md.find("task_day_plan",Filters.eq("taskId", taskId+"")).iterator().hasNext()){
				continue;
			}
			
			Connection conn=null;
			
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				QueryRunner run = new QueryRunner();
				
				String rdLinkSql = "SELECT NVL(SUM(r.length),0) FROM rd_link r,DATA_PLAN d WHERE r.link_pid = d.pid AND r.u_record <> 2 AND d.data_type = 2 AND d.task_id = ";
				String poiSql = "SELECT COUNT(1) FROM ix_poi p,DATA_PLAN d WHERE p.pid = d.pid AND p.u_record <> 2 AND d.data_type = 1 AND d.task_id = ";
				String planSuffix = " AND d.is_plan_selected=1 ";
				
					
				Map<String,String> map  = new HashMap<>();
				
				String sql1 = rdLinkSql+taskId+planSuffix;
				String sql2 = rdLinkSql+taskId;
				String sql3 = poiSql+taskId+planSuffix;
				String sql4 = poiSql+taskId;
				String sql5 = rdLinkSql+taskId+" AND r.kind >= 1 AND r.kind <= 7";
				String sql6 = rdLinkSql+taskId+" AND r.kind >= 2 AND r.kind <= 7";
				
				String linkPlanLen = run.query(conn, sql1,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				String linkAllLen = run.query(conn, sql2,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				
				String poiPlanNum = run.query(conn, sql3,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				String poiAllNum = run.query(conn, sql4,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				String link17AllLen = run.query(conn, sql5,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				String link27AllLen = run.query(conn, sql6,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				
				
				map.put("taskId", taskId.toString());
				map.put("linkPlanLen", linkPlanLen);
				map.put("linkAllLen", linkAllLen);
				map.put("poiPlanNum", poiPlanNum);
				map.put("poiAllNum", poiAllNum);
				map.put("link17AllLen", link17AllLen);
				map.put("link27AllLen", link27AllLen);
				
				stats.add(map);
				

			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("dbId("+dbId+")Day_规划量数据统计失败");
			}finally{
				DbUtils.closeQuietly(conn);
			}
		
		}
		
		log.debug(JSONArray.fromObject(stats));
		
		return stats;
			
	 }
	
	public List<Map<String,Integer>> getTaskIdList() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String,Integer>> taskIdList = new ArrayList<>();
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "SELECT task_id,r.DAILY_DB_ID FROM task t,region r WHERE t.data_plan_status=1 and T.REGION_ID = R.REGION_ID";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				Map<String,Integer> map = new HashMap<>();
				map.put("taskId", rs.getInt(1));
				map.put("dbId", rs.getInt(2));
				taskIdList.add(map);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DbUtils.commitAndClose(conn);
		}
		return taskIdList;
		
	}
	

}
