package com.navinfo.dataservice.engine.man.subtask;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
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
	public int create(Subtask bean)throws ServiceException {
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
			/*if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(conn,bean);
			}*/	
			log.debug("子任务创建成功!");
			return subtaskId;
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
			/*if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(conn,bean);
			}*/	
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
	public String update(JSONArray subtaskArray, long userId) throws ServiceException, ParseException {
		List<Subtask> subtaskList = new ArrayList<Subtask>();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		for(int i = 0;i<subtaskArray.size();i++){
			//***************zl 2016.11.4 ******************
			Integer qualitySubtaskId = 0;//质检子任务id
			Integer qualityExeUserId = 0;//是否新建质检子任务标识
			String qualityPlanStartDate = "";
			String qualityPlanEndDate ="";
			
			if(subtaskArray.getJSONObject(i).containsKey("qualitySubtaskId")){
				qualitySubtaskId = subtaskArray.getJSONObject(i).getInt("qualitySubtaskId");
				//删除 质检子任务id ,因为质检子任务Subtask实体类里不应该有这个字段
				subtaskArray.getJSONObject(i).discard("qualitySubtaskId");
			}
			if(subtaskArray.getJSONObject(i).containsKey("qualityExeUserId")){
				qualityExeUserId = subtaskArray.getJSONObject(i).getInt("qualityExeUserId");
				qualityPlanStartDate = subtaskArray.getJSONObject(i).getString("qualityPlanStartDate");
				qualityPlanEndDate =subtaskArray.getJSONObject(i).getString("qualityPlanEndDate");
				subtaskArray.getJSONObject(i).discard("qualityExeUserId");//删除 是否新建质检子任务标识 ,因为Subtask实体类里灭幼这个字段
				subtaskArray.getJSONObject(i).discard("qualityPlanStartDate");//删除 质检子任务计划开始时间 ,因为Subtask实体类里灭幼这个字段
				subtaskArray.getJSONObject(i).discard("qualityPlanEndDate");//删除 质检子任务计划结束时间 ,因为Subtask实体类里灭幼这个字段
			}				
			
			//正常修改子任务
			Subtask subtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);	
			
			if(qualitySubtaskId != 0){//非0的时候，表示要修改质检子任务
				Subtask qualitySubtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);//生成质检子任务的bean
				qualitySubtask.setSubtaskId(qualitySubtaskId);
				qualitySubtask.setExeUserId(qualityExeUserId);
				qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
				qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
				subtaskList.add(qualitySubtask);//将质检子任务也加入修改列表
			}else{
				if(qualityExeUserId != 0){//qualitySubtaskId=0，且qualityExeUserId非0的时候，表示要创建质检子任务
					//subtaskArray.getJSONObject(i).discard("subtaskId");//删除subtaskId ,新建质检子任务
					//Subtask qualitySubtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);//生成质检子任务的bean
					Subtask qualitySubtask = SubtaskService.getInstance().queryBySubtaskIdS(subtaskArray.getJSONObject(i).getInt("subtaskId"));
					qualitySubtask.setName(qualitySubtask.getName()+"_质检");
					qualitySubtask.setSubtaskId(null);
					qualitySubtask.setPlanStartDate(new Timestamp(df.parse(qualityPlanStartDate).getTime()));
					qualitySubtask.setPlanEndDate(new Timestamp(df.parse(qualityPlanEndDate).getTime()));
					qualitySubtask.setIsQuality(1);//表示此bean是质检子任务
					qualitySubtask.setExeUserId(qualityExeUserId);
					//创建质检子任务 subtask	
					Integer newQualitySubtaskId = SubtaskService.getInstance().create(qualitySubtask);	
					subtask.setIsQuality(0);
					subtask.setQualitySubtaskId(newQualitySubtaskId);
				}
			}
			subtaskList.add(subtask);
			//正常修改子任务
			//Subtask subtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);
		}
		
		List<Integer> updatedSubtaskIdList = SubtaskService.getInstance().updateSubtask(subtaskList,userId);
		
		String message = "批量修改子任务：" + updatedSubtaskIdList.size() + "个成功，" + (subtaskList.size() - updatedSubtaskIdList.size()) + "个失败。";
		return message;
	}

	/*
	 * 批量修改子任务详细信息。 参数：Subtask对象列表
	 */
	public List<Integer> updateSubtask(List<Subtask> subtaskList, long userId) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			conn = DBConnector.getInstance().getManConnection();

			List<Integer> updatedSubtaskIdList = new ArrayList<Integer>();
			for (int i = 0; i < subtaskList.size(); i++) {
				SubtaskOperation.updateSubtask(conn, subtaskList.get(i));
				updatedSubtaskIdList.add(subtaskList.get(i).getSubtaskId());
				
			}
			//发送消息
			try {
				List<Integer> subtaskIdList = new ArrayList<Integer>();
				for (Subtask subtask : subtaskList) {
					//消息发布
					if(subtask!=null&&subtask.getStatus()!=null &&subtask.getStatus()==1){
						subtaskIdList.add(subtask.getSubtaskId());
					}
				}
				if(subtaskIdList.size()==0){return updatedSubtaskIdList;}
				//查询子任务
				if(!subtaskIdList.isEmpty()){
					List<Subtask> list = SubtaskOperation.getSubtaskListBySubtaskIdList(conn,subtaskIdList);
					if(list != null && list.size()>0){
						for (Subtask subtask : list) {
							SubtaskOperation.pushMessage(conn,subtask,userId);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
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
					+ ",st.GEOMETRY"
					+ ",st.REFER_GEOMETRY";
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
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						//REFER_GEOMETRY
						STRUCT struct1 = (STRUCT) rs.getObject("REFER_GEOMETRY");
						try {
							if(struct1!=null){
								String clobStr = GeoTranslator.struct2Wkt(struct1);
								subtask.setReferGeometryJSON(Geojson.wkt2Geojson(clobStr));
							}else{
								subtask.setReferGeometryJSON(null);
							}	
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
					+ ",st.REFER_GEOMETRY"
					//新增返回字段
					+ "	,st.quality_Subtask_Id qualitySubtaskId,Q.qualityPlanStartDate ,Q.qualityPlanEndDate ,Q.qualityExeUserId ,Q.qualityTaskStatus";
			String userSql = ",u.user_id as executer_id,u.user_real_name as executer";
			String groupSql = ",ug.group_id as executer_id,ug.group_name as executer";
			String taskSql = ",T.CITY_ID AS BLOCK_ID,T.TASK_ID AS BLOCK_MAN_ID,T.NAME AS BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";
			String blockSql = ",B.BLOCK_ID,BM.BLOCK_MAN_ID, BM.BLOCK_MAN_NAME,T.TASK_TYPE AS TASK_TYPE";

			String fromSql_task = " from subtask st "
					//左外关联质检子任务表
					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate,s.STATUS qualityTaskStatus from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id "
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					//左外关联质检子任务表
					+ " left join (select s.SUBTASK_ID ,s.EXE_USER_ID qualityExeUserId,s.PLAN_START_DATE as qualityPlanStartDate,s.PLAN_END_DATE as qualityPlanEndDate,s.STATUS qualityTaskStatus from subtask s where s.is_quality = 1 ) Q  on st.quality_subtask_id = Q.subtask_id"
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
						subtask.setQualityTaskStatus(rs.getInt("qualityTaskStatus"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
							String clobStr = GeoTranslator.struct2Wkt(struct);
							subtask.setGeometryJSON(Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						//REFER_GEOMETRY
						STRUCT struct1 = (STRUCT) rs.getObject("REFER_GEOMETRY");
						try {
							if(struct1!=null){
								String clobStr = GeoTranslator.struct2Wkt(struct1);
								subtask.setReferGeometryJSON(Geojson.wkt2Geojson(clobStr));
							}else{
								subtask.setReferGeometryJSON(null);
							}	
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
	public List<Integer> close(List<Integer> subtaskIdList, long userId)
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
			//发送消息
			try {
				//查询子任务
				List<Subtask> subtaskList = SubtaskOperation.getSubtaskListBySubtaskIdList(conn, closedSubtaskList);
				Iterator<Subtask> iter = subtaskList.iterator();
				while(iter.hasNext()){
					Subtask subtask = (Subtask) iter.next();
					//查询分配的作业组组长
					List<Long> groupIdList = new ArrayList<Long>();
					groupIdList.add((long)subtask.getExeGroupId());
					List<Long> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
					/*采集/日编/月编子任务关闭
					 * 分配的作业员
					 * 采集/日编/月编子任务关闭：XXX(子任务名称)已关闭，请关注*/
					String msgTitle = "";
					String msgContent = "";
					if(subtask.getStage()== 0){
						msgTitle = "采集子任务关闭";
						msgContent = "采集子任务关闭:" + subtask.getName() + "已关闭,请关注";
					}else if(subtask.getStage()== 1){
						msgTitle = "日编子任务关闭";
						msgContent = "日编子任务关闭:" + subtask.getName() + "已关闭,请关注";
					}else{
						msgTitle = "月编子任务关闭";
						msgContent = "月编子任务关闭:" + subtask.getName() + "已关闭,请关注";
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
					
					if(leaderIdByGroupId !=null && leaderIdByGroupId.size()>0){
						for(int i=0;i<leaderIdByGroupId.size();i++){
							Message message = new Message();
							message.setMsgTitle(msgTitle);
							message.setMsgContent(msgContent);
							message.setPushUserId((int)userId);
							message.setReceiverId(leaderIdByGroupId.get(i).intValue());
							message.setMsgParam(msgParam.toString());
							message.setPushUser(pushUserName);
							
							MessageService.getInstance().push(message, 0);
							
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
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

			String selectSql = "select c.admin_id from block_man bm,block b, city c, subtask s where s.block_man_id=bm.block_man_id and b.block_id = bm.block_id and b.city_id=c.city_id and s.subtask_id=:1";

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
				/*
				List<Integer> userIdList = new ArrayList<Integer>();
				if(subtask.getExeGroupId()!=0){
					userIdList = SubtaskOperation.getUserListByGroupId(conn,subtask.getExeGroupId());
				}else{
					userIdList.add(subtask.getExeUserId());
				}
				*/
				try {
					/*采集/日编/月编子任务发布
					 * 分配的作业员
					 * 新增采集/日编/月编子任务：XXX(子任务名称)，请关注*/
					String msgTitle = "";
					String msgContent = "";
					if(subtask.getStage()== 0){
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
					
					MessageService.getInstance().push(message, 1);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					log.error("发送失败,原因:"+e.getMessage(), e);
				}
				
				if( (int)subtask.getStatus()== 2){
					SubtaskOperation.updateStatus(conn,subtask.getSubtaskId());
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
