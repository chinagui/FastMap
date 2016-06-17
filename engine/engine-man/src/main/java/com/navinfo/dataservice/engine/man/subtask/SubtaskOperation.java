package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: SubtaskOperation
 * @author songdongyan
 * @date 2016年6月13日
 * @Description: SubtaskOperation.java
 */
public class SubtaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public SubtaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static void updateSubtask(Connection conn,Subtask bean) throws Exception{
		try{
			String baseSql = "update SUBTASK set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";

			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DESCP= " + "'" + bean.getDescp() + "'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_START_DATE= " + "to_date('" + bean.getPlanStartDate().toString().substring(0,10) + "','yyyy-MM-dd HH24:MI:ss')";
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_USER_ID= " + bean.getExeUserId();
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_END_DATE= " + "to_date('" + bean.getPlanEndDate().toString().substring(0,10) + "','yyyy-MM-dd HH24:MI:ss')";
			};
			
			
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				updateSql += " where SUBTASK_ID= " + bean.getSubtaskId();
			};
			
			run.update(conn,baseSql+updateSql);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	//根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
	public static List<Subtask> getSubtaskListByIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String subtaskIds = "(";
			
			subtaskIds += StringUtils.join(subtaskIdList.toArray(),",") + ")";
			
			
			String selectSql = "select m.SUBTASK_ID"
					+ ",listagg(m.GRID_ID, ',') within group(order by m.SUBTASK_ID) as GRID_ID"
					+ ",s.TYPE"
					+ ",s.STAGE"
					+ " from SUBTASK_GRID_MAPPING m"
					+ ", SUBTASK s"
					+ " where s.SUBTASK_ID = m.Subtask_Id"
					+ " and s.SUBTASK_ID in " + subtaskIds
					+ " group by m.SUBTASK_ID"
					+ ", s.TYPE, s.STAGE";
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						String gridIds = rs.getString("GRID_ID");
						String[] gridIdList = gridIds.split(",");
						subtask.setGridIds(gridIdList);
						list.add(subtask);
					}
					return list;
				}
	    		
	    	};
	    	
	    	List<Subtask> subtaskList = run.query(conn, selectSql,rsHandler);
	    	return subtaskList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	//判断采集任务是否可关闭
	public static Boolean isCollectReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<String> gridIds = new ArrayList<String>();
			Collections.addAll(gridIds, subtask.getGridIds());
			List<GridStatInfo> gridStatInfoColArr = staticsApi.getCollectStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(1 > (int)gridStatInfoColArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(1 > (int)gridStatInfoColArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if((1 > (int)gridStatInfoColArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoColArr.get(j).getPercentRoad())){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	//判断日编任务是否可关闭
	public static Boolean isDailyEditReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<String> gridIds = new ArrayList<String>();
			Collections.addAll(gridIds, subtask.getGridIds());
			List<GridStatInfo> gridStatInfoDailyEditArr = staticsApi.getDailyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if((1 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad())){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	//判断月编任务是否可关闭
	public static Boolean isMonthlyEditReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<String> gridIds = new ArrayList<String>();
			Collections.addAll(gridIds, subtask.getGridIds());
			List<GridStatInfo> gridStatInfoMonthlyEditArr = staticsApi.getMonthlyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if((1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad())){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static void closeBySubtaskList(Connection conn,List<Integer> closedSubtaskList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String closedSubtaskStr = "(";
			
			closedSubtaskStr += StringUtils.join(closedSubtaskList.toArray(),",") + ")";
			
			closedSubtaskStr += ")";
						
			String updateSql = "update SUBTASK "
					+ "set STATUS=0 "
					+ "where SUBTASK_ID in"
					+ closedSubtaskStr;	
			

			run.update(conn,updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
}
