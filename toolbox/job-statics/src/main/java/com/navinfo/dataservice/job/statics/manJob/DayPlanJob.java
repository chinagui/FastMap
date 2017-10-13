package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Clob;
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
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * DayPlanJob
 * Day_规划量统计job
 * @author sjw
 *
 */
public class DayPlanJob extends AbstractStatJob {

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
		List<Map<String, Object>> taskIdMapList = null;
		List<Map<String,String>> stats = new ArrayList<>();
		try {
			taskIdMapList = getTaskIdList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		
		for (Map<String, Object> taskIdMap : taskIdMapList) {
			Integer dbId = Integer.valueOf(String.valueOf(taskIdMap.get("dbId")));
			Integer taskId = Integer.valueOf(String.valueOf(taskIdMap.get("taskId")));
			log.info("start taskId="+taskId);
			if(md.find("task_day_plan",Filters.eq("taskId", taskId+"")).iterator().hasNext()){
				continue;
			}
			String originWkt=String.valueOf(taskIdMap.get("originWkt"));
			Connection conn=null;
			
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, originWkt);
				QueryRunner run = new QueryRunner();
				//此处不排除删除的link，poi，随规划时的状态处理
				String rdLinkSql = "select NVL(SUM(t.length),0) from RD_LINK t where "
				+"sdo_relate(T.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'";
				String poiSql = "select COUNT(1) from IX_POI p where "
				+"sdo_relate(p.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'";
				
					
				Map<String,String> map  = new HashMap<>();
				
				String sql2 = rdLinkSql;
				String sql4 = poiSql;
				String sql5 = rdLinkSql+" AND t.kind >= 1 AND t.kind <= 7";
				String sql6 = rdLinkSql+" AND t.kind >= 2 AND t.kind <= 7";
				
				
				String rdLinkSql1 = "SELECT NVL(SUM(r.length),0) FROM rd_link r,DATA_PLAN d WHERE r.link_pid = d.pid AND d.data_type = 2 AND d.task_id = ";
				String poiSql1 = "SELECT COUNT(1) FROM ix_poi p,DATA_PLAN d WHERE p.pid = d.pid AND d.data_type = 1 AND d.task_id = ";				
				
				String sql21 = rdLinkSql1+taskId;
				String sql41 = poiSql1+taskId;
				String sql51 = rdLinkSql1+taskId+" AND r.kind >= 1 AND r.kind <= 7";
				String sql61 = rdLinkSql1+taskId+" AND r.kind >= 2 AND r.kind <= 7";
				
				
				String linkAllLen = run.query(conn, sql21,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				String link17AllLen ="0";
				String link27AllLen ="0";
				if(linkAllLen.equals("0")){
					linkAllLen = run.query(conn, sql2,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					},clob);
					link17AllLen = run.query(conn, sql5,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					},clob);
					
					link27AllLen = run.query(conn, sql6,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					},clob);
				}else {
					link17AllLen = run.query(conn, sql51,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					});
					
					link27AllLen = run.query(conn, sql61,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					});
				}
				
				String poiAllNum = run.query(conn, sql41,new ResultSetHandler<String>() {
					@Override
					public String handle(ResultSet rs)
							throws SQLException {
						if(rs.next()){
							return rs.getString(1);
						}
						return null;
					}
				});
				if(poiAllNum.equals("0")){
					poiAllNum = run.query(conn, sql4,new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs)
								throws SQLException {
							if(rs.next()){
								return rs.getString(1);
							}
							return null;
						}
					},clob);
				}
				
				
				map.put("taskId", taskId.toString());
				map.put("linkAllLen", linkAllLen);
				map.put("poiAllNum", poiAllNum);
				map.put("link17AllLen", link17AllLen);
				map.put("link27AllLen", link27AllLen);
				
				stats.add(map);
				

			}catch(Exception e){
				log.error("dbId("+dbId+")Day_规划量数据统计失败.taskId="+taskId+";originGeo="+originWkt);
				log.error(e.getMessage(),e);
			}finally{
				DbUtils.closeQuietly(conn);
			}
		
		}
		
		log.debug(JSONArray.fromObject(stats));
		
		return stats;
			
	 }
	
	public List<Map<String,Object>> getTaskIdList() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String,Object>> taskIdList = new ArrayList<>();
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "SELECT TASK_ID, R.DAILY_DB_ID,B.ORIGIN_GEO"
					+ "  FROM TASK T, REGION R,BLOCK B"
					+ " WHERE T.REGION_ID = R.REGION_ID"
					+ " AND T.BLOCK_ID=B.BLOCK_ID";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				Map<String,Object> map = new HashMap<>();
				map.put("taskId", rs.getInt(1));
				map.put("dbId", rs.getInt(2));
				String clobStrOrig = null;
				STRUCT structOrig = (STRUCT) rs.getObject("ORIGIN_GEO");
				try {
					clobStrOrig = GeoTranslator.struct2Wkt(structOrig);
					map.put("originWkt", clobStrOrig);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(StringUtils.isEmpty(clobStrOrig)){continue;}
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
