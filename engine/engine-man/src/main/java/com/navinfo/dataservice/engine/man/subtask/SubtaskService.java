package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.dataservice.commons.log.LoggerRepos;

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

	
	public void create(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			
			JSONArray gridIds = json.getJSONArray("gridIds");

			json.remove("gridIds");

			String wkt = GridUtils.grids2Wkt(gridIds);
			
			Subtask  bean = (Subtask)JSONObject.toBean(json, Subtask.class);
			
			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";
			
			int subTaskId = Integer.valueOf(run.query(conn,querySql,new MapHandler()).get("subTaskId").toString()); 
			
			String createSql = "insert into SUBTASK (SUBTASK_ID, BLOCK_ID, TASK_ID, GEOMETRY, STAGE, TYPE, CREATE_USER_ID, CREATE_DATE, EXE_USER_ID, STATUS, PLAN_START_DATE, PLAN_END_DATE, START_DATE, END_DATE, DESCP) "
					+ "values(" + subTaskId + ","+bean.getBlockId()+","+bean.getTaskId()+","+ "sdo_geometry(" +  "'" + wkt + "',8307)" 
					+","+ bean.getStage()+","+ bean.getType()
					+","+ bean.getCreateUserId()+","+  bean.getCreateDate()
					+","+ bean.getExeUserId()+","+  "1"
					+",to_date('" + bean.getPlanStartDate() + "','yyyymmdd')" + ",to_date('" + bean.getPlanEndDate() + "','yyyymmdd')"
					+","+  bean.getStartDate()+","+ bean.getEndDate()
					+","+ bean.getDescp()+")";			
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
			
			if(!json.containsKey("tasks")){return;}
			
			JSONArray taskArray=json.getJSONArray("tasks");
			
			String updateSql = "update SUBTASK "
					+ "set TYPE=?"
					+ ", EXE_USER_ID=?"
					+ ", PLAN_START_DATE=?"
					+ ", PLAN_END_DATE=?"
					+ ", DESCP=? "
					+ "where SUBTASK_ID=?";	
			
			Object[][] inParam = new Object[taskArray.size()][];
            for (int i = 0; i < taskArray.size(); i++)
            {
            	Subtask  bean = (Subtask)JSONObject.toBean(taskArray.getJSONObject(i), Subtask.class);
            	Object[] temp = new Object[6];
                temp[0] = bean.getType();
                temp[1] = bean.getExeUserId();
                temp[2] = bean.getPlanStartDate();
                temp[3] = bean.getPlanEndDate();
                temp[4] = bean.getDescp();
                temp[5] = bean.getSubtaskId();
            	inParam[i] = temp;
            }
			run.batch(conn,updateSql, inParam);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public void delete(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			JSONObject obj = JSONObject.fromObject(json);	
			Subtask  bean = (Subtask)JSONObject.toBean(obj, Subtask.class);	
			
			String deleteSql = "delete from  SUBTASK where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				deleteSql+=" and SUBTASK_ID=? ";
				values.add(bean.getSubtaskId());
			};
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				deleteSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				deleteSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				deleteSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getStage()!=null && StringUtils.isNotEmpty(bean.getStage().toString())){
				deleteSql+=" and STAGE=? ";
				values.add(bean.getStage());
			};
			if (bean!=null&&bean.getType()!=null && StringUtils.isNotEmpty(bean.getType().toString())){
				deleteSql+=" and TYPE=? ";
				values.add(bean.getType());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				deleteSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				deleteSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				deleteSql+=" and EXE_USER_ID=? ";
				values.add(bean.getExeUserId());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				deleteSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				deleteSql+=" and PLAN_START_DATE=? ";
				values.add(bean.getPlanStartDate());
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				deleteSql+=" and PLAN_END_DATE=? ";
				values.add(bean.getPlanEndDate());
			};
			if (bean!=null&&bean.getStartDate()!=null && StringUtils.isNotEmpty(bean.getStartDate().toString())){
				deleteSql+=" and START_DATE=? ";
				values.add(bean.getStartDate());
			};
			if (bean!=null&&bean.getEndDate()!=null && StringUtils.isNotEmpty(bean.getEndDate().toString())){
				deleteSql+=" and END_DATE=? ";
				values.add(bean.getEndDate());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				deleteSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Page listByBlock(JSONObject json,final int currentPageNum,final int pageSize)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			int blockId = json.getInt("blockId");
			
			String selectSql = "select s.SUBTASK_ID,"
					+ "s.STAGE,"
					+ "s.TYPE,"
					+ "TO_CHAR(s.PLAN_START_DATE) AS PLAN_START_DATE,"
					+ "TO_CHAR(s.PLAN_END_DATE) AS PLAN_END_DATE,"
					+ "s.DESCP,"
					+ "TO_CHAR(s.GEOMETRY.get_wkt()) AS GEOMETRY "
					+ "from SUBTASK s "
					+ "where s.BLOCK_ID = " + blockId;

			if(json.containsKey("stage")){
				int stage = json.getInt("stage");
				selectSql = selectSql + "and s.STAGE = " + stage;
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
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
	    	
	    	return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler);

	    	
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

			String selectSql;
			
			if(0 == snapshot){
				selectSql = "select * from SUBTASK where 1=1 ";
			}else{
				selectSql = "select * from SUBTASK where 1=1 ";
			}
			
			if(json.containsKey("stage")){
				int stage = json.getInt("stage");
				selectSql = selectSql +"";
			}
			
			if(json.containsKey("type")){
				int type = json.getInt("type");
				selectSql = selectSql +"";
			}
			
			if(json.containsKey("status")){
				int status = json.getInt("status");
				selectSql = selectSql +"";
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
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}		;

	    	return run.query(currentPageNum,pageSize,conn, selectSql, rsHandler);


		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public HashMap query(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID,"
					+ "s.STAGE,"
					+ "s.TYPE,"
					+ "TO_CHAR(s.PLAN_START_DATE) AS PLAN_START_DATE,"
					+ "TO_CHAR(s.PLAN_END_DATE) AS PLAN_END_DATE,"
					+ "s.DESCP,"
					+ "TO_CHAR(s.GEOMETRY.get_wkt()) AS GEOMETRY "
					+ "from SUBTASK s "
					+ "where s.SUBTASK_ID="
					+ json.getInt("subtaskId");
			
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
//						map.put("planStartDate", rs.getString("PLAN_START_DATE"));
//						map.put("planEndDate", rs.getString("PLAN_END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						return map;
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
	
}
