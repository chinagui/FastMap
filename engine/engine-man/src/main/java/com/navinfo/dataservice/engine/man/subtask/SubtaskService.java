package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.lang.StringUtils;


/** 
* @ClassName:  SubtaskService 
* @author code generator
* @date 2016-06-06 07:40:14 
* @Description: TODO
*/
@Service
public class SubtaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	/*
	 * 创建一个子任务。
	 * 参数1：Subtask对象
	 * 参数2：ArrayList<Integer>，组成Subtask的gridId列表
	 */
	public void create(Subtask bean,ArrayList<Integer> gridIds)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			//获取subtaskId
			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";
			
			int subTaskId = Integer.valueOf(run.query(conn,querySql,new MapHandler()).get("subTaskId").toString()); 
			//插入subtask
			String createSql = "insert into SUBTASK "
					+ "(SUBTASK_ID"
					+ ", NAME"
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
					+ "," + bean.getName()
					+ "," + bean.getBlockId()
					+ "," + bean.getTaskId()
					+ "," + "sdo_geometry(" +  "'" + bean.getGeometry() + "',8307)" 
					+ "," + bean.getStage()
					+ "," + bean.getType()
					+ "," + bean.getCreateUserId() 
					+ "," + bean.getExeUserId()
					+ ", sysdate"
					+ ","+  "1"
					+ ",to_date('" + bean.getPlanStartDate() + "','yyyymmdd')" 
					+ ",to_date('" + bean.getPlanEndDate() + "','yyyymmdd')"
					+ ",'"+ bean.getDescp()+"')";			
			
			run.update(conn,createSql);
			//插入SUBTASK_GRID_MAPPING
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
	
	/*
	 * 根据几何范围,任务类型，作业阶段查询任务列表
	 * 参数1：几何范围，String wkt
	 * 参数1：任务类型，ArrayList<Integer> types
	 * 参数1：作业阶段，int stage
	 */
	public List<Subtask> listByWkt(String wkt,ArrayList<Integer> types, int stage)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	

			String type = types.toString();
			type = type.replace("[", "(");
			type = type.replace("]", ")");
			
			String querySql = "select "
					+ "s.subtask_id"
					+ ", TO_CHAR(s.geometry.get_wkt()) as geometry"
					+ ",s.descp "
					+ "from subtask s "
					+ "where type in" + type 
					+ " and stage =" + stage 
					+ " and SDO_GEOM.RELATE(geometry, 'ANYINTERACT', " + "sdo_geometry(" +  "'" + wkt + "',8307)" + ", 0.000005) ='TRUE'";
		
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setGeometry(rs.getString("GEOMETRY"));
						subtask.setDescp(rs.getString("DESCP"));
						list.add(subtask);
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
	
	/*
	 * 批量修改子任务详细信息。
	 * 参数：Subtask对象列表
	 */
	public void update(List<Subtask> subtaskList)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			for (int i = 0; i < subtaskList.size(); i++) {
				SubtaskOperation.updateSubtask(conn, subtaskList.get(i));				
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	
	public List<Subtask> list(Subtask bean,List<String> sortby,long pageSize,long curPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select distinct s.SUBTASK_ID"
					+ ",s.STAGE"
					+ ",s.TYPE"
					+ ",s.PLAN_START_DATE"
					+ ",s.PLAN_END_DATE"
					+ ",s.DESCP"
					+ ",TO_CHAR(s.GEOMETRY.get_wkt()) AS GEOMETRY "
					+ ",(case when s.block_id is not null and s.block_id = b.block_id then b.block_id else -1 end) AS block_id"
					+ ",(case when s.block_id is not null and s.block_id = b.block_id then b.block_name else null end) AS block_name"
					+ ",(case when s.task_id is not null and s.task_id = t.task_id then t.task_id else -1 end) AS task_id"
					+ ",(case when s.task_id is not null and s.task_id = t.task_id then t.descp else null end) AS task_descp";
			//0采集，1日编，2月编，
			if(0 == bean.getStage()){
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.COLLECT_PLAN_START_DATE else null end) AS COLLECT_PLAN_START_DATE_b";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.COLLECT_PLAN_END_DATE else null end) AS COLLECT_PLAN_END_DATE_b";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.COLLECT_PLAN_START_DATE else null end) AS COLLECT_PLAN_START_DATE_t";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.COLLECT_PLAN_END_DATE else null end) AS COLLECT_PLAN_END_DATE_t";
			}else if(1 == bean.getStage()){
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.DAY_EDIT_PLAN_START_DATE else null end) AS DAY_EDIT_PLAN_START_DATE_b";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.DAY_EDIT_PLAN_END_DATE else null end) AS DAY_EDIT_PLAN_END_DATE_b";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.DAY_EDIT_PLAN_START_DATE else null end) AS DAY_EDIT_PLAN_START_DATE_t";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.DAY_EDIT_PLAN_END_DATE else null end) AS DAY_EDIT_PLAN_END_DATE_t";
			}else if(2 == bean.getStage()){
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.MONTH_EDIT_PLAN_START_DATE else null end) AS MONTH_EDIT_PLAN_START_DATE_b";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.MONTH_EDIT_PLAN_END_DATE else null end) AS MONTH_EDIT_PLAN_END_DATE_b";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.C_MONTH_EDIT_PLAN_START_DATE else null end) AS C_MONTH_EDIT_PLAN_START_DATE_t";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.C_MONTH_EDIT_PLAN_END_DATE else null end) AS C_MONTH_EDIT_PLAN_END_DATE_t";	
			}
			
			selectSql =  selectSql + " from SUBTASK s, Task t, Block b, Block_man bm "
					+ " where (s.block_id=b.block_id or s.task_id=t.task_id)"
					+ " and b.block_id = bm.block_id";
			//筛选条件
			if(bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				selectSql += " and block_id = " + bean.getBlockId();
			}
			if(bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				selectSql += " and subtask_id = " + bean.getSubtaskId();
			}
			if(bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				selectSql += " and EXE_USER_ID = " + bean.getExeUserId();
			}
			//排序
			selectSql += " order by block_id";
			if(!sortby.isEmpty()){
				selectSql += ",";
				for(int i = 0;i<sortby.size();i++){
					selectSql += sortby.get(i);
					if(i<(sortby.size()-1)){
						selectSql += ",";
					}
				}
			}
	
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){

				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setGeometry(rs.getString("GEOMETRY"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));	
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						//与block关联，返回block信息。
						if(rs.getInt("block_id") > 0){
							//block
							Block block  = new Block();
							block.setBlockId(rs.getInt("block_id"));
							block.setBlockName(rs.getString("block_name"));
							subtask.setBlock(block);
							//blockMan
							BlockMan blockMan = new BlockMan();
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_b") > 0){
									blockMan.setCollectPlanStartDate(DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_START_DATE_b")));
									blockMan.setCollectPlanStartDate(DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_END_DATE_b")));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_b") > 0){
									blockMan.setDayEditPlanStartDate(DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE_b")));
									blockMan.setDayEditPlanStartDate(DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE_b")));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_b") > 0){
									blockMan.setMonthEditPlanStartDate(DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE_b")));
									blockMan.setMonthEditPlanStartDate(DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE_b")));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							subtask.setBlockMan(blockMan);
						}
						
						//与task关联，返回block信息。
						if(rs.getInt("task_id") > 0){
							//task
							Task task = new Task();
							task.setTaskId(rs.getInt("task_id"));
							task.setDescp(rs.getString("task_descp"));
							
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_t") > 0){
									task.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_START_DATE_t"));
									task.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_END_DATE_t"));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_t") > 0){
									task.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE_t"));
									task.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE_t"));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							try{
								if(rs.findColumn("COLLECT_PLAN_START_DATE_t") > 0){
									task.setCMonthEditPlanStartDate(rs.getTimestamp("C_MONTH_EDIT_PLAN_START_DATE_t"));
									task.setCMonthEditPlanStartDate(rs.getTimestamp("C_MONTH_EDIT_PLAN_END_DATE_t"));
								}
							}
							catch (SQLException e) {
						        int a = 1;
						    }
							
							subtask.setTask(task);
							
						}
						
						list.add(subtask);
					}

					return list;
				}
	    		
	    	}	;
	    	
	    	return run.query(curPageNum,pageSize,conn, selectSql, rsHandler);

	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	
	
	public List<Subtask> listByUser(Subtask bean,int snapshot,final int currentPageNum,final int pageSize)throws ServiceException{
		Connection conn = null;
		try{
			
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	

			String selectSql = "select st.SUBTASK_ID "
					+ ",st.NAME"
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
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";
			
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
			
			if(bean.getStage()!= null){
				conditionSql = conditionSql + " and st.STAGE = " + bean.getStage();
			}
			
			if(bean.getType()!= null){
				conditionSql = conditionSql + " and st.TYPE = " + bean.getType();
			}
			
			if(bean.getStatus()!= null){
				conditionSql = conditionSql + " and st.STATUS = " + bean.getStatus();
			}
			
			if(0 == snapshot){
				selectSql = selectSql + cellsExtra + fromSql + fromExtra + conditionSql + conditionExtra + groupBySql + groupByExtra;
			}else{
				selectSql = selectSql + fromSql + conditionSql + groupBySql;
			}
					
			ResultSetHandler<List<Subtask>> rsHandler =  new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));	
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						
						try{
							if(rs.findColumn("GEOMETRY") > 0){
								subtask.setGeometry(rs.getString("GEOMETRY"));
							}
							if(rs.findColumn("GRID_ID") > 0){
								String gridIds = rs.getString("GRID_ID");
								String[] gridIdList = gridIds.split(",");
								subtask.setGridIds(gridIdList);
							}
						}
						catch (SQLException e) {
					        int a = 1;
					    }
						
						if(1 == rs.getInt("STAGE")){
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						}else if(2 == rs.getInt("STAGE")){
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						}
						list.add(subtask);
						
					}
					return list;
				}
	    		
	    	};

	    	return run.query(pageSize,currentPageNum,conn, selectSql, rsHandler);


		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/*
	 * 根据subtaskId查询一个任务的详细信息。
	 * 参数为Subtask对象
	 */
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
						subtask.setGeometry(rs.getString("GEOMETRY"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));	
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
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
	
	/*
	 * 关闭多个子任务。
	 * 参数：Subtask对象列表，List<Subtask>
	 */
	public void close(List<Subtask> subtaskArray)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	

			String subtaskStr = "(";
			
			for(int i =0; i<subtaskArray.size(); i++){
				subtaskStr += subtaskArray.get(i).getSubtaskId();
				if(i < (subtaskArray.size()- 1)){
					subtaskStr += ",";
				}
			}
			
			subtaskStr += ")";
						
			String updateSql = "update SUBTASK "
					+ "set STATUS=0 "
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
