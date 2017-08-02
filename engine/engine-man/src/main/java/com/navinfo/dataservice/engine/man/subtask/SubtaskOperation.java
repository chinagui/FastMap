package com.navinfo.dataservice.engine.man.subtask;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** 
 * @ClassName: SubtaskOperation
 * @author songdongyan
 * @date 2016年6月13日
 * @Description: SubtaskOperation.java
 */
public class SubtaskOperation {
	private static Logger log = LoggerRepos.getLogger(SubtaskOperation.class);
	
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
			Map<String, Object> changeFields = bean.getChangeFields();
			String baseSql = "update SUBTASK set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			List<Object> value = new ArrayList<Object>();
			if (changeFields.containsKey("NAME")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " NAME= " + "'" + bean.getName() + "'";
			};
			if (changeFields.containsKey("DESCP")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DESCP= " + "'" + bean.getDescp() + "'";
			};
			if (changeFields.containsKey("PLAN_START_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_START_DATE= " + "to_timestamp('" + bean.getPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("EXE_USER_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_USER_ID= " + bean.getExeUserId();
			};
			if (changeFields.containsKey("EXE_GROUP_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_GROUP_ID= " + bean.getExeGroupId();
			};
			if (changeFields.containsKey("PLAN_END_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_END_DATE= " + "to_timestamp('" + bean.getPlanEndDate()+ "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("STATUS")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " STATUS= " + bean.getStatus();
			};
			//修改新增的两个字段
			if (changeFields.containsKey("QUALITY_SUBTASK_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " QUALITY_SUBTASK_ID= " + bean.getQualitySubtaskId();
			};
			if (changeFields.containsKey("IS_QUALITY")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " IS_QUALITY= " + bean.getIsQuality();
			};
			if (changeFields.containsKey("REFER_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " REFER_ID= " + bean.getReferId();
			};
			
			if (changeFields.containsKey("WORK_KIND")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " work_kind= " + bean.getWorkKind();
			};
			
			if (changeFields.containsKey("GEOMETRY")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" GEOMETRY=? ";
				value.add(GeoTranslator.wkt2Struct(conn,bean.getGeometry()));
			};	
			if (changeFields.containsKey("QUALITY_METHOD")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " QUALITY_METHOD= " + bean.getQualityMethod();
			};
			if(bean.getGridIds() != null&&bean.getGridIds().size()>0){
				//前端传入grids修改，需要重新更新子任务的grid
				SubtaskOperation.deleteSubtaskGridMapping(conn, bean.getSubtaskId());
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			}
			
			updateSql += " where SUBTASK_ID= " + bean.getSubtaskId();
			
			log.info("updateSubtask sql:" + baseSql+updateSql);
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
	public static void updateSubtaskGeo(Connection conn,String geoStr,int subtaskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String baseSql = "update SUBTASK set GEOMETRY=? where SUBTASK_ID="+subtaskId;			
			log.info("updateSubtask sql:" + baseSql);
			run.update(conn,baseSql,GeoTranslator.wkt2Struct(conn,geoStr));			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * @Title: getSubtaskListBySubtaskIdList
	 * @Description: 根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
	 * @Operation: 修改
	 * @Old Author: 张晓毅
	 * @param conn
	 * @param subtaskIdList
	 * @return
	 * @throws Exception  List<Subtask>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年7月5日 下午3:36:17 
	 */
	public static List<Subtask> getSubtaskListBySubtaskIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String subtaskIds = "(" + StringUtils.join(subtaskIdList.toArray(),",") + ")";
			
			
			String selectSql = "SELECT s.geometry,S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.EXE_USER_ID,S.EXE_GROUP_ID,s.create_user_id,s.work_kind,S.STATUS,S.TASK_ID,"
					+ " s.refer_id "
					+ " FROM SUBTASK S"
					+ " WHERE S.SUBTASK_ID IN " + subtaskIds;
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						//if(rs.getInt("refer_id") > 0){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setExeUserId(rs.getInt("EXE_USER_ID"));
						subtask.setExeGroupId(rs.getInt("EXE_GROUP_ID"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setCreateUserId(rs.getInt("create_user_id"));
						subtask.setTaskId(rs.getInt("TASK_ID"));
						subtask.setWorkKind(rs.getInt("WORK_KIND"));
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						String wkt="";
						try {
							wkt=GeoTranslator.struct2Wkt(struct);
							Geometry geometry=GeoTranslator.struct2Jts(struct);
							subtask.setGeometryJSON(GeoTranslator.jts2Geojson(geometry));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						list.add(subtask);
					}
						
					//}
					return list;
				}
	    	};
	    	log.info("getSubtaskListBySubtaskIdList sql:" + selectSql);
	    	List<Subtask> subtaskList = run.query(conn, selectSql,rsHandler);
	    	return subtaskList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static int closeBySubtaskList(Connection conn,List<Integer> closedSubtaskList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String closedSubtaskStr = "(";
			
			closedSubtaskStr += StringUtils.join(closedSubtaskList.toArray(),",") + ")";
			//TYPE!=4 非区域子任务，直接关闭；type=4区域子任务，判断这个区域子任务范围内的所有一体化_grid粗编子任务均关闭		
			String updateSql = "update SUBTASK S "
					+ "set S.STATUS=0 "
					+ "where S.SUBTASK_ID in "
					+ closedSubtaskStr 
					+ " AND (S.TYPE!=4 or s.descp like '%预处理%' OR (S.TYPE=4 AND NOT EXISTS (SELECT 1"	
					+ "          FROM SUBTASK SS, SUBTASK_GRID_MAPPING SM, TASK_GRID_MAPPING TM"
					+ "         WHERE SS.SUBTASK_ID = SM.SUBTASK_ID"
					+ "           AND SM.GRID_ID = TM.GRID_ID"
					+ "           AND SS.TASK_ID = TM.TASK_ID"
					+ "           AND SS.STATUS != 0"
					+ "           AND SS.is_quality=0"
					+ "           AND S.TASK_ID = TM.TASK_ID"
					+ "           AND SS.TYPE = 3)))";
			log.info("关闭SQL："+updateSql);
			return run.update(conn,updateSql);
		}catch(Exception e){
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
	public static int getSubtaskId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";

			int subTaskId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("subTaskId")
					.toString());
			return subTaskId;
		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
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
			Map<String, Object> changeFields = bean.getChangeFields();
			String column = "";
			String values = "";
			List<Object> value = new ArrayList<Object>();
			if (changeFields.containsKey("SUBTASK_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" SUBTASK_ID ";
				values+=" ? ";
				value.add(bean.getSubtaskId());
			};
			if (changeFields.containsKey("NAME")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" NAME ";
				values+=" ? ";
				value.add(bean.getName());
			};
			if (changeFields.containsKey("GEOMETRY")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" GEOMETRY ";
				values+=" ? ";
				value.add(GeoTranslator.wkt2Struct(conn,bean.getGeometry()));
			};
			if (changeFields.containsKey("STAGE")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" STAGE ";
				values+=" ? ";
				value.add(bean.getStage());
			};
			if (changeFields.containsKey("TYPE")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" TYPE ";
				values+=" ? ";
				value.add(bean.getType());
			};
			if (changeFields.containsKey("CREATE_USER_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" CREATE_USER_ID ";
				values+=" ? ";
				value.add(bean.getCreateUserId());
			};
			
			if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
			column+=" CREATE_DATE ";
			values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
			value.add(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10));
			
			if (changeFields.containsKey("STATUS")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" STATUS ";
				values+=" ? ";
				value.add(bean.getStatus());
			};
			
			if (changeFields.containsKey("PLAN_START_DATE")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" PLAN_START_DATE ";
				values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
				value.add(bean.getPlanStartDate().toString().substring(0, 10));
			};
			if (changeFields.containsKey("PLAN_END_DATE")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" PLAN_END_DATE ";
				values+=" to_date(?,'yyyy-MM-dd HH24:MI:ss') ";
				value.add(bean.getPlanEndDate().toString().substring(0, 10));
			};			
			if (changeFields.containsKey("DESCP")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" DESCP ";
				values+=" ? ";
				value.add(bean.getDescp());
			};
			if (changeFields.containsKey("TASK_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" TASK_ID ";
				values+=" ? ";
				value.add(bean.getTaskId());
			};
			if (changeFields.containsKey("QUALITY_SUBTASK_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" QUALITY_SUBTASK_ID ";
				values+=" ? ";
				value.add(bean.getQualitySubtaskId());
			};
			if (changeFields.containsKey("IS_QUALITY")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" IS_QUALITY ";
				values+=" ? ";
				value.add(bean.getIsQuality());
			}
			//外业参考任务圈
			if (changeFields.containsKey("REFER_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" REFER_ID ";
				values+=" ? ";
				value.add(bean.getReferId());
			}
			if (changeFields.containsKey("EXE_GROUP_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" EXE_GROUP_ID ";
				values+=" ? ";
				value.add(bean.getExeGroupId());
			};
			if (changeFields.containsKey("EXE_USER_ID")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" EXE_USER_ID ";
				values+=" ? ";
				value.add(bean.getExeUserId());
			};
			if (changeFields.containsKey("WORK_KIND")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" work_kind ";
				values+=" ? ";
				value.add(bean.getWorkKind());
			};
			if (changeFields.containsKey("QUALITY_METHOD")){
				if(StringUtils.isNotEmpty(column)){column+=" , ";values+=" , ";}
				column+=" QUALITY_METHOD ";
				values+=" ? ";
				value.add(bean.getQualityMethod());
			};
			
			String createSql ="insert into subtask ("+ column+") values("+values+")";
			log.info("insertSubtask createSql:" + createSql);
			run.update(conn, createSql,value.toArray());
		}catch(Exception e){
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
			Map<Integer,Integer> gridIds = bean.gridIdMap();
			if(gridIds!=null&&gridIds.size()!=0){
				insertSubtaskGridMapping(conn,bean.getSubtaskId(),gridIds);
			}

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param subtaskId
	 * @param gridIdsToInsert
	 * @throws Exception 
	 */
	public static void insertSubtaskGridMapping(Connection conn, Integer subtaskId,
			Map<Integer, Integer> gridIds) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "insert into SUBTASK_GRID_MAPPING (SUBTASK_ID, GRID_ID,TYPE) VALUES (?,?,?)";
			Object[][] inParam = new Object[gridIds.size()][];
			int i = 0;
			for(Map.Entry<Integer, Integer> entry:gridIds.entrySet()){
				Object[] temp = new Object[3];
				temp[0] = subtaskId;
				temp[1] = entry.getKey();
				temp[2] = entry.getValue();
				inParam[i] = temp;
				i++;
			}
			run.batch(conn, createMappingSql, inParam);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}
	
	/**
	 * web端对于通过不规则任务圈创建的常规子任务，可能会出现grid计算超出block范围的情况（web无法解决），在此处进行二次处理
	 * @param conn
	 * @param subtaskId
	 * @param gridIdsToInsert
	 * @throws Exception 
	 */
	public static List<Integer> checkSubtaskGridMapping(Connection conn, Subtask bean) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String sql = "SELECT G.GRID_ID"
					+ "  FROM SUBTASK_GRID_MAPPING G, SUBTASK S, TASK T"
					+ " WHERE G.SUBTASK_ID = "+bean.getSubtaskId()
					+ "   AND G.SUBTASK_ID = S.SUBTASK_ID"
					+ "   AND S.TASK_ID = T.TASK_ID"
					+ "   AND T.BLOCK_ID != 0"
					+ " MINUS"
					+ " SELECT GRID_ID FROM TASK_GRID_MAPPING WHERE TASK_ID = "+bean.getTaskId();
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> grids=new ArrayList<Integer>();
					while (rs.next()) {
						grids.add(rs.getInt("GRID_ID"));
					}
					return grids;
				}

			};
			log.info("checkSubtaskGridMapping-sql:"+sql);
			List<Integer> grids= run.query(conn, sql, rsHandler);
			if(grids==null||grids.size()==0){return grids;}
			//存在block外的grid，需删除
			sql="DELETE FROM SUBTASK_GRID_MAPPING WHERE GRID_ID IN "+grids.toString().replace("[", "(").replace("]", ")");
			run.execute(conn, sql);
			return grids;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}


//	/**
//	 * @param conn
//	 * @param bean
//	 * @param curPageNum
//	 * @param pageSize
//	 * @param platForm 
//	 * @return
//	 * @throws Exception 
//	 */
//	public static Page getListByUserSnapshotPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			String selectSql = "select st.SUBTASK_ID ,st.NAME,st.geometry"
//					+ ",st.DESCP,st.PLAN_START_DATE,st.PLAN_END_DATE"
//					+ ",st.STAGE,st.TYPE,st.STATUS"
//					+ ",r.DAILY_DB_ID,r.MONTHLY_DB_ID";
//
//			String fromSql_task = " from subtask st,task t,city c,region r";
//
//			String fromSql_block = " from subtask st,block_man bm,block b,region r";
//
//			String conditionSql_task = " where st.task_id = t.task_id "
//					+ "and t.city_id = c.city_id "
//					+ "and c.region_id = r.region_id "
//					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
////					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";
//
//			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
//					+ "and b.region_id = r.region_id "
//					+ "and bm.block_id = b.block_id "
//					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
////					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";
//
//			if (bean.getStage() != null) {
//				conditionSql_task = conditionSql_task + " and st.STAGE = "
//						+ bean.getStage();
//				conditionSql_block = conditionSql_block + " and st.STAGE = "
//						+ bean.getStage();
//			}else{
//				if(0 == platForm){
//					//采集端
//					conditionSql_task = conditionSql_task + " and st.STAGE in (0) ";
//					conditionSql_block = conditionSql_block + " and st.STAGE in (0) ";
//				}else if(1 == platForm){
//					//编辑端
//					conditionSql_task = conditionSql_task + " and st.STAGE in (1,2) ";
//					conditionSql_block = conditionSql_block + " and st.STAGE in (1,2) ";
//				}
//			}
//
//			if (bean.getType() != null) {
//				conditionSql_task = conditionSql_task + " and st.TYPE = "
//						+ bean.getType();
//				conditionSql_block = conditionSql_block + " and st.TYPE = "
//						+ bean.getType();
//			}
//
//			if (bean.getStatus() != null) {
//				conditionSql_task = conditionSql_task + " and st.STATUS = "
//						+ bean.getStatus();
//				conditionSql_block = conditionSql_block + " and st.STATUS = "
//						+ bean.getStatus();
//			}
//
//
//			selectSql = selectSql + fromSql_task + conditionSql_task
//						+ " union all " + selectSql
//						+ fromSql_block + conditionSql_block;
//			
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
//					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
//					Page page = new Page(curPageNum);
//				    page.setPageSize(pageSize);
//				    int total = 0;
//					while (rs.next()) {
//						log.debug("start subtask");
//						if(total==0){
//							total=rs.getInt("TOTAL_RECORD_NUM_");
//						}
//						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
//						
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("name", rs.getString("NAME"));
//						subtask.put("descp", rs.getString("DESCP"));
//
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
//						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
//						subtask.put("status", rs.getInt("STATUS"));
//						//版本信息
//						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						
//						if (1 == rs.getInt("STAGE")) {
//							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
//						} else if (2 == rs.getInt("STAGE")) {
//							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
//						} else {
//							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
//						}
//
//						//日编POI,日编一体化GRID粗编完成度，任务量信息
//						if((1==rs.getInt("STAGE")&&0==rs.getInt("TYPE"))||(1==rs.getInt("STAGE")&&3==rs.getInt("TYPE"))){
//							try {
//								STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//								String wkt="";
//								try {
//									wkt=GeoTranslator.struct2Wkt(struct);
//								} catch (Exception e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								}
//								//log.info("get gridIds");
//								//List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//								//Map<String,Integer> subtaskStat = subtaskStatRealtime((
//								//List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//								log.debug("get stat");
//								Map<String,Integer> subtaskStat = subtaskStatRealtime((int)subtask.get("dbId"),rs.getInt("TYPE"),wkt);
//								if(subtaskStat != null){
//									if(subtaskStat.containsKey("poiFinish")){
//										subtask.put("poiFinish",subtaskStat.get("poiFinish"));
//										subtask.put("poiTotal",subtaskStat.get("poiTotal"));
//									}
//									if(subtaskStat.containsKey("tipsFinish")){
//										subtask.put("tipsFinish",subtaskStat.get("tipsFinish"));
//										subtask.put("tipsTotal",subtaskStat.get("tipsTotal"));
//									}
//								}else{
//									subtask.put("poiFinish",0);
//									subtask.put("poiTotal",0);
//									subtask.put("tipsFinish",0);
//									subtask.put("tipsTotal",0);
//								}
//								log.info("end stat");
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						
//						list.add(subtask);
//						log.debug("end subtask");
//					}
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//
//			};
//			log.info("getListByUserSnapshotPage-sql:"+selectSql);
//			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
//
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
//		}
//	}

	/**
	 * @param conn
	 * @param dataJson
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserSnapshotPage(Connection conn, JSONObject dataJson, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			
			String groupSql="";
			if(dataJson.containsKey("exeGroupId")&&!dataJson.getJSONArray("exeGroupId").isEmpty()){
				groupSql=" OR st.EXE_GROUP_ID in "+dataJson.getJSONArray("exeGroupId").toString().replace("[", "(").replace("]", ")");
			}
						
			sb.append("select st.SUBTASK_ID ,st.task_id,st.NAME,st.geometry,st.DESCP,st.PLAN_START_DATE,st.PLAN_END_DATE,st.STAGE,"
					+ "st.TYPE,st.STATUS,r.DAILY_DB_ID,r.MONTHLY_DB_ID,st.is_quality,p.type program_type,st.exe_user_id,st.work_kind");
			sb.append(" from subtask st,task t,region r,program p");
			sb.append(" where st.task_id = t.task_id");
			sb.append(" and t.region_id = r.region_id");
			sb.append(" and t.program_id = p.program_id");
//			if(dataJson.containsKey("lot") && StringUtils.isNotBlank(dataJson.get("lot").toString())){
//				sb.append(" and t.lot = "+dataJson.getInt("lot"));
//			}
			sb.append(" and (st.EXE_USER_ID = " + dataJson.getInt("exeUserId") + groupSql + ")");

			if (dataJson.containsKey("stage")){
				sb.append(" and st.STAGE = "+ dataJson.getInt("stage"));
			}else{
				if(0 == platForm){
					//采集端
					sb.append(" and st.STAGE in (0) ");
				}
//				else if(1 == platForm){
//					//编辑端
//					sb.append(" and st.STAGE in (1,2) ");
//				}
			}

			if (dataJson.containsKey("type")) {
				sb.append(" and st.TYPE = "+ dataJson.getInt("type"));
			}

			if (dataJson.containsKey("status")) {
				sb.append(" and st.status = "+ dataJson.getInt("status"));
			}


			String selectSql = sb.toString();
			log.info("getListByUserSnapshotPage-sql:"+selectSql);
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
						subtask.put("isQuality", rs.getInt("IS_QUALITY"));
						subtask.put("programType", rs.getInt("PROGRAM_TYPE"));
						subtask.put("workKind", rs.getInt("work_kind"));
						//版本信息
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						
						if (1 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						}

						//采集poi,采集一体化，日编grid粗编，质检日编grid粗编，质检日编区域粗编
						if(0==rs.getInt("TYPE")||3==rs.getInt("TYPE")||4==rs.getInt("TYPE")||2==rs.getInt("TYPE")||(1==rs.getInt("IS_QUALITY")&&1==rs.getInt("STAGE")&&(3==rs.getInt("TYPE")||4==rs.getInt("TYPE")))){
							try {
								STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
								String wkt="";
								try {
									wkt=GeoTranslator.struct2Wkt(struct);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								Subtask subtaskObj=new Subtask();
								subtaskObj.setDbId((int)subtask.get("dbId"));
								subtaskObj.setType(rs.getInt("TYPE"));
								subtaskObj.setGeometry(wkt);
								subtaskObj.setSubtaskId((int)subtask.get("subtaskId"));
								subtaskObj.setTaskId(rs.getInt("TASK_ID"));
								subtaskObj.setIsQuality(rs.getInt("IS_QUALITY"));
								subtaskObj.setExeUserId(rs.getInt("exe_user_id"));

								log.debug("get stat");
								Map<String,Integer> subtaskStat = subtaskStatRealtime(subtaskObj);
								if(subtaskStat != null){
//									if(subtaskStat.containsKey("poiCommit")){
									if(rs.getInt("TYPE") == 0 || rs.getInt("TYPE") == 2){
										subtask.put("poiCommit",subtaskStat.get("poiCommit"));
										subtask.put("poiWorked",subtaskStat.get("poiWorked"));
										subtask.put("poiWaitWork",subtaskStat.get("poiWaitWork"));
									}
//									}
									if(subtaskStat.containsKey("tipsPrepared")){
										//subtask.put("tipsPrepared",subtaskStat.get("tipsPrepared"));
										subtask.put("tipsPrepared",subtaskStat.get("tipsPrepared"));
										subtask.put("tipsTotal",subtaskStat.get("tipsTotal"));
									}
								}else{
									subtask.put("poiWaitWork",0);
									subtask.put("poiWorked",0);
									subtask.put("poiCommit",0);
									subtask.put("tipsPrepared",0);
									subtask.put("tipsTotal",0);
								}
								log.info("end stat");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						//日编道路子任务的质检任务需要获取质检量的统计
						if(1==rs.getInt("IS_QUALITY")&&1==rs.getInt("STAGE")&&(3==rs.getInt("TYPE")||4==rs.getInt("TYPE"))){
							try {
								FccApi fccApi=(FccApi) ApplicationContextUtil.getBean("fccApi");
								Map<String, Integer> checkMap = fccApi.getCheckTaskCount((int)subtask.get("subtaskId"));
								subtask.put("checkCount",checkMap.get("checkCount"));
								subtask.put("tipsTypeCount",checkMap.get("tipsTypeCount"));
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
			
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param dataJson
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserPage(Connection conn, JSONObject dataJson, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			final StringBuilder sb = new StringBuilder();
			
			String groupSql="";
			if(dataJson.containsKey("exeGroupId")&&!dataJson.getJSONArray("exeGroupId").isEmpty()){
				groupSql=" OR st.EXE_GROUP_ID in "+dataJson.getJSONArray("exeGroupId").toString().replace("[", "(").replace("]", ")");
			}
			
			sb.append("SELECT ST.SUBTASK_ID");
			sb.append(" ,ST.NAME");
			sb.append(" ,ST.DESCP");
			sb.append(" ,ST.PLAN_START_DATE");
			sb.append(" ,ST.PLAN_END_DATE");
			sb.append(" ,ST.STAGE");
			sb.append(" ,ST.TYPE");
			sb.append(" ,ST.task_id,st.is_quality");
			sb.append(" ,ST.STATUS");
			sb.append(" ,ST.GEOMETRY");
			sb.append(" ,ST.EXE_USER_ID");
			sb.append(" ,ST.EXE_GROUP_ID");
			sb.append(" ,R.DAILY_DB_ID");
			sb.append(" ,R.MONTHLY_DB_ID");
			sb.append(" ,RR.GEOMETRY REFER_GEOMETRY");
			sb.append(" FROM SUBTASK ST, TASK T, REGION R, SUBTASK_REFER RR");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID");
			sb.append(" AND T.REGION_ID = R.REGION_ID");
			sb.append(" AND ST.REFER_ID = RR.ID(+)");
			sb.append(" AND (ST.EXE_USER_ID = " + dataJson.getInt("exeUserId") + groupSql+ ")");
			
			if (dataJson.containsKey("stage")) {
				sb.append(" AND ST.STAGE = " + dataJson.getInt("stage"));
			}else{
				if(0 == platForm){//采集端
					sb.append(" AND ST.STAGE = 0");
				}
//				else if(1 == platForm){//编辑端
//					sb.append(" AND ST.STAGE IN (1,2) ");
//				}
			}

			if (dataJson.containsKey("type")) {
				sb.append(" AND T.TYPE = "+ dataJson.getInt("type"));
			}

			if (dataJson.containsKey("status")) {
				sb.append(" AND ST.STATUS = "+ dataJson.getInt("status"));
			}else{
				if(0 == platForm){//采集端
					sb.append(" AND ST.STATUS IN (0,1)");
				}
			}
			sb.append(" ORDER BY SUBTASK_ID DESC");
			
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
						subtask.put("isQuality", rs.getInt("IS_QUALITY"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						
						//版本信息
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
						
						List<Integer> gridIds = new ArrayList<Integer>();
						try {
							Map<Integer,Integer> gridIdMap = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							gridIds.addAll(gridIdMap.keySet());
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
						
							
						list.add(subtask);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
			};
			
			String selectSql = sb.toString();
			log.info("getListByUserPage-selectSql"+selectSql);
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
		}catch(Exception e){
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
	protected static Map<String, Integer> subtaskStatRealtime(Subtask subtask) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(subtask.getDbId());
			Map<String, Integer> stat = new HashMap<String, Integer>();
			//Map<String, Integer> taskAndType = SubtaskService.getInstance().getTaskBySubtaskId(subtask.getSubtaskId());

			log.debug("get poi stat");
			//查询POI总量
			QueryRunner run = new QueryRunner();
			String sql = "select pes.status, count(1) finishNum"
					+ " from ix_poi ip, poi_edit_status pes"
					+ " where ip.pid = pes.pid"
					+ " and pes.status ！= 0"
					+ " and (pes.quick_subtask_id="+subtask.getSubtaskId()+" or pes.medium_subtask_id="+subtask.getSubtaskId()+")"
					//+ " AND sdo_within_distance(ip.geometry, sdo_geometry('"+ wkt + "', 8307), 'mask=anyinteract') = 'TRUE' "
					+ "group by pes.status ";
			//POI待作业
			stat = run.query(conn, sql,new ResultSetHandler<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> stat = new HashMap<String, Integer>();
//					int finish = 0;
//					int total=0;
					int poiCommit = 0;
					int poiWorked = 0;
					int poiWaitWork = 0;
					while(rs.next()){
						int status = rs.getInt("status");
						if(status == 1){poiWaitWork = rs.getInt("finishNum");};
						if(status == 2){poiWorked = rs.getInt("finishNum");};
						if(status == 3){poiCommit = rs.getInt("finishNum");};
//						if(status==3){finish = rs.getInt("finishNum");}
//						total+=rs.getInt("finishNum");
					}
//					stat.put("poiFinish", finish);
//					stat.put("poiTotal", total);
					stat.put("poiCommit", poiCommit);
					stat.put("poiWorked", poiWorked);
					stat.put("poiWaitWork", poiWaitWork);
					return stat;
				}
			}
			);
			//type=3,一体化grid粗编子任务。增加道路数量及完成度
			log.debug("get tips stat");
			if(3 == subtask.getType()||4 == subtask.getType()){
				FccApi api=(FccApi) ApplicationContextUtil.getBean("fccApi");
				Set<Integer> collectTaskId = TaskService.getInstance().getCollectTaskIdsByTaskId(subtask.getTaskId());
				JSONObject resultRoad = api.getSubTaskStatsByWkt(subtask.getSubtaskId(), subtask.getGeometry(), subtask.getType(), subtask.getExeUserId(), subtask.getIsQuality());
//				int tips = resultRoad.getInt("total") + resultRoad.getInt("finished");
				stat.put("tipsPrepared", resultRoad.getInt("prepared"));
				stat.put("tipsTotal", resultRoad.getInt("total"));
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


//	/**
//	 * @param conn
//	 * @param bean
//	 * @param curPageNum
//	 * @param pageSize
//	 * @param platForm 
//	 * @return
//	 * @throws Exception 
//	 */
//	public static Page getListByUserPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			
//			StringBuilder sb = new StringBuilder();
//			
//			sb.append("select st.SUBTASK_ID ,st.NAME,st.geometry,st.DESCP,st.PLAN_START_DATE,st.PLAN_END_DATE,st.STAGE,st.TYPE,st.STATUS,r.DAILY_DB_ID,r.MONTHLY_DB_ID");
//			sb.append(" from subtask st,task t,region r");
//			sb.append(" where st.task_id = t.task_id");
//			sb.append(" and t.region_id = r.region_id");
//			sb.append(" and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")");
//			
//			String selectSql = "SELECT *"
//					+ "  FROM (SELECT ST.SUBTASK_ID,"
//					+ "               ST.NAME,"
//					+ "               ST.DESCP,"
//					+ "               ST.PLAN_START_DATE,"
//					+ "               ST.PLAN_END_DATE,"
//					+ "               ST.STAGE,"
//					+ "               ST.TYPE,"
//					+ "               ST.STATUS,"
//					+ "               ST.GEOMETRY,"
//					+ "               ST.EXE_USER_ID,"
//					+ "               ST.EXE_GROUP_ID,"
//					+ "               R.DAILY_DB_ID,"
//					+ "               R.MONTHLY_DB_ID,"
//					+ "               RR.GEOMETRY REFER_GEOMETRY"
//					+ "          FROM SUBTASK ST, TASK T, CITY C, REGION R, SUBTASK_REFER RR"
//					+ "         WHERE ST.TASK_ID = T.TASK_ID"
//					+ "           AND T.CITY_ID = C.CITY_ID"
//					+ "           AND C.REGION_ID = R.REGION_ID"
//					+ "           AND ST.REFER_ID = RR.ID(+)"
//					+ "        UNION ALL"
//					+ "        SELECT ST.SUBTASK_ID,"
//					+ "               ST.NAME,"
//					+ "               ST.DESCP,"
//					+ "               ST.PLAN_START_DATE,"
//					+ "               ST.PLAN_END_DATE,"
//					+ "               ST.STAGE,"
//					+ "               ST.TYPE,"
//					+ "               ST.STATUS,"
//					+ "               ST.GEOMETRY,"
//					+ "               ST.EXE_USER_ID,"
//					+ "               ST.EXE_GROUP_ID,"
//					+ "               R.DAILY_DB_ID,"
//					+ "               R.MONTHLY_DB_ID,"
//					+ "               RR.GEOMETRY REFER_GEOMETRY"
//					+ "          FROM SUBTASK ST, BLOCK_MAN BM, BLOCK B, REGION R, SUBTASK_REFER RR"
//					+ "         WHERE ST.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//					+ "           AND BM.BLOCK_ID = B.BLOCK_ID"
//					+ "           AND B.REGION_ID = R.REGION_ID"
//					+ "           AND ST.REFER_ID = RR.ID(+)) T"
//					+ " WHERE 1 = 1";
//			String conditonSql=" AND (T.EXE_USER_ID = " + bean.getExeUserId() 
//					+ " or T.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//			if (bean.getStage() != null) {
//				conditonSql+=" and T.STAGE = "+ bean.getStage();
//			}else{
//				if(0 == platForm){//采集端
//					conditonSql+=" and T.STAGE = 0";
//				}else if(1 == platForm){//编辑端
//					conditonSql+=" and T.STAGE in (1,2) ";
//				}
//			}
//
//			if (bean.getType() != null) {
//				conditonSql+=" and t.TYPE = "+ bean.getType();
//			}
//
//			if (bean.getStatus() != null) {
//				conditonSql+=" and t.STATUS = "+ bean.getStatus();
//			}else{
//				if(0 == platForm){//采集端
//					conditonSql+=" and t.STATUS in (0,1)";
//				}
//			}
//			selectSql+=conditonSql;
//			selectSql+=conditonSql+" order by subtask_id desc";
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
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
//						
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("name", rs.getString("NAME"));
//						subtask.put("descp", rs.getString("DESCP"));
//
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
//						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
//						subtask.put("status", rs.getInt("STATUS"));
//						
//						//版本信息
//						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						
//						
//						
//						List<Integer> gridIds = null;
//						try {
//							gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						subtask.put("gridIds", gridIds);
//						
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						String wkt="";
//						try {
//							wkt=GeoTranslator.struct2Wkt(struct);
//							subtask.put("geometry", wkt);
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//
//						if (1 == rs.getInt("STAGE")) {
//							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
//						} else if (2 == rs.getInt("STAGE")) {
//							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
//						} else if (0 == rs.getInt("STAGE")) {
//							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
//							STRUCT structRefer = (STRUCT) rs.getObject("REFER_GEOMETRY");
//							try {
//								subtask.put("referGeometry", GeoTranslator.struct2Wkt(structRefer));
//							} catch (Exception e1) {
//								e1.printStackTrace();
//								subtask.put("referGeometry", subtask.get("geometry"));
//							}
//							JSONArray referGrid;
//							try {
//								referGrid = SubtaskOperation.getReferSubtasksByGridIds(rs.getInt("SUBTASK_ID"),gridIds,GeoTranslator.wkt2Geometry((String) subtask.get("referGeometry")));
//								subtask.put("referGrid", referGrid);
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}	
//						}else {
//							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));				
//						}
//						
//						//日编POI,日编一体化GRID粗编完成度，任务量信息
//						if((1==rs.getInt("STAGE")&&0==rs.getInt("TYPE"))||(1==rs.getInt("STAGE")&&3==rs.getInt("TYPE"))){
//							try {
//								Map<String,Integer> subtaskStat = subtaskStatRealtime((int)subtask.get("dbId"),rs.getInt("TYPE"),wkt);
//								if(subtaskStat != null){
//									if(subtaskStat.containsKey("poiFinish")){
//										subtask.put("poiFinish",subtaskStat.get("poiFinish"));
//										subtask.put("poiTotal",subtaskStat.get("poiTotal"));
//									}
//									if(subtaskStat.containsKey("tipsFinish")){
//										subtask.put("tipsFinish",subtaskStat.get("tipsFinish"));
//										subtask.put("tipsTotal",subtaskStat.get("tipsTotal"));
//									}
//								}else{
//									subtask.put("poiFinish",0);
//									subtask.put("poiTotal",0);
//									subtask.put("tipsFinish",0);
//									subtask.put("tipsTotal",0);
//								}
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}		
//						list.add(subtask);
//					}
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//			};
//			log.info("getListByUserPage-selectSql"+selectSql);
//			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
//		}
//
//	}


	/**
	 * @param int1
	 * @return
	 * @throws Exception 
	 */
	public static Map<Integer,Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception {
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
	public static List<Integer> getGridIdListBySubtaskId(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			Map<Integer,Integer> gridIds = getGridIdsBySubtaskIdWithConn(conn, subtaskId);
			List<Integer> result = new ArrayList<Integer>();
			result.addAll(gridIds.keySet());
			return result;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	
	/**
	 * @param conn
	 * @param subtaskId
	 * @return:Map<gridId:type>
	 * @throws Exception 
	 */
	public static Map<Integer,Integer> getGridIdsBySubtaskIdWithConn(Connection conn,int subtaskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select sgm.grid_id,sgm.type from subtask_grid_mapping sgm where sgm.subtask_id = " + subtaskId;

			ResultSetHandler<Map<Integer,Integer>> rsHandler = new ResultSetHandler<Map<Integer,Integer>>() {
				public Map<Integer,Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> gridIds = new HashMap<Integer,Integer>();
					while (rs.next()) {
						gridIds.put(rs.getInt("grid_id"), rs.getInt("type"));
		
					}
					return gridIds;
				}
			};
			log.info("getGridIdsBySubtaskIdWithConn sql:" + selectSql);
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
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
			log.info("getSubtaskIdsByGridIdWithConn sql:" + selectSql);
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


//	/**
//	 * 根据taskId获取city几何
//	 * @param taskId
//	 * @return
//	 * @throws Exception 
//	 */
//	public static String getWktByTaskId(int taskId) throws Exception {
//		// TODO Auto-generated method stub
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			String selectSql = "SELECT C.GEOMETRY FROM TASK T, CITY C WHERE T.CITY_ID = C.CITY_ID AND T.LATEST = 1 AND T.TASK_ID = " + taskId;
//			
//			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
//				public String handle(ResultSet rs) throws SQLException {
//					String wkt = null; 
//					if(rs.next()) {
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						try {
//							wkt = GeoTranslator.struct2Wkt(struct);
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//					}
//					return wkt;
//				}
//			};
//			return run.query(conn, selectSql, rsHandler);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询task几何失败，原因为:"+e.getMessage(),e);
//		}finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}


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
			//收信人列表
			List<UserInfo> receiverList = new ArrayList<UserInfo>();
			if(subtask.getExeUserId()!=0){
				UserInfo receiver =  UserInfoOperation.getUserInfoByUserId(conn, subtask.getExeUserId());
				receiverList.add(receiver);
			}else if(subtask.getExeGroupId()!=0){
				UserGroup bean = new UserGroup();
				bean.setGroupId(subtask.getExeGroupId());
				receiverList = UserInfoService.getInstance().list(conn,bean);
			}
			if(receiverList==null||receiverList.size()==0){return;}
			UserInfo pushObj = UserInfoOperation.getUserInfoByUserId(conn, userId);
			//发送消息
			for(UserInfo receiver:receiverList){
					/*采集/日编/月编子任务发布
					 * 分配的作业员
					 * 新增采集/日编/月编子任务：XXX(子任务名称)，请关注*/
					String msgTitle = "";
					String msgContent = "";
					//2给web发消息，1给手持端发消息
					int pushtype=2;
					if(subtask.getStage()== 0){
						pushtype=1;
						msgTitle = "采集子任务发布";
						msgContent = "新增采集子任务:" + subtask.getName() + ",请关注";
					}else if(subtask.getStage()== 1){
						msgTitle = "日编子任务发布";
						msgContent = "新增日编子任务:" + subtask.getName() + ",请关注";
					}else{
						msgTitle = "月编子任务发布";
						msgContent = "新增月编子任务:" + subtask.getName() + ",请关注";
					}
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "SUBTASK");
					msgParam.put("relateObjectId", subtask.getSubtaskId());
					
					Message message = new Message();
					message.setMsgTitle(msgTitle);
					message.setMsgContent(msgContent);
					message.setPushUserId(pushObj.getUserId());
					message.setReceiverId(receiver.getUserId());
					message.setMsgParam(msgParam.toString());
					message.setPushUser(pushObj.getUserRealName());
					
					MessageService.getInstance().push(message, pushtype);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("推送消息失败，原因为:"+e.getMessage(),e);
		}
	}

//	/**
//	 * @param conn 
//	 * @param userId 
//	 * @param bean
//	 * @throws Exception 
//	 */
//	public static void pushMessage(Connection conn, Subtask subtask, long userId) throws Exception {
//		// TODO Auto-generated method stub
//		try{
//			//List<Integer> userIdList = new ArrayList<Integer>();
//			//作业组
//			/*
//			if(subtask.getExeGroupId()!=0){
//				userIdList = SubtaskOperation.getUserListByGroupId(conn,subtask.getExeGroupId());
//			}else{
//				userIdList.add(subtask.getExeUserId());
//			}
//			*/
//			//构造消息
//			/*采集/日编/月编子任务编辑
//			 * 分配的作业员
//			 * 采集/日编/月编子任务变更：XXX(子任务名称)信息发生变更，请关注*/
//			String msgTitle = "";
//			String msgContent = "";
//			//2web,1手持端消息
//			int pushtype=2;
//			if((int)subtask.getStage()== 0){
//				pushtype=1;
//				msgTitle = "采集子任务编辑";
//				msgContent = "采集子任务变更:" + subtask.getName() + "内容发生变更,请关注";
//			}else if((int)subtask.getStage()== 1){
//				msgTitle = "日编子任务编辑";
//				msgContent = "日编子任务变更:" + subtask.getName() + "内容发生变更,请关注";
//			}else{
//				msgTitle = "月编子任务编辑";
//				msgContent = "月编子任务变更:" + subtask.getName() + "内容发生变更,请关注";
//			}
//			//关联要素
//			JSONObject msgParam = new JSONObject();
//			msgParam.put("relateObject", "SUBTASK");
//			msgParam.put("relateObjectId", subtask.getSubtaskId());
//			//查询用户名称
//			UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn, subtask.getExeUserId());
//			String pushUserName = null;
//			if(userInfo != null && userInfo.getUserRealName()!=null){
//				pushUserName = (String) userInfo.getUserRealName();
//			}
//			
//			Message message = new Message();
//			message.setMsgTitle(msgTitle);
//			message.setMsgContent(msgContent);
//			message.setPushUserId((int)userId);
//			message.setReceiverId(subtask.getExeUserId());
//			message.setMsgParam(msgParam.toString());
//			message.setPushUser(pushUserName);
//			
//			MessageService.getInstance().push(message, pushtype);
//		}catch(Exception e){
//			log.error(e.getMessage(), e);
//			throw new Exception("推送消息失败，原因为:"+e.getMessage(),e);
//		}
//	}


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
			//boolean collectAndDay=true;
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("blockManId".equals(key)) {conditionSql+=" AND subtask_list.BLOCK_MAN_ID="+condition.getInt(key);}
				if ("taskId".equals(key)) {
					conditionSql+=" AND subtask_list.TASK_ID="+condition.getInt(key);
				}
				if ("stage".equals(key)) {
					//collectAndDay=false;
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
//			if (collectAndDay){conditionSql+=" AND subtask_list.stage IN (0,1)";}
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
			log.info("subtask getList sql:" + sql);
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


//	/**
//	 * @Title: getListByGroup
//	 * @Description: 根据作业组获取子任务列表（修改）(第七迭代)
//	 * @param conn
//	 * @param groupId
//	 * @param stage
//	 * @param conditionJson
//	 * @param orderJson
//	 * @param pageSize
//	 * @param curPageNum
//	 * @return  (增加返回值:qualitySubtaskId,qualityExeUserId, qualityPlanStartDate, qualityPlanEndDate)
//	 * @throws ServiceException  Page
//	 * @throws 
//	 * @author zl zhangli5174@navinfo.com
//	 * @date 2016年11月4日 下午1:58:11 
//	 */
//	public static Page getListByGroup(Connection conn, long groupId, int stage, JSONObject conditionJson,
//			JSONObject orderJson, final int pageSize, final int curPageNum) throws ServiceException {
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
//				/*selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
//						+ " WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.COLLECT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;*/
//				selectUserSql = "SELECT "
//						+ " S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
//						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
//						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S "
//						//左外关联 质检子任务表
//						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
//						+ "	BLOCK B, USER_INFO U, BLOCK_MAN BM"
//						+ " WHERE "
//						+ " S.is_quality = 0" //排除 Subtask 表中的质检子任务
//						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.COLLECT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//			} else if (1 == stage) {
//				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
//						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
//						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME, U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S "
//						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
//						+ " BLOCK B, USER_INFO U, BLOCK_MAN BM"
//						+ " WHERE "
//						+ " S.is_quality = 0" 
//						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//				selectGroupSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY,"
//						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
//						+ " B.BLOCK_ID, BM.BLOCK_MAN_ID,BM.BLOCK_MAN_NAME ,UG1.GROUP_NAME AS EXECUTER"
//						+ " FROM SUBTASK S "
//						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
//						+ " BLOCK B, BLOCK_MAN BM, USER_GROUP UG1"
//						+ " WHERE "
//						+ " S.is_quality = 0" 
//						+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
//						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
//						+ " AND BM.LATEST = 1"
//						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//			} else if (2 == stage) {
//				selectUserSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,"
//						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
//						+ " T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,U.USER_REAL_NAME AS EXECUTER"
//						+ " FROM SUBTASK S "
//						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
//						+ " USER_INFO U, TASK T"
//						+ " WHERE "
//						+ " S.is_quality = 0" 
//						+ " AND S.TASK_ID = T.TASK_ID"
//						+ " AND U.USER_ID = S.EXE_USER_ID"
//						+ " AND T.LATEST = 1"
//						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
//						+ " AND S.STAGE = " + stage;
//				
//				selectGroupSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,"
//						+ "	NVL(S.quality_Subtask_Id,0) qualitySubtaskId,NVL(Q.qualityPlanStartDate,NULL) qualityPlanStartDate ,NVL(Q.qualityPlanEndDate,NULL) qualityPlanEndDate ,NVL(Q.qualityExeUserId,0) qualityExeUserId, " //新增加返回值
//						+ " T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,UG1.GROUP_NAME AS EXECUTER"
//						+ " FROM SUBTASK S "
//						+ " left join (select st.SUBTASK_ID ,st.EXE_USER_ID qualityExeUserId,st.PLAN_START_DATE as qualityPlanStartDate,st.PLAN_END_DATE as qualityPlanEndDate from subtask st where st.is_quality = 1 ) Q  on S.quality_subtask_id = Q.subtask_id,"
//						+ " TASK T, USER_GROUP UG1"
//						+ " WHERE "
//						+ " S.is_quality = 0" 
//						+ " AND S.TASK_ID = T.TASK_ID"
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
//					if ("blockManName".equals(key)) {
//						extraConditionSql+=" AND S.BLOCK_ID = B.BLOCK_ID AND BM.BLOCK_MAN_NAME LIKE '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockManId".equals(key)) {extraConditionSql+=" AND S.BLOCK_MAN_ID = "+conditionJson.getInt(key);}
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
//					if ("blockManId".equals(key)) {orderSql+=" ORDER BY BLOCK_MAN_ID "+orderJson.getString(key);}
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
//						//**************zl 2016.11.04 ******************
//						subtask.put("qualitySubtaskId", rs.getInt("qualitySubtaskId"));
//						subtask.put("qualityExeUserId", rs.getInt("qualityExeUserId"));
//						subtask.put("qualityPlanStartDate", df.format(rs.getTimestamp("qualityPlanStartDate")));
//						subtask.put("qualityPlanEndDate", df.format(rs.getTimestamp("qualityPlanEndDate")));
//						
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
//							subtask.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
//							subtask.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
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
//	public static void closeBySubtaskId(int subtaskId) throws Exception {
//			// TODO Auto-generated method stub
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			ArrayList<Integer> closedSubtaskList = new ArrayList<Integer>();
//			closedSubtaskList.add(subtaskId);
//			closeBySubtaskList(conn, closedSubtaskList);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
//		}finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}


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
			
			List<Clob> values=new ArrayList<Clob>();
			if(geoWithNeighbor.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, geoWithNeighbor.toString().replace("[", "").replace("]", ""));
				gridIdsStr="select to_number(column_value) from table(clob_to_table(?))";
				values.add(clob);
			}
			
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
			PreparedStatement pstmt=conn.prepareStatement(selectSql);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			
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
					
			log.info("getReferSubtasksByGridIds sql:" + selectSql);
			return referSubtasks;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * @param conn
	 * @param subtaskId
	 * @throws Exception 
	 */
	public static int closeBySubtaskId(Connection conn, int subtaskId) throws Exception {
		try{
			ArrayList<Integer> closedSubtaskList = new ArrayList<Integer>();
			closedSubtaskList.add(subtaskId);
			return closeBySubtaskList(conn, closedSubtaskList);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
		
	}
	
//	20170705未使用方法/**
//	 * @param conn
//	 * @param subtaskId
//	 * @throws Exception 
//	 */
//	public static void closeBySubtaskId(int subtaskId) throws Exception {
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			ArrayList<Integer> closedSubtaskList = new ArrayList<Integer>();
//			closedSubtaskList.add(subtaskId);
//			closeBySubtaskList(conn, closedSubtaskList);
//		}catch(Exception e){
//			log.error(e.getMessage(), e);
//			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//		
//	}

	/*
	 * 查询大区库履历，获取子任务修改的POI几何列表
	 */
	public static Set<Integer> loadPoiGeoBySubtaskFromLog(Subtask subtask)throws Exception{
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getConnectionById(subtask.getDbId());
			String sqlString="SELECT DISTINCT G.GRID_ID"
					+ "  FROM LOG_ACTION A, LOG_OPERATION O, LOG_DETAIL_GRID G, LOG_DETAIL D"
					+ " WHERE A.ACT_ID = O.ACT_ID"
					+ "   AND G.LOG_ROW_ID = D.ROW_ID"
					+ "   AND D.OP_ID = O.OP_ID"
					+ "   AND A.STK_ID = "+subtask.getSubtaskId();
			
			log.info("loadPoiGeoBySubtaskFromLog SQL："+sqlString);
			ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> gridList = new HashSet<Integer>();
					while (rs.next()) {
						gridList.add(rs.getInt("GRID_ID"));
					}
					return gridList;
				}
			};
			return new QueryRunner().query(conn, sqlString, rsHandler);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	/*
	 * 查询大区库履历，获取子任务修改的POI几何列表
	 */
	public static Set<Integer> loadPoiGeoBySubtaskFromEdit(Subtask subtask)throws Exception{
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getConnectionById(subtask.getDbId());
			String sql="SELECT I.GEOMETRY"
					+ "  FROM POI_EDIT_STATUS S, IX_POI I"
					+ " WHERE S.PID = I.PID"
					+ "   AND (S.QUICK_SUBTASK_ID = "+subtask.getSubtaskId()+" OR S.MEDIUM_SUBTASK_ID = "+subtask.getSubtaskId()+")";
			
			log.info("loadPoiGeoBySubtaskFromEdit SQL："+sql);
			ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> gridIdList = new HashSet<Integer>();
					while (rs.next()) {
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							String wkt = GeoTranslator.struct2Wkt(struct);
							Geometry geo = GeoTranslator.wkt2Geometry(wkt);
							String[] grids = CompGridUtil.point2Grids(geo.getCoordinate().x, geo.getCoordinate().y);
							for(String grid:grids){
								gridIdList.add(Integer.parseInt(grid));
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return gridIdList;
				}
			};
			return new QueryRunner().query(conn, sql, rsHandler);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}


	/**
	 * @param subtask
	 * @return
	 * 根据subtask查询大区库，获取gridList
	 * @throws Exception 
	 */
	public static Map<Integer,Integer> getGridIdMapBySubtaskFromLog(Subtask subtask,int programType) throws Exception {
		//查询大区库履历，获取子任务数据几何列表
		Set<Integer> gridIdList = new HashSet<Integer>();
		if(subtask.getStage()==0){
			gridIdList = loadPoiGeoBySubtaskFromEdit(subtask);
			FccApi api=(FccApi) ApplicationContextUtil.getBean("fccApi");			
			Set<Integer> tipsGrids=api.getTipsGridsBySubtaskId(subtask.getSubtaskId(), programType);
			if(tipsGrids!=null&&tipsGrids.size()>0){
				log.info("子任务"+subtask.getSubtaskId()+"对应tips所在grid范围："+tipsGrids);
				gridIdList.addAll(tipsGrids);
			}
		}else{gridIdList = loadPoiGeoBySubtaskFromLog(subtask);}
		
		///获得需要调整的gridMap
		Map<Integer,Integer> gridIdsBefore = subtask.gridIdMap();
		Map<Integer,Integer> gridIdsToInsert = new HashMap<Integer,Integer>();
		for(Integer gridId:gridIdList){
			if(gridIdsBefore.containsKey(gridId)){
				continue;
			}else{
				gridIdsToInsert.put(gridId,2);
			}
		}
		return gridIdsToInsert;
	}

	public static int changeRegionSubtaskGridByTask(Connection conn,
			int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="INSERT INTO SUBTASK_GRID_MAPPING"
					+ "  (SUBTASK_ID, GRID_ID, TYPE)"
					+ "  SELECT S.SUBTASK_ID, GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, SUBTASK S"
					+ "   WHERE M.TASK_ID = "+taskId
					+ "     AND S.TASK_ID = M.TASK_ID"
					+ "     AND S.STATUS!=0"
					+ "     AND S.TYPE = 4"
					+ "  MINUS"
					+ "  SELECT S.SUBTASK_ID, GRID_ID, 2"
					+ "    FROM SUBTASK_GRID_MAPPING M, SUBTASK S"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND S.SUBTASK_ID = M.SUBTASK_ID"
					+ "     AND S.STATUS!=0"
					+ "     AND S.TYPE = 4";
			return run.update(conn, sql);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static List<Integer> getRegionSubtaskByTask(Connection conn,
			int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="  SELECT S.SUBTASK_ID"
					+ "    FROM SUBTASK S"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND S.STATUS!=0"
					+ "     AND S.TYPE = 4";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>(){

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> subtaskIds=new ArrayList<Integer>();
					while(rs.next()){
						subtaskIds.add(rs.getInt("SUBTASK_ID"));
					}
					return subtaskIds;
				}
				
			});	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static int changeDayRegionSubtaskByCollectTask(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "INSERT INTO SUBTASK_GRID_MAPPING"
					+ "  (SUBTASK_ID, GRID_ID, TYPE)"
					+ "  SELECT T.SUBTASK_ID, GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, TASK S, TASK UT, SUBTASK T"
					+ "   WHERE M.TASK_ID = "+taskId
					+ "     AND UT.TASK_ID = M.TASK_ID"
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND UT.LATEST = 1"
					+ "     AND UT.TYPE = 1"
					+ "     AND UT.TASK_ID = T.TASK_ID"
					+ "     AND T.STATUS!=0"
					+ "     AND T.TYPE = 4"
					+ "  MINUS"
					+ "  SELECT T.SUBTASK_ID, M.GRID_ID, 2"
					+ "    FROM SUBTASK_GRID_MAPPING M, TASK S, TASK UT, SUBTASK T"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND M.SUBTASK_ID = T.SUBTASK_ID"
					+ "     AND UT.LATEST = 1"
					+ "     AND UT.TYPE = 1"
					+ "     AND UT.TASK_ID = T.TASK_ID"
					+ "     AND T.STATUS!=0"
					+ "     AND T.TYPE = 4";
			return run.update(conn, createMappingSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}
	
	public static List<Integer> getDayRegionSubtaskByCollectTask(Connection conn,
			int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="  SELECT T.SUBTASK_ID"
					+ "    FROM TASK S, TASK UT, SUBTASK T"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND UT.LATEST = 1"
					+ "     AND UT.TYPE = 1"
					+ "     AND UT.TASK_ID = T.TASK_ID"
					+ "     AND T.STATUS!=0"
					+ "     AND T.TYPE = 4";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>(){

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> subtaskIds=new ArrayList<Integer>();
					while(rs.next()){
						subtaskIds.add(rs.getInt("SUBTASK_ID"));
					}
					return subtaskIds;
				}
				
			});	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 获取对应采集/日编子任务对应的同任务下的快线月编子任务
	 * @param Connection
	 * @param int
	 * @throws Exception
	 * 
	 * */
	public static List<Integer> getMonthSubtaskByTask(Connection conn, int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="  SELECT T.SUBTASK_ID"
					+ "    FROM TASK S, TASK UT, SUBTASK T, PROGRAM P"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND P.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND T.TASK_ID = UT.TASK_ID"
					+ "     AND T.STAGE = 2"
					+ "     AND P.TYPE = 4";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>(){

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> subtaskIds=new ArrayList<Integer>();
					while(rs.next()){
						subtaskIds.add(rs.getInt("SUBTASK_ID"));
					}
					return subtaskIds;
				}
				
			});	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * 快线：采集/日编子任务关闭进行动态调整，增加动态调整快线月编任务，月编子任务范围
	 * 根据任务修改月编子任务范围，快线月编子任务的范围和任务范围一致
	 * @param Connection
	 * @param taskId
	 * @return int
	 * @throws Exception
	 * 
	 * */
	public static int changeMonthSubtaskGridByTask(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String sql = "INSERT INTO SUBTASK_GRID_MAPPING"
					+ "  (SUBTASK_ID, GRID_ID, TYPE)"
					+ "  SELECT ST.SUBTASK_ID, M.GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, TASK T, TASK UT, SUBTASK ST, PROGRAM P"
					+ "   WHERE T.TASK_ID = "+taskId
					+ "     AND UT.PROGRAM_ID = T.PROGRAM_ID"
					+ "     AND P.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND ST.TASK_ID = UT.TASK_ID"
					+ "     AND M.TASK_ID = UT.TASK_ID"
					+ "     AND P.TYPE = 4"
					+ "     AND ST.STAGE = 2"
					+ "  MINUS"
					+ "  SELECT S.SUBTASK_ID, T.GRID_ID, 2"
					+ "    FROM SUBTASK_GRID_MAPPING T, SUBTASK S, TASK P, TASK UT, PROGRAM M"
					+ "   WHERE P.TASK_ID = "+taskId
					+ "     AND UT.PROGRAM_ID = P.PROGRAM_ID"
					+ "     AND M.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND S.TASK_ID = UT.TASK_ID"
					+ "     AND T.SUBTASK_ID = S.SUBTASK_ID"
					+ "     AND M.TYPE = 4"
					+ "     AND S.STAGE = 2";
			log.info("根据任务调整月编子任务sql："+sql);
			return run.update(conn, sql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据质检子任务id，获取常规子任务相关信息
	 * @param Connection
	 * @param qualitySets
	 * @return Map<Integer, Map<String, String>>：key：具体的qualitySubtaskId，value：Map<String, String>常规子任务相关信息
	 * @throws Exception
	 * 
	 * */
	public static Map<Integer, Map<String, String>> getCommonByQuality(Connection conn,Set<Integer> qualitySets) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT S.SUBTASK_ID,S.QUALITY_SUBTASK_ID,s.status,"
					+ "       nvl(S.EXE_USER_ID,0) EXE_USER_ID,"
					+ "       I.USER_REAL_NAME,"
					+ "       nvl(T.GROUP_ID,0) GROUP_ID,"
					+ "       G.GROUP_NAME,"
					+ "       nvl(F.FINISHED_ROAD,0) FINISHED_ROAD,"
					+ "       S.NAME           SUBTASK_NAME,"
					+ "       T.NAME           TASK_NAME"
					+ "  FROM TASK                     T,"
					+ "       SUBTASK                  S,"
					+ "       USER_GROUP               G,"
					+ "       SUBTASK                  SQ,"
					+ "       USER_INFO                I,"
					+ "       FM_STAT_OVERVIEW_SUBTASK F"
					+ " WHERE S.TASK_ID = T.TASK_ID"
					+ "   AND T.GROUP_ID = G.GROUP_ID"
					+ "   AND S.EXE_USER_ID = I.USER_ID"
					+ "   AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
					+ "   AND S.QUALITY_SUBTASK_ID = SQ.SUBTASK_ID"
					+ "   AND SQ.SUBTASK_ID IN "+qualitySets.toString().replace("[", "(").replace("]", ")");
			log.info("getCommonSubtaskByQualitySubtask SQL："+selectSql);
			

			ResultSetHandler<Map<Integer, Map<String, String>>> rsHandler = new ResultSetHandler<Map<Integer, Map<String, String>>>() {
				public Map<Integer, Map<String, String>> handle(ResultSet rs) throws SQLException {
					Map<Integer, Map<String, String>> returnMap=new HashMap<Integer, Map<String, String>>();
					while (rs.next()) {
						Map<String, String> map=new HashMap<String, String>();
						map.put("subtaskId", rs.getString("SUBTASK_ID"));
						map.put("exeUserId", rs.getString("EXE_USER_ID"));
						map.put("exeUserName", rs.getString("USER_REAL_NAME"));
						map.put("groupId", rs.getString("GROUP_ID"));
						map.put("status",  rs.getString("STATUS"));
						map.put("groupName", rs.getString("GROUP_NAME"));
						map.put("finishedRoad", rs.getString("FINISHED_ROAD"));
						map.put("subtaskName", rs.getString("SUBTASK_NAME"));
						map.put("taskName", rs.getString("TASK_NAME"));
						returnMap.put(rs.getInt("QUALITY_SUBTASK_ID"), map);
					}
					return returnMap;
				}	
			};
			return run.query(conn, selectSql,rsHandler);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
//
//	/**
//	 * 调整子任务范围
//	 * @param conn 
//	 * @param subtask
//	 * @param gridIds
//	 * @throws ServiceException 
//	 */
//	public static void adjustSubtaskRegion(Connection conn, Subtask subtask, List<Integer> gridIds) throws ServiceException {
//		try{
//			Map<Integer,Integer> gridIdsBefore = subtask.getGridIds();
//			Map<Integer,Integer> gridIdsToInsert = null ;
//			for(Integer gridId:gridIds){
//				if(gridIdsBefore.containsKey(gridId)){
//					continue;
//				}else{
//					gridIdsToInsert.put(gridId,2);
//				}
//			}
//			if(gridIdsToInsert!=null&&gridIdsToInsert.size()!=0){
//				insertSubtaskGridMapping(conn,subtask.getSubtaskId(),gridIdsToInsert);
//			}
//		}catch (Exception e) {
//			log.error(e.getMessage(), e);
//			throw new ServiceException("子任务范围调整失败，原因为:" + e.getMessage(), e);
//		}
//		
//	}
}
