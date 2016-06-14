package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.task.Task;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;
//import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  SubtaskService 
* @author code generator
* @date 2016-06-06 07:40:14 
* @Description: TODO
*/
@Service
public class SubtaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(Subtask bean,long userId,JSONArray gridIds,String wkt)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	

			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";
			
			int subTaskId = Integer.valueOf(run.query(conn,querySql,new MapHandler()).get("subTaskId").toString()); 
			
			String createSql = "insert into SUBTASK "
					+ "(SUBTASK_ID"
					+ ", BLOCK_ID"
					+ ", TASK_ID"
					+ ", GEOMETRY"
					+ ", STAGE"
					+ ", TYPE"
					+ ", CREATE_USER_ID"
					+ ", EXE_USER_ID"
					+ ", CREATE_DATE"
					+ ", STATUS"
					+ ", PLAN_START_DATE"
					+ ", PLAN_END_DATE"
					+ ", DESCP) "
					+ "values(" + subTaskId 
					+ "," + bean.getBlockId()
					+ "," + bean.getTaskId()
					+ "," + "sdo_geometry(" +  "'" + wkt + "',8307)" 
					+ "," + bean.getStage()
					+ "," + bean.getType()
					+ "," + userId 
					+ "," + bean.getExeUserId()
					+ ", sysdate"
					+ ","+  "1"
					+ ",to_date('" + bean.getPlanStartDate() + "','yyyymmdd')" 
					+ ",to_date('" + bean.getPlanEndDate() + "','yyyymmdd')"
					+ ",'"+ bean.getDescp()+"')";			
			run.update(conn,createSql);
			
			String createMappingSql = "insert into SUBTASK_GRID_MAPPING (SUBTASK_ID, GRID_ID) VALUES (?,?)";	
			
			Object[][] inParam = new Object[gridIds.size()][];
            for (int i = 0; i < inParam.length; i++)
            {
            	Object[] temp = new Object[2];
                temp[0] = subTaskId;
                temp[1] = gridIds.get(i);
            	inParam[i] = temp;
               
            }
			run.batch(conn,createMappingSql, inParam);
			
				
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public List<HashMap> listByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			JSONArray types = json.getJSONArray("types");
			int stage = json.getInt("stage");

			String wkt = json.getString("wkt");
			
			String type = "(";
			for (int i = 0;i<types.size();i++){
				type += types.getInt(i);
				if(i < types.size()-1){
					type += ",";
				}
			}
			
			type += ")";
			
			String querySql = "select s.subtask_id, TO_CHAR(s.geometry.get_wkt()) as geometry,s.descp from subtask s where type in" + type 
			+ " and stage =" + stage 
			+ " and SDO_GEOM.RELATE(geometry, 'ANYINTERACT', " + "sdo_geometry(" +  "'" + wkt + "',8307)" + ", 0.000005) ='TRUE'";
		
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("descp", rs.getString("DESCP"));
						list.add(map);
					}
					return list;
				}
	    		
	    	};
	    	
	    	
	    	return run.query(conn, querySql, rsHandler);
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void update(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			if(!json.containsKey("subtasks")){return;}
			
			JSONArray subtaskArray=json.getJSONArray("subtasks");
			
			String updateSql = "update SUBTASK set "
					+ "EXE_USER_ID=? "
					+ ", PLAN_START_DATE= ?"
					+ ", PLAN_END_DATE=?"
					+ ", DESCP=? "
					+ " where SUBTASK_ID=?";	
			
			
			Object[][] inParam = new Object[subtaskArray.size()][];
			
            for (int i = 0; i < subtaskArray.size(); i++)
            {
            	JSONObject subtaskJson = subtaskArray.getJSONObject(i);
            	Object[] temp = new Object[5];
            	temp[0] = subtaskJson.getInt("exeUserId");
            	temp[1] = "to_date('" + subtaskJson.getString("planStartDate") +"','yyyymmdd')" ;
            	temp[2] = "to_date('" + subtaskJson.getString("planEndDate") +"','yyyymmdd')";
            	temp[3] = subtaskJson.getString("descp");
            	temp[4] = subtaskJson.getInt("subtaskId");
            	inParam[i] = temp;
            	
            }
            
            for(int i = 0 ; i< inParam.length; i++){
            	run.update(conn,updateSql, inParam[i]);
            }
            
//			run.batch(conn,updateSql, inParam);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	
	public List<Subtask> listByBlock(Subtask bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID,"
					+ "s.STAGE,"
					+ "s.TYPE,"
					+ "s.PLAN_START_DATE,"
					+ "s.PLAN_END_DATE,"
					+ "s.DESCP,"
					+ "TO_CHAR(s.GEOMETRY.get_wkt()) AS GEOMETRY "
					+ "from SUBTASK s "
					+ "where s.BLOCK_ID = " + bean.getBlockId()
					+ " and s.STAGE = " + bean.getStage();
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){

				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setGeometry(rs.getObject("GEOMETRY"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));	
						subtask.setPlanEndDate(DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						subtask.setDescp(rs.getString("DESCP"));
						list.add(subtask);
					}

					return list;
				}
	    		
	    	}	;
	    	
	    	return run.query(conn, selectSql, rsHandler);

	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	
	
	public Page listByUser(JSONObject json,final int currentPageNum,final int pageSize)throws ServiceException{
		Connection conn = null;
		try{
			
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
					
			int userId = json.getInt("userId");
			int snapshot = json.getInt("snapshot");

			String selectSql = "select st.SUBTASK_ID"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID";
			
			String cellsExtra = ",TO_CHAR(st.GEOMETRY.get_wkt()) AS GEOMETRY"
					+ ",listagg(sgm.GRID_ID, ',') within group(order by st.SUBTASK_ID) as GRID_ID ";
			
			String fromSql = " from subtask st"
						+ ",task t"
						+ ",city c"
						+ ",region r";
			String fromExtra = ",subtask_grid_mapping sgm ";
			
			String conditionSql = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + userId + " ";
			
			String conditionExtra = " and st.subtask_id = sgm.subtask_id ";
			
			String groupBySql = " group by st.SUBTASK_ID"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID";
			String groupByExtra = ",TO_CHAR(st.GEOMETRY.get_wkt())";
			
			if(json.containsKey("stage")){
				int stage = json.getInt("stage");
				conditionSql = conditionSql + " and st.STAGE = " + stage;
			}
			
			if(json.containsKey("type")){
				int type = json.getInt("type");
				conditionSql = conditionSql + " and st.TYPE = " + type;
			}
			
			if(json.containsKey("status")){
				int status = json.getInt("status");
				conditionSql = conditionSql + " and st.STATUS = " + status;
			}
			
			if(0 == snapshot){
				selectSql = selectSql + cellsExtra + fromSql + fromExtra + conditionSql + conditionExtra + groupBySql + groupByExtra;
			}else{
				selectSql = selectSql + fromSql + conditionSql + groupBySql;
			}
					
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));	
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						
						map.put("descp", rs.getString("DESCP"));
						map.put("status", rs.getInt("STATUS"));
						
						try{
							if(rs.findColumn("GEOMETRY") > 0){
								map.put("geometry", rs.getObject("GEOMETRY"));
							}
							if(rs.findColumn("GRID_ID") > 0){
								String gridIds = rs.getString("GRID_ID");
								String[] gridIdList = gridIds.split(",");
								map.put("gridIds", gridIdList);
							}
						}
						catch (SQLException e) {
					        int a = 1;
					    }
						
						if(1 == rs.getInt("STAGE")){
							map.put("dbId", rs.getString("DAILY_DB_ID"));
						}else if(2 == rs.getInt("STAGE")){
							map.put("dbId", rs.getString("MONTHLY_DB_ID"));
						}
						list.add(map);
						
					}
					page.setResult(list);
					return page;
				}
	    		
	    	};

	    	return run.query(currentPageNum,pageSize,conn, selectSql, rsHandler);


		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public Subtask query(Subtask bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID,"
					+ "s.STAGE,"
					+ "s.TYPE,"
					+ "s.PLAN_START_DATE,"
					+ "s.PLAN_END_DATE,"
					+ "s.DESCP,"
					+ "TO_CHAR(s.GEOMETRY.get_wkt()) AS GEOMETRY "
					+ "from SUBTASK s "
					+ "where s.SUBTASK_ID="
					+ bean.getSubtaskId();
			
			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>(){
				public Subtask handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setGeometry(rs.getObject("GEOMETRY"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));	
						subtask.setPlanEndDate(DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						subtask.setDescp(rs.getString("DESCP"));
						return subtask;
					}
					return null;
				}
	    		
	    	};		
			return run.query(conn, selectSql,rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void close(JSONArray subtaskArray)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	

			String subtaskStr = "(";
			
			for(int i =0; i<subtaskArray.size(); i++){
				subtaskStr += subtaskArray.getInt(i);
				if(i < (subtaskArray.size()- 1)){
					subtaskStr += ",";
				}
			}
			
			subtaskStr += ")";
			
			
			String updateSql = "update SUBTASK "
					+ "set STATUS=3 "
					+ "where SUBTASK_ID in"
					+ subtaskStr;	
			

			run.update(conn,updateSql);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	
}
