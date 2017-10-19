package com.navinfo.dataservice.scripts;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.scripts.refinement.PoiTaskTabLogDependent;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ImportCityBlockByJson
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: ImportCityBlockByJson.java
 */
public class InitDataPoi2Tab {

	/**
	 * @Title: execute
	 * @Description: 执行方法
	 * @param request
	 * @return
	 * @throws Exception  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月16日 下午3:27:38 
	 */
	public static JSONObject execute(JSONObject request) throws Exception {
		JSONObject response = new JSONObject();
		//中间库连接
		Connection conn = null; 
		DataSource dataSource = null;
		JSONArray errorLogs = null;
		
		try { 
			errorLogs = new JSONArray();
			
			JSONObject db_conf = request.getJSONObject("db_conf");
			String db_ip = db_conf.getString("db_ip");
			String db_port = db_conf.getString("db_port");
			String service_name = db_conf.getString("service_name");
			String db_username = db_conf.getString("db_username");
			String db_password = db_conf.getString("db_password");
		
			JSONArray data = (JSONArray) request.get("data");
			//获取中间库的连接	
			dataSource = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@"+db_ip+":"+db_port+"/"+service_name+"", db_username, db_password);
			conn = dataSource.getConnection();
			 
			if(conn != null && data != null && data.size() > 0){
				//解析参数
				for(Object obj : data){
					JSONObject jObj = (JSONObject) obj;
					
					int taskId = 0;
					if(jObj.containsKey("taskId")){
						taskId = jObj.getInt("taskId");
					}else{
						JSONObject errorLog = new JSONObject();
						errorLog.put("taskId", taskId);
						errorLog.put("taskName", "");
						errorLog.put("subTaskId", "");
						errorLog.put("errorMsg", "脚本本参数无任好号,请检查.");
						errorLogs.add(errorLog);
					}
					
					String subTaskIdStr = "";
					if(jObj.containsKey("subTaskId")){
						subTaskIdStr = jObj.getString("subTaskId");
					}
					
					//判断:任务号和对应子任务号有一个未关闭，则任务号、子任务号都不提，报log，报出采集任务名称
					Set<String> unclosedTasks = queryUnclosedTasks(taskId,subTaskIdStr);
					if(unclosedTasks != null  && unclosedTasks.size() > 0){
						for(String taskName : unclosedTasks){
							JSONObject errorLog = new JSONObject();
							errorLog.put("taskId", taskId);
							errorLog.put("taskName", taskName);
							errorLog.put("subTaskId", subTaskIdStr);
							errorLog.put("errorMsg", "存在未关闭任务或子任务.");
							errorLogs.add(errorLog);
						}
						continue;
					}
					
					Set<Integer> subtIds = null;
					if(taskId > 0 ){
						subtIds = querySubtaskIdsByTaskId(taskId);
					}else{
						JSONObject errorLog = new JSONObject();
						errorLog.put("taskId", taskId);
						errorLog.put("subTaskId", subTaskIdStr);
						errorLog.put("errorMsg", "taskId 号不能为空");
						errorLogs.add(errorLog);
						continue;
						
					}
					//判断:是否 task 下的子任务和参数中的子任务是否存在
					if((subTaskIdStr == null || StringUtils.isEmpty(subTaskIdStr)) && (subtIds == null || subtIds.size() == 0)){
						JSONObject errorLog = new JSONObject();
						errorLog.put("taskId", taskId);
						errorLog.put("subTaskId", subTaskIdStr);
						errorLog.put("errorMsg", "本组中没有对应的子任务.");
						errorLogs.add(errorLog);
						continue;
					}
					
					Set<Integer> daylyDbIds = getDaylyDbIdBySubtaskId(subtIds ,subTaskIdStr);
					if(daylyDbIds != null && daylyDbIds.size() > 0){
						//遍历各个日大区库,将大区库中需要导出的数据导入目标大区库
						for(Integer dDbID : daylyDbIds){
							//TODO 临时加的
							/*if(dDbID != 13){
								continue;
							}*/
							moveDataToTargetDB(dataSource,conn,dDbID,taskId,subtIds,subTaskIdStr,db_conf);
						}
					}else{
						JSONObject errorLog = new JSONObject();
						errorLog.put("taskId", taskId);
						errorLog.put("subTaskId", subTaskIdStr);
						errorLog.put("errorMsg", "没有获取到对应的大区库.");
						errorLogs.add(errorLog);
						continue;
					}
				}
				
			}else{
				throw new Exception("data 参数不能为空 .");
			}
				conn.commit();
//				response.put("region_" + key + "_man_rows", "success");
			response.put("errorLogs", errorLogs);
			response.put("msg", "执行成功");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			response.put("errorLogs", errorLogs);
			response.put("msg", "ERROR:" + e.getMessage());
			System.out.println(e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return response;
	}

	/**
	 * @param dataSource 
	 * @Title: moveDataToTargetDB
	 * @Description: TODO
	 * @param conn
	 * @param dDbID
	 * @param taskId 
	 * @param subtIds
	 * @param subTaskIdStr  void
	 * @param db_conf 
	 * @throws Exception 
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月12日 上午11:10:01 
	 */
	private static void moveDataToTargetDB(DataSource dataSource, Connection conn, Integer dDbID, int taskId, Set<Integer> subtIds, String subTaskIdStr, JSONObject db_conf) throws Exception {
		try{
			//1.创建大区库和中间库的dblink 
				createMetaDbLink(dataSource, dDbID);
			//2.1 DBLINK_TAB 在中间库中创建  
				createTable(conn);
			//2.2 在大区库上创建 POI_TASK_TEMP 临时表
				createTempTable(dDbID);
			//3.建需要导出的poi 的pid 赋值到 表:POI_TASK_TAB
				Clob subtsClob = ConnectionUtil.createClob(conn);
				String subts = StringUtils.join(subtIds, ",");
				if(subTaskIdStr != null && StringUtils.isNotEmpty(subTaskIdStr)){
					if(subts != null && StringUtils.isNotEmpty(subts)){
						subts+=","+subTaskIdStr;
//						subTaskIdStr+=","+subts;
					}else{
						subts = subTaskIdStr;
					}
				}
					subtsClob.setString(1, subts);
				System.out.println(subts);	
			//4.1 为中间库导入POI_TASK_TAB数据
				StringBuilder insertPoiTaskTabDataSql = new StringBuilder();
				insertPoiTaskTabDataSql.append( " insert into POI_TASK_TAB (pid,task_id,subtask_id) select distinct pid, "+taskId+" task,subtask_id   from ("
						+ "  select s.pid,s.quick_subtask_id subtask_id   from poi_edit_status@DBLINK_TAB s where s.quick_subtask_id  in (select to_number(column_value) from table(clob_to_table(?))) "
						+ " 	union all "
						+ "  select s.pid,s.medium_subtask_id subtask_id   from poi_edit_status@DBLINK_TAB s where s.medium_subtask_id in (select to_number(column_value) from table(clob_to_table(?))) "
						+ " ) ");
				
					System.out.println("insertPoiTaskTabDataSql.toString(): "+insertPoiTaskTabDataSql.toString());
				
				Object [] params ={subtsClob, subtsClob};
				new QueryRunner().update(conn, insertPoiTaskTabDataSql.toString(),params);
				
				conn.commit();
			//4.2 为大区库上的临时表添加数据
				insertDataTotable(dDbID,taskId,subts);
				
			//5.获取中间库中所有的pids 
				Set<Long> objPids = queryPidsByTaskId(conn,taskId);
			
			//6.创建中间库的其他表
				
				String tableName = createIxpoiTempTable(dDbID, taskId, "IX_POI","PID");
				System.out.println("tableName: "+tableName);
				createAndInsertTable(conn, taskId, "IX_POI","PID", tableName);
				//insertTable(conn,taskId,"IX_POI","PID");
				insertTable(conn,taskId,"IX_POI_NAME","POI_PID");
				insertTable(conn,taskId,"IX_POI_CONTACT","POI_PID");
				insertTable(conn,taskId,"IX_POI_ADDRESS","POI_PID");
				insertTable(conn,taskId,"IX_POI_CHILDREN","CHILD_POI_PID");
				insertTable(conn,taskId,"IX_POI_PARENT","PARENT_POI_PID ");
				insertTable(conn,taskId,"IX_POI_RESTAURANT","POI_PID");
				insertTable(conn,taskId,"IX_POI_PARKING","POI_PID");
				insertTable(conn,taskId,"IX_POI_HOTEL","POI_PID");
				insertTable(conn,taskId,"IX_SAMEPOI_PART","POI_PID");
				insertIxSamePoiTable(conn);
				insertTable(conn,taskId,"IX_POI_FLAG_METHOD","POI_PID");
			
			
			//7.查询这一组中 最早的开始时间 及最晚的结束时间 //String startDate,String endDate
				Map<String,String> timeMap =  queryStartTimeAndEndTime(subtIds, subTaskIdStr);
				
				String start_date = null;
				String end_date = null;
				if(timeMap != null && timeMap.size() > 0){
					start_date = timeMap.get("start_date");
					end_date = timeMap.get("end_date");
				}
				System.out.println("start_date:"+start_date + "  end_date" +end_date);
			
			//8.在大区库上查询查询最后一条编辑履历的人员ID
				Map<Long,Long>  pidUserIds = queryLastUserIdByLog(dDbID,objPids,start_date,end_date);
			
			//9.为中间库的表赋值人员ID
				insertPoiTaskTabUserIds(conn,pidUserIds);
			
			//10.为POI_TASK_TAB 表打删除标记,位移标记,新增标记
				PoiTaskTabLogDependent pd = new PoiTaskTabLogDependent();
				pd.run(conn,objPids,start_date,end_date,dDbID);
				
			//11.为中间库的统计表添加数据 :fm_poi_cutout_stat
				insertFmPoiCutoutStat(conn,taskId);
				
			//12.为中间库的统计表添加数据 :fm_poi_cutout
				//12.1 在中间库向 中间库fm_poi_cutout 插入 pid,taskId,subtaskId,poi_num,
				insertFmPoiCutout(conn);
				
				//12.2 根据 taskid 及 subtaskId 去man 库查询相关数据
				insertFmPoiCutoutFromMan(conn,taskId,subts);
				
		} catch (Exception e) {
			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw e;
		} finally {
//			DbUtils.closeQuietly(dDbConn);
		}
		
	}
	
private static void insertFmPoiCutoutFromMan(Connection conn, int taskId, String subts) throws SQLException {
		
		Connection connMan = null;
		PreparedStatement perstmt = null;
		try{
			String updateFmPoiCutoutSql = "update  fm_poi_cutout set subtask_name = ?,task_name = ?,program_id = ?,program_name = ?,inforcode = ?,province = ?  where subtask_id = ? ";
			//获取man库连接
			connMan = DBConnector.getInstance().getManConnection();
			Clob clob = connMan.createClob();
			clob.setString(1, subts);
			String selectSql = "  select s.subtask_id,s.subtask_name ,t.task_id,t.name task_name,p.program_id,p.name program_name,p.infor_id inforcode ,c.province_name province  " 
					+" from ( select s.subtask_id,s.name subtask_name ,"+taskId+" task_id  from subtask s  "
					+ "where s.task_id = "+taskId+" or s.subtask_id in (select to_number(column_value) from table(clob_to_table(?)) )) s ,"
					+ "task t,program p,city c "
					+" where s.task_id =t.task_id(+)  and t.program_id = p.program_id(+) "
					+"  and p.city_id = c.city_id(+)    ";
			System.out.println("queryFmPoiCutoutFromMan: "+selectSql);
			ResultSetHandler< List<Map<String, Object>>> rs = new ResultSetHandler< List<Map<String, Object>>>() {
				@Override
				public  List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("subtask_id", rs.getLong("subtask_id"));
						map.put("subtask_name", rs.getString("subtask_name"));
						map.put("task_id", rs.getLong("task_id"));
						map.put("task_name", rs.getString("task_name"));
						map.put("program_id",  rs.getLong("program_id"));
						map.put("program_name", rs.getString("program_name"));
						map.put("inforcode", rs.getLong("inforcode"));
						map.put("province", rs.getString("province"));
						
						results.add(map);
					}
					return results;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(connMan,selectSql,clob, rs);
			
			if(result != null && result.size() > 0){
				if(perstmt==null){
					perstmt = conn.prepareStatement(updateFmPoiCutoutSql);
				}
				for(Map<String, Object> fmPoiCutoutMap : result ){
					//
					long subtask_id = (long) fmPoiCutoutMap.get("subtask_id");
					String subtask_name = (String) fmPoiCutoutMap.get("subtask_name");
//					long task_id = (long) fmPoiCutoutMap.get("task_id");
					String task_name = (String) fmPoiCutoutMap.get("task_name");
					long program_id = (long) fmPoiCutoutMap.get("program_id");
					String program_name = (String) fmPoiCutoutMap.get("program_name");
					long inforcode = (long) fmPoiCutoutMap.get("inforcode");
					String province = (String) fmPoiCutoutMap.get("province");
					
					
					perstmt.setString(1,subtask_name);
					perstmt.setString(2,task_name);
					perstmt.setLong(3, program_id);
					perstmt.setString(4,program_name);
					perstmt.setLong(5, inforcode);
					perstmt.setString(6,province);
					perstmt.setLong(7, subtask_id);
					perstmt.addBatch();
					
				}
				if(perstmt!=null){
					perstmt.executeBatch();
					conn.commit();
				}
			}
		}catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			DbUtils.close(perstmt);
			DbUtils.commitAndCloseQuietly(connMan);
		}
		
	}

	private static void insertFmPoiCutout(Connection conn) throws Exception {
		try {
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('fm_poi_cutout') ;    ");
			sb.append("    if num = 0 then                                                                                  ");
			sb.append("        execute immediate 'create table fm_poi_cutout (pid  NUMBER(10) not null,poi_num  VARCHAR2(36), "
					+ "task_id  NUMBER(10) default 0 not null,task_name  VARCHAR2(200) ,gdbversion  VARCHAR2(20) ,"
					+ "program_id NUMBER(10),program_name  VARCHAR2(200), subtask_id  NUMBER(10) default 0 not null,"
					+ "subtask_name VARCHAR2(200),region_id   NUMBER(10),province  VARCHAR2(20) ,inforcode  NUMBER(10),"
					+ "fetchdate  VARCHAR2(50))' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			r.execute(conn, sb.toString());
			
			String currentDate = com.navinfo.dataservice.commons.util.StringUtils.getCurrentTime();	
			String seasonVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
			String insertFmPoiCutoutSql = " insert into  fm_poi_cutout(gdbversion,pid,poi_num,task_id,subtask_id,fetchdate) "
					+ "   select '"+seasonVersion+"' gdbversion,p.pid,i.poi_num,p.task_id,p.subtask_id,'"+currentDate+"' fetchdate from poi_task_tab p,ix_poi i where p.pid = i.pid  ";
			System.out.println("insertFmPoiCutoutSql: "+insertFmPoiCutoutSql.toString());
			r.execute(conn, insertFmPoiCutoutSql);	
			conn.commit();
			
			System.out.println(" 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private static void insertFmPoiCutoutStat(Connection conn, int taskId) throws Exception {
		PreparedStatement perstmt = null;

		try {
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('fm_poi_cutout_stat') ;    ");
			sb.append("    if num = 0 then                                                                                  ");
			sb.append("        execute immediate 'create table fm_poi_cutout_stat (id number(10) , task_id    NUMBER(10) default 0 not null,task_poi_num number(6) default 0 not null,subtask_id_num  CLOB,fetchdate  VARCHAR2(50))' ;                                       ");
			sb.append("        execute immediate 'create sequence fm_poi_cutout_stat_SEQ  minvalue 1 maxvalue 9999999999 start with 1  increment by 1 cache 20 ' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			r.execute(conn, sb.toString());
			
			String currentDate = com.navinfo.dataservice.commons.util.StringUtils.getCurrentTime();	
			String insertFmPoiCutoutStat = " insert into  fm_poi_cutout_stat(id,task_id,task_poi_num,subtask_id_num,fetchdate) VALUES(fm_poi_cutout_stat_SEQ.NEXTVAL,?,?,?,'"+currentDate+"')  ";
			System.out.println("insertFmPoiCutoutStat.toString(): "+insertFmPoiCutoutStat.toString());
			List<Map<String, Object>>  subtaskIdNums = querysubtaskIdNumsByTaskId(conn,taskId);
			int task_poi_num =queryTotalByTaskId(conn, taskId);
				if(perstmt==null){
					perstmt = conn.prepareStatement(insertFmPoiCutoutStat);
				}
				
				perstmt.setInt(1, taskId);
				perstmt.setInt(2, task_poi_num);
				JSONArray jsonArray = JSONArray.fromObject(subtaskIdNums);
//				JSONObject jsonObject = JSONObject.fromObject(subtaskIdNums);
				Clob clob = conn.createClob();
					clob.setString(1, jsonArray.toString());
				perstmt.setClob(3, clob);
				
				if(perstmt!=null){
					perstmt.execute();
					conn.commit();
				}
			System.out.println(" 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	private static List<Map<String, Object>> querysubtaskIdNumsByTaskId(Connection conn, int taskId) throws Exception {
		try{
			String selectSql = " select p.subtask_id,count(1) NUM  from POI_TASK_TAB p WHERE P.TASK_ID = "+taskId+"  group by p.subtask_id  ";
			System.out.println("querysubtaskIdNumsByTaskId: "+selectSql);
			ResultSetHandler< List<Map<String, Object>>> rs = new ResultSetHandler< List<Map<String, Object>>>() {
				@Override
				public  List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("subtask_id", rs.getLong("subtask_id"));
						map.put("num",  rs.getInt("num"));
						results.add(map);
					}
					return results;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			System.out.println(e.getMessage());
			throw new Exception("查询subtaskIdNums 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private static int queryTotalByTaskId(Connection conn, int taskId) throws Exception {
		try{
			String selectSql = " select count(1) total  from POI_TASK_TAB p WHERE P.TASK_ID = "+taskId+" ";
			System.out.println("queryTotalByTaskId: "+selectSql);
			ResultSetHandler< Integer> rs = new ResultSetHandler< Integer>() {
				@Override
				public  Integer handle(ResultSet rs) throws SQLException {
					int result = 0;
					while(rs.next()){
						result =  rs.getInt("total");
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			int result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			System.out.println(e.getMessage());
			throw new Exception("查询subtaskIdNums 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @Title: insertDataTotable
	 * @Description: 向大区库的中间表中添加数据
	 * @param dDbID
	 * @param taskId
	 * @param subts
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月16日 下午6:14:38 
	 */
	private static void insertDataTotable(Integer dDbID, int taskId, String subts) throws Exception {
		Connection conn = null;
		try{
			//获取大区库连接
			conn = DBConnector.getInstance().getConnectionById(dDbID);
			Clob subtsClob = ConnectionUtil.createClob(conn);
			subtsClob.setString(1, subts);
			StringBuilder insertPoiTaskTempDataSql = new StringBuilder();
			insertPoiTaskTempDataSql.append( " insert into POI_TASK_TEMP (pid,task_id,subtask_id) select distinct pid, "+taskId+" task,subtask_id  from ("
				+ "  select s.pid,s.quick_subtask_id subtask_id   from poi_edit_status s where s.quick_subtask_id  in (select to_number(column_value) from table(clob_to_table(?))) "
				+ " 	union all "
				+ "  select s.pid,s.medium_subtask_id subtask_id   from poi_edit_status s where s.medium_subtask_id in (select to_number(column_value) from table(clob_to_table(?))) "
				+ " ) ");
		
			System.out.println("insertPoiTaskTempDataSql.toString(): "+insertPoiTaskTempDataSql.toString());
		
			Object [] paramsTemp ={subtsClob, subtsClob};
			new QueryRunner().update(conn, insertPoiTaskTempDataSql.toString(),paramsTemp);
			
			conn.commit();
		}catch(Exception e){
			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询pid 失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

	private static void insertIxSamePoiTable(Connection conn) throws SQLException {
		System.out.println("开始新增表:IX_SAMEPOI");
		StringBuilder createAndInsertIxSamePoiTableSql = new StringBuilder();
		createAndInsertIxSamePoiTableSql.append( " insert into  IX_SAMEPOI  select p.*  from IX_SAMEPOI@DBLINK_TAB p,IX_SAMEPOI_PART s  "
				+ "  where  p.GROUP_ID = s.GROUP_ID ");
		
		System.out.println("createAndInsertIxSamePoiTable.toString(): "+createAndInsertIxSamePoiTableSql.toString());
		
		try {
			QueryRunner r = new QueryRunner();
			
			r.update(conn, createAndInsertIxSamePoiTableSql.toString());
			conn.commit();
			System.out.println("新增表:IX_SAMEPOI 完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		}
		
	}
	
	private static void insertTable(Connection conn, int taskId, String tbName, String column) throws SQLException {
		System.out.println("开始新增表:"+tbName);
		StringBuilder insertTableSql = new StringBuilder();
		//SELECT distinct p.pid FROM POI_TASK_TAB p WHERE p.TASK_ID ="+taskId
		insertTableSql.append( " INSERT INTO  "+tbName+"  select p.*  from "+tbName+"@DBLINK_TAB p,POI_TASK_TAB t  "
				+ "  where p."+column+" = t.pid and t.TASK_ID ="+taskId);
		
		System.out.println("insertTable.toString(): "+insertTableSql.toString());
		
		try {
			QueryRunner r = new QueryRunner();
			
			int a = r.update(conn, insertTableSql.toString());
			conn.commit();
			System.out.println("新增表:"+tbName+"完毕. "+a);
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		}
	}
	
	private static String createIxpoiTempTable(int DbId, int taskId ,String tbName, String column) throws Exception {
		String tableName = tbName+"_"+taskId;
		System.out.println("开始新增表:"+tableName);
		StringBuilder createAndInsertTableSql = new StringBuilder();
		createAndInsertTableSql.append( " CREATE TABLE "+tableName+" as select p.*  from "+tbName+" p ,POI_TASK_TEMP t  "
				+ "  where p."+column+" = t.pid and t.TASK_ID ="+taskId);
		System.out.println("createIxpoiTempTable.toString(): "+createAndInsertTableSql.toString());
		Connection dDbConn = null;
		
		try {
			//获取大区库连接
			dDbConn = DBConnector.getInstance().getConnectionById(DbId);
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('"+tableName+"') ;    ");
			sb.append("    if num > 0 then                                                                                  ");
			sb.append("        execute immediate 'drop table "+tableName+"' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			r.execute(dDbConn, sb.toString());
			System.out.println("drop table : drop table "+tableName);
			r.update(dDbConn, createAndInsertTableSql.toString());
			System.out.println("createInsert table :  "+tableName);
			
			dDbConn.commit();
			System.out.println("新增表:"+tableName+"完毕.");
			return tableName;
		} catch (SQLException e) {
			dDbConn.rollback();
			e.printStackTrace();
			System.out.println("createIxpoiTempTable 异常:"+e.getMessage());
			return null;
		}
	}
	
	private static void createAndInsertTable(Connection conn, int taskId, String tbName, String column, String tableName) throws SQLException {
		System.out.println("开始新增表:"+tbName);
		StringBuilder createAndInsertTableSql = new StringBuilder();
		//SELECT distinct p.pid FROM POI_TASK_TAB p WHERE p.TASK_ID ="+taskId
		createAndInsertTableSql.append( " insert into  "+tbName+"  select *  from "+tableName+"@DBLINK_TAB p  ");
		
		System.out.println("createAndInsertTable.toString(): "+createAndInsertTableSql.toString());
		
		try {
			QueryRunner r = new QueryRunner();
			
			r.update(conn, createAndInsertTableSql.toString());
			conn.commit();
			System.out.println("新增表:"+tbName+"完毕.");
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		}
	}

	private static void insertPoiTaskTabUserIds(Connection conn, Map<Long, Long> pidUserIds) throws Exception {
		PreparedStatement perstmtInserted = null;
		try{
			if(pidUserIds != null && pidUserIds.size() > 0){
				String insertedSql = " update  POI_TASK_TAB  set OPERATOR =?  where PID = ? ";
				
				for(Map.Entry<Long, Long> entry:pidUserIds.entrySet()){
					if(perstmtInserted==null){
						perstmtInserted = conn.prepareStatement(insertedSql);
					}
					perstmtInserted.setLong(1,entry.getValue());
					perstmtInserted.setLong(2,entry.getKey());
					perstmtInserted.addBatch();
				}
				
				if(perstmtInserted!=null){
					perstmtInserted.executeBatch();
				}
			}
			
		}catch(Exception e){
			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询pid 失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.close(perstmtInserted);
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @Title: queryLastUserIdByLog
	 * @Description: 获取最后一条编辑履历的人员ID的集合
	 * @param conn
	 * @param objPids
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception  Map<Long,Long>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月15日 下午7:38:11 
	 */
	private static Map<Long, Long> queryLastUserIdByLog(Integer dDbID, Set<Long> objPids, String startDate,String endDate) throws Exception {
		
		Connection conn = null;
		try{
			//获取大区库连接
			conn = DBConnector.getInstance().getConnectionById(dDbID);
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(objPids, ","));
			
			String dateWhereStr = "";
			if(StringUtils.isNotEmpty(startDate)){
				dateWhereStr += "    AND o.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss') " ;
			}
			if(StringUtils.isNotEmpty(endDate)){
				dateWhereStr += "    AND o.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss') ";
			}
			
			
			String selectSql = "with q1 as ( "
					+ " select d.ob_pid ,a.us_id,o.op_seq seq from log_detail d,log_operation o, log_action  a "
					+ "  where d.op_id = o.op_id and o.act_id = a.act_id   and d.ob_nm = 'IX_POI' "
					+ "  and  d.ob_pid in (select column_value from table(clob_to_table(?))) "
					+ dateWhereStr 
					+ "  group by d.ob_pid ,a.us_id,o.op_seq "
					+ "  ) "
					+ "  SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY q.ob_pid ORDER BY q.seq DESC) rn,q.*  FROM q1 q )  WHERE rn = 1 ";
			System.out.println("queryLastUserIdByLog: "+selectSql);
			ResultSetHandler< Map<Long, Long>> rs = new ResultSetHandler< Map<Long, Long>>() {
				@Override
				public  Map<Long, Long> handle(ResultSet rs) throws SQLException {
					 Map<Long, Long> result = new HashMap<Long, Long>();
					while(rs.next()){
						result.put(rs.getLong("ob_pid"), rs.getLong("us_id"));
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Map<Long, Long> result = run.query(conn,selectSql, rs,pidsClob);
			return result;
		}catch(Exception e){
			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询pid 失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	private static Map<String, String> queryStartTimeAndEndTime(Set<Integer> subtIds, String subTaskIdStr) throws Exception {
		Connection conn = null;
		//select to_char(min(s.plan_start_date),'yyyymmddhh24miss') star_date,to_char(max(s.plan_end_date),'yyyymmddhh24miss') end_date from Subtask s
		try{
			conn = DBConnector.getInstance().getManConnection();
			Clob subtsClob = ConnectionUtil.createClob(conn);
			String subts = StringUtils.join(subtIds, ",");
			if(subTaskIdStr != null && StringUtils.isNotEmpty(subTaskIdStr)){
				if(subts != null && StringUtils.isNotEmpty(subts)){
					subTaskIdStr+=","+subts;
				}
				subtsClob.setString(1, subTaskIdStr);
			}else{
				subtsClob.setString(1, subts);
			}
			String selectSql = "select to_char(min(s.plan_start_date),'yyyymmddhh24miss') start_date,to_char(max(s.plan_end_date),'yyyymmddhh24miss') end_date from Subtask s "
					+ "where s.subtask_id in (select column_value from table(clob_to_table(?))) ";
			ResultSetHandler< Map<String, String>> rs = new ResultSetHandler< Map<String, String>>() {
				
				@Override
				public  Map<String, String> handle(ResultSet rs) throws SQLException {
					 Map<String, String> result = new HashMap<String, String>();
					while(rs.next()){
						result.put("start_date", rs.getString("start_date"));
						result.put("end_date", rs.getString("end_date"));
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Map<String, String> result = run.query(conn,selectSql, rs,subtsClob);
			return result;
		}catch(Exception e){
//			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询pid 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	
	/**
	 * @Title: queryPidsByTaskId
	 * @Description: 从中间库中查询出本组的所有pid
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception  Set<Long>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月15日 下午3:53:02 
	 */
	private static Set<Long> queryPidsByTaskId(Connection conn, int taskId) throws Exception {
		try{
			String selectSql = "SELECT distinct p.pid FROM POI_TASK_TAB p WHERE p.TASK_ID ="+taskId;
			ResultSetHandler<Set<Long>> rs = new ResultSetHandler<Set<Long>>() {
				
				@Override
				public Set<Long> handle(ResultSet rs) throws SQLException {
					Set<Long> result = new HashSet<Long>();
					while(rs.next()){
						long subtaskId = rs.getLong("pid");
						result.add(subtaskId);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Set<Long> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
//			DbUtils.rollback(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询pid 失败，原因为:"+e.getMessage(),e);
		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static void createTable(Connection conn) throws SQLException{
		QueryRunner r = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("declare                                                                                              ");
		sb.append("    num   number;                                                                                    ");
		sb.append("begin                                                                                                ");
		sb.append("    select count(1) into num from user_tables where table_name = upper('POI_TASK_TAB') ;    ");
		sb.append("    if num > 0 then                                                                                  ");
		sb.append("        execute immediate 'drop table POI_TASK_TAB' ;                                       ");
		sb.append("    end if;                                                                                          ");
		sb.append("end;                                                                                                 ");
		
		r.execute(conn, sb.toString());
		sb.delete( 0, sb.length() );
		sb.append(" create table POI_TASK_TAB     ");
		sb.append("(                                      ");
		sb.append("  pid     NUMBER(10) not null,         ");
		sb.append("  task_id NUMBER(10) default 0 not null,");
		sb.append("  subtask_id NUMBER(10) default 0 not null,");
		sb.append("  DELFLAG  VARCHAR2(1) default 'F' not null,");
		sb.append("  moveflag VARCHAR2(1) default 'F' not null,");
		sb.append("  addflag  VARCHAR2(1) default 'F' not null,");
		sb.append("  OPERATOR VARCHAR2(50), ");
		sb.append("  log     VARCHAR2(50) ");
		sb.append(")      ");
		r.execute(conn, sb.toString());
	}
	
	public static void createTempTable(Integer dDbID) throws Exception{
		Connection conn = null;
		try{
			//获取大区库连接
			conn = DBConnector.getInstance().getConnectionById(dDbID);
		
		QueryRunner r = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("declare                                                                                              ");
		sb.append("    num   number;                                                                                    ");
		sb.append("begin                                                                                                ");
		sb.append("    select count(1) into num from user_tables where table_name = upper('POI_TASK_TEMP') ;    ");
		sb.append("    if num > 0 then                                                                                  ");
		sb.append("        execute immediate 'drop table POI_TASK_TEMP' ;                                       ");
		sb.append("    end if;                                                                                          ");
		sb.append("end;                                                                                                 ");
		
		r.execute(conn, sb.toString());
		sb.delete( 0, sb.length() );
		sb.append(" create table POI_TASK_TEMP     ");
		sb.append("(                                      ");
		sb.append("  pid     NUMBER(10) not null,         ");
		sb.append("  task_id NUMBER(10) default 0 not null,");
		sb.append("  subtask_id NUMBER(10) default 0 not null,");
		sb.append("  constraint PK_POI_TASK_TEMP primary key (PID) ");
		sb.append(")      ");
		r.execute(conn, sb.toString());
		} catch (Exception e) {
			conn.rollback();
			System.out.println(e.getMessage());
			throw e;
		} 
		finally{
			DbUtils.closeQuietly(conn);
		}
	}

	private static void createMetaDbLink(DataSource dataSource, int dbid)
			throws Exception {
		//获取大区库连接
		DatahubApi datahub = (DatahubApi) ApplicationContextUtil
				.getBean("datahubApi");

		DbInfo db = datahub.getDbById(dbid);
		Map<String,Object> dbMap =db.getConnectParam();
		QueryRunner r = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("declare                                                                                              ");
		sb.append("    num   number;                                                                                    ");
		sb.append("begin                                                                                                ");
		sb.append("    select count(1) into num from dba_db_links dbl where dbl.DB_LINK = 'DBLINK_TAB' ;    ");
		sb.append("    if num > 0 then                                                                                  ");
		sb.append("        execute immediate ' DROP DATABASE LINK DBLINK_TAB ' ;                                       ");
		sb.append("    end if;                                                                                          ");
		sb.append("end;                                                                                                 ");
		r.execute(dataSource.getConnection(), sb.toString());
		
		
		DbLinkCreator cr = new DbLinkCreator();
		//
		String db_ip = (String) dbMap.get("serverIp");
		Integer db_port = (Integer) dbMap.get("serverPort");
		String service_name = (String) dbMap.get("serviceName");
		String db_username = (String) dbMap.get("dbUserName");
		String db_password = (String) dbMap.get("dbUserPasswd");
		
		cr.create("DBLINK_TAB", false, dataSource, db_username,
				db_password, db_ip, db_port.toString(), service_name); 
	}

	//获取任务或者子任务状态不是关闭的 任务名称
	public static Set<String> queryUnclosedTasks(int taskId,String subTaskIdStr) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String sqlWhereStr = "";
			if(subTaskIdStr != null && StringUtils.isNotEmpty(subTaskIdStr)){
				sqlWhereStr+= " union  "
						+ " select distinct t.name  from subtask s ,task t where s.task_id = t.task_id  and (t.status != 0 or s.status != 0)"
						+ "  and s.subtask_id in ("+subTaskIdStr+")";
			}
			String selectSql = 
					"select distinct t.name  from subtask s ,task t where s.task_id = t.task_id  and (t.status != 0 or s.status != 0)"
					+ "  and t.task_id = "+taskId 
					+  sqlWhereStr;
			ResultSetHandler<Set<String>> rs = new ResultSetHandler<Set<String>>() {
				
				@Override
				public Set<String> handle(ResultSet rs) throws SQLException {
					Set<String> result = new HashSet<String>();
					while(rs.next()){
						
						String name = rs.getString("name");
						
						result.add(name);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Set<String> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			System.out.println(e.getMessage());
			throw new Exception("获取任务或者子任务状态不是关闭的失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	//获取任务及子任务相关详情
	public static Set<Integer> querySubtaskIdsByTaskId(int taskId) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT distinct S.SUBTASK_ID FROM SUBTASK S WHERE s.TASK_ID ="+taskId;
			ResultSetHandler<Set<Integer>> rs = new ResultSetHandler<Set<Integer>>() {
				
				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> result = new HashSet<Integer>();
					while(rs.next()){
						int subtaskId = rs.getInt("SUBTASK_ID");
						result.add(subtaskId);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Set<Integer> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			System.out.println(e.getMessage());
			throw new Exception("查询task的subtaskIds失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static Set<Integer> getDaylyDbIdBySubtaskId(Set<Integer> subtaskIds ,String subtaskIdStr) throws Exception{
		Set<Integer> daylyDbIds = null;
		Connection manConn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			//*********zl 将放入clob 查询**
			
			Clob subtsClob = ConnectionUtil.createClob(manConn);
			String subts = StringUtils.join(subtaskIds, ",");
			if(subtaskIdStr != null && StringUtils.isNotEmpty(subtaskIdStr)){
				if(subts != null && StringUtils.isNotEmpty(subts)){
					subtaskIdStr+=","+subts;
				}
				subtsClob.setString(1, subtaskIdStr );
			}else{
				subtsClob.setString(1, subts);
			}
			
			
			//String sql = "SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id in (select column_value from table(clob_to_table(?))) ";
			String sql = " SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE  g.region_id=r.region_id and grid_id in (select distinct  m.grid_id from  subtask_grid_mapping m where m.subtask_id in (select column_value from table(clob_to_table(?)))) ";
			System.out.println(sql);
			daylyDbIds = new QueryRunner().query(manConn, sql, new ResultSetHandler<Set<Integer>>(){

				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> set = new HashSet<Integer>();
					while (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						set.add(dbId);
					}
					return set;
				}
				
			},subtsClob);
			
			return daylyDbIds;
		} catch (Exception e) {
			System.out.println("e.getMessage(): "+e.getMessage());
			throw e;
		}finally {
			DbUtils.closeQuietly(manConn);
		}
	}

	public Collection<IxPointaddress> getPaByGrids(Map<String,String> gridDateMap) throws Exception{
		Map<Long,IxPointaddress> results = new HashMap<Long,IxPointaddress>();//key:pid,value:obj
		Connection manConn = null;
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			//*********zl 将grids 放入clob 查询**
			
			Clob gridClob = ConnectionUtil.createClob(manConn);
			
			gridClob.setString(1, StringUtils.join(gridDateMap.keySet(), ","));
			
			String sql = "SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id in (select column_value from table(clob_to_table(?))) ";
			System.out.println(sql);
			Map<Integer,Collection<String>> dbGridMap = new QueryRunner().query(manConn, sql, new ResultSetHandler<Map<Integer,Collection<String>>>(){

				@Override
				public Map<Integer, Collection<String>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Collection<String>> map = new HashMap<Integer,Collection<String>>();
					while (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						List<String> gridList = new ArrayList<String>();
						if (map.containsKey(dbId)) {
							gridList = (List<String>) map.get(dbId);
						}
						gridList.add(rs.getString("grid_id"));
						map.put(dbId, gridList);
					}
					return map;
				}
				
			},gridClob);
			
			return results.values();
		} catch (Exception e) {
			System.out.println("e.getMessage(): "+e.getMessage());
			throw e;
		}finally {
			DbUtils.closeQuietly(manConn);
		}
	}
	
	public static void main(String[] args) {
		// testExeSqlOrPck();
//		testInstallPcks(111);
	}



}
