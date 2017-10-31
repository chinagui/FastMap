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
			
			Map<String,List<Map<String, Object>>> result = new HashMap<String,List<Map<String, Object>>>();
			result.put("task_day_plan", getStats());

//			log.debug("task_day_plan---"+JSONObject.fromObject(result).toString());
			
			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getStats() {
		Map<Integer, Object> taskIdMapList = null;
		List<Map<String, Object>> stats = new ArrayList<>();
		try {
			taskIdMapList = getTaskIdList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		
		Connection conn = null;
		for(Map.Entry<Integer, Object> entry : taskIdMapList.entrySet()){
			int dbId = entry.getKey();
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);
				QueryRunner run = new QueryRunner();
				String sql = "select t.task_id, r.function_class, nvl(sum(r.length),0) from DATA_PLAN t, rd_link r where "
						+ "r.link_pid = t.pid and t.data_type = 2 group by t.task_id, r.function_class";
				Map<Integer, Map<Integer, Double>> linkLenth = run.query(conn, sql, new ResultSetHandler<Map<Integer, Map<Integer, Double>>>() {
					@Override
					public Map<Integer, Map<Integer, Double>> handle(ResultSet rs)
					throws SQLException {
						Map<Integer, Map<Integer, Double>> result = new HashMap<>();
						while(rs.next()){
							Map<Integer, Double> length = new HashMap<Integer, Double>();
							int taskId = rs.getInt(1);
							if(result.containsKey(taskId)){
								length = result.get(taskId);
							}
							length.put(rs.getInt(2), rs.getDouble(3));
							result.put(rs.getInt(1), length);
						}
						return result;
					}
				});
					
				Map<Integer, Object> taskMap = (Map<Integer, Object>) entry.getValue();
				for(Map.Entry<Integer, Object> entry1 : taskMap.entrySet()){
					int taskId = entry1.getKey();
					log.info("start taskId="+taskId);
					if(md.find("task_day_plan",Filters.eq("taskId", taskId+"")).iterator().hasNext()){
						continue;
					}
					String originWkt = entry1.getValue().toString();
					
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, originWkt);
					//此处不排除删除的link，poi，随规划时的状态处理
					String rdLinkSql = "select NVL(SUM(t.length),0) from RD_LINK t where "
					+"sdo_relate(T.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'";
					String poiSql = "select COUNT(1) from IX_POI p where "
					+"sdo_relate(p.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'";
							
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
					//modiby by songhe 2017/10/31  添加了一些统计项，修改原来的部分逻辑
					double taskFc1len = 0d;
					double taskFc2len = 0d;
					double taskFc3len = 0d;
					double taskFc4len = 0d;
					double taskFc5len = 0d;
					
					Map<Integer, Double> taskLinkLength = new HashMap<Integer, Double>();
					if(linkLenth.containsKey(taskId)){
						taskLinkLength = linkLenth.get(taskId);
					}else{
						String sqlLength = "select t.function_class, NVL(SUM(t.length),0) from RD_LINK t where "
								+"sdo_relate(t.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE' group by t.function_class";
					
						taskLinkLength = run.query(conn, sqlLength,new ResultSetHandler<Map<Integer, Double>>() {
							@Override
							public Map<Integer, Double> handle(ResultSet rs)
									throws SQLException {
								Map<Integer, Double> result = new HashMap<>();
								while(rs.next()){
									result.put(rs.getInt(1), rs.getDouble(2));
								}
								return result;
							}
						},clob);
					}
					
					for(Map.Entry<Integer, Double> en : taskLinkLength.entrySet()){
						int kind = en.getKey();
						switch(kind){
						case 1 :
							taskFc1len = en.getValue();
							break;
						case 2 :
							taskFc2len = en.getValue();
							break;
						case 3 :
							taskFc3len = en.getValue();
							break;
						case 4 :
							taskFc4len = en.getValue();
							break;
						case 5 :
							taskFc5len = en.getValue();
							break;
						}
					}
					
					Map<String, Object> map  = new HashMap<>();
					map.put("taskId", taskId);
					map.put("linkAllLen", linkAllLen);
					map.put("poiAllNum", poiAllNum);
					map.put("link17AllLen", link17AllLen);
					map.put("link27AllLen", link27AllLen);
					map.put("taskFc1len", taskFc1len);
					map.put("taskFc2len", taskFc2len);
					map.put("taskFc3len", taskFc3len);
					map.put("taskFc4len", taskFc4len);
					map.put("taskFc5len", taskFc5len);
					
					stats.add(map);
				}
				}catch(Exception e){
					log.error("dbId("+dbId+")Day_规划量数据统计失败");
					log.error(e.getMessage(),e);
				}finally{
					DbUtils.closeQuietly(conn);
				}
			}
		log.debug(JSONArray.fromObject(stats));
		return stats;
	}
	
	public Map<Integer,Object> getTaskIdList() throws Exception{
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String sql  = "SELECT TASK_ID, R.DAILY_DB_ID,B.ORIGIN_GEO"
					+ "  FROM TASK T, REGION R,BLOCK B"
					+ " WHERE T.REGION_ID = R.REGION_ID"
					+ " AND T.BLOCK_ID=B.BLOCK_ID";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			Map<Integer, Object> resultMap = new HashMap<Integer, Object>();
			while(rs.next()){
				Map<Integer,Object> map = new HashMap<>();
				if(resultMap.containsKey(rs.getInt("DAILY_DB_ID"))){
					map = (Map<Integer, Object>) resultMap.get(rs.getInt("DAILY_DB_ID"));
				}
				String clobStrOrig = null;
				STRUCT structOrig = (STRUCT) rs.getObject("ORIGIN_GEO");
				try {
					clobStrOrig = GeoTranslator.struct2Wkt(structOrig);
					if(StringUtils.isEmpty(clobStrOrig)){continue;}
					map.put(rs.getInt(1), clobStrOrig);
				} catch (Exception e1) {
					continue;
				}
				resultMap.put(rs.getInt(2), map);
			}
			return resultMap;
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			DbUtils.commitAndClose(conn);
		}
		return new HashMap<Integer, Object>();
	}
	

}
