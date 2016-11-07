package com.navinfo.dataservice.engine.man.subtask;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;

/**
 * @ClassName: SubtaskService
 * @author code generator
 * @date 2016-06-06 07:40:14
 * @Description: TODO
 */

public class SubtaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private SubtaskService() {
	}

	private static class SingletonHolder {
		private static final SubtaskService INSTANCE = new SubtaskService();
	}

	public static SubtaskService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/*
	 * 创建一个子任务。 参数1：Subtask对象
	 */
	public void create(Subtask bean)throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			// 获取subtaskId
			int subtaskId = SubtaskOperation.getSubtaskId(conn, bean);

			bean.setSubtaskId(subtaskId);
			if(bean.getStatus()== null){
				bean.setStatus(1);
			}
			
			// 插入subtask
			SubtaskOperation.insertSubtask(conn, bean);
			
			// 插入SUBTASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			}
			
			//消息发布
			if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(conn,bean);
			}	
			log.debug("子任务创建成功!");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @Title: createQualitySubtask
	 * @Description: 创建一个质检子任务。 参数1：Subtask对象(新增)(第七迭代)
	 * @param bean
	 * @return
	 * @throws ServiceException  Integer
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午6:16:01 
	 */
	public Integer createQualitySubtask(Subtask bean)throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			// 获取subtaskId
			int subtaskId = SubtaskOperation.getSubtaskId(conn, bean);

			bean.setSubtaskId(subtaskId);
			if(bean.getStatus()== null){
				bean.setStatus(1);
			}
			
			// 插入subtask
			SubtaskOperation.insertSubtask(conn, bean);
			
			// 插入SUBTASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			}
			
			//消息发布
			if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(conn,bean);
			}	
			return subtaskId;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/*
	 * 根据几何范围,任务类型，作业阶段查询任务列表 参数1：几何范围，String wkt
	 */
	public List<Subtask> listByWkt(String wkt) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String querySql = "select "
					+ "s.subtask_id"
					+ ",s.name"
					+ ",s.type"
					+ ",s.stage"
					+ ",s.status"
					+ ", s.geometry"
					+ ",s.descp"
					+ " from subtask s "
					+ "where SDO_GEOM.RELATE(geometry, 'ANYINTERACT', "
					+ "sdo_geometry("
					+ "'"
					+ wkt
					+ "',8307)"
					+ ", 0.000005) ='TRUE'";

			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>() {
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setName(rs.getString("name"));
						subtask.setType(rs.getInt("type"));
						subtask.setStage(rs.getInt("stage"));
						subtask.setStatus(rs.getInt("status"));
						
						try {
							List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						list.add(subtask);
					}
					return list;
				}

			};

			return run.query(conn, querySql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*
	 * 批量修改子任务详细信息。 参数：Subtask对象列表
	 */
	public List<Integer> update(List<Subtask> subtaskList) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			conn = DBConnector.getInstance().getManConnection();

			List<Integer> updatedSubtaskIdList = new ArrayList<Integer>();
			for (int i = 0; i < subtaskList.size(); i++) {
				SubtaskOperation.updateSubtask(conn, subtaskList.get(i));
				updatedSubtaskIdList.add(subtaskList.get(i).getSubtaskId());
				
				//消息发布
				if(subtaskList.get(i)!=null&&subtaskList.get(i).getStatus()!=null &&subtaskList.get(i).getStatus()==1){
					List<Integer> subtaskIdList = new ArrayList<Integer>();
					subtaskIdList.add(subtaskList.get(i).getSubtaskId());
					Subtask subtask = SubtaskOperation.getSubtaskListBySubtaskIdList(conn,subtaskIdList).get(0);
					SubtaskOperation.pushMessage(conn,subtask);
				}
			}
			return updatedSubtaskIdList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	

	/*
	 * 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象
	 */
	public Subtask query(Subtask bean) throws ServiceException {
		return queryBySubtaskId(bean.getSubtaskId());
	}

	/*
	 * 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象
	 */
	
	public Subtask queryBySubtaskIdS(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select st.SUBTASK_ID "
					+ ",st.NAME"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID"
					+ ",st.GEOMETRY";
			String userSql = ",u.user_real_name as executer";
			String groupSql = ",ug.group_name as executer";
			String taskSql = ",T.CITY_ID AS BLOCK_ID,T.TASK_ID AS BLOCK_MAN_ID,T.NAME AS BLOCK_MAN_NAME";
			String blockSql = ",B.BLOCK_ID,BM.BLOCK_MAN_ID, BM.BLOCK_MAN_NAME";

			String fromSql_task = " from subtask st"
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					+ ",block b,block_man bm"
					+ ",region r";
			
			String fromSql_user = "  ,user_info u";

			String fromSql_group = " , user_group ug";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ " and st.SUBTASK_ID=" + subtaskId;

			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
					+ "and b.region_id = r.region_id "
					+ "and bm.block_id = b.block_id"
					+ " and st.SUBTASK_ID=" + subtaskId;
			
			String conditionSql_user = " and st.exe_user_id = u.user_id";
			String conditionSql_group = " and st.exe_group_id = ug.group_id";

			
			selectSql = selectSql + userSql + taskSql + fromSql_task + fromSql_user + conditionSql_task + conditionSql_user
					+ " union all " + selectSql + userSql + blockSql + fromSql_block + fromSql_user + conditionSql_block + conditionSql_user
					+ " union all " + selectSql + groupSql + taskSql + fromSql_task + fromSql_group + conditionSql_task + conditionSql_group
					+ " union all " + selectSql + groupSql + blockSql + fromSql_block + fromSql_group + conditionSql_block + conditionSql_group;
			

			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
				public Subtask handle(ResultSet rs) throws SQLException {
					StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					if (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

	
						if (1 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
							subtask.setBlockId(rs.getInt("BLOCK_ID"));
							subtask.setBlockManId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setBlockManName(rs.getString("BLOCK_MAN_NAME"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
							subtask.setCityId(rs.getInt("BLOCK_ID"));
							subtask.setTaskId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setTaskName(rs.getString("BLOCK_MAN_NAME"));
						} else {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
							subtask.setBlockId(rs.getInt("BLOCK_ID"));
							subtask.setBlockManId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setBlockManName(rs.getString("BLOCK_MAN_NAME"));
						}

						return subtask;
					}
					return null;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @Title: queryBySubtaskId
	 * @Description: 根据subtaskId查询一个任务的详细信息。 参数为Subtask对象(修改)(第七迭代)
	 * @param subtaskId
	 * @return
	 * @throws ServiceException  Subtask
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月4日 下午4:08:09 
	 */
	public Subtask queryBySubtaskId(Integer subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select st.SUBTASK_ID "
					+ ",st.NAME"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID"
					+ ",st.GEOMETRY"
					//新增返回字段
					+ "	,st.quality_Subtask_Id qualitySubtaskId,Q.qualityPlanStartDate ,Q.qualityPlanEndDate ,Q.qualityExeUserId ";
			String userSql = ",u.user_id as executer_id,u.user_real_name as executer";
			String groupSql = ",ug.group_id as executer_id,ug.group_name as executer";
			String taskSql = ",T.CITY_ID AS BLOCK_ID,T.TASK_ID AS BLOCK_MAN_ID,T.NAME AS BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";
			String blockSql = ",B.BLOCK_ID,BM.BLOCK_MAN_ID, BM.BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";

			String fromSql_task = " from subtask st "
					//左外关联质检子任务表
					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id "
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					//左外关联质检子任务表
					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id"
					+ ",block b,block_man bm"
					+ ",region r,task t";
			
			String fromSql_user = "  ,user_info u";

			String fromSql_group = " , user_group ug";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ " and st.SUBTASK_ID=" + subtaskId;

			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
					
					+ "and b.region_id = r.region_id "
					+ "and bm.block_id = b.block_id "
					+ "and bm.task_id = t.task_id "
					+ " and st.SUBTASK_ID=" + subtaskId;
			
			String conditionSql_user = " and st.exe_user_id = u.user_id";
			String conditionSql_group = " and st.exe_group_id = ug.group_id";

			
			selectSql = selectSql + userSql + taskSql + fromSql_task + fromSql_user + conditionSql_task + conditionSql_user
					+ " union all " + selectSql + userSql + blockSql + fromSql_block + fromSql_user + conditionSql_block + conditionSql_user
					+ " union all " + selectSql + groupSql + taskSql + fromSql_task + fromSql_group + conditionSql_task + conditionSql_group
					+ " union all " + selectSql + groupSql + blockSql + fromSql_block + fromSql_group + conditionSql_block + conditionSql_group;
			

			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
				public Subtask handle(ResultSet rs) throws SQLException {
					StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					if (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						//**************zl 2016.11.04 ******************
						subtask.setQualitySubtaskId(rs.getInt("qualitySubtaskId"));
						subtask.setQualityExeUserId(rs.getInt("qualityExeUserId"));
						subtask.setQualityPlanStartDate(rs.getTimestamp("qualityPlanStartDate"));
						subtask.setQualityPlanEndDate(rs.getTimestamp("qualityPlanEndDate"));
						
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
							String clobStr = GeoTranslator.struct2Wkt(struct);
							subtask.setGeometryJSON(Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						if(rs.getInt("TYPE")== 0
//								||rs.getInt("TYPE")== 1
//								||rs.getInt("TYPE")== 2
//								||rs.getInt("TYPE")== 3
//								||rs.getInt("TYPE")== 8
//								||rs.getInt("TYPE")== 9
//								||(rs.getInt("TYPE")==4&&rs.getInt("TASK_TYPE")==4)){
//							try {
//								List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//								subtask.setGridIds(gridIds);
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}

						if (1 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
							subtask.setBlockId(rs.getInt("BLOCK_ID"));
							subtask.setBlockManId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setBlockManName(rs.getString("BLOCK_MAN_NAME"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
							subtask.setCityId(rs.getInt("BLOCK_ID"));
							subtask.setTaskId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setTaskName(rs.getString("BLOCK_MAN_NAME"));
						} else {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
							subtask.setBlockId(rs.getInt("BLOCK_ID"));
							subtask.setBlockManId(rs.getInt("BLOCK_MAN_ID"));
							subtask.setBlockManName(rs.getString("BLOCK_MAN_NAME"));
						}

						subtask.setExecuter(rs.getString("EXECUTER"));
						subtask.setExecuterId(rs.getInt("EXECUTER_ID"));
						
						if(1 == rs.getInt("STATUS")){
							SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
							subtask.setPercent(stat.getPercent());
						}
						
						subtask.setVersion(SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						return subtask;
					}
					return null;
				}
	
			};
			log.debug("queryBySubtaskId: "+selectSql);
			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*
	 * 关闭多个子任务。 参数：Subtask对象列表，List<Subtask>
	 */
	public List<Integer> close(List<Integer> subtaskIdList)
			throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			List<Integer> unClosedSubtaskList = new ArrayList<Integer>();
			List<Integer> closedSubtaskList = new ArrayList<Integer>();
			
			StaticsApi staticsApi = (StaticsApi) ApplicationContextUtil.getBean("staticsApi");
			
			for(int i=0;i<subtaskIdList.size();i++){
				SubtaskStatInfo subtaskStatic = staticsApi.getStatBySubtask(subtaskIdList.get(i));
				if(subtaskStatic.getPercent()<100){
					unClosedSubtaskList.add(subtaskIdList.get(i));
				}else{
					closedSubtaskList.add(subtaskIdList.get(i));
				}
			}
			// 根据subtaskId列表关闭subtask
			if (!closedSubtaskList.isEmpty()) {
				SubtaskOperation.closeBySubtaskList(conn, closedSubtaskList);
			}

			return unClosedSubtaskList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public int queryAdminIdBySubtask(int subtaskId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select c.admin_id from block b, city c, subtask s where s.block_id=b.block_id and b.city_id=c.city_id and s.subtask_id=:1";

			selectSql += " union all";

			selectSql += " select c.admin_id from city c, subtask s, task t where c.city_id=t.city_id and s.task_id=t.task_id and s.subtask_id=:2";

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {

				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {

						int adminId = rs.getInt("admin_id");

						return adminId;

					}

					return 0;
				}
			};

			return run.query(conn, selectSql, rsHandler, subtaskId, subtaskId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/**
	 * @param bean
	 * @param snapshot
	 * @param platForm
	 * @param pageSize
	 * @param curPageNum 
	 * @return
	 * @throws ServiceException 
	 */
	public Page listByUserPage(Subtask bean, int snapshot, int platForm, int pageSize, int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			//获取用户所在组
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(bean.getExeUserId());
			Map<Object,Object> userGroup = UserInfoOperation.getUserGroup(conn, userInfo);
			if(!userGroup.isEmpty()){
				bean.setExeGroupId((int)userGroup.get("groupId"));
			}
			
			Page page = new Page();
			//snapshot=1不返回geometry和gridIds
			if(snapshot==1){
				page = SubtaskOperation.getListByUserSnapshotPage(conn, bean,curPageNum,pageSize,platForm);
			}else{
				page = SubtaskOperation.getListByUserPage(conn, bean,curPageNum,pageSize,platForm);		
			}
			
			return page;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * @Title: createSubtaskBean
	 * @Description: (修改)根据参数生成subtask bean(第七迭代)
	 * @param userId
	 * @param dataJson
	 * @return
	 * @throws ServiceException  Subtask
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午5:07:59 
	 */
	public Subtask createSubtaskBean(long userId, JSONObject dataJson) throws ServiceException {
		try{
			String wkt = null;
			if(dataJson.containsKey("gridIds")){
				JSONArray gridIds = dataJson.getJSONArray("gridIds");
				Object[] gridIdList = gridIds.toArray();
				dataJson.put("gridIds",gridIdList);
				//根据gridIds获取wkt
				wkt = GridUtils.grids2Wkt(gridIds);
//				if(wkt.contains("MULTIPOLYGON")){
//					throw new IllegalArgumentException("请输入符合条件的grids");
//				}
			}else{
				if(dataJson.containsKey("taskId")){
					int taskId = dataJson.getInt("taskId");
					wkt = SubtaskOperation.getWktByTaskId(taskId);
				}else if(dataJson.containsKey("blockManId")){
					int blockManId = dataJson.getInt("blockManId");
					wkt = SubtaskOperation.getWktByBlockManId(blockManId);
				}
			}
			
			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
			bean.setCreateUserId((int)userId);
			bean.setGeometry(wkt);
			return bean;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("子任务创建失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * @param userId
	 * @param subTaskIds
	 * @return
	 * @throws Exception 
	 */
	public String pushMsg(long userId, JSONArray subtaskIds) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询子任务
			List<Subtask> subtaskList = SubtaskOperation.getSubtaskListBySubtaskIdList(conn, subtaskIds);
			
			Iterator<Subtask> iter = subtaskList.iterator();
			while(iter.hasNext()){
				Subtask subtask = (Subtask) iter.next();
				//作业组
				List<Integer> userIdList = new ArrayList<Integer>();
				if(subtask.getExeGroupId()!=0){
					userIdList = SubtaskOperation.getUserListByGroupId(conn,subtask.getExeGroupId());
				}else{
					userIdList.add(subtask.getExeUserId());
				}
				
				String msgTitle = "子任务开启";
				String msgContent = "";
				int push = 0;
				if(subtask.getStage()== 0){
					msgContent = "采集子任务:" + subtask.getName() + "内容发生变更，请关注";
					push = 1;
				}else if(subtask.getStage()== 1){
					msgContent = "日编子任务:" + subtask.getName() + "内容发生变更，请关注";
				}else{
					msgContent = "月编子任务:" + subtask.getName() + "内容发生变更，请关注";
				}

				for(int i=0;i<userIdList.size();i++){
					Message message = new Message();
					message.setMsgTitle(msgTitle);
					message.setMsgContent(msgContent);
					message.setPushUserId((int)userId);
					message.setReceiverId(userIdList.get(i));

					MessageService.getInstance().push(message, push);
					
					if( (int)subtask.getStatus()== 2){
						SubtaskOperation.updateStatus(conn,subtask.getSubtaskId());
					}
				}
			}
			return "发布成功";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
//	public Page list(long userId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
//			final int curPageNum,int snapshot) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			
//			//获取用户所在组信息
//			UserInfo userInfo = new UserInfo();
//			userInfo.setUserId((int)userId);
//			Map<Object, Object> group = UserInfoOperation.getUserGroup(conn, userInfo);
//			int groupId = (int) group.get("groupId");
//			
//			//返回简略信息
//			if (snapshot==1){
//				Page page = SubtaskOperation.getListSnapshot(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}else{
//				Page page = SubtaskOperation.getList(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}		
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

	/**
	 * @param planStatus 
	 * @param condition
	 * @param filter 
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 */
	public Page list(int planStatus, JSONObject condition, JSONObject filter, int pageSize,int curPageNum) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			Page page = SubtaskOperation.getList(conn,planStatus,condition,filter,pageSize,curPageNum);
			return page;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public Page listByGroup(long groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum,int snapshot) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
//			//获取用户所在组信息
//			UserInfo userInfo = new UserInfo();
//			userInfo.setUserId((int)userId);
//			Map<Object, Object> group = UserInfoOperation.getUserGroup(conn, userInfo);
//			int groupId = (int) group.get("groupId");
			
			Page page = SubtaskOperation.getListByGroup(conn,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
			return page;
			
//			//返回简略信息
//			if (snapshot==1){
//				Page page = SubtaskOperation.getListSnapshot(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}else{
//				Page page = SubtaskOperation.getList(conn,userId,groupId,stage,conditionJson,orderJson,pageSize,curPageNum);
//				return page;
//			}		

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
