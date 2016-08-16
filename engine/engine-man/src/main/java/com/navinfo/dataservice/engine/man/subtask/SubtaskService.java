package com.navinfo.dataservice.engine.man.subtask;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
			
			// 插入subtask
			SubtaskOperation.insertSubtask(conn, bean);
			
			// 插入SUBTASK_GRID_MAPPING
			SubtaskOperation.insertSubtaskGridMapping(conn, bean);
			
			//消息发布
			if(bean.getStatus()==1){
				SubtaskOperation.pushMessage(bean.getCreateUserId(),bean.getExeUserId(),bean.getName());

			}	

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
					+ ", s.geometry.get_wkt() as geometry"
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
						subtask.setGeometry(rs.getString("GEOMETRY"));
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
					SubtaskOperation.pushMessage(subtaskList.get(i).getCreateUserId(),subtaskList.get(i).getExeUserId(),subtaskList.get(i).getName());
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
	
	
	public Page list(int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum,int snapshot) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//返回简略信息
			if (snapshot==1){
				Page page = SubtaskOperation.getListSnapshot(stage,conditionJson,orderJson,pageSize,curPageNum);
				return page;
			}else{
				Page page = SubtaskOperation.getList(stage,conditionJson,orderJson,pageSize,curPageNum);
				return page;
			}		
//			String selectSql = "select s.SUBTASK_ID,s.STAGE,s.EXE_USER_ID,u.user_real_name,s.TYPE,s.PLAN_START_DATE,s.PLAN_END_DATE,s.DESCP,s.NAME,s.STATUS,s.GEOMETRY";
//			
//			String selectBlockSql = ",b.block_id,b.block_name,-1 as task_id,'' as task_name";
//			String selectTaskSql = ",-1 as block_id,'' as block_name,t.task_id,t.name as task_name";
//
//			// 0采集，1日编，2月编，
//			if (0 == stage) {
//				selectBlockSql += ",bm.COLLECT_PLAN_START_DATE AS COLLECT_PLAN_START_DATE,bm.COLLECT_PLAN_END_DATE AS COLLECT_PLAN_END_DATE,bm.COLLECT_GROUP_ID AS group_id";
//			} else if (1 == stage) {
//				selectBlockSql += ",bm.DAY_EDIT_PLAN_START_DATE AS DAY_EDIT_PLAN_START_DATE,bm.DAY_EDIT_PLAN_END_DATE AS DAY_EDIT_PLAN_END_DATE,bm.DAY_EDIT_GROUP_ID AS group_id";
//			} else if (2 == stage) {
//				selectBlockSql += ",bm.MONTH_EDIT_PLAN_START_DATE AS MONTH_EDIT_PLAN_START_DATE,bm.MONTH_EDIT_PLAN_END_DATE AS MONTH_EDIT_PLAN_END_DATE,bm.MONTH_EDIT_GROUP_ID AS group_id";
//				selectTaskSql += ",t.MONTH_EDIT_PLAN_START_DATE AS MONTH_EDIT_PLAN_START_DATE,t.MONTH_EDIT_PLAN_END_DATE AS MONTH_EDIT_PLAN_END_DATE,t.MONTH_EDIT_GROUP_ID AS group_id";
//			}
//
//			String fromBlockSql = " from SUBTASK s, Block b, Block_man bm, user_info u where s.block_id = b.block_id and u.user_id = s.exe_user_id and b.block_id = bm.block_id and bm.latest = 1";
//			String fromTaskSql = " from SUBTASK s, Task t, user_info u where s.task_id = t.task_id and u.user_id = s.exe_user_id";
//			String conditionSql = " and s.stage = " + stage;
//			
//			//查询条件
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("subtaskId".equals(key)) {conditionSql+=" and s.SUBTASK_ID="+conditionJson.getInt(key);}
//					if ("subtaskName".equals(key)) {	
//						conditionSql+=" and s.NAME like '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("ExeUserId".equals(key)) {conditionSql+=" and s.EXE_USER_ID="+conditionJson.getInt(key);}
//					if ("ExeUserName".equals(key)) {
//						conditionSql+=" and u.user_real_name like '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockName".equals(key)) {
//						conditionSql+=" and s.block_id = b.block_id and b.block_name like '%" + conditionJson.getString(key) +"%'";
//					}
//					if ("blockId".equals(key)) {conditionSql+=" and s.block_id="+conditionJson.getInt(key);}
//					if ("taskId".equals(key)) {conditionSql+=" and s.task_id="+conditionJson.getInt(key);}
//					if ("taskName".equals(key)) {
//						conditionSql+=" and s.task_id = t.task_id and t.name like '%" + conditionJson.getInt(key) +"%'";
//					}
//					if ("status".equals(key)) {
//						conditionSql+=" and s.status in (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
//					}
//				}
//			}
//			
//			String orderSql = "";
//			
//			// 排序
//			if(null!=orderJson && !orderJson.isEmpty()){
//				Iterator keys = orderJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("status".equals(key)) {orderSql+=" order by s.status "+orderJson.getString(key);}
//					if ("subtaskId".equals(key)) {orderSql+=" order by s.SUBTASK_ID "+orderJson.getString(key);}
//					if ("blockId".equals(key)) {orderSql+=" order by block_id "+orderJson.getString(key);}
//					if ("planStartDate".equals(key)) {orderSql+=" order by s.PLAN_START_DATE "+orderJson.getString(key);}
//					if ("planEndDate".equals(key)) {orderSql+=" order by s.PLAN_END_DATE "+orderJson.getString(key);}
//				}
//			}else{orderSql += " order by block_id";}
//
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
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("subtaskName", rs.getString("NAME"));
//						subtask.put("descp", rs.getString("DESCP"));
//
//						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
//						try {
//							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
//						} catch (Exception e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//
//						subtask.put("stage", rs.getInt("STAGE"));
//						subtask.put("type", rs.getInt("TYPE"));
//						subtask.put("status", rs.getInt("STATUS"));
//						subtask.put("ExeUserId", rs.getInt("EXE_USER_ID"));
//						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
//						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
//						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						subtask.put("groupId", rs.getInt("group_id"));
//						
//						// 与block关联，返回block信息。
//						if (rs.getInt("block_id") > 0) {
//							// block
//							subtask.put("blockId", rs.getInt("block_id"));
//							subtask.put("blockName", rs.getString("block_name"));
//							//采集
//							if(0 == rs.getInt("STAGE")){
//								subtask.put("BlockCollectPlanStartDate", df.format(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
//								subtask.put("BlockCollectPlanEndDate",df.format(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
//							}
//							//日编
//							else if(1 == rs.getInt("STAGE")){
//								subtask.put("BlockDayEditPlanStartDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
//								subtask.put("BlockDayEditPlanEndDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
//							}
//							//月编
//							else if(2 == rs.getInt("STAGE")){
//								subtask.put("BlockCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
//								subtask.put("BlockCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
//							}
//						}
//						// 与task关联，返回task信息。
//						if (rs.getInt("task_id") > 0) {
//							// task
//							subtask.put("taskId", rs.getInt("task_id"));
//							subtask.put("taskName", rs.getString("task_name"));
//							// 月编
//							if(2 == rs.getInt("STAGE")){
//								subtask.put("TaskCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
//								subtask.put("TaskCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
//							}
//						}
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
//			if (2 == stage){
//				selectSql = selectSql + selectBlockSql + fromBlockSql + conditionSql + " union all " + selectSql + selectTaskSql + fromTaskSql + conditionSql + orderSql;
//			}else{
//				selectSql = selectSql + selectBlockSql + fromBlockSql + conditionSql + orderSql;
//			}
//			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	
	
	public Page listPage(int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID"
					+ ",s.STAGE"
					+ ",s.EXE_USER_ID"
					+ ",u.user_real_name"
					+ ",s.TYPE"
					+ ",s.PLAN_START_DATE"
					+ ",s.PLAN_END_DATE"
					+ ",s.DESCP"
					+ ",s.NAME"
					+ ",s.STATUS"
					+ ",s.GEOMETRY"
					+ ",(case when s.block_id is not null and s.block_id = b.block_id then b.block_id else -1 end) AS block_id"
					+ ",(case when s.block_id is not null and s.block_id = b.block_id then b.block_name else null end) AS block_name"
					+ ",(case when s.task_id is not null and s.task_id = t.task_id then t.task_id else -1 end) AS task_id"
					+ ",(case when s.task_id is not null and s.task_id = t.task_id then t.descp else null end) AS task_descp"
					+ ",(case when s.task_id is not null and s.task_id = t.task_id then t.name else null end) AS task_name";
			// 0采集，1日编，2月编，
			if (0 == stage) {
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.COLLECT_PLAN_START_DATE else null end) AS COLLECT_PLAN_START_DATE";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.COLLECT_PLAN_END_DATE else null end) AS COLLECT_PLAN_END_DATE";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.COLLECT_GROUP_ID else null end) AS group_id";
			} else if (1 == stage) {
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.DAY_EDIT_PLAN_START_DATE else null end) AS DAY_EDIT_PLAN_START_DATE";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.DAY_EDIT_PLAN_END_DATE else null end) AS DAY_EDIT_PLAN_END_DATE";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.DAY_EDIT_GROUP_ID else null end) AS group_id";
			} else if (2 == stage) {
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.MONTH_EDIT_PLAN_START_DATE else null end) AS MONTH_EDIT_PLAN_START_DATE_b";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.MONTH_EDIT_PLAN_END_DATE else null end) AS MONTH_EDIT_PLAN_END_DATE_b";
				selectSql += ",(case when s.block_id is not null and s.block_id = b.block_id and bm.LATEST = 1 and b.block_id = bm.block_id then bm.MONTH_EDIT_GROUP_ID else -1 end) AS group_id";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.MONTH_EDIT_PLAN_START_DATE else null end) AS MONTH_EDIT_PLAN_START_DATE_t";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.MONTH_EDIT_PLAN_END_DATE else null end) AS MONTH_EDIT_PLAN_END_DATE_t";
				selectSql += ",(case when s.task_id is not null and s.task_id = t.task_id then t.MONTH_EDIT_GROUP_ID else -1 end) AS group_id_t";
			}

			selectSql = selectSql
					+ " from SUBTASK s, Task t, Block b, Block_man bm,user_info u "
					+ " where s.block_id=b.block_id(+)"
					+ " and s.task_id=t.task_id(+)"
					+ " and u.user_id = s.exe_user_id"
					+ " and b.block_id = bm.block_id" + " and bm.latest=1"
					+ " and s.stage=" + stage;
			
			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskId".equals(key)) {selectSql+=" and s.SUBTASK_ID="+conditionJson.getInt(key);break;}
					if ("subtaskName".equals(key)) {	
						selectSql+=" and s.NAME like '%" + conditionJson.getString(key) +"%'";
						break;
					}
					if ("ExeUserId".equals(key)) {selectSql+=" and s.EXE_USER_ID="+conditionJson.getInt(key);break;}
					if ("ExeUserName".equals(key)) {
						selectSql+=" and u.user_real_name like '%" + conditionJson.getString(key) +"%'";
						break;
					}
					if ("blockName".equals(key)) {
						selectSql+=" and s.block_id = b.block_id and b.block_name like '%" + conditionJson.getString(key) +"%'";
						break;
					}
					if ("blockId".equals(key)) {selectSql+=" and s.block_id="+conditionJson.getInt(key);break;}
					if ("taskId".equals(key)) {selectSql+=" and s.task_id="+conditionJson.getInt(key);break;}
					if ("taskName".equals(key)) {
						selectSql+=" and s.task_id = t.task_id and t.name like '%" + conditionJson.getInt(key) +"%'";
						break;
					}
				}
			}
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {selectSql+=" order by s.status "+orderJson.getString(key);break;}
					if ("subtaskId".equals(key)) {selectSql+=" order by s.SUBTASK_ID "+orderJson.getString(key);break;}
					if ("blockId".equals(key)) {selectSql+=" order by block_id "+orderJson.getString(key);break;}
					if ("planStartDate".equals(key)) {selectSql+=" order by s.PLAN_START_DATE "+orderJson.getString(key);break;}
					if ("planEndDate".equals(key)) {selectSql+=" order by s.PLAN_END_DATE "+orderJson.getString(key);break;}}
			}else{selectSql += " order by block_id";}

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
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("ExeUserId", rs.getInt("EXE_USER_ID"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						if(rs.getInt("group_id")>0){
							subtask.put("groupId", rs.getInt("group_id"));
						}else{
							subtask.put("groupId", rs.getInt("group_id_t"));
						}
						
						// 与block关联，返回block信息。
						if (rs.getInt("block_id") > 0) {
							// block
							subtask.put("blockId", rs.getInt("block_id"));
							subtask.put("blockName", rs.getString("block_name"));
							//采集
							if(0 == rs.getInt("STAGE")){
								subtask.put("BlockCollectPlanStartDate", df.format(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
								subtask.put("BlockCollectPlanEndDate",df.format(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
							}
							//日编
							else if(1 == rs.getInt("STAGE")){
								subtask.put("BlockDayEditPlanStartDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
								subtask.put("BlockDayEditPlanEndDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
							}
							//月编
							else if(2 == rs.getInt("STAGE")){
								subtask.put("BlockCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE_b")));
								subtask.put("BlockCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE_b")));
							}
						}
						// 与task关联，返回task信息。
						if (rs.getInt("task_id") > 0) {
							// task
							subtask.put("taskId", rs.getInt("task_id"));
							subtask.put("taskDescp", rs.getString("task_descp"));
							subtask.put("taskName", rs.getString("task_name"));
							// 月编
							if(2 == rs.getInt("STAGE")){
								subtask.put("TaskCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE_t")));
								subtask.put("TaskCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE_t")));
							}
						}

						list.add(subtask);
					}

					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}

			};

			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
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
					+ ",st.GEOMETRY.get_wkt() AS GEOMETRY";

			String fromSql_task = " from subtask st"
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					+ ",block b"
					+ ",city c"
					+ ",region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ " and st.SUBTASK_ID=" + subtaskId;

			String conditionSql_block = " where st.block_id = b.block_id "
					+ "and b.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ " and st.SUBTASK_ID=" + subtaskId;

			
			selectSql = selectSql + fromSql_task
					+ conditionSql_task
					+ " union all " + selectSql
					+ fromSql_block + conditionSql_block;
			

			ResultSetHandler<Subtask> rsHandler = new ResultSetHandler<Subtask>() {
				public Subtask handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs
								.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
	
						subtask.setGeometry(rs.getString("GEOMETRY"));
						
						try {
							List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.setGridIds(gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

	
						if (1 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
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

			// 根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
			List<Subtask> subtaskList = SubtaskOperation
					.getSubtaskListByIdList(conn, subtaskIdList);

			List<Integer> unClosedSubtaskList = new ArrayList<Integer>();
			List<Integer> closedSubtaskList = new ArrayList<Integer>();

			StaticsApi staticsApi = (StaticsApi) ApplicationContextUtil
					.getBean("staticsApi");

			for (int i = 0; i < subtaskList.size(); i++) {
				// 采集
				if (0 == subtaskList.get(i).getStage()) {
					// 判断采集任务是否可关闭
					Boolean flg = SubtaskOperation.isCollectReadyToClose(
							staticsApi, subtaskList.get(i));
					if (flg) {
						closedSubtaskList
								.add(subtaskList.get(i).getSubtaskId());
					} else {
						unClosedSubtaskList.add(subtaskList.get(i).getSubtaskId());
					}
				}

				// 日编
				else if (1 == subtaskList.get(i).getStage()) {
					// 判断日编任务是否可关闭
					Boolean flg = SubtaskOperation.isDailyEditReadyToClose(
							staticsApi, subtaskList.get(i));
					if (flg) {
						closedSubtaskList.add(subtaskList.get(i).getSubtaskId());
					} else {
						unClosedSubtaskList.add(subtaskList.get(i).getSubtaskId());
					}
				}

				// 月编
				else if (2 == subtaskList.get(i).getStage()) {
					// 判断月编任务是否可关闭
					Boolean flg = SubtaskOperation.isMonthlyEditReadyToClose(
							staticsApi, subtaskList.get(i));
					if (flg) {
						closedSubtaskList.add(subtaskList.get(i).getSubtaskId());
					} else {
						unClosedSubtaskList.add(subtaskList.get(i).getSubtaskId());
					}
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
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public Page listByUserPage(Subtask bean, int snapshot, int pageSize, int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {

			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			Page page = new Page();
			//snapshot=1不返回geometry和gridIds
			if(snapshot==1){
				page = SubtaskOperation.getListByUserSnapshotPage(conn, bean,curPageNum,pageSize);
			}else{
				page = SubtaskOperation.getListByUserPage(conn, bean,curPageNum,pageSize);		
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
}
