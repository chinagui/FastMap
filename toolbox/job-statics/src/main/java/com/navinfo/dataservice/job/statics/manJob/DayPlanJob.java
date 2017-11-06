package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
		
		DecimalFormat df = new DecimalFormat("######0.00");   
		String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		MongoDao md = new MongoDao(dbName);
		Connection conn = null;
		for(Map.Entry<Integer, Object> entry : taskIdMapList.entrySet()){
			int dbId = entry.getKey();
			Map<Integer, Object> tasks = new HashMap<>();
			Map<Integer, Object> taskMap = (Map<Integer, Object>) entry.getValue();
			for(Map.Entry<Integer, Object> taskEnty : taskMap.entrySet()){
				int taskId = taskEnty.getKey();
				if(md.find("task_day_plan",Filters.eq("taskId", taskId+"")).iterator().hasNext()){
					continue;
				}
				tasks.put(taskId, taskEnty.getValue());
			}
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);
				String linkFuncSql = "select t.task_id, r.function_class, nvl(sum(r.length),0) from DATA_PLAN t, rd_link r where "
						+ "r.link_pid = t.pid and t.data_type = 2 group by t.task_id, r.function_class";
				
				String poiDataSql = "SELECT d.task_id, 0, COUNT(1) FROM ix_poi p,DATA_PLAN d WHERE p.pid = d.pid AND d.data_type = 1 group by d.task_id";
				String linkKindcSql = "select t.task_id, r.kind, nvl(sum(r.length),0) from DATA_PLAN t, rd_link r where "
						+ "r.link_pid = t.pid and t.data_type = 2 group by t.task_id, r.kind";
				String containsTable = "select count(1) from user_tables where table_name = 'DATA_PLAN'";
				int count = containsTable(conn, containsTable);
				Map<Integer, Map<Integer, Object>> linkFuncLenth = new HashMap<>();
				Map<Integer, Map<Integer, Object>> linkKindLenth = new HashMap<>();
				Map<Integer, Map<Integer, Object>> poiAllNumMap = new HashMap<>();
				if(count > 0){
					linkFuncLenth = getLinkOrPoiData(conn, linkFuncSql);
					linkKindLenth = getLinkOrPoiData(conn, linkKindcSql);
					poiAllNumMap = getLinkOrPoiData(conn, poiDataSql);
				}
					
				for(Map.Entry<Integer, Object> entry1 : tasks.entrySet()){
					int taskId = entry1.getKey();
					log.info("start taskId="+taskId);
					String originWkt = entry1.getValue().toString();
					
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, originWkt);
					//modiby by songhe 2017/10/31  添加了一些统计项，修改原来的部分逻辑
					double taskFc1len = 0d;
					double taskFc2len = 0d;
					double taskFc3len = 0d;
					double taskFc4len = 0d;
					double taskFc5len = 0d;
					
					double link17AllLen = 0d;
					double link27AllLen = 0d;
					double linkAllLen = 0d;
					
					int poiAllNum = 0;
					
					Map<Integer, Object> linkFucLength = new HashMap<Integer, Object>();
					if(linkFuncLenth.containsKey(taskId)){
						linkFucLength = linkFuncLenth.get(taskId);
					}else{
						String sqlLength = "select t.function_class, NVL(SUM(t.length),0) from RD_LINK t where "
								+"sdo_relate(t.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE' group by t.function_class";
					
						linkFucLength = getLinkOrPoiDataByTaskGeo(conn, sqlLength, clob);
					}
					for(Map.Entry<Integer, Object> en : linkFucLength.entrySet()){
						int kind = en.getKey();
						double value = Double.valueOf(en.getValue().toString());
						switch(kind){
						case 1 :
							taskFc1len = value;
							break;
						case 2 :
							taskFc2len = value;
							break;
						case 3 :
							taskFc3len = value;
							break;
						case 4 :
							taskFc4len = value;
							break;
						case 5 :
							taskFc5len = value;
							break;
						}
					}
					
					Map<Integer, Object> linkKindLength = new HashMap<Integer, Object>();
					if(linkKindLenth.containsKey(taskId)){
						linkKindLength = linkKindLenth.get(taskId);
					}else{
						String sql = "select t.kind, NVL(SUM(t.length),0) from RD_LINK t where "
								+"sdo_relate(T.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE' group by t.kind";
						linkKindLength = getLinkOrPoiDataByTaskGeo(conn, sql, clob);
					}
					for(Map.Entry<Integer, Object> en : linkKindLength.entrySet()){
						int kind = en.getKey();
						double value =  Double.valueOf(en.getValue().toString());
						linkAllLen += value;
						if(kind >= 1 && kind <= 7){
							link17AllLen += value;
						}
						if(kind >= 2 && kind <= 7){
							link27AllLen += value;
						}
					}
					
					
					Map<Integer, Object> poitempMap = new HashMap<Integer, Object>();
					if(poiAllNumMap.containsKey(taskId)){
						poitempMap = poiAllNumMap.get(taskId);
						double temp = (Double) poitempMap.get(0);
						poiAllNum = (int) temp;
					}else{
						String sql = "select 1, COUNT(1) from IX_POI p where "
								+"sdo_relate(p.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'";
						poitempMap = getLinkOrPoiDataByTaskGeo(conn, sql, clob);
						double temp = Double.valueOf(poitempMap.get(1).toString());
						poiAllNum = (int) temp; 
					}
					
					Map<String, Object> map  = new HashMap<>();
					map.put("taskId", taskId);
					map.put("linkAllLen", df.format(linkAllLen));
					map.put("poiAllNum", poiAllNum);
					map.put("link17AllLen", df.format(link17AllLen));
					map.put("link27AllLen", df.format(link27AllLen));
					map.put("taskFc1len", df.format(taskFc1len));
					map.put("taskFc2len", df.format(taskFc2len));
					map.put("taskFc3len", df.format(taskFc3len));
					map.put("taskFc4len", df.format(taskFc4len));
					map.put("taskFc5len", df.format(taskFc5len));
					
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
	
	/**
	 * dataPlan中查询所有任务对应的link和poi通用方法
	 * @param Connection
	 * @param String
	 * 
	 * */
	public Map<Integer, Map<Integer, Object>> getLinkOrPoiData(Connection conn, String sql) throws Exception{
		try {
			log.info("sql" + sql);
			QueryRunner run = new QueryRunner();
			Map<Integer, Map<Integer, Object>> linkLenth = run.query(conn, sql, new ResultSetHandler<Map<Integer, Map<Integer, Object>>>() {
				@Override
				public Map<Integer, Map<Integer, Object>> handle(ResultSet rs)throws SQLException {
					Map<Integer, Map<Integer, Object>> result = new HashMap<>();
					while(rs.next()){
						Map<Integer, Object> map = new HashMap<Integer, Object>();
						int taskId = rs.getInt(1);
						if(result.containsKey(taskId)){
							map = result.get(taskId);
						}
						map.put(rs.getInt(2), rs.getDouble(3));
						result.put(rs.getInt(1), map);
					}
					return result;
				}
			});
			return linkLenth;
		}catch(Exception e){
			e.printStackTrace();
		}
		return new HashMap<Integer, Map<Integer, Object>>();
	}
	
	/**
	 * 查询日库中是否存在dataPlan的表
	 * @param Connection
	 * @param String
	 * 
	 * */
	public Integer containsTable(Connection conn, String sql) throws Exception{
		try {
			log.info("sql" + sql);
			QueryRunner run = new QueryRunner();
			return run.query(conn, sql, new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs)throws SQLException {
					int count = 0;
					if(rs.next()){
						count = rs.getInt(1);
					}
					return count;
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * dataPlan中没有的数据，用任务范围圈数据通用方法
	 * @param Connection
	 * @param String
	 * @param Clob
	 * 
	 * */
	public Map<Integer, Object> getLinkOrPoiDataByTaskGeo(Connection conn, String sql, Clob clob){
		try {
			QueryRunner run = new QueryRunner();
			log.info("sql" + sql);
			Map<Integer, Object> reusltMap = run.query(conn, sql, new ResultSetHandler<Map<Integer, Object>>() {
				@Override
				public Map<Integer, Object> handle(ResultSet rs)
						throws SQLException {
					Map<Integer, Object> result = new HashMap<>();
					while(rs.next()){
						result.put(rs.getInt(1), rs.getDouble(2));
					}
					return result;
				}
			},clob);
			return reusltMap;
		}catch(Exception e){
			e.printStackTrace();
		}
		return new HashMap<Integer, Object>();
	}
	

}
