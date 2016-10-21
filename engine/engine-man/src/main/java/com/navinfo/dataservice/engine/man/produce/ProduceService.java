package com.navinfo.dataservice.engine.man.produce;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.CLOB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.alibaba.druid.proxy.jdbc.ClobProxyImpl;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.inforMan.InforManOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.StringUtil;

public class ProduceService {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private ProduceService(){}
	private static class SingletonHolder{
		private static final ProduceService INSTANCE =new ProduceService();
	}
	public static ProduceService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public long generateDaily(long userId, JSONObject dataJson) throws Exception {
		//创建日出品任务
		String produceName=dataJson.getString("produceName");
		String produceType=dataJson.getString("produceType");
		JSONArray gridIds = dataJson.getJSONArray("gridIds");
		JSONObject paraJson=new JSONObject();
		paraJson.put("gridIds", gridIds);
		int produceId=this.create(userId,produceName,produceType,paraJson);
		//创建日出品job
		// TODO Auto-generated method stub
		JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
		/*
		 * {"gridIds":[213424,343434,23423432],"stopTime":"yyyymmddhh24miss","dataType":"POI"//POI,ALL}
		 * jobType:releaseFmIdbDailyJob/releaseFmIdbMonthlyJob
		 */
		//TODO
		JSONObject jobDataJson=new JSONObject();
		jobDataJson.put("produceId", produceId);
		jobDataJson.put("gridList", gridIds);
		jobDataJson.put("featureType", produceType);
		long jobId=jobApi.createJob("releaseFmIdbDailyJob", jobDataJson, userId, "日出品");
		return jobId;
	}
	
	/**
	 * 创建日出品任务
	 * @param userId
	 * @param gridIds 
	 * @param json
	 * @throws Exception
	 */
	public int create(long userId,String produceName,String produceType, JSONObject paraJson) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			int produceId=ProduceOperation.getNewProduceId(conn);
			QueryRunner run = new QueryRunner();
			String createSql = "insert into produce (produce_id,produce_name,produce_type,create_user_id,create_date,produce_status,parameter) "
					+ "values("+produceId+",'"+produceName+"','"+produceType+"',"+userId+",sysdate,0,'"+paraJson+"')";			
			run.update(conn,createSql);		
			return produceId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void updateProduceStatus(int produceId, int status) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			ProduceOperation.updateProduceStatus(conn, produceId, status);					
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Page list(final int currentPageNum,int pageSize) throws Exception {
		Connection conn=null;
		try{
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			conn = DBConnector.getInstance().getManConnection();
			String sql="WITH QUERY AS"
					+ " (SELECT P.PRODUCE_ID,"
					+ "       P.PRODUCE_NAME,"
					+ "       P.PRODUCE_TYPE,"
					+ "       P.PRODUCE_STATUS,"
					+ "       P.CREATE_USER_ID,"
					+ "       I.USER_REAL_NAME CREATE_USER_NAME,"
					+ "       P.CREATE_DATE"
					+ "  FROM PRODUCE P, USER_INFO I"
					+ " WHERE P.CREATE_USER_ID = I.USER_ID"
					+ "  ORDER BY P.PRODUCE_NAME)"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " T.*, (SELECT COUNT(1) FROM QUERY) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM QUERY T WHERE ROWNUM <= "+pageEndNum+") T"
				+ " WHERE T.ROWNUM_ >= "+pageStartNum;;
			QueryRunner run=new QueryRunner();
			ResultSetHandler<Page> rsHandler=new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException{
					Page page = new Page(currentPageNum);
					int totalCount=0;
					List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map=new HashMap<String, Object>();
						map.put("produceId", rs.getInt("PRODUCE_ID"));
						map.put("produceName", rs.getString("PRODUCE_NAME"));
						map.put("produceType", rs.getString("PRODUCE_TYPE"));
						map.put("produceStatus", rs.getInt("PRODUCE_STATUS"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createUserName", rs.getString("CREATE_USER_NAME"));
						map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
						//CLOB inforGeo = (CLOB) rs.getClob("PAR");
						//String inforGeo1 = StringUtil.ClobToString(inforGeo);
						//map.put("parameter",inforGeo1);
						//map.put("parameter",rs.getString("PAR"));
						if(totalCount==0){totalCount=rs.getInt("TOTAL_RECORD_NUM");}
						result.add(map);
					}
					page.setTotalCount(totalCount);
					page.setResult(result);
					return page;
				}
			};		
			
			return run.query(conn, sql,rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Map<String,Object> query(int produceId) throws Exception {
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String sql="SELECT P.PRODUCE_ID,"
					+ "       P.PRODUCE_NAME,"
					+ "       P.PRODUCE_TYPE,"
					+ "       P.PRODUCE_STATUS,"
					+ "       P.CREATE_USER_ID,"
					+ "       I.USER_REAL_NAME CREATE_USER_NAME,"
					+ "       P.CREATE_DATE"
					+ "  FROM PRODUCE P, USER_INFO I"
					+ " WHERE P.CREATE_USER_ID = I.USER_ID"
					+ "  and p.produce_id="+produceId;
			QueryRunner run=new QueryRunner();
			ResultSetHandler<Map<String,Object>> rsHandler=new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException{
					while(rs.next()){
						Map<String,Object> map=new HashMap<String, Object>();
						map.put("produceId", rs.getInt("PRODUCE_ID"));
						map.put("produceName", rs.getString("PRODUCE_NAME"));
						map.put("produceType", rs.getString("PRODUCE_TYPE"));
						map.put("produceStatus", rs.getInt("PRODUCE_STATUS"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createUserName", rs.getString("CREATE_USER_NAME"));
						map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
						CLOB inforGeo = (CLOB) rs.getClob("PAR");
						String inforGeo1 = StringUtil.ClobToString(inforGeo);
						map.put("parameter",inforGeo1);
						//map.put("parameter",rs.getString("PAR"));
						return map;
					}
					return null;
				}
			};		
			
			return run.query(conn, sql,rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
