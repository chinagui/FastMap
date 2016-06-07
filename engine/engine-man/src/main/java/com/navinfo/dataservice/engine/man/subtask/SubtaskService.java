package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.navinfo.dataservice.engine.dao.DBConnector;
import com.navinfo.dataservice.engine.man.subtask.Subtask;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
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
			String geometry = json.getString("geometry");
			
			json.remove("gridIds");

			json.remove("geometry");
			
			Subtask  bean = (Subtask)JSONObject.toBean(json, Subtask.class);
			
			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";
			
			int subTaskId = Integer.valueOf(run.query(conn,querySql,new MapHandler()).get("subTaskId").toString()); 
			
			String createSql = "insert into SUBTASK (SUBTASK_ID, BLOCK_ID, TASK_ID, GEOMETRY, STAGE, TYPE, CREATE_USER_ID, CREATE_DATE, EXE_USER_ID, STATUS, PLAN_START_DATE, PLAN_END_DATE, START_DATE, END_DATE, DESCP) "
					+ "values(" + subTaskId + ","+bean.getBlockId()+","+bean.getTaskId()+","+ "sdo_geometry(" +  "'" + geometry + "',8307)" 
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
	public void update(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			JSONObject obj = JSONObject.fromObject(json);	
			Subtask  bean = (Subtask)JSONObject.toBean(obj, Subtask.class);	
			
			String updateSql = "update SUBTASK set SUBTASK_ID=?, BLOCK_ID=?, TASK_ID=?, GEOMETRY=?, STAGE=?, TYPE=?, CREATE_USER_ID=?, CREATE_DATE=?, EXE_USER_ID=?, STATUS=?, PLAN_START_DATE=?, PLAN_END_DATE=?, START_DATE=?, END_DATE=?, DESCP=? where 1=1 SUBTASK_ID=? and BLOCK_ID=? and TASK_ID=? and GEOMETRY=? and STAGE=? and TYPE=? and CREATE_USER_ID=? and CREATE_DATE=? and EXE_USER_ID=? and STATUS=? and PLAN_START_DATE=? and PLAN_END_DATE=? and START_DATE=? and END_DATE=? and DESCP=?";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				updateSql+=" and SUBTASK_ID=? ";
				values.add(bean.getSubtaskId());
			};
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				updateSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				updateSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				updateSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getStage()!=null && StringUtils.isNotEmpty(bean.getStage().toString())){
				updateSql+=" and STAGE=? ";
				values.add(bean.getStage());
			};
			if (bean!=null&&bean.getType()!=null && StringUtils.isNotEmpty(bean.getType().toString())){
				updateSql+=" and TYPE=? ";
				values.add(bean.getType());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				updateSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				updateSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				updateSql+=" and EXE_USER_ID=? ";
				values.add(bean.getExeUserId());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				updateSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				updateSql+=" and PLAN_START_DATE=? ";
				values.add(bean.getPlanStartDate());
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				updateSql+=" and PLAN_END_DATE=? ";
				values.add(bean.getPlanEndDate());
			};
			if (bean!=null&&bean.getStartDate()!=null && StringUtils.isNotEmpty(bean.getStartDate().toString())){
				updateSql+=" and START_DATE=? ";
				values.add(bean.getStartDate());
			};
			if (bean!=null&&bean.getEndDate()!=null && StringUtils.isNotEmpty(bean.getEndDate().toString())){
				updateSql+=" and END_DATE=? ";
				values.add(bean.getEndDate());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				updateSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			run.update(conn, 
					   updateSql, 
					   bean.getSubtaskId() ,bean.getBlockId(),bean.getTaskId(),bean.getGeometry(),bean.getStage(),bean.getType(),bean.getCreateUserId(),bean.getCreateDate(),bean.getExeUserId(),bean.getStatus(),bean.getPlanStartDate(),bean.getPlanEndDate(),bean.getStartDate(),bean.getEndDate(),bean.getDescp(),
					   values.toArray()
					   );
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
	public Page list(JSONObject json ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			JSONObject obj = JSONObject.fromObject(json);	
			Subtask  bean = (Subtask)JSONObject.toBean(obj, Subtask.class);
			
			String selectSql = "select * from SUBTASK where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				selectSql+=" and SUBTASK_ID=? ";
				values.add(bean.getSubtaskId());
			};
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				selectSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				selectSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				selectSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getStage()!=null && StringUtils.isNotEmpty(bean.getStage().toString())){
				selectSql+=" and STAGE=? ";
				values.add(bean.getStage());
			};
			if (bean!=null&&bean.getType()!=null && StringUtils.isNotEmpty(bean.getType().toString())){
				selectSql+=" and TYPE=? ";
				values.add(bean.getType());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				selectSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				selectSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				selectSql+=" and EXE_USER_ID=? ";
				values.add(bean.getExeUserId());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				selectSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				selectSql+=" and PLAN_START_DATE=? ";
				values.add(bean.getPlanStartDate());
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				selectSql+=" and PLAN_END_DATE=? ";
				values.add(bean.getPlanEndDate());
			};
			if (bean!=null&&bean.getStartDate()!=null && StringUtils.isNotEmpty(bean.getStartDate().toString())){
				selectSql+=" and START_DATE=? ";
				values.add(bean.getStartDate());
			};
			if (bean!=null&&bean.getEndDate()!=null && StringUtils.isNotEmpty(bean.getEndDate().toString())){
				selectSql+=" and END_DATE=? ";
				values.add(bean.getEndDate());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				selectSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			ResultSetHandler rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("exeUserId", rs.getInt("EXE_USER_ID"));
						map.put("status", rs.getInt("STATUS"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
						map.put("startDate", rs.getObject("START_DATE"));
						map.put("endDate", rs.getObject("END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<HashMap> list(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
					
			JSONObject obj = JSONObject.fromObject(json);	
			Subtask  bean = (Subtask)JSONObject.toBean(obj, Subtask.class);	
			String selectSql = "select * from SUBTASK where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				selectSql+=" and SUBTASK_ID=? ";
				values.add(bean.getSubtaskId());
			};
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				selectSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				selectSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				selectSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getStage()!=null && StringUtils.isNotEmpty(bean.getStage().toString())){
				selectSql+=" and STAGE=? ";
				values.add(bean.getStage());
			};
			if (bean!=null&&bean.getType()!=null && StringUtils.isNotEmpty(bean.getType().toString())){
				selectSql+=" and TYPE=? ";
				values.add(bean.getType());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				selectSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				selectSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				selectSql+=" and EXE_USER_ID=? ";
				values.add(bean.getExeUserId());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				selectSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				selectSql+=" and PLAN_START_DATE=? ";
				values.add(bean.getPlanStartDate());
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				selectSql+=" and PLAN_END_DATE=? ";
				values.add(bean.getPlanEndDate());
			};
			if (bean!=null&&bean.getStartDate()!=null && StringUtils.isNotEmpty(bean.getStartDate().toString())){
				selectSql+=" and START_DATE=? ";
				values.add(bean.getStartDate());
			};
			if (bean!=null&&bean.getEndDate()!=null && StringUtils.isNotEmpty(bean.getEndDate().toString())){
				selectSql+=" and END_DATE=? ";
				values.add(bean.getEndDate());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				selectSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("exeUserId", rs.getInt("EXE_USER_ID"));
						map.put("status", rs.getInt("STATUS"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
						map.put("startDate", rs.getObject("START_DATE"));
						map.put("endDate", rs.getObject("END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
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
			
			JSONObject obj = JSONObject.fromObject(json);	
			Subtask  bean = (Subtask)JSONObject.toBean(obj, Subtask.class);	
			
			String selectSql = "select * from SUBTASK where SUBTASK_ID=? and BLOCK_ID=? and TASK_ID=? and GEOMETRY=? and STAGE=? and TYPE=? and CREATE_USER_ID=? and CREATE_DATE=? and EXE_USER_ID=? and STATUS=? and PLAN_START_DATE=? and PLAN_END_DATE=? and START_DATE=? and END_DATE=? and DESCP=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("stage", rs.getInt("STAGE"));
						map.put("type", rs.getInt("TYPE"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("exeUserId", rs.getInt("EXE_USER_ID"));
						map.put("status", rs.getInt("STATUS"));
						map.put("planStartDate", rs.getObject("PLAN_START_DATE"));
						map.put("planEndDate", rs.getObject("PLAN_END_DATE"));
						map.put("startDate", rs.getObject("START_DATE"));
						map.put("endDate", rs.getObject("END_DATE"));
						map.put("descp", rs.getString("DESCP"));
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   bean.getSubtaskId(), bean.getBlockId(), bean.getTaskId(), bean.getGeometry(), bean.getStage(), bean.getType(), bean.getCreateUserId(), bean.getCreateDate(), bean.getExeUserId(), bean.getStatus(), bean.getPlanStartDate(), bean.getPlanEndDate(), bean.getStartDate(), bean.getEndDate(), bean.getDescp());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
