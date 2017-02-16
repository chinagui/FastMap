package com.navinfo.dataservice.engine.man.produce;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.CLOB;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
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
	/**
	 * @Title: generateDaily
	 * @Description: (修改)创建出品包(第七迭代)
	 * @param userId
	 * @param dataJson
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午1:49:24 
	 */
	public void generateDaily(long userId, JSONObject dataJson) throws Exception {
		//创建日出品任务
		int subtaskId=dataJson.getInt("subtaskId");
		String produceType=dataJson.getString("produceType");
		//通过subtaskId 查询 gridIds 
		List<Integer> gridIds =SubtaskOperation.getGridIdsBySubtaskId(subtaskId);
		
		//JSONArray gridIds = dataJson.getJSONArray("gridIds");
		JSONObject paraJson=new JSONObject();
		paraJson.put("gridIds", gridIds);
		int produceId=this.create(userId,produceType,paraJson,subtaskId);
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
		long jobId=jobApi.createJob("releaseFmIdbDailyJob", jobDataJson, userId,subtaskId, "日出品");
		
	}
	

	/**
	 * @Title: create
	 * @Description: (修改)创建日出品任务(第七迭代)
	 * @param userId
	 * @param produceType
	 * @param paraJson
	 * @return
	 * @throws Exception  int
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午1:48:02 
	 */
	public int create(long userId,String produceType, JSONObject paraJson,int subtaskId) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			int produceId=ProduceOperation.getNewProduceId(conn);
			QueryRunner run = new QueryRunner();
			String createSql = "insert into produce (produce_id,produce_type,create_user_id,create_date,produce_status,parameter,subtask_id) "
					+ "values("+produceId+",'"+produceType+"',"+userId+",sysdate,0,'"+paraJson+"',"+subtaskId+")";			
			//ClobProxyImpl impl=(ClobProxyImpl)ConnectionUtil.createClob(conn);
			log.debug("创建日出品sql:"+createSql);
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
	
	/**
	 * @Title: list
	 * @Description: (修改)查询日出品列表(第七迭代)
	 * @param conditionJson
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws Exception  Page
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午2:02:41 
	 */
	public Page list(JSONObject conditionJson,final int currentPageNum,int pageSize) throws Exception {
		Connection conn=null;
		try{			
			String conditionStr="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
					Iterator keys = conditionJson.keys();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						if ("name".equals(key)) {
							conditionStr+=" and T.NAME like '%"+conditionJson.getString(key)+"%'";
						}
						if("selectParam".equals(key)){
							JSONArray selectParamArr=conditionJson.getJSONArray(key);
							String statuss = selectParamArr.toString();
							if(statuss != null && StringUtils.isNotEmpty(statuss)){
								conditionStr+=" and T.PRODUCE_STATUS in "
											+statuss.replace("[", "(").replace("]", ")");
							}								
						}
					}
			}
				long pageStartNum = (currentPageNum - 1) * pageSize + 1;
				long pageEndNum = currentPageNum * pageSize;
				conn = DBConnector.getInstance().getManConnection();
				String sql="WITH PRODUCE_LIST AS"
						+ " (SELECT P.PRODUCE_ID,"
						+ "       P.PROGRAM_ID,"
						+ "       G.NAME,"
						+ "       G.TYPE,"
						+ "       G.PRODUCE_PLAN_START_DATE,"
						+ "       G.PRODUCE_PLAN_END_DATE,"
						+ "       P.CREATE_DATE,"
						+ "       NVL(P.PRODUCE_STATUS, 0) PRODUCE_STATUS"
						+ "  FROM PRODUCE P, PROGRAM G"
						+ " WHERE P.PROGRAM_ID(+) = G.PROGRAM_ID"
						+ "   AND G.TYPE = 4"
						+ "   AND G.STATUS = 0"
						+ "   AND G.LATEST = 1)"
						+ "SELECT /*+FIRST_ROWS ORDERED*/"
						+ " T.*, (SELECT COUNT(1) FROM PRODUCE_LIST) AS TOTAL_RECORD_NUM"
						+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM PRODUCE_LIST T WHERE ROWNUM <= "+pageEndNum+") T"
						+ " WHERE T.ROWNUM_ >= "+pageStartNum
						+ conditionStr
						+ " ORDER BY T.PRODUCE_STATUS              DESC,"
						+ "          T.PRODUCE_PLAN_START_DATE DESC,"
						+ "          T.CREATE_DATE                 DESC";
				log.debug("查询日初评列表sql: "+sql);
				QueryRunner run=new QueryRunner();
				ResultSetHandler<Page> rsHandler=new ResultSetHandler<Page>() {
					public Page handle(ResultSet rs) throws SQLException{
						Page page = new Page(currentPageNum);
						int totalCount=0;
						List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
						while(rs.next()){
							String dayProducePlanStartDate = null ;
							String dayProducePlanEndDate = null ;
							String createDate = null ;
							if(rs.getTimestamp("PRODUCE_PLAN_START_DATE") != null && StringUtils.isNotEmpty(rs.getTimestamp("DAY_PRODUCE_PLAN_START_DATE").toString())){
								dayProducePlanStartDate=DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_START_DATE"), "yyyyMMdd");
							}
							if(rs.getTimestamp("PRODUCE_PLAN_END_DATE") != null && StringUtils.isNotEmpty(rs.getTimestamp("DAY_PRODUCE_PLAN_END_DATE").toString())){
								dayProducePlanEndDate=DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_END_DATE"), "yyyyMMdd");
							}
							if(rs.getTimestamp("CREATE_DATE") != null && StringUtils.isNotEmpty(rs.getTimestamp("CREATE_DATE").toString())){
								createDate=DateUtils.dateToString(rs.getTimestamp("CREATE_DATE"), "yyyyMMdd");
							}
							Map<String,Object> map=new HashMap<String, Object>();
							map.put("produceId", rs.getInt("PRODUCE_ID"));
							map.put("programId", rs.getInt("PROGRAM_ID"));
							map.put("name", rs.getString("NAME"));
							map.put("type", rs.getInt("TYPE"));
							map.put("producePlanStartDate", dayProducePlanStartDate);
							map.put("producePlanEndDate", dayProducePlanEndDate);
							map.put("createDate", createDate);
							map.put("produceStatus", rs.getInt("PRODUCE_STATUS"));
							
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
		final Connection conn=DBConnector.getInstance().getManConnection();
		try{
			String sql="SELECT P.PRODUCE_ID,"
					+ "       P.PRODUCE_NAME,"
					+ "       P.PRODUCE_TYPE,"
					+ "       P.PRODUCE_STATUS,"
					+ "       P.CREATE_USER_ID,"
					+ "       I.USER_REAL_NAME CREATE_USER_NAME,"
					+ "       P.CREATE_DATE,P.parameter PAR"
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
						CLOB inforGeo = ConnectionUtil.getClob(conn, rs,"PAR");
						String inforGeo1 = StringUtil.ClobToString(inforGeo);
						map.put("parameter",JSONObject.fromObject(inforGeo1));
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

	public static void main(String[] args) {
		
	}
}
