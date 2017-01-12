package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

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
	
	
	/**
	 * @Title: updateSubtask
	 * @Description: 修改子任务(修)(第七迭代)
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月7日 下午2:21:21 
	 */
	public static void updateSubtask(Connection conn,Subtask bean) throws Exception{
		try{
			String baseSql = "update SUBTASK set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			List<Object> value = new ArrayList<Object>();
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " NAME= " + "'" + bean.getName() + "'";
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DESCP= " + "'" + bean.getDescp() + "'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_START_DATE= " + "to_timestamp('" + bean.getPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_USER_ID= " + bean.getExeUserId();
			};
			if (bean!=null&&bean.getExeGroupId()!=null && StringUtils.isNotEmpty(bean.getExeGroupId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_GROUP_ID= " + bean.getExeGroupId();
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_END_DATE= " + "to_timestamp('" + bean.getPlanEndDate()+ "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " STATUS= " + bean.getStatus();
			};
			//修改新增的两个字段
			if (bean!=null&&bean.getQualitySubtaskId()!=null && StringUtils.isNotEmpty(bean.getQualitySubtaskId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " QUALITY_SUBTASK_ID= " + bean.getQualitySubtaskId();
			};
			if (bean!=null&&bean.getIsQuality()!=null && StringUtils.isNotEmpty(bean.getIsQuality().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " IS_QUALITY= " + bean.getIsQuality();
			};
			if (bean!=null&&bean.getReferId()!=null && StringUtils.isNotEmpty(bean.getReferId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " REFER_ID= " + bean.getReferId();
			};
			
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" GEOMETRY=? ";
				value.add(GeoTranslator.wkt2Struct(conn,bean.getGeometry()));
			};	
			if(bean.getGridIds() != null){
				//前端传入grids修改，需要重新更新子任务的grid
				SubtaskOperation.deleteSubtaskGridMapping(conn, bean.getSubtaskId());
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			}

			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				updateSql += " where SUBTASK_ID= " + bean.getSubtaskId();
			};
			if(value.isEmpty() || value.size()==0){
				run.update(conn,baseSql+updateSql);}
			else{
				Object[] valueObjects=new Object[value.size()];
				int i=0;
				for(Object tmp:value){
					valueObjects[i]=tmp;
					i++;
				}
				run.update(conn,baseSql+updateSql,valueObjects);}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	//根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
	public static List<Subtask> getSubtaskListBySubtaskIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String subtaskIds = "(" + StringUtils.join(subtaskIdList.toArray(),",") + ")";
			
			
			String selectSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.EXE_USER_ID,S.EXE_GROUP_ID,S.STATUS,S.TASK_ID"
					+ " FROM SUBTASK S"
					+ " WHERE S.SUBTASK_ID IN " + subtaskIds;
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setExeUserId(rs.getInt("EXE_USER_ID"));
						subtask.setExeGroupId(rs.getInt("EXE_GROUP_ID"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setTaskId(rs.getInt("TASK_ID"));
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
	
//	//根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
//	public static List<Subtask> getSubtaskListByIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			String subtaskIds = "(";
//			
//			subtaskIds += StringUtils.join(subtaskIdList.toArray(),",") + ")";
//			
//			
//			String selectSql = "select m.SUBTASK_ID"
//					+ ",listagg(m.GRID_ID, ',') within group(order by m.SUBTASK_ID) as GRID_ID"
//					+ ",s.TYPE"
//					+ ",s.STAGE"
//					+ " from SUBTASK_GRID_MAPPING m"
//					+ ", SUBTASK s"
//					+ " where s.SUBTASK_ID = m.Subtask_Id"
//					+ " and s.SUBTASK_ID in " + subtaskIds
//					+ " group by m.SUBTASK_ID"
//					+ ", s.TYPE, s.STAGE";
//			
//			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
//				public List<Subtask> handle(ResultSet rs) throws SQLException {
//					List<Subtask> list = new ArrayList<Subtask>();
//					while(rs.next()){
//						Subtask subtask = new Subtask();
//						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
//						subtask.setStage(rs.getInt("STAGE"));
//						subtask.setType(rs.getInt("TYPE"));
//						String gridIds = rs.getString("GRID_ID");
//
//						String[] gridIdList = gridIds.split(",");
//						subtask.setGridIds(ArrayUtil.convertList(Arrays.asList(gridIdList)));
//						list.add(subtask);
//					}
//					return list;
//				}
//	    		
//	    	};
//	    	
//	    	List<Subtask> subtaskList = run.query(conn, selectSql,rsHandler);
//	    	return subtaskList;
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
	
	//判断采集任务是否可关闭
	public static Boolean isCollectReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoColArr = staticsApi.getLatestCollectStatByGrids(gridIdList);
			//0POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(100 > (int)gridStatInfoColArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//1道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(100 > (int)gridStatInfoColArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//2一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if((100 > (int)gridStatInfoColArr.get(j).getPercentPoi()) 
							|| 
							(100 > (int)gridStatInfoColArr.get(j).getPercentRoad())){
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
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoDailyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIdList);
			//0POI,5多源POI
			if(0==subtask.getType()||5==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//3一体化_grid粗编，4一体化_区域粗编
			else if(3==subtask.getType()||4==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if((100 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()) 
							|| 
							(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad())){
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
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoMonthlyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIdList);
			//6代理店， 7POI专项
			if(6==subtask.getType()||7==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//8道路_grid精编，9道路_grid粗编，10道路区域专项
			else if(8==subtask.getType()||9==subtask.getType()||10==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad()){
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
			//TYPE!=4 非区域子任务，直接关闭；type=4区域子任务，判断这个区域子任务范围内的所有一体化_grid粗编子任务均关闭		
			String updateSql = "update SUBTASK S "
					+ "set S.STATUS=0 "
					+ "where S.SUBTASK_ID in "
					+ closedSubtaskStr 
					+ " AND (S.TYPE!=4 OR (S.TYPE=4 AND NOT EXISTS (SELECT 1"
					+ "          FROM SUBTASK              SS,"
					+ "               SUBTASK_GRID_MAPPING MM,"
					+ "               BLOCK_GRID_MAPPING   M,"
					+ "               BLOCK_MAN            B"
					+ "         WHERE SS.SUBTASK_ID = MM.SUBTASK_ID"
					+ "           AND MM.GRID_ID = M.GRID_ID"
					+ "           AND S.BLOCK_MAN_ID = B.BLOCK_MAN_ID"
					+ "           AND B.BLOCK_ID = M.BLOCK_ID"
					+ "           AND SS.STATUS != 0"
					+ "           AND SS.TYPE = 3)))";	
			run.update(conn,updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param bean
	 * @return
	 * @throws Exception 
	 */
	public static int getSubtaskId(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";

			int subTaskId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("subTaskId")
					.toString());
			return subTaskId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public static void insertSubtask(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			String column = "";
			String values = "";
			List<Object> value = new ArrayList<Object>();
			if (bean!=null&&bean.getSubtaskId()!=null && bean.getSubtaskId()!=0 && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" SUBTASK_ID ";
				values+=" ? ";
				value.add(bean.getSubtaskId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" NAME ";
				values+=" ? ";
				value.add(bean.getName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" GEOMETRY ";
				values+=" ? ";
				value.add(GeoTranslator.wkt2Struct(conn,bean.getGeometry()));
			};
			if (bean!=null&&bean.getStage()!=null && bean.getStage()!=0 
					&& StringUtils.isNotEmpty(bean.getStage().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" STAGE ";
				values+=" ? ";
				value.add(bean.getStage());
			};
			if (bean!=null&&bean.getType()!=null && bean.getType()!=0 
					&& StringUtils.isNotEmpty(bean.getType().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" TYPE ";
				values+=" ? ";
				value.add(bean.getType());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && bean.getCreateUserId()!=0 
					&& StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" CREATE_USER_ID ";
				values+=" ? ";
				value.add(bean.getCreateUserId());
			};
			
			if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
			column+=" CREATE_DATE ";
			values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
			value.add(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10));
			
			if (bean!=null&&bean.getStatus()!=null && bean.getStatus()!=0 
					&& StringUtils.isNotEmpty(bean.getStatus().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" STATUS ";
				values+=" ? ";
				value.add(bean.getStatus());
			};
			
			if (bean!=null&&bean.getPlanStartDate()!=null
					&& StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" PLAN_START_DATE ";
				values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
				value.add(bean.getPlanStartDate().toString().substring(0, 10));
			};
			if (bean!=null&&bean.getPlanEndDate()!=null
					&& StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" PLAN_END_DATE ";
				values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
				value.add(bean.getPlanEndDate().toString().substring(0, 10));
			};			
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" DESCP ";
				values+=" ? ";
				value.add(bean.getDescp());
			};
			if (bean!=null&&bean.getBlockManId()!=null && bean.getBlockManId()!=0 
					&& StringUtils.isNotEmpty(bean.getBlockManId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" BLOCK_MAN_ID ";
				values+=" ? ";
				value.add(bean.getBlockManId());
			};
			if (bean!=null&&bean.getTaskId()!=null && bean.getTaskId()!=0 
					&& StringUtils.isNotEmpty(bean.getTaskId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" TASK_ID ";
				values+=" ? ";
				value.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getQualitySubtaskId()!=null && bean.getQualitySubtaskId()!=0 
					&& StringUtils.isNotEmpty(bean.getQualitySubtaskId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" QUALITY_SUBTASK_ID ";
				values+=" ? ";
				value.add(bean.getQualitySubtaskId());
			};
			if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
			column+=" IS_QUALITY ";
			values+=" ? ";
			if (bean!=null&&bean.getIsQuality()!=null && bean.getIsQuality()!=0 
					&& StringUtils.isNotEmpty(bean.getIsQuality().toString())){
				value.add(bean.getIsQuality());
			}else{value.add(0);}
			//外业参考任务圈
			if (bean!=null&&bean.getReferId()!=null && bean.getReferId()!=0 
					&& StringUtils.isNotEmpty(bean.getReferId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" REFER_ID ";
				values+=" ? ";
				value.add(bean.getReferId());
			}
			if (bean!=null&&bean.getExeGroupId()!=null && bean.getExeGroupId()!=0 
					&& StringUtils.isNotEmpty(bean.getExeGroupId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" EXE_GROUP_ID ";
				values+=" ? ";
				value.add(bean.getExeGroupId());
			};
			if (bean!=null&&bean.getExeUserId()!=null && bean.getExeUserId()!=0 
					&& StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" EXE_USER_ID ";
				values+=" ? ";
				value.add(bean.getExeUserId());
			};
			
			String createSql ="insert into subtask ("+ column+") values("+values+")";
			run.update(conn, createSql,value.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public static void deleteSubtaskGridMapping(Connection conn, int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String createMappingSql = "delete from sUBTASK_GRID_MAPPING where SUBTASK_ID=?";
			Object[] temp = new Object[1];
			temp[0] = subtaskId;
			run.update(conn, createMappingSql, temp);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public static void insertSubtaskGridMapping(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "insert into SUBTASK_GRID_MAPPING (SUBTASK_ID, GRID_ID) VALUES (?,?)";

			List<Integer> gridIds = bean.getGridIds();
			Object[][] inParam = new Object[gridIds.size()][];
			for (int i = 0; i < inParam.length; i++) {
				Object[] temp = new Object[2];
				temp[0] = bean.getSubtaskId();
				temp[1] = gridIds.get(i);
				inParam[i] = temp;

			}

			run.batch(conn, createMappingSql, inParam);

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserSnapshotPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select st.SUBTASK_ID ,st.NAME,st.geometry"
					+ ",st.DESCP,st.PLAN_START_DATE,st.PLAN_END_DATE"
					+ ",st.STAGE,st.TYPE,st.STATUS"
					+ ",r.DAILY_DB_ID,r.MONTHLY_DB_ID";

			String fromSql_task = " from subtask st,task t,city c,region r";

			String fromSql_block = " from subtask st,block_man bm,block b,region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
					+ "and b.region_id = r.region_id "
					+ "and bm.block_id = b.block_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			if (bean.getStage() != null) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
			}else{
				if(0 == platForm){
					//采集端
					conditionSql_task = conditionSql_task + " and st.STAGE in (0) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (0) ";
				}else if(1 == platForm){
					//编辑端
					conditionSql_task = conditionSql_task + " and st.STAGE in (1,2) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (1,2) ";
				}
			}

			if (bean.getType() != null) {
				conditionSql_task = conditionSql_task + " and st.TYPE = "
						+ bean.getType();
				conditionSql_block = conditionSql_block + " and st.TYPE = "
						+ bean.getType();
			}

			if (bean.getStatus() != null) {
				conditionSql_task = conditionSql_task + " and st.STATUS = "
						+ bean.getStatus();
				conditionSql_block = conditionSql_block + " and st.STATUS = "
						+ bean.getStatus();
			}


			selectSql = selectSql + fromSql_task + conditionSql_task
						+ " union all " + selectSql
						+ fromSql_block + conditionSql_block;
			
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						log.debug("start subtask");
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("name", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						//版本信息
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						
						if (1 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						}

						//日编POI,日编一体化GRID粗编完成度，任务量信息
						if((1==rs.getInt("STAGE")&&0==rs.getInt("TYPE"))||(1==rs.getInt("STAGE")&&3==rs.getInt("TYPE"))){
							try {
								STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
								String wkt="";
								try {
									wkt=GeoTranslator.struct2Wkt(struct);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								//log.info("get gridIds");
								//List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
								//Map<String,Integer> subtaskStat = subtaskStatRealtime((
								//List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
								log.debug("get stat");
								Map<String,Integer> subtaskStat = subtaskStatRealtime((int)subtask.get("dbId"),rs.getInt("TYPE"),wkt);
								if(subtaskStat != null){
									if(subtaskStat.containsKey("poiFinish")){
										subtask.put("poiFinish",subtaskStat.get("poiFinish"));
										subtask.put("poiTotal",subtaskStat.get("poiTotal"));
									}
									if(subtaskStat.containsKey("tipsFinish")){
										subtask.put("tipsFinish",subtaskStat.get("tipsFinish"));
										subtask.put("tipsTotal",subtaskStat.get("tipsTotal"));
									}
								}else{
									subtask.put("poiFinish",0);
									subtask.put("poiTotal",0);
									subtask.put("tipsFinish",0);
									subtask.put("tipsTotal",0);
								}
								log.info("end stat");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						list.add(subtask);
						log.debug("end subtask");
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}

			};
			log.info("getListByUserSnapshotPage-sql:"+selectSql);
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param <FccApi>
	 * @param dbId
	 * @param type 
	 * @param gridIds
	 * @return
	 * @throws ServiceException 
	 */
	protected static Map<String, Integer> subtaskStatRealtime(Integer dbId, int type, String wkt) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
//			if(gridIds.isEmpty()){
//				return null;
//			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			Map<String, Integer> stat = new HashMap<String, Integer>();
			
//			JSONArray gridIdsJsonArray = JSONArray.fromObject(gridIds);
//			
//			String wkt = GridUtils.grids2Wkt(gridIdsJsonArray);
			log.debug("get poi stat");
			//查询POI总量
			QueryRunner run = new QueryRunner();
			String sql = "select pes.status, count(1) finishNum"
					+ " from ix_poi ip, poi_edit_status pes"
					+ " where ip.pid = pes.pid"
					//+ " and pes.status = 1"
					+ " AND sdo_within_distance(ip.geometry, sdo_geometry('"+ wkt + "', 8307), 'mask=anyinteract') = 'TRUE' "
							+ "group by pes.status ";
			//POI待作业
			stat = run.query(conn, sql,new ResultSetHandler<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> stat = new HashMap<String, Integer>();
					int unfinish = 0;
					int total=0;
					while(rs.next()){
						int status=rs.getInt("status");
						if(status==1){unfinish = rs.getInt("finishNum");}
						total+=rs.getInt("finishNum");
					}
					stat.put("poiFinish", total-unfinish);
					stat.put("poiTotal", total);
					return stat;
				}
			}
			);
//			String sql_unfinish = "select count(1) unfinish"
//					+ " from ix_poi ip, poi_edit_status pes"
//					+ " where ip.pid = pes.pid"
//					+ " and pes.status = 1"
//					+ " AND sdo_within_distance(ip.geometry, sdo_geometry('"+ wkt + "', 8307), 'mask=anyinteract') = 'TRUE' ";			
//			//POI待作业
//			Integer unfinishPOI = run.query(conn, sql_unfinish, new ResultSetHandler<Integer>() {
//				@Override
//				public Integer handle(ResultSet rs) throws SQLException {
//					int unfinish = 0;
//					if(rs.next()){
//						unfinish = rs.getInt("unfinish");
//					}
//					return unfinish;
//				}
//			}
//			);
//			log.info("get poi stat2");
//			
//			String sql_total = "select count(1) toal"
//					+ " from ix_poi ip, poi_edit_status pes"
//					+ " where ip.pid = pes.pid"
//					+ " AND sdo_within_distance(ip.geometry, sdo_geometry('"+ wkt + "', 8307), 'mask=anyinteract') = 'TRUE' ";			
//			//poi总量
//			Integer totalPOI = run.query(conn, sql_total, new ResultSetHandler<Integer>() {
//				@Override
//				public Integer handle(ResultSet rs) throws SQLException {
//					int toal = 0;
//					if(rs.next()){
//						toal = rs.getInt("toal");
//					}
//					return toal;
//				}
//			}
//			);
			//int percent = 0;
			//int percentPOI = 0;
			//int percentRoad = 0; 
			//poi数量及完成度
//			stat.put("poiFinish", totalPOI-unfinishPOI);
//			stat.put("poiTotal", totalPOI);
			/*if(0 != unfinishPOI){
				percentPOI = (totalPOI-unfinishPOI)*100/totalPOI;
			}else{
				percentPOI = 100;
			}*/
			//type=3,一体化grid粗编子任务。增加道路数量及完成度
			log.debug("get tips stat");
			if(3 == type){
				FccApi api=(FccApi) ApplicationContextUtil.getBean("fccApi");
				JSONObject resultRoad = api.getSubTaskStatsByWkt(wkt);
				int tips = resultRoad.getInt("total") + resultRoad.getInt("finished");
				stat.put("tipsFinish", resultRoad.getInt("finished"));
				stat.put("tipsTotal", tips);	
				/*if(0 != tips){
					percentRoad = resultRoad.getInt("finished")*100/tips;
				}else{
					percentRoad = 100;
				}*/
			}
			/*percent = (int) (percentPOI*0.5 + percentRoad*0.5);
			stat.put("percent", percent);*/
			return stat;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT *"
					+ "  FROM (SELECT ST.SUBTASK_ID,"
					+ "               ST.NAME,"
					+ "               ST.DESCP,"
					+ "               ST.PLAN_START_DATE,"
					+ "               ST.PLAN_END_DATE,"
					+ "               ST.STAGE,"
					+ "               ST.TYPE,"
					+ "               ST.STATUS,"
					+ "               ST.GEOMETRY,"
					+ "               ST.EXE_USER_ID,"
					+ "               ST.EXE_GROUP_ID,"
					+ "               R.DAILY_DB_ID,"
					+ "               R.MONTHLY_DB_ID,"
					+ "               RR.GEOMETRY REFER_GEOMETRY"
					+ "          FROM SUBTASK ST, TASK T, CITY C, REGION R, SUBTASK_REFER RR"
					+ "         WHERE ST.TASK_ID = T.TASK_ID"
					+ "           AND T.CITY_ID = C.CITY_ID"
					+ "           AND C.REGION_ID = R.REGION_ID"
					+ "           AND ST.REFER_ID = RR.ID(+)"
					+ "        UNION ALL"
					+ "        SELECT ST.SUBTASK_ID,"
					+ "               ST.NAME,"
					+ "               ST.DESCP,"
					+ "               ST.PLAN_START_DATE,"
					+ "               ST.PLAN_END_DATE,"
					+ "               ST.STAGE,"
					+ "               ST.TYPE,"
					+ "               ST.STATUS,"
					+ "               ST.GEOMETRY,"
					+ "               ST.EXE_USER_ID,"
					+ "               ST.EXE_GROUP_ID,"
					+ "               R.DAILY_DB_ID,"
					+ "               R.MONTHLY_DB_ID,"
					+ "               RR.GEOMETRY REFER_GEOMETRY"
					+ "          FROM SUBTASK ST, BLOCK_MAN BM, BLOCK B, REGION R, SUBTASK_REFER RR"
					+ "         WHERE ST.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
					+ "           AND BM.BLOCK_ID = B.BLOCK_ID"
					+ "           AND B.REGION_ID = R.REGION_ID"
					+ "           AND ST.REFER_ID = RR.ID(+)) T"
					+ " WHERE 1 = 1";
			String conditonSql=" AND (T.EXE_USER_ID = " + bean.getExeUserId() 
					+ " or T.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
			if (bean.getStage() != null) {
				conditonSql+=" and T.STAGE = "+ bean.getStage();
			}else{
				if(0 == platForm){//采集端
					conditonSql+=" and T.STAGE = 0";
				}else if(1 == platForm){//编辑端
					conditonSql+=" and T.STAGE in (1,2) ";
				}
			}

			if (bean.getType() != null) {
				conditonSql+=" and t.TYPE = "+ bean.getType();
			}

			if (bean.getStatus() != null) {
				conditonSql+=" and t.STATUS = "+ bean.getStatus();
			}else{
				if(0 == platForm){//采集端
					conditonSql+=" and t.STATUS in (0,1)";
				}
			}
			selectSql+=conditonSql;
			selectSql+=conditonSql+" order by subtask_id desc";
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("name", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						
						//版本信息
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						
						
						
						List<Integer> gridIds = null;
						try {
							gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						subtask.put("gridIds", gridIds);
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						String wkt="";
						try {
							wkt=GeoTranslator.struct2Wkt(struct);
							subtask.put("geometry", wkt);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						if (1 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else if (0 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
							STRUCT structRefer = (STRUCT) rs.getObject("REFER_GEOMETRY");
							try {
								subtask.put("referGeometry", GeoTranslator.struct2Wkt(structRefer));
							} catch (Exception e1) {
								e1.printStackTrace();
								subtask.put("referGeometry", subtask.get("geometry"));
							}
							JSONArray referGrid;
							try {
								referGrid = SubtaskOperation.getReferSubtasksByGridIds(rs.getInt("SUBTASK_ID"),gridIds,GeoTranslator.wkt2Geometry((String) subtask.get("referGeometry")));
								subtask.put("referGrid", referGrid);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						}else {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));				
						}
						
						//日编POI,日编一体化GRID粗编完成度，任务量信息
						if((1==rs.getInt("STAGE")&&0==rs.getInt("TYPE"))||(1==rs.getInt("STAGE")&&3==rs.getInt("TYPE"))){
							try {
								Map<String,Integer> subtaskStat = subtaskStatRealtime((int)subtask.get("dbId"),rs.getInt("TYPE"),wkt);
								if(subtaskStat != null){
									if(subtaskStat.containsKey("poiFinish")){
										subtask.put("poiFinish",subtaskStat.get("poiFinish"));
										subtask.put("poiTotal",subtaskStat.get("poiTotal"));
									}
									if(subtaskStat.containsKey("tipsFinish")){
										subtask.put("tipsFinish",subtaskStat.get("tipsFinish"));
										subtask.put("tipsTotal",subtaskStat.get("tipsTotal"));
									}
								}else{
									subtask.put("poiFinish",0);
									subtask.put("poiTotal",0);
									subtask.put("tipsFinish",0);
									subtask.put("tipsTotal",0);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}		
						list.add(subtask);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
			};
			log.info("getListByUserPage-selectSql"+selectSql);
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}

	}


	/**
	 * @param int1
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return getGridIdsBySubtaskIdWithConn(conn, subtaskId);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	
	/**
	 * @param int1
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getGridIdsBySubtaskIdWithConn(Connection conn,int subtaskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
//			String selectSql = "select sgm.grid_id from subtask_grid_mapping sgm where sgm.subtask_id = " + subtaskId;
			String selectSql = "select sgm.grid_id"
					+ " from subtask s, subtask_grid_mapping sgm"
					+ " where sgm.subtask_id = s.subtask_id"
					+ " and s.type in (0, 1, 2, 3,4, 8, 9)"
					+ " and s.subtask_id = " + subtaskId
					+ " union all "
					+ " select bgm.grid_id"
					+ " from subtask s, block b, block_man bm, task t, block_grid_mapping bgm"
					+ " where s.block_man_id = bm.block_man_id"
					+ " and bm.block_id = b.block_id"
					+ " and bm.task_id = t.task_id"
					+ " and t.task_type = 1"
					+ " and b.block_id = bgm.block_id"
					+ " and s.type in (4, 5)"
					+ " and s.subtask_id = " + subtaskId
					+ " union all "
					+ " select g.grid_id"
					+ " from subtask s, task t, grid g"
					+ " where s.task_id = t.task_id"
					+ " and t.city_id = g.city_id"
					+ " and s.type in (6, 7, 10)"
					+ " and s.subtask_id = " + subtaskId;

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> gridIds= new ArrayList<Integer>(); 
					while (rs.next()) {
						gridIds.add(rs.getInt("grid_id"));
					}
					return gridIds;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}

	}
	
	/**
	 * 根据gridId获取子任务列表
	 * @param int1
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getSubtaskIdsByGridIdWithConn(Connection conn,int gridId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
//			String selectSql = "select sgm.grid_id from subtask_grid_mapping sgm where sgm.subtask_id = " + subtaskId;
			String selectSql = "select s.subtask_id"
					+ " from subtask s, subtask_grid_mapping sgm"
					+ " where sgm.subtask_id = s.subtask_id"
					+ " and s.type in (0, 1, 2, 3,4, 8, 9)"
					+ " and sgm.grid_id = " + gridId
					+ " union"
					+ " select s.subtask_id"
					+ " from subtask s, block b, block_man bm, task t, block_grid_mapping bgm"
					+ " where s.block_man_id = bm.block_man_id"
					+ " and bm.block_id = b.block_id"
					+ " and bm.task_id = t.task_id"
					+ " and t.task_type = 1"
					+ " and b.block_id = bgm.block_id"
					+ " and s.type in (4, 5)"
					+ " and bgm.grid_id = " + gridId
					+ " union"
					+ " select s.subtask_id"
					+ " from subtask s, task t, grid g"
					+ " where s.task_id = t.task_id"
					+ " and t.city_id = g.city_id"
					+ " and s.type in (6, 7, 10)"
					+ " and g.grid_id = " + gridId;

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> subtaskIds= new ArrayList<Integer>(); 
					while (rs.next()) {
						subtaskIds.add(rs.getInt("subtask_id"));
					}
					return subtaskIds;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}

	}
	
//
//	/**
//	 * @param createUserId
//	 * @param exeUserId 
//	 * @param name
//	 * @return
//	 * @throws Exception 
//	 */
//	public static Object pushMessage(Integer createUserId, Integer exeUserId, String subtaskName) throws Exception {
//		// TODO Auto-generated method stub
//		try{
//			String msgTitle="子任务通知";
//			UserDeviceService userDeviceService=new UserDeviceService();
//			UserInfoService userService=UserInfoService.getInstance();
//			UserInfo userObj=userService.queryUserInfoByUserId((int)createUserId);
//			String msgContent="【Fastmap】通知："+userObj.getUserRealName()+"已分配“"+subtaskName+"”子任务；请下载数据，安排作业！";
//			userDeviceService.pushMessage(exeUserId, msgTitle, msgContent, 
//					XingeUtil.PUSH_MSG_TYPE_PROJECT, "");
//			
//			return null;
//		
//		}catch(Exception e){
//			log.error(e.getMessage(), e);
//			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
//		}
//		
//	}



//	/**
//	 * @param conn 
//	 * @param userId 
//	 * @param groupId
//	 * @param stage 
//	 * @param conditionJson
//	 * @param orderJson
//	 * @param pageSize
//	 * @param curPageNum
//	 * @return
//	 * @throws ServiceException 
//	 */
//	public static Page getList(Connection conn, long userId, int groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
//			final int curPageNum) throws ServiceException {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			
//			String selectSql = "";
//			String selectUserSql = "";
//			String selectGroupSql = "";
//			String extraConditionSql = "";
//			
//			// 0采集，1日编，2月编，
//			if (0 == stage) {
//				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
//						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.COLLECT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//			} else if (1 == stage) {
//				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
//						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//				selectGroupSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, UG1.GROUP_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, BLOCK B, BLOCK_MAN BM, USER_GROUP UG1"
//						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
//						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//			} else if (2 == stage) {
//				selectUserSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, USER_INFO U, TASK T"
//						+ " WHERE S.TASK_ID = T.TASK_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND T.LATEST = 1"
//						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//				
//				selectGroupSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,UG1.GROUP_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, TASK T, USER_GROUP UG1"
//						+ " WHERE S.TASK_ID = T.TASK_ID"
//						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
//						+ " AND T.LATEST = 1"
//						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//			} 
//		
//			//查询条件
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator<?> keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("subtaskId".equals(key)) {extraConditionSql+=" AND S.SUBTASK_ID="+conditionJson.getInt(key);}
//					if ("subtaskName".equals(key)) {	
//						extraConditionSql+=" AND S.NAME LIKE '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("ExeUserId".equals(key)) {extraConditionSql+=" AND S.EXE_USER_ID="+conditionJson.getInt(key);}
//					if ("ExeUserName".equals(key)) {
//						extraConditionSql+=" AND U.USER_REAL_NAME LIKE '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockName".equals(key)) {
//						extraConditionSql+=" AND S.BLOCK_ID = B.BLOCK_ID AND B.BLOCK_NAME LIKE '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockId".equals(key)) {extraConditionSql+=" AND S.BLOCK_ID = "+conditionJson.getInt(key);}
//					if ("taskId".equals(key)) {extraConditionSql+=" ADN S.TASK_ID = "+conditionJson.getInt(key);}
//					if ("taskName".equals(key)) {
//						extraConditionSql+=" AND T.NAME LIKE '%" + conditionJson.getInt(key) +"%'";
//					}
//					if ("status".equals(key)) {
//						extraConditionSql+=" AND S.STATUS IN (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
//					}
//				}
//			}
//			
//			String orderSql = "";
//			
//			// 排序
//			if(null!=orderJson && !orderJson.isEmpty()){
//				Iterator<?> keys = orderJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("status".equals(key)) {orderSql+=" ORDER BY STATUS "+orderJson.getString(key);}
//					if ("subtaskId".equals(key)) {orderSql+=" ORDER BY SUBTASK_ID "+orderJson.getString(key);}
//					if ("blockId".equals(key)) {orderSql+=" ORDER BY block_id "+orderJson.getString(key);}
//					if ("planStartDate".equals(key)) {orderSql+=" ORDER BY PLAN_START_DATE "+orderJson.getString(key);}
//					if ("planEndDate".equals(key)) {orderSql+=" ORDER BY PLAN_END_DATE "+orderJson.getString(key);}
//				}
//			}else{orderSql += " ORDER BY SUBTASK_ID";}
//	
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
//					StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
//					Page page = new Page(curPageNum);
//				    page.setPageSize(pageSize);
//				    int total = 0;
//					while (rs.next()) {
//						if(total==0){
//							total=rs.getInt("TOTAL_RECORD_NUM_");
//						}
//						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("subtaskName", rs.getString("NAME"));
//						subtask.put("descp", rs.getString("DESCP"));
//						
//						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
//						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
//						
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						subtask.put("status", rs.getInt("STATUS"));
//						
//						subtask.put("executer", rs.getString("EXECUTER"));
//	
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						try {
//							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						
//						//月编
//						if(2 == rs.getInt("STAGE")){
//							subtask.put("taskId", rs.getInt("TASK_ID"));
//							subtask.put("taskName", rs.getString("TASK_NAME"));
//							subtask.put("taskType", rs.getInt("TASK_TYPE"));
//						}else{
//							subtask.put("blockId", rs.getInt("BLOCK_ID"));
//							subtask.put("blockName", rs.getString("BLOCK_NAME"));
//						}
//						
//						if(1 == rs.getInt("STATUS")){
//							SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));						
//							subtask.put("percent", stat.getPercent());
//						}
//	
//						list.add(subtask);
//					}
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//			};
//			
//			if(0==stage){
//				selectSql = selectUserSql + extraConditionSql + orderSql;
//			}else{
//				selectSql = selectUserSql + extraConditionSql + " UNION ALL " + selectGroupSql + extraConditionSql + orderSql;
//			}
//			
//			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		}
//	}
//
//
//	/**
//	 * @param conn 
//	 * @param userId 
//	 * @param groupId
//	 * @param stage 
//	 * @param conditionJson
//	 * @param orderJson
//	 * @param pageSize
//	 * @param curPageNum
//	 * @return
//	 * @throws ServiceException 
//	 */
//	public static Page getListSnapshot(Connection conn, long userId, int groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
//			final int curPageNum) throws ServiceException {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			
//			String selectSql = "";
//			
//			//0采集，1日编，2月编
//			if(0 == stage){
//				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,B.BLOCK_ID,B.BLOCK_NAME"
//						+ " FROM SUBTASK S ,BLOCK B,BLOCK_MAN BM"
//						+ " WHERE S.STAGE = 0"
//						+ " AND S.BLOCK_ID = B.BLOCK_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.COLLECT_GROUP_ID = " + groupId;
//			}else if(1 == stage){
//				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,B.BLOCK_ID,B.BLOCK_NAME"
//						+ " FROM SUBTASK S ,BLOCK B,BLOCK_MAN BM"
//						+ " WHERE S.STAGE = 1"
//						+ " AND S.BLOCK_ID = B.BLOCK_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId;
//			}
//			else if(2 ==stage){
//				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,T.TASK_ID,T.NAME AS TASK_NAME"
//						+ " FROM SUBTASK S ,TASK T"
//						+ " WHERE S.STAGE = 2"
//						+ " AND S.TASK_ID = T.TASK_ID"
//						+ " AND T.LATEST = 1"
//						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId;
//			}
//
//			//查询条件
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator<?> keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("subtaskName".equals(key)) {	
//						selectSql+=" AND S.NAME like '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockId".equals(key)) {selectSql+=" AND S.BLOCK_ID="+conditionJson.getInt(key);}
//					if ("taskId".equals(key)) {selectSql+=" AND S.TASK_ID="+conditionJson.getInt(key);}
//				}
//			}
//			
//			// 排序
//			if(null!=orderJson && !orderJson.isEmpty()){
//				Iterator keys = orderJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("status".equals(key)) {selectSql+=" ORDER BY S.STATUS "+orderJson.getString(key);}
//					if ("subtaskId".equals(key)) {selectSql+=" ORDER BY S.SUBTASK_ID "+orderJson.getString(key);}
//				}
//			}else{
//				selectSql+=" ORDER BY S.STATUS DESC";
//			}
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
//					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
//					List<HashMap<Object,Object>> openList = new ArrayList<HashMap<Object,Object>>();
//					List<HashMap<Object,Object>> closeList = new ArrayList<HashMap<Object,Object>>();
//					List<HashMap<Object,Object>> draftList = new ArrayList<HashMap<Object,Object>>();
//					Map<Integer,Object> draftMap = new HashMap<Integer,Object>();
//					Page page = new Page(curPageNum);
//				    page.setPageSize(pageSize);
//				    int total = 0;
//				    StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//					while (rs.next()) {
//						if(total==0){
//							total=rs.getInt("TOTAL_RECORD_NUM_");
//						}
//						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("subtaskName", rs.getString("NAME"));
//						subtask.put("status", rs.getInt("STATUS"));
//						subtask.put("type", rs.getInt("TYPE"));
//						
//						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
//						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
//						
//						if(rs.getInt("STAGE") == 2){
//							subtask.put("taskId", rs.getInt("TASK_ID"));
//							subtask.put("taskName", rs.getString("TASK_NAME"));
//						}else{
//							subtask.put("blockId", rs.getInt("BLOCK_ID"));
//							subtask.put("blockName", rs.getString("BLOCK_NAME"));
//						}
//						
//						if(2==rs.getInt("STATUS")){
//							draftList.add(subtask);
//							continue;
//						}else if(0==rs.getInt("STATUS")){
//							closeList.add(subtask);
//							continue;
//						}
//						
//						SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
//						int percent = stat.getPercent();
//						
//						subtask.put("percent", percent);
//						
//						draftMap.put(rs.getInt("SUBTASK_ID"), subtask);
//	
////						list.add(subtask);
//					}
//	
//					//开启子任务根据完成度排序
//					Object[] key_arr = draftMap.keySet().toArray();     
//					Arrays.sort(key_arr);     
//					for  (Object key : key_arr) {     
//						openList.add((HashMap<Object, Object>) draftMap.get(key));     
//					}  
//					
//					list.addAll(draftList);
//					list.addAll(openList);
//					list.addAll(closeList);
//					
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//	
//			};
//			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		}
//	}


	/**
	 * 根据taskId获取city几何
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static String getWktByTaskId(int taskId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT C.GEOMETRY FROM TASK T, CITY C WHERE T.CITY_ID = C.CITY_ID AND T.LATEST = 1 AND T.TASK_ID = " + taskId;
			
			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
				public String handle(ResultSet rs) throws SQLException {
					String wkt = null; 
					if(rs.next()) {
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							wkt = GeoTranslator.struct2Wkt(struct);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return wkt;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询city几何失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * 根据blockId获取block几何
	 * @param blockId
	 * @return
	 * @throws Exception 
	 */
	public static String getWktByBlockManId(int blockManId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT B.GEOMETRY FROM BLOCK B,BLOCK_MAN BM WHERE B.BLOCK_ID = BM.BLOCK_ID AND BM.BLOCK_MAN_ID = " + blockManId;
			
			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
				public String handle(ResultSet rs) throws SQLException {
					String wkt = null; 
					if(rs.next()) {
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							wkt = GeoTranslator.struct2Wkt(struct);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return wkt;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询block几何失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


//	/**
//	 * @param conn 
//	 * @param subTaskIds
//	 * @return
//	 * @throws SQLException 
//	 */
//	public static Map<Integer, Map<String, Object>> queryMessageAccessorByIds(Connection conn, JSONArray subTaskIds) throws SQLException {
//		// TODO Auto-generated method stub
//		String conditionSql="";
//		conditionSql+=" AND S.SUBTASK_ID IN  ("+subTaskIds.join(",")+")";
//
//		
//		String selectSql1="SELECT S.SUBTASK_ID,S.NAME,S.EXE_USER_ID,S.STAGE,S.STATUS"
//				+ " FROM SUBTASK S , USER_INFO U"
//				+ " WHERE S.EXE_USER_ID = U.USER_ID "+conditionSql;
//		String selectSql2="SELECT S.SUBTASK_ID, S.NAME, U.USER_ID AS EXE_USER_ID, S.STAGE, S.STATUS"
//				+ " FROM SUBTASK S, USER_INFO U, USER_GROUP UG, GROUP_USER_MAPPING GUM"
//				+ " WHERE S.EXE_GROUP_ID = UG.GROUP_ID"
//				+ " AND UG.GROUP_ID = GUM.GROUP_ID"
//				+ " AND GUM.USER_ID = U.USER_ID "+conditionSql;
//		
//		String selectSql = selectSql1 + " UNION ALL " + selectSql2;
//		
//		ResultSetHandler<Map<Integer, Map<String, Object>>> rsHandler = new ResultSetHandler<Map<Integer, Map<String, Object>>>(){
//			public Map<Integer, Map<String, Object>> handle(ResultSet rs) throws SQLException {
//				Map<Integer,Map<String, Object>> result = new HashMap<Integer,Map<String, Object>>();
//				while(rs.next()){
//					if(result.containsKey(rs.getInt("SUBTASK_ID"))){
//						Map<String, Object> map = result.get(rs.getInt("SUBTASK_ID"));
//						List<Integer> accessors = (List<Integer>) map.get("accessors");
//						accessors.add(rs.getInt("EXE_USER_ID"));
//						map.put("accessors", accessors);
//						result.put(rs.getInt("SUBTASK_ID"), map);
//					}else{
//						Map<String, Object> map = new HashMap<String, Object>();
//						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						map.put("name", rs.getString("NAME"));
//						map.put("stage", rs.getInt("STAGE"));
//						map.put("status", rs.getInt("STATUS"));
//						List<Integer> accessors = new ArrayList<Integer>();
//						accessors.add(rs.getInt("EXE_USER_ID"));
//						map.put("accessors", accessors);
//						result.put(rs.getInt("SUBTASK_ID"), map);
//					}
//				}
//				return result;
//			}
//    	};
//		
//		QueryRunner run=new QueryRunner();
//		return run.query(conn, selectSql, rsHandler);
//	}


	/**
	 * @param conn
	 * @param subtaskId
	 * @throws Exception 
	 */
	public static void updateStatus(Connection conn, int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE SUBTASK T SET T.STATUS=1 WHERE T.SUBTASK_ID =" + subtaskId 
					+ " OR EXISTS (SELECT 1"
					+ "          FROM SUBTASK T2"
					+ "         WHERE T2.SUBTASK_ID = " + subtaskId
					+ "           AND T.SUBTASK_ID = T2.QUALITY_SUBTASK_ID)";
			run.update(conn,updateSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn 
	 * @param userId 
	 * @param bean
	 * @throws Exception 
	 */
	public static void pushMessage(Connection conn, Subtask subtask, long userId) throws Exception {
		// TODO Auto-generated method stub
		try{
			//List<Integer> userIdList = new ArrayList<Integer>();
			//作业组
			/*
			if(subtask.getExeGroupId()!=0){
				userIdList = SubtaskOperation.getUserListByGroupId(conn,subtask.getExeGroupId());
			}else{
				userIdList.add(subtask.getExeUserId());
			}
			*/
			//构造消息
			/*采集/日编/月编子任务编辑
			 * 分配的作业员
			 * 采集/日编/月编子任务变更：XXX(子任务名称)信息发生变更，请关注*/
			String msgTitle = "";
			String msgContent = "";
			//2web,1手持端消息
			int pushtype=2;
			if((int)subtask.getStage()== 0){
				pushtype=1;
				msgTitle = "采集子任务编辑";
				msgContent = "采集子任务变更:" + subtask.getName() + "内容发生变更,请关注";
			}else if((int)subtask.getStage()== 1){
				msgTitle = "日编子任务编辑";
				msgContent = "日编子任务变更:" + subtask.getName() + "内容发生变更,请关注";
			}else{
				msgTitle = "月编子任务编辑";
				msgContent = "月编子任务变更:" + subtask.getName() + "内容发生变更,请关注";
			}
			//关联要素
			JSONObject msgParam = new JSONObject();
			msgParam.put("relateObject", "SUBTASK");
			msgParam.put("relateObjectId", subtask.getSubtaskId());
			//查询用户名称
			Map<String, Object> userInfo = UserInfoOperation.getUserInfoByUserId(conn, subtask.getExeUserId());
			String pushUserName = null;
			if(userInfo != null && userInfo.size() > 0){
				pushUserName = (String) userInfo.get("userRealName");
			}
			
			Message message = new Message();
			message.setMsgTitle(msgTitle);
			message.setMsgContent(msgContent);
			message.setPushUserId((int)userId);
			message.setReceiverId(subtask.getExeUserId());
			message.setMsgParam(msgParam.toString());
			message.setPushUser(pushUserName);
			
			MessageService.getInstance().push(message, pushtype);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("推送消息失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn 
	 * @param exeGroupId
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getUserListByGroupId(Connection conn, Integer exeGroupId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select gum.user_id from group_user_mapping gum where gum.group_id  = " + exeGroupId;
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> userIds = new ArrayList<Integer>(); 
					while (rs.next()) {
						userIds.add(rs.getInt("user_id"));
					}
					return userIds;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}


//	/**
//	 * @param conn
//	 * @param planStatus 
//	 * @param condition
//	 * @param filter 
//	 * @param pageSize
//	 * @param curPageNum
//	 * @return
//	 * @throws ServiceException 
//	 */
//	public static Page getList(Connection conn, int planStatus, JSONObject condition, JSONObject filter, final int pageSize, final int curPageNum) throws ServiceException {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			
//			String sql = "";
//
//			
//			String selectSqlCollect = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,USER_INFO U"
//					+ " WHERE S.STAGE = 0"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//
//			String selectSqlDailyUser = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,USER_INFO U"
//					+ " WHERE S.STAGE = 1"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//			String selectSqlDailyGroup = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS, UG.GROUP_NAME AS EXECUTER"
//					+ " FROM SUBTASK S , USER_GROUP UG"
//					+ " WHERE S.STAGE = 1"
//					+ " AND UG.GROUP_ID = S.EXE_GROUP_ID";
//
//			String selectSqlMonthlyUser = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S,USER_INFO U"
//					+ " WHERE S.STAGE = 2"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//			String selectSqlMonthlyGroup = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS, UG.GROUP_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,USER_GROUP UG"
//					+ " WHERE S.STAGE = 2"
//					+ " AND UG.GROUP_ID = S.EXE_GROUP_ID";
//
//
//			//查询条件
//			String conditionSql = "";
//			int taskFlg = 0;
//			int stage = -1;
//			Iterator<?> conditionKeys = condition.keys();
//			while (conditionKeys.hasNext()) {
//				String key = (String) conditionKeys.next();
//				//查询条件
//				if ("blockManId".equals(key)) {conditionSql+=" AND S.BLOCK_MAN_ID="+condition.getInt(key);}
//				if ("taskId".equals(key)) {
//					conditionSql+=" AND S.TASK_ID="+condition.getInt(key);
//					taskFlg = 1;
//				}
//				if ("stage".equals(key)) {stage = condition.getInt(key);}
//			}
//			
//			
//			String sql_T = "";
//			if(stage==0){
//				//采集
//				sql_T = selectSqlCollect + conditionSql;
//			}else if(stage==1){
//				//日编
//				sql_T = selectSqlDailyUser + conditionSql + " UNION ALL " + selectSqlDailyGroup + conditionSql;
//			}else if(stage==2){
//				//月编
//				sql_T = selectSqlMonthlyUser + conditionSql + " UNION ALL " + selectSqlMonthlyGroup + conditionSql;
//			}else{
//				if(0 == taskFlg){
//					//采集/日编
//					sql_T = selectSqlCollect + conditionSql + " UNION ALL " + selectSqlDailyUser + conditionSql + " UNION ALL " + selectSqlDailyGroup + conditionSql;
//				}else{
//					//月编
//					sql_T = selectSqlMonthlyUser + conditionSql + " UNION ALL " + selectSqlMonthlyGroup + conditionSql;
//				}
//			}
//
//			sql = "SELECT T.SUBTASK_ID, T.STAGE, T.NAME, T.TYPE, T.STATUS,T.EXECUTER,FSOS.PERCENT,FSOS.DIFF_DATE,FSOS.PROGRESS"
//					+ " FROM (" + sql_T + ") T,FM_STAT_OVERVIEW_SUBTASK FSOS"
//							+ " WHERE T.SUBTASK_ID = FSOS.SUBTASK_ID(+) ";
//
//			String filterSql = "";
//			if(null != filter){
//				Iterator<?> filterKeys = filter.keys();
//				while (filterKeys.hasNext()) {
//					String key = (String) filterKeys.next();
//					//模糊查询
//					if ("subtaskName".equals(key)) {	
//						filterSql+=" AND T.NAME like '%" + filter.getString(key) +"%'";
//					}
//					//筛选条件
//					//"progress" //进度。1正常，2异常，3关闭，4完成,5草稿,6完成状态逾期，7完成状态按时，8完成状态提前
//					if ("progress".equals(key)){
//						JSONArray progress = filter.getJSONArray(key);
//						if(progress.isEmpty()){
//							continue;
//						}
//						
//						List<String> progressList = new ArrayList<String>();
//
//						if(progress.contains(1)){
//							progressList.add("FSOS.PROGRESS = 1");
//						}
//						if(progress.contains(2)){
//							progressList.add("FSOS.PROGRESS = 2");
//						}
//						if(progress.contains(3)){
//							progressList.add("T.STATUS = 0");
//						}
//						if(progress.contains(4)){
//							progressList.add("T.STATUS = 1 AND FSOS.PERCENT = 100");
//						}
//						if(progress.contains(5)){
//							progressList.add("T.STATUS = 2");
//						}
//						if(progress.contains(6)){
//							progressList.add("FSOS.DIFF_DATE < 0");
//						}
//						if(progress.contains(7)){
//							progressList.add("FSOS.DIFF_DATE = 0");
//						}
//						if(progress.contains(8)){
//							progressList.add("FSOS.DIFF_DATE > 0");
//						}
//						
//						if(!progressList.isEmpty()){
//							String tempSql = StringUtils.join(progressList," or ");
//							filterSql += " AND (" + tempSql + ")";
//						}
////						int progress = filter.getInt(key);
////						if(1==progress&&2==progress){
////							filterSql += " AND FSOS.PROGRESS = " + progress;
////						}else if(3==progress){
////							filterSql += " AND T.STATUS = 0" ;
////						}else if(4==progress){
////							filterSql += " AND T.STATUS = 1 AND FSOS.PERCENT = 100";
////						}else if(5==progress){
////							filterSql += " AND T.STATUS = 2";
////						}
////						////"progress" //进度。6完成状态逾期，7完成状态按时，8完成状态提前
////						else if(6==progress){
////							filterSql += " AND FSOS.DIFF_DATE < 0";
////						}else if(7==progress){
////							filterSql += " AND FSOS.DIFF_DATE = 0" ;
////						}else if(8==progress){
////							filterSql += " AND FSOS.DIFF_DATE > 0";
////						}
//					}
////					//"completionStatus"//完成状态。0逾期，1按时，2提前
////					if ("completionStatus".equals(key)){
////						int completionStatus = filter.getInt(key);
////						if(0==completionStatus){
////							filterSql += " AND FSOS.DIFF_DATE < 0";
////						}else if(1==completionStatus){
////							filterSql += " AND FSOS.DIFF_DATE = 0" ;
////						}else if(2==completionStatus){
////							filterSql += " AND FSOS.DIFF_DATE > 0";
////						}
////					}
//				}
//			}
//			
//			sql += filterSql;
//			String sqlFinal = "";
//			//排序
////			"planStatus"//block/task规划状态。2:"已发布",3:"已完成" 。状态不同，排序方式不同。
//			if(3 == planStatus){
//				//已完成
//				String orderSql = " ORDER BY DIFF_DATE ASC ";
//				sqlFinal = sql + orderSql;
//			}else if(2 == planStatus){
//				//已发布
////				String orderSql = "ORDER BY DIFF_DATE ASC, PERCENT DESC";
////				String Sql2Close = sql + " AND T.STATUS = 0 ";
////				String Sql2Draft = sql + " AND T.STATUS = 2 ";
////				String Sql2OpenFinish = sql  + " AND T.STATUS = 1 AND FSOS.PERCENT = 100 ";
////				String Sql2OpenUnfinish = sql + " AND T.STATUS = 1 AND (FSOS.PERCENT < 100 OR FSOS.PERCENT IS NULL)";
//				String orderSql = "ORDER BY PRI ASC,DIFF_DATE ASC, PERCENT DESC";
//				String Sql2Close = "SELECT SUBTASK_ID, STAGE, NAME, TYPE, STATUS,EXECUTER,PERCENT,DIFF_DATE,PROGRESS ,4 AS PRI FROM (" + sql + " AND T.STATUS = 0 " + ")";
//				String Sql2Draft = "SELECT SUBTASK_ID, STAGE, NAME, TYPE, STATUS,EXECUTER,PERCENT,DIFF_DATE,PROGRESS ,2 AS PRI FROM (" + sql + " AND T.STATUS = 2 " + ")";
//				String Sql2OpenFinish = "SELECT SUBTASK_ID, STAGE, NAME, TYPE, STATUS,EXECUTER,PERCENT,DIFF_DATE,PROGRESS ,3 AS PRI FROM (" + sql  + " AND T.STATUS = 1 AND FSOS.PERCENT = 100 " + ")";
//				String Sql2OpenUnfinish = "SELECT SUBTASK_ID, STAGE, NAME, TYPE, STATUS,EXECUTER,PERCENT,DIFF_DATE,PROGRESS ,1 AS PRI FROM (" + sql + " AND T.STATUS = 1 AND (FSOS.PERCENT < 100 OR FSOS.PERCENT IS NULL) " + ")";
//				
//				sqlFinal = Sql2OpenUnfinish + " UNION ALL " + Sql2Draft + " UNION ALL " + Sql2OpenFinish + " UNION ALL "  + Sql2Close  + orderSql;
//			}
//			
//			
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
//					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
//					Page page = new Page(curPageNum);
//				    page.setPageSize(pageSize);
//				    int total = 0;
//					while (rs.next()) {
//						if(total==0){
//							total=rs.getInt("TOTAL_RECORD_NUM_");
//						}
//						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("subtaskName", rs.getString("NAME"));
//						subtask.put("status", rs.getInt("STATUS"));
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						
//						subtask.put("executer", rs.getString("EXECUTER"));
//
//						subtask.put("percent", rs.getInt("percent"));
//						subtask.put("diffDate", rs.getInt("DIFF_DATE"));
//	
//						list.add(subtask);
//					}
//					
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//	
//			};
//			return run.query(curPageNum, pageSize, conn, sqlFinal, rsHandler);
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		}
//	}

	
	/**
	 * @Title: getList
	 * @Description: 获取subtask列表,只返回作业子任务(修改)(第七迭代)
	 * @param conn
	 * @param planStatus
	 * @param condition
	 * @param filter
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException  Page
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 下午4:00:59 
	 */
	public static Page getList(Connection conn, int planStatus, JSONObject condition,final int pageSize, final int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			//查询条件
			String conditionSql = "";
			Iterator<?> conditionKeys = condition.keys();
			boolean collectAndDay=true;
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("blockManId".equals(key)) {conditionSql+=" AND subtask_list.BLOCK_MAN_ID="+condition.getInt(key);}
				if ("taskId".equals(key)) {
					conditionSql+=" AND subtask_list.TASK_ID="+condition.getInt(key);
				}
				if ("stage".equals(key)) {
					collectAndDay=false;
					conditionSql+=" AND subtask_list.stage ="+condition.getInt(key);}
				//子任务名称模糊查询
				if ("subtaskName".equals(key)) {	
					conditionSql+=" AND subtask_list.NAME like '%" + condition.getString(key) +"%'";
				}
				//根据gridId获取子任务列表，主要用于地图联动。点地图上的某个grid，查询出包含这个grid的所有子任务
				if("gridId".equals(key)){
					List<Integer> subtaskIds=getSubtaskIdsByGridIdWithConn(conn,condition.getInt(key));
					conditionSql+=" AND subtask_list.subtask_id in ("+subtaskIds.toString().replace("[", "").replace("]", "")+")";
				}
				//筛选条件
				//"progress" //进度。1采集正常，2采集异常，3采集关闭，4采集完成,5采集草稿,6日编正常，7日编异常，8日编关闭，
				//9日编完成,10日编草稿,11逾期完成，12按时完成，13提前完成,
				//14月编正常15月编异常16月编关闭，17月编完成,18月编草稿
				if ("progress".equals(key)){
					JSONArray progress = condition.getJSONArray(key);
					if(progress.isEmpty()){
						continue;
					}
					List<String> progressList = new ArrayList<String>();
					for(Object i:progress){
						int tmp=(int) i;
					if(tmp==1){progressList.add(" subtask_list.PROGRESS = 1 AND subtask_list.stage=0 ");}
					if(tmp==2){progressList.add(" subtask_list.PROGRESS = 2 AND subtask_list.stage=0");}
					if(tmp==3){progressList.add(" subtask_list.STATUS = 0 AND subtask_list.stage=0");}
					if(tmp==4){
						progressList.add(" subtask_list.STATUS = 1 AND subtask_list.PERCENT = 100 "
								+ "AND subtask_list.stage=0");}
					if(tmp==5){progressList.add(" subtask_list.STATUS = 2 AND subtask_list.stage=0 ");}
					if(tmp==6){progressList.add(" subtask_list.PROGRESS = 1 AND subtask_list.stage=1 ");}
					if(tmp==7){progressList.add(" subtask_list.PROGRESS = 2 AND subtask_list.stage=1");}
					if(tmp==8){progressList.add(" subtask_list.STATUS = 0 AND subtask_list.stage=1");}
					if(tmp==9){
						progressList.add(" subtask_list.STATUS = 1 AND subtask_list.PERCENT = 100 "
								+ "AND subtask_list.stage=1");}
					if(tmp==10){progressList.add(" subtask_list.STATUS = 2 AND subtask_list.stage=1 ");}
					
					if(tmp==11){
						progressList.add("subtask_list.DIFF_DATE < 0");
						progressList.add("subtask_list.DIFF_DATE < 0");
					}
					if(tmp==12){
						progressList.add("subtask_list.DIFF_DATE = 0");
						progressList.add("subtask_list.DIFF_DATE = 0");
					}
					if(tmp==13){
						progressList.add("subtask_list.DIFF_DATE > 0");
						progressList.add("subtask_list.DIFF_DATE > 0");
					}
					if(tmp==14){progressList.add(" subtask_list.PROGRESS = 1 AND subtask_list.stage=2 ");}
					if(tmp==15){progressList.add(" subtask_list.PROGRESS = 2 AND subtask_list.stage=2");}
					if(tmp==16){progressList.add(" subtask_list.STATUS = 0 AND subtask_list.stage=2");}
					if(tmp==17){
						progressList.add(" subtask_list.STATUS = 1 AND subtask_list.PERCENT = 100 "
								+ "AND subtask_list.stage=2");}
					if(tmp==18){progressList.add(" subtask_list.STATUS = 2 AND subtask_list.stage=2 ");}
					}
					
					if(!progressList.isEmpty()){
						String tempSql = StringUtils.join(progressList," OR ");
						tempSql += " AND (" + tempSql + ")";
					}
				}
			}
			if (collectAndDay){conditionSql+=" AND subtask_list.stage IN (0,1)";}
			QueryRunner run = new QueryRunner();
			long pageStartNum = (curPageNum - 1) * pageSize + 1;
			long pageEndNum = curPageNum * pageSize;
			//质检子任务语句
			String sql="WITH quality_task as(select Ss.SUBTASK_ID quality_subtask_id,"
					+ "                                     Ss.EXE_USER_ID     quality_Exe_User_Id,"
					+ "                                     Ss.PLAN_START_DATE as quality_Plan_Start_Date,"
					+ "                                     Ss.PLAN_END_DATE   as quality_Plan_End_Date,"
					+ "                                     Ss.STATUS          quality_Task_Status,"
					+ "                                     UU.USER_REAL_NAME  AS quality_Exe_User_Name"
					+ "                                from subtask Ss, USER_INFO UU"
					+ "                               where Ss.is_quality = 1"
					+ "                                 AND SS.EXE_USER_ID = UU.USER_ID),"
					+ "             subtask_list AS( SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,"
					+ "						U.USER_REAL_NAME AS EXECUTER,UG.GROUP_NAME AS Group_EXECUTER,"
					+ "						NVL(FSOS.PERCENT,0) PERCENT,NVL(FSOS.DIFF_DATE,0) DIFF_DATE,"
					+ "						NVL(FSOS.PROGRESS,1) PROGRESS,S.BLOCK_MAN_ID,S.TASK_ID,"
					+ "						NVL(Q.quality_subtask_id,0) quality_subtask_id ,NVL(Q.quality_Exe_User_Id,0) quality_Exe_User_Id,"
					+ "						Q.quality_Plan_Start_Date,Q.quality_Plan_End_Date,"
					+ "						NVL(Q.quality_Task_Status,0) quality_Task_Status,Q.quality_Exe_User_Name,"
					/*• 记录默认排序原则：
					 * ①根据状态排序：开启>草稿>100%(已完成)>已关闭
					 * 用order_status来表示这个排序的先后顺序。分别是开启0>草稿1>100%(已完成)2>已关闭3
					 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
					 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
					 */
					+ "                  CASE S.STATUS"
					+ "                      WHEN 1 THEN CASE NVL(FSOS.PERCENT,0) when 100 then 2 WHEN 2 THEN 0 end "
	                + "                         when 2 then 1"
	                + "                           when 0 then 3 end order_status"
					+ " FROM SUBTASK S ,USER_INFO U,USER_GROUP UG,FM_STAT_OVERVIEW_SUBTASK FSOS,quality_task Q"
					+ " WHERE Q.quality_subtask_id(+) = S.quality_subtask_id"
					+ " AND S.is_quality = 0" //排除 Subtask 表中的质检子任务
					+ " AND U.USER_ID(+) = S.EXE_USER_ID"
					+ " AND UG.GROUP_ID(+) = S.EXE_GROUP_ID"
					+ " AND S.SUBTASK_ID = FSOS.SUBTASK_ID(+)),"
					+ " FINAL_TABLE AS"
				+ " (SELECT *"
				+ "    FROM subtask_list"
				+ "    WHERE 1=1"
				+ conditionSql+""
				+ " order by subtask_list.order_status asc,subtask_list.diff_date desc,subtask_list.percent desc,subtask_list.name)"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
					
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page();
//					Page page = new Page(curPageNum);
//				    page.setPageSize(pageSize);
				    int totalCount = 0;
				    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while (rs.next()) {
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						String executer=rs.getString("EXECUTER");
						if(executer==null||executer.isEmpty()){
							executer=rs.getString("GROUP_EXECUTER");
						}
						subtask.put("executer", executer);
						
						subtask.put("percent", rs.getInt("percent"));
						subtask.put("diffDate", rs.getInt("DIFF_DATE"));
						
						subtask.put("qualitySubtaskId", rs.getInt("quality_subtask_id"));
						subtask.put("qualityExeUserId", rs.getInt("quality_Exe_User_Id"));
						Timestamp qualityPlanStartDate = rs.getTimestamp("quality_Plan_Start_Date");
						Timestamp qualityPlanEndDate = rs.getTimestamp("quality_Plan_End_Date");
						if(qualityPlanStartDate != null){
							subtask.put("qualityPlanStartDate", df.format(qualityPlanStartDate));
						}else {subtask.put("qualityPlanStartDate", null);}
						if(qualityPlanEndDate != null){
							subtask.put("qualityPlanEndDate",df.format(qualityPlanEndDate));
						}else{subtask.put("qualityPlanEndDate", null);}
						
						subtask.put("qualityTaskStatus", rs.getInt("quality_Task_Status"));
						subtask.put("qualityExeUserName", rs.getString("quality_Exe_User_Name"));
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(subtask);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}
	
			};
			Page page= run.query(conn, sql, rsHandler);
			page.setPageNum(curPageNum);
		    page.setPageSize(pageSize);
		    return page;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}


	/**
	 * @Title: getListByGroup
	 * @Description: 根据作业组获取子任务列表（修改）(第七迭代)
	 * @param conn
	 * @param groupId
	 * @param stage
	 * @param conditionJson
	 * @param orderJson
	 * @param pageSize
	 * @param curPageNum
	 * @return  (增加返回值:qualitySubtaskId,qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
	 * @throws ServiceException  Page
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 下午1:58:11 
	 */
	public static Page getListByGroup(Connection conn, long groupId, int stage, JSONObject conditionJson,
			JSONObject orderJson, final int pageSize, final int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			String selectSql = "";
			String selectUserSql = "";
			String selectGroupSql = "";
			String extraConditionSql = "";
			
			// 0采集，1日编，2月编，
			if (0 == stage) {
				/*selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
						+ " WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.COLLECT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;*/
				selectUserSql = "SELECT "
						+ " S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S "
						//左外关联 质检子任务表
						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
						+ "	BLOCK B, USER_INFO U, BLOCK_MAN BM"
						+ " WHERE "
						+ " S.is_quality = 0" //排除 Subtask 表中的质检子任务
						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.COLLECT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} else if (1 == stage) {
				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S "
						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
						+ " BLOCK B, USER_INFO U, BLOCK_MAN BM"
						+ " WHERE "
						+ " S.is_quality = 0" 
						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
				selectGroupSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME ,UG1.GROUP_NAME AS EXECUTER"
						+ " FROM SUBTASK S "
						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
						+ " BLOCK B, BLOCK_MAN BM, USER_GROUP UG1"
						+ " WHERE "
						+ " S.is_quality = 0" 
						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} else if (2 == stage) {
				selectUserSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,"
						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
						+ " T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S "
						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
						+ " USER_INFO U, TASK T"
						+ " WHERE "
						+ " S.is_quality = 0" 
						+ " AND S.TASK_ID = T.TASK_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND T.LATEST = 1"
						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
				
				selectGroupSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,"
						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
						+ " T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,UG1.GROUP_NAME AS EXECUTER"
						+ " FROM SUBTASK S "
						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
						+ " TASK T, USER_GROUP UG1"
						+ " WHERE "
						+ " S.is_quality = 0" 
						+ " AND S.TASK_ID = T.TASK_ID"
						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
						+ " AND T.LATEST = 1"
						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} 
		
			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator<?> keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskId".equals(key)) {extraConditionSql+=" AND S.SUBTASK_ID="+conditionJson.getInt(key);}
					if ("subtaskName".equals(key)) {	
						extraConditionSql+=" AND S.NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("ExeUserId".equals(key)) {extraConditionSql+=" AND S.EXE_USER_ID="+conditionJson.getInt(key);}
					if ("ExeUserName".equals(key)) {
						extraConditionSql+=" AND U.USER_REAL_NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockManName".equals(key)) {
						extraConditionSql+=" AND S.BLOCK_ID = B.BLOCK_ID AND BM.BLOCK_MAN_NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockManId".equals(key)) {extraConditionSql+=" AND S.BLOCK_MAN_ID = "+conditionJson.getInt(key);}
					if ("taskId".equals(key)) {extraConditionSql+=" ADN S.TASK_ID = "+conditionJson.getInt(key);}
					if ("taskName".equals(key)) {
						extraConditionSql+=" AND T.NAME LIKE '%" + conditionJson.getInt(key) +"%'";
					}
					if ("status".equals(key)) {
						extraConditionSql+=" AND S.STATUS IN (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
					}
				}
			}
			
			String orderSql = "";
			
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator<?> keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {orderSql+=" ORDER BY STATUS "+orderJson.getString(key);}
					if ("subtaskId".equals(key)) {orderSql+=" ORDER BY SUBTASK_ID "+orderJson.getString(key);}
					if ("blockManId".equals(key)) {orderSql+=" ORDER BY BLOCK_MAN_ID "+orderJson.getString(key);}
					if ("planStartDate".equals(key)) {orderSql+=" ORDER BY PLAN_START_DATE "+orderJson.getString(key);}
					if ("planEndDate".equals(key)) {orderSql+=" ORDER BY PLAN_END_DATE "+orderJson.getString(key);}
				}
			}else{orderSql += " ORDER BY SUBTASK_ID";}
	
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));
						
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						
						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("status", rs.getInt("STATUS"));
						
						subtask.put("executer", rs.getString("EXECUTER"));
						//**************zl 2016.11.04 ******************
						subtask.put("qualitySubtaskId", rs.getInt("qualitySubtaskId"));
						subtask.put("qualityExeUserId", rs.getInt("qualityExeUserId"));
						subtask.put("qualityPlanStartDate", df.format(rs.getTimestamp("qualityPlanStartDate")));
						subtask.put("qualityPlanEndDate", df.format(rs.getTimestamp("qualityPlanEndDate")));
						
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						//月编
						if(2 == rs.getInt("STAGE")){
							subtask.put("taskId", rs.getInt("TASK_ID"));
							subtask.put("taskName", rs.getString("TASK_NAME"));
							subtask.put("taskType", rs.getInt("TASK_TYPE"));
						}else{
							subtask.put("blockId", rs.getInt("BLOCK_ID"));
							subtask.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
							subtask.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
						}
						
						if(1 == rs.getInt("STATUS")){
							SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));						
							subtask.put("percent", stat.getPercent());
						}
	
						list.add(subtask);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
			};
			
			if(0==stage){
				selectSql = selectUserSql + extraConditionSql + orderSql;
			}else{
				selectSql = selectUserSql + extraConditionSql + " UNION ALL " + selectGroupSql + extraConditionSql + orderSql;
			}
			
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}


	public static void closeBySubtaskId(int subtaskId) throws Exception {
			// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			ArrayList<Integer> closedSubtaskList = new ArrayList<Integer>();
			closedSubtaskList.add(subtaskId);
			closeBySubtaskList(conn, closedSubtaskList);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * @param subtaskId 
	 * @param gridIds
	 * @param geometry 
	 * @return
	 * @throws Exception 
	 */
	public static JSONArray getReferSubtasksByGridIds(Integer subtaskId, List<Integer> gridIds, Geometry subtaskReferGeo) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			//先按照grid扩圈
			Set<Integer> gridsWithNeighbor = new HashSet<Integer>();
			for(int j=0;j<gridIds.size();j++)  
	        {              
				String gridId = String.valueOf(gridIds.get(j));
				String[] gridAfter = GridUtils.get9NeighborGrids(gridId);
				for(int i=0;i<gridAfter.length;i++){
					gridsWithNeighbor.add(Integer.valueOf(gridAfter[i]));
				}           
	        } 
			//扩圈后，去除子任务自己的grid，剩余grid依次与不规则任务圈进行关系判断，若接边/交叉，则计算；否则放弃。
			Set<Integer> geoWithNeighbor=new HashSet<Integer>();
			geoWithNeighbor.addAll(gridIds);
			gridsWithNeighbor.removeAll(gridIds);
			for(Integer grid:gridsWithNeighbor){
				String gridId = String.valueOf(grid);
				Geometry gridGeo = GridUtils.grid2Geometry(gridId);
				if(gridGeo.intersects(subtaskReferGeo)){
					geoWithNeighbor.add(grid);
				}
			}
			//获取到与不规则子任务圈有交叉/接边的所有grid，然后查询所有非该子任务的所有子任务信息
			String gridIdsStr = StringUtils.join(geoWithNeighbor.toArray(), ",");
			
			String selectSql = "select distinct sgm.grid_id,"
					+ " u.user_real_name"
					+ " from subtask s,"
					+ " user_info u,subtask_grid_mapping sgm"
					+ " where s.stage = 0"
					+ " and sgm.grid_id in (" + gridIdsStr + ")"
					+ " and s.subtask_id = sgm.subtask_id"
					+ " and s.exe_user_id = u.user_id"
					+ " and s.status=1"
					+ " and s.subtask_id <> " + subtaskId
					+ " order by sgm.grid_id";
			
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					JSONArray referSubtasks = new JSONArray(); 
					int gridId=0;
					JSONArray gridUserName = new JSONArray();
					while (rs.next()) {
						int temp = rs.getInt("grid_id");
						if(gridId==0){
							gridId=temp;
							gridUserName.add(rs.getString("user_real_name"));
							continue;}
						if(gridId!=temp){
							JSONObject tempGridRefer=new JSONObject();
							tempGridRefer.put("gridId", gridId);
							tempGridRefer.put("userNameList", gridUserName);
							referSubtasks.add(tempGridRefer);
							gridUserName=new JSONArray();
						}
						gridId=temp;
						gridUserName.add(rs.getString("user_real_name"));
					}
					if(gridId!=0){
						JSONObject tempGridRefer=new JSONObject();
						tempGridRefer.put("gridId", gridId);
						tempGridRefer.put("userNameList", gridUserName);
						referSubtasks.add(tempGridRefer);}
					return referSubtasks;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
