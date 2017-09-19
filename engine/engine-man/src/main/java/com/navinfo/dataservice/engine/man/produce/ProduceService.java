package com.navinfo.dataservice.engine.man.produce;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.inforMan.InforManService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.StringUtil;
import com.sun.tools.internal.ws.wsdl.framework.Entity;

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
		List<Integer> gridIds =SubtaskOperation.getGridIdListBySubtaskId(subtaskId);
		
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
								conditionStr+=" and T.ORDER_STATUS in "
											+statuss.replace("[", "(").replace("]", ")");
							}								
						}
					}
			}
				long pageStartNum = (currentPageNum - 1) * pageSize + 1;
				long pageEndNum = currentPageNum * pageSize;
				conn = DBConnector.getInstance().getManConnection();
				String sql="WITH PRODUCE_LIST AS"
						+ " (SELECT NVL(P.PRODUCE_ID,0) PRODUCE_ID,"
						+ "       G.PROGRAM_ID,"
						+ "       G.NAME,"
						+ "       G.TYPE,"
						+ "       G.PRODUCE_PLAN_START_DATE,"
						+ "       G.PRODUCE_PLAN_END_DATE,"
						+ "       P.CREATE_DATE,"
						+ "       NVL(P.PRODUCE_STATUS, 0) PRODUCE_STATUS,"
						+ "       CASE NVL(P.PRODUCE_STATUS, 0)"
						+ "            WHEN 0 THEN 4 ELSE NVL(P.PRODUCE_STATUS, 0) END ORDER_STATUS"
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
						+ " ORDER BY T.ORDER_STATUS              DESC,"
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
							if(rs.getTimestamp("PRODUCE_PLAN_START_DATE") != null && StringUtils.isNotEmpty(rs.getTimestamp("PRODUCE_PLAN_START_DATE").toString())){
								dayProducePlanStartDate=DateUtils.dateToString(rs.getTimestamp("PRODUCE_PLAN_START_DATE"), "yyyyMMdd");
							}
							if(rs.getTimestamp("PRODUCE_PLAN_END_DATE") != null && StringUtils.isNotEmpty(rs.getTimestamp("PRODUCE_PLAN_END_DATE").toString())){
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
	public Map<String,Object> query(int programId) throws Exception {
		Connection conn=DBConnector.getInstance().getManConnection();
		try{
			String sql="SELECT P.INFOR_ID"
					+ "  FROM PROGRAM P"
					+ " WHERE P.program_id ="+programId;
			QueryRunner run=new QueryRunner();
			ResultSetHandler<Map<String,Object>> rsHandler=new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException{
					while(rs.next()){
						Map<String,Object> map=new HashMap<String, Object>();
						map.put("inforId", rs.getInt("INFOR_ID"));
						return map;
					}
					return null;
				}
			};		
			
			Map<String,Object> produceMap=run.query(conn, sql,rsHandler);
			if(produceMap!=null&&produceMap.containsKey("inforId")){
				produceMap.put("grids", InforManService.getInstance().getProgramGridsByInfor(conn,(Integer)produceMap.get("inforId")));
			}
			return produceMap;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取待出品的情报项目list
	 * 应用场景：定时日出品（一体化）脚本
	 * @return List<Map<String, Object>>：Map<String, Object> 
	 * 			key:"produceId":int,
	 * 				"programId":int,
	 * 				"gridIds":Map<Integer, Set<Integer>> key:dbId value:grid的集合
	 * 例如：{"produceId":1,"programId":1,"gridIds":{17:[59567301,59567302],18:[59567801]}}
	 * @throws Exception
	 */
	public List<Map<String, Object>> getProduceProgram() throws Exception {
		Connection conn=DBConnector.getInstance().getManConnection();
		try{
			//对已关闭，但是未创建出品任务的情报项目，创建情报出品记录
			String sql="INSERT INTO PRODUCE"
					+ "  (PRODUCE_ID,"
					+ "   PRODUCE_NAME,"
					+ "   PRODUCE_TYPE,"
					+ "   CREATE_USER_ID,"
					+ "   CREATE_DATE,"
					+ "   PRODUCE_STATUS,"
					+ "   PROGRAM_ID)"
					+ "  SELECT PRODUCE_SEQ.NEXTVAL, P.NAME, 'ALL', 0, SYSDATE, 0, PROGRAM_ID"
					+ "    FROM PROGRAM P"
					+ "   WHERE P.STATUS = 0"
					+ "     AND P.TYPE = 4"
					+ "     AND NOT EXISTS"
					+ "   (SELECT 1 FROM PRODUCE PR WHERE P.PROGRAM_ID = PR.PROGRAM_ID)";
			QueryRunner run=new QueryRunner();
			run.update(conn, sql);
			//获取未出品&出品失败&出品冲突的情报出品记录
			sql="SELECT P.PRODUCE_ID,P.PROGRAM_ID,P.PRODUCE_STATUS FROM PRODUCE P WHERE P.PRODUCE_STATUS IN (0, 3, 4) AND P.PROGRAM_ID IS NOT NULL";
			ResultSetHandler<List<Map<String,Object>>> rsHandler=new ResultSetHandler<List<Map<String,Object>>>() {
				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException{
					List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map=new HashMap<String, Object>();
						map.put("produceId", rs.getInt("PRODUCE_ID"));
						map.put("programId", rs.getInt("PROGRAM_ID"));
						map.put("produceStatus", rs.getInt("PRODUCE_STATUS"));
						list.add(map);
					}
					return list;
				}
			};		
			List<Map<String,Object>> produceList=run.query(conn, sql,rsHandler);
			List<Map<String,Object>> produceResultList = new ArrayList<>();
			List<Map<String,Object>> intersectList = new ArrayList<>();
			Map<Integer, Set<Integer>> allGridsWithOpenProgram = queryAllGridsWithOpenProgram(conn);
			//补充相关的grid信息
			for(Map<String,Object> produce:produceList){
				int programId=(int) produce.get("programId");
				int produceId=(int) produce.get("produceId");
				int produceStatus = (int) produce.get("produceStatus");
				Map<Integer, Set<Integer>> gridIds =ProgramService.getInstance().queryInforProgramGridById(conn, programId);
				produce.put("gridIds", gridIds);
				//modify by songhe
				//新增判断，快线关闭待出品的与开启状态的项目之间存在grid交集，则不能出品
				boolean notIntersec = true;
				for(Set<Integer> grids : gridIds.values()){
					for(int gridId : grids){
						//是否有重复的数据
						for(Entry<Integer, Set<Integer>> entry : allGridsWithOpenProgram.entrySet()){
							Set<Integer> openGrids = entry.getValue();
							if(openGrids.contains(gridId)){
								produce.put("intersecGrids", produce.containsKey("intersecGrids") && produce.get("intersecGrids").toString().indexOf(String.valueOf(gridId)) == -1 ? produce.get("intersecGrids").toString() + "," + gridId : gridId);
								produce.put("intersecProgramId", produce.containsKey("intersecProgramId") && produce.get("intersecProgramId").toString().indexOf(String.valueOf(entry.getKey())) == -1 ? produce.get("intersecProgramId").toString() + "," + entry.getKey() : entry.getKey());
								intersectList.add(produce);
								notIntersec = false;
							}
						}
					}
				}
				if(notIntersec){
					JSONObject paraJson=new JSONObject();
					paraJson.put("gridIds", gridIds.toString());
					if(gridIds==null||gridIds.size()==0){continue;}
					StringBuffer sb = new StringBuffer();
					sb.append("update produce p set parameter='"+paraJson+"' ");
					if(produceStatus == 4){
						 sb.append(", produce_status = 0");
						 produce.put("produceStatus", 0);
					}
					sb.append("where produce_id = "+produceId);
					run.update(conn, sb.toString());
					produceResultList.add(produce);
				}
			}
			//保存冲突信息到出品表中
			if(intersectList.size() > 0){
				updateIntersectMessage(conn, intersectList);
			}
			return produceResultList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询所有开启项目以及情报的grid集合
	 * @throws Exception 
	 * 
	 * */
	public Map<Integer, Set<Integer>> queryAllGridsWithOpenProgram(Connection conn) throws Exception{
		try{
			conn = DBConnector.getInstance().getManConnection();
			String sql = "select to_number(t.program_id) as program_id,t.grid_id from PROGRAM_GRID_MAPPING t where t.program_id in"
					+ " (select p.program_id from program p where p.status = 1) union all "
					+ " select pm.program_id, t.grid_id from INFOR_GRID_MAPPING t,program pm where t.infor_id in "
					+ " (select p.infor_id from program p where p.status = 1 "
					+ " and p.infor_id <> 0) and t.infor_id = pm.infor_id(+)";
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Map<Integer, Set<Integer>>> rsHandler = new ResultSetHandler<Map<Integer, Set<Integer>>>() {
				public Map<Integer, Set<Integer>> handle(ResultSet rs) throws SQLException{
					Map<Integer, Set<Integer>> result = new HashMap<>();
					while(rs.next()){
						Set<Integer> grids = new HashSet<>();
						int programId = rs.getInt("program_id");
						if(programId != 0){
							if(result.containsKey(programId)){
								grids = result.get(programId);
								grids.add(rs.getInt("grid_id"));
								result.put(programId, grids);
							}else{
								grids.add(rs.getInt("grid_id"));
								result.put(programId, grids);
							}
						}
					}
					return result;
				}
			};
			return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(), e);
		}
	}
	
	/**
	 * 保存冲突的无法出品的项目到出品表中记录信息
	 * @param Connection
	 * @param List<Map<String,Object>>
	 * @throws Exception 
	 * 
	 * */
	public void updateIntersectMessage(Connection conn, List<Map<String,Object>> intersectList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();

			String sql = "update PRODUCE t set t.parameter = ?, t.produce_status = 4 where t.produce_id = ?";
			Object[][] inParam = new Object[intersectList.size()][];
			int i = 0;
			for(Map<String,Object> map : intersectList){
				JSONObject json = new JSONObject();
				Object[] temp = new Object[2];
				json.put("programId", map.get("programId"));
				json.put("intersecGrids", map.get("intersecGrids"));
				json.put("intersecProgramId", map.get("intersecProgramId"));
				temp[0] = json.toString();
				temp[1] = map.get("produceId");
				inParam[i] = temp;
				i++;
			}
			log.info("保存冲突的无法出品的项目到出品表中记录信息:" + sql);
			run.batch(conn, sql, inParam);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("保存失败，原因为:"+e.getMessage(),e);
		}
		
	}

	public static void main(String[] args) {
		
	}
}
