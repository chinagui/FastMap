package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.message.MessageOperation;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;

/**
 * @ClassName: BlockService
 * @author code generator
 * @date 2016-06-08 01:32:00
 * @Description: TODO
 */

public class BlockService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private List blockIdList;

	private BlockService() {
	}

	private static class SingletonHolder {
		private static final BlockService INSTANCE = new BlockService();
	}

	public static BlockService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public int batchOpen(long userId, JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");
			int updateCount = 0;
			List<Integer> blockIdList = new ArrayList<Integer>();
			String createSql = "insert into block_man (BLOCK_MAN_ID, BLOCK_MAN_NAME,CREATE_USER_ID,BLOCK_ID,COLLECT_GROUP_ID, COLLECT_PLAN_START_DATE,"
					+ "COLLECT_PLAN_END_DATE,DAY_EDIT_GROUP_ID,DAY_EDIT_PLAN_START_DATE,DAY_EDIT_PLAN_END_DATE,MONTH_EDIT_GROUP_ID,"
					+ "MONTH_EDIT_PLAN_START_DATE,MONTH_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,"
					+ "MONTH_PRODUCE_PLAN_START_DATE,MONTH_PRODUCE_PLAN_END_DATE,DESCP,STATUS,TASK_ID) "
					+ "values(BLOCK_MAN_SEQ.NEXTVAL,?,?,?,?,to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,?,(SELECT DISTINCT TASK_ID FROM BLOCK T,TASK K WHERE T.CITY_ID=K.CITY_ID AND K.LATEST=1 AND T.BLOCK_ID=?))";

			Object[][] param = new Object[blockArray.size()][];
			
			//获取block名称
			Map<Integer,String> blockNameMap = BlockOperation.queryBlockNameByBlocks(conn, blockArray);
			List<Integer> updateBlockList = BlockOperation.queryOperationBlocks(conn, blockArray);
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				if (updateBlockList.contains(block.getInt("blockId"))) {
					continue;
				}

				Object[] obj = new Object[] { blockNameMap.get(block.getInt("blockId")),userId, block.getInt("blockId"), block.getInt("collectGroupId"),
						block.getString("collectPlanStartDate"), block.getString("collectPlanEndDate"),
						block.getInt("dayEditGroupId"), block.getString("dayEditPlanStartDate"),
						block.getString("dayEditPlanEndDate"), block.getInt("monthEditGroupId"),
						block.getString("monthEditPlanStartDate"), block.getString("monthEditPlanEndDate"),
						block.getString("dayProducePlanStartDate"), block.getString("dayProducePlanEndDate"),
						block.getString("monthProducePlanStartDate"), block.getString("monthProducePlanEndDate"),
						block.getString("descp"), 2, block.getInt("blockId") };
				param[i] = obj;
				blockIdList.add(block.getInt("blockId"));
			}
			BlockOperation.openBlockByBlockIdList(conn, blockIdList);

			int[] rows = null;
			if(param[0]!=null){
				rows = run.batch(conn, createSql, param);
			}
			if(rows!=null){
				updateCount = rows.length;
			}
			
			return updateCount;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public int batchUpdate(JSONObject json, long userId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");
			List blockIdList = new ArrayList();
			int updateCount = 0;
			String createSql = "update block_man set COLLECT_GROUP_ID=?, COLLECT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "COLLECT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_GROUP_ID=?,DAY_EDIT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_EDIT_GROUP_ID=?,"
					+ "MONTH_EDIT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_EDIT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_PRODUCE_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_PRODUCE_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "MONTH_PRODUCE_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_PRODUCE_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'), DESCP=?,STATUS=? where BLOCK_MAN_ID=?";

			Object[][] param = new Object[blockArray.size()][];
			List<Integer> updateBlockList = BlockOperation.queryOperationBlocks(conn, blockArray);
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				BlockMan bean = (BlockMan) JSONObject.toBean(block, BlockMan.class);
				if (updateBlockList.contains(bean.getBlockId())) {
					Object[] obj = new Object[] { bean.getCollectGroupId(), bean.getCollectPlanStartDate(),
							bean.getCollectPlanEndDate(), bean.getDayEditGroupId(), bean.getDayEditPlanStartDate(),
							bean.getDayEditPlanEndDate(), bean.getMonthEditGroupId(), bean.getMonthEditPlanStartDate(),
							bean.getMonthEditPlanEndDate(), bean.getDayProducePlanStartDate(),
							bean.getDayProducePlanEndDate(), bean.getMonthProducePlanStartDate(),
							bean.getMonthProducePlanStartDate(), bean.getDescp(), bean.getStatus(), bean.getBlockManId() };
					param[i] = obj;
					if (1 == bean.getStatus()) {
						blockIdList.add(bean.getBlockManId());
					}
				}
			}
			if (param[0]!=null){
				int[] rows = run.batch(conn, createSql, param);
				updateCount = rows.length;
			}
			blockPushMsg(blockIdList);
			return updateCount;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap> listByProduce(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY from BLOCK t where sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
			return BlockOperation.queryProduceBlock(conn, selectSql, json);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap> listByWkt(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			String wkt = json.getString("wkt");
			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']',
					')');

			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY,t.PLAN_STATUS,t.CITY_ID from BLOCK t where t.PLAN_STATUS in "
					+ planningStatus;

			if (StringUtils.isNotEmpty(json.getString("snapshot"))) {
				if ("1".equals(json.getString("snapshot"))) {
					selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.PLAN_STATUS,t.CITY_ID from BLOCK t where t.PLAN_STATUS in "
							+ planningStatus;
				}
			}
			;
			if (!json.containsKey("relation") || ("intersect".equals(json.getString("relation")))) {
				selectSql += " and SDO_ANYINTERACT(t.geometry,sdo_geometry('" + wkt + "',8307))='TRUE'";
			} else {
				if ("within".equals(json.getString("relation"))) {
					selectSql += " and sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry('" + wkt
							+ "', 8307)), 'DISTANCE=0') = 'TRUE'";
				}
			}

			return BlockOperation.queryBlockBySql(conn, selectSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public HashMap<?, ?> query(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			// 鎸佷箙鍖�
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			BlockMan bean = (BlockMan) JSONObject.toBean(obj, BlockMan.class);

			String selectSql = "select B.BLOCK_MAN_ID,t.CITY_ID, B.BLOCK_MAN_NAME, t.GEOMETRY,"
					+ " t.PLAN_STATUS, k.name taskName,k.task_type,b.descp,nvl(u.user_real_name, '') USER_REAL_NAME, b.collect_group_id, b.day_edit_group_id,"
					+ " b.month_edit_group_id, to_char(b.collect_plan_start_date, 'yyyymmdd') collect_plan_start_date, to_char(b.collect_plan_end_date, 'yyyymmdd') collect_plan_end_date,"
					+ " to_char(b.day_edit_plan_start_date, 'yyyymmdd') day_edit_plan_start_date, to_char(b.day_edit_plan_end_date, 'yyyymmdd') day_edit_plan_end_date, to_char(b.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ " to_char(b.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date,to_char(b.day_produce_plan_start_date, 'yyyymmdd') day_produce_plan_start_date,"
					+ " to_char(b.day_produce_plan_end_date, 'yyyymmdd') day_produce_plan_end_date,"
					+ " to_char(b.month_produce_plan_start_date, 'yyyymmdd') month_produce_plan_start_date,"
					+ " to_char(b.month_produce_plan_end_date, 'yyyymmdd') month_produce_plan_end_date"
					+ " from BLOCK t, BLOCK_MAN b, TASK k,USER_INFO u where B.BLOCK_MAN_ID = ?"
					+ " and t.block_id = b.block_id and t.city_id = k.city_id and k.latest = 1 and b.latest=1 and b.create_user_id=u.user_id ";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>() {
				public HashMap<String, Object> handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						map.put("taskName", rs.getString("taskName"));
						map.put("collectGroupId", rs.getInt("collect_group_id"));
						map.put("dayEditGroupId", rs.getInt("day_edit_group_id"));
						map.put("monthEditGroupId", rs.getInt("month_edit_group_id"));
						map.put("collectPlanStartDate", rs.getString("collect_plan_start_date"));
						map.put("collectPlanEndDate", rs.getString("collect_plan_end_date"));
						map.put("dayEditPlanStartDate", rs.getString("day_edit_plan_start_date"));
						map.put("dayEditPlanEndDate", rs.getString("day_edit_plan_end_date"));
						map.put("monthEditPlanStartDate", rs.getString("month_edit_plan_start_date"));
						map.put("monthEditPlanEndDate", rs.getString("month_edit_plan_end_date"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("dayProducePlanStartDate", rs.getString("day_produce_plan_start_date"));
						map.put("dayProducePlanEndDate", rs.getString("day_produce_plan_end_date"));
						map.put("monthProducePlanStartDate", rs.getString("month_produce_plan_start_date"));
						map.put("monthProducePlanEndDate", rs.getString("month_produce_plan_end_date"));
						map.put("taskType", rs.getInt("task_type"));
						map.put("blockDescp", rs.getString("descp"));
						map.put("createUserName", rs.getString("USER_REAL_NAME"));
						return map;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, bean.getBlockManId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
/*
	public Page listByGroupId(JSONObject json, int currentPageNum, int pageSize) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String selectSql = null;
			int stage = json.getInt("stage");
			JSONObject conditionJson = json.getJSONObject("condition");
			JSONObject orderJson = json.getJSONObject("order");

			int groupId = json.getInt("groupId");
			if (0 == stage) {
				selectSql = "SELECT P.BLOCK_MAN_ID,P.BLOCK_MAN_NAME,P.STATUS,P.PLAN_START_DATE,P.PLAN_END_DATE,P.FLAG,P.TASK_TYPE FROM "
						+ " (SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.COLLECT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.COLLECT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,1 FLAG,T.COLLECT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,SUBTASK S,TASK TT   WHERE "
						+ "  T.BLOCK_MAN_ID=S.BLOCK_MAN_ID AND T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 AND S.STAGE =0  UNION ALL "
						+ "  SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.COLLECT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.COLLECT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,0 FLAG,T.COLLECT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,TASK TT WHERE T.TASK_ID = TT.TASK_ID   AND T.LATEST = 1 "
						+ "   AND T.STATUS=1 AND NOT EXISTS (SELECT su.subtask_id FROM subtask su WHERE su.block_man_id=T.BLOCK_MAN_ID)) P"
						+ " WHERE  P.COLLECT_GROUP_ID = " + groupId;

			}
			if (1 == stage) {

				selectSql = "SELECT P.BLOCK_MAN_ID,P.BLOCK_MAN_NAME,P.STATUS,P.PLAN_START_DATE,P.PLAN_END_DATE,P.FLAG,P.TASK_TYPE FROM "
						+ " (SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,1 FLAG,T.DAY_EDIT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,SUBTASK S,TASK TT   WHERE "
						+ "  T.BLOCK_MAN_ID=S.BLOCK_MAN_ID AND T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 AND S.STAGE =1  UNION ALL "
						+ "  SELECT DISTINCT T.BLOCK_MAN_ID,T.BLOCK_MAN_NAME,T.STATUS,to_char(T.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,to_char(T.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,0 FLAG,T.DAY_EDIT_GROUP_ID,TT.TASK_TYPE "
						+ "   FROM BLOCK_MAN T,TASK TT WHERE T.TASK_ID = TT.TASK_ID  AND T.LATEST = 1 "
						+ "   AND T.STATUS=1 AND NOT EXISTS (SELECT su.subtask_id FROM subtask su WHERE su.BLOCK_MAN_ID=T.BLOCK_MAN_ID)) P"
						+ " WHERE  P.DAY_EDIT_GROUP_ID = " + groupId;
			}

			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockManName".equals(key)) {
						selectSql += " and P.BLOCK_MAN_NAME like '%" + conditionJson.getString(key) + "%'";
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockManId".equals(key)) {
						selectSql += (" order by P.BLOCK_MAN_ID " + orderJson.getString(key));
						break;
					}
				}
			} else {
				selectSql += " order by P.BLOCK_MAN_ID";
			}
			return BlockOperation.queryBlockByGroup(conn, selectSql, stage, currentPageNum, pageSize);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
*/
	public List<Integer> close(List<Integer> blockManIdList) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			// 获取所有blockIdList中可以关闭的block
			List<Integer> blockReadyToClose = BlockOperation.getBlockListReadyToClose(conn, blockManIdList);

			if (!blockReadyToClose.isEmpty()) {
				BlockOperation.closeBlockByBlockIdList(conn, blockReadyToClose);
			}

			List<Integer> unClosedBlocks = new ArrayList<Integer>();
			for (int i = 0; i < blockManIdList.size(); i++) {
				if (!blockReadyToClose.contains(blockManIdList.get(i))) {
					unClosedBlocks.add(blockManIdList.get(i));
				}
			}
			return unClosedBlocks;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("block关闭失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
/*
	public Page listAllBak(JSONObject enterParam, String listType, JSONObject conditionJson, JSONObject orderJson,
			int currentPageNum, int pageSize) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select distinct m.BLOCK_ID,m.DESCP,m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP,"
					+ "m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group"
					+ "  where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP, m.MONTH_EDIT_GROUP_ID,(select distinct group_name"
					+ "  from user_group where group_id = m.MONTH_EDIT_GROUP_ID) MONTH_EDIT_GROUP,"
					+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE,"
					+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_START_DATE, 'yyyymmdd') MONTH_EDIT_PLAN_START_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_END_DATE, 'yyyymmdd') MONTH_EDIT_PLAN_END_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_START_DATE, 'yyyymmdd') MONTH_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_END_DATE, 'yyyymmdd') MONTH_PRODUCE_PLAN_END_DATE,"
					+ " t.BLOCK_NAME," + " nvl(u.user_real_name, '') USER_REAL_NAME," + " m.STATUS," + " k.TASK_ID,"
					+ " k.NAME," + " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,"
					+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_START_DATE, 'yyyymmdd') TASK_START_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_END_DATE, 'yyyymmdd') TASK_END_DATE"
					+ " from block_man m, block t, user_info u, task k, user_group u"
					+ " where m.block_id = t.block_id(+) and m.latest = 1 and m.create_user_id = u.user_id(+)"
					+ " and t.city_id = k.city_id(+)" + " and k.latest = 1" + "and m.collect_group_id = u.group_id(+)";
			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockId".equals(key)) {
						selectSql += " and t.block_id=" + conditionJson.getInt(key);
					}
					if ("createUserName".equals(key)) {
						selectSql += " and u.USER_REAL_NAME like '%" + conditionJson.getString(key) + "%'";
					}
					if ("blockName".equals(key)) {
						selectSql += " and t.block_name like '%" + conditionJson.getString(key) + "%'";
					}
					if ("status".equals(key)) {
						String status = ((conditionJson.getJSONArray(key).toString()).replace('[', '(')).replace(']',
								')');
						selectSql += " and m.status in " + status;
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("collectPlanStartDate".equals(key)) {
						selectSql += (" order by t.COLLECT_PLAN_START_DATE "
								+ orderJson.getString("collectPlanStartDate"));
						break;
					}
					if ("collectPlanEndDate".equals(key)) {
						selectSql += (" order by t.COLLECT_PLAN_END_DATE " + orderJson.getString("collectPlanEndDate"));
						break;
					}
					if ("blockId".equals(key)) {
						selectSql += (" order by m.block_id " + orderJson.getString("blockId"));
						break;
					}
				}
			} else {
				selectSql += " order by m.block_id";
			}
			return BlockOperation.selectBlockList(conn, selectSql, null, currentPageNum, pageSize);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	/*
	public List listAll(JSONObject enterParam, String listType, JSONObject conditionJson, JSONObject orderJson)
			throws Exception {
		Connection conn = null;
		String selectSql = "";
		String selectNoPlanSqlByCityId = "";// 未规划城市下的block查询
		try {
			conn = DBConnector.getInstance().getManConnection();

			int cityId = 0;
			String inforId = "";
			int taskType = enterParam.getInt("taskType");
			if (4 == taskType) {
				inforId = enterParam.getString("inforId");
			} else {
				cityId = enterParam.getInt("cityId");
			}
			if (listType == null || "snapshot".equals(listType)) {
				if (!inforId.isEmpty()) {
					selectSql = "WITH T AS (SELECT m.block_id,t.block_name,m.status blockStatus,i.plan_status FROM BLOCK t,Block_Man m,infor i WHERE i.infor_id='"
							+ inforId + "' AND i.task_id=m.task_id AND t.block_id=m.block_id AND m.latest = 1) "
							+ "SELECT block_id,block_name,blockStatus,plan_status from T WHERE 1=1";
				} else {
					selectSql = " WITH T AS (SELECT t.block_id,t.block_name,m.status blockStatus,t.plan_status FROM BLOCK t,Block_Man m"
							+ " WHERE t.block_id=m.block_id  AND m.latest = 1 AND t.city_id=" + cityId
							+ " UNION ALL  SELECT t.block_id,t.block_name,0 blockStatus,t.plan_status "
							+ "FROM BLOCK t WHERE t.plan_status=0 AND t.city_id=" + cityId
							+ " ) SELECT block_id,block_name,blockStatus,plan_status from T  WHERE 1=1";
				}

			} else {
				if (!inforId.isEmpty()) {
					selectSql = "WITH T AS (select distinct t.block_id,t.block_name,m.status blockStatus,t.plan_status,m.DESCP, nvl(u.user_real_name, '') USER_REAL_NAME,"
							+ " m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP, "
							+ " m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group  "
							+ " where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP, "
							+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE,  "
							+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE,  "
							+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE,  "
							+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE,  "
							+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE,  "
							+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE,  "
							+ " k.TASK_ID, k.NAME, k.task_type, "
							+ " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE,  "
							+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE from block_man m, block t, user_info u, task k, user_group u,infor i where i.infor_id='"
							+ inforId
							+ "' AND m.block_id = t.block_id(+) and m.latest = 1 and m.create_user_id = u.user_id(+)  "
							+ " and i.task_id = m.task_id AND m.task_id=k.task_id and k.latest = 1 and m.collect_group_id = u.group_id(+) ) "
							+ " SELECT block_id,block_name,blockStatus,plan_status,DESCP,USER_REAL_NAME,COLLECT_GROUP_ID,COLLECT_GROUP,"
							+ " DAY_EDIT_GROUP_ID,DAY_EDIT_GROUP,COLLECT_PLAN_START_DATE,COLLECT_PLAN_END_DATE,DAY_EDIT_PLAN_START_DATE, "
							+ " DAY_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,TASK_ID,NAME, task_type,"
							+ " PLAN_START_DATE,PLAN_END_DATE from T  WHERE 1=1 ";
				} else {
					selectSql = "WITH T AS (select distinct t.block_id,t.block_name,m.status blockStatus,t.plan_status,m.DESCP, nvl(u.user_real_name, '') USER_REAL_NAME,"
							+ " m.COLLECT_GROUP_ID,u.GROUP_NAME COLLECT_GROUP, "
							+ " m.DAY_EDIT_GROUP_ID,(select distinct group_name from user_group "
							+ " where group_id = m.DAY_EDIT_GROUP_ID) DAY_EDIT_GROUP,"
							+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyymmdd') COLLECT_PLAN_START_DATE, "
							+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyymmdd') COLLECT_PLAN_END_DATE, "
							+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyymmdd') DAY_EDIT_PLAN_START_DATE, "
							+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyymmdd') DAY_EDIT_PLAN_END_DATE, "
							+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_START_DATE, "
							+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyymmdd') DAY_PRODUCE_PLAN_END_DATE, "
							+ " k.TASK_ID, k.NAME, k.task_type,"
							+ " to_char(k.PLAN_START_DATE, 'yyyymmdd') PLAN_START_DATE, "
							+ " to_char(k.PLAN_END_DATE, 'yyyymmdd') PLAN_END_DATE"
							+ " from block_man m, block t, user_info u, task k, user_group u"
							+ " where m.block_id = t.block_id and m.latest = 1 and m.create_user_id = u.user_id(+)"
							+ " and t.city_id = k.city_id and k.latest = 1 and m.collect_group_id = u.group_id(+)"
							+ " AND t.city_id=" + cityId
							+ " UNION ALL SELECT t.block_id,t.block_name, 0 blockStatus,t.plan_status,'---' DESCP,'---' USER_REAL_NAME,0 COLLECT_GROUP_ID,'---' COLLECT_GROUP,"
							+ " 0 DAY_EDIT_GROUP_ID,'---' DAY_EDIT_GROUP,'---' COLLECT_PLAN_START_DATE,'---' COLLECT_PLAN_END_DATE,'---' DAY_EDIT_PLAN_START_DATE, "
							+ " '---' DAY_EDIT_PLAN_END_DATE,'---' DAY_PRODUCE_PLAN_START_DATE,'---' DAY_PRODUCE_PLAN_END_DATE,0 TASK_ID,'---' NAME, 0 task_type,"
							+ " '---' PLAN_START_DATE,'---' PLAN_END_DATE FROM BLOCK t WHERE t.plan_status=0 AND t.city_id="
							+ cityId
							+ ") SELECT block_id,block_name,blockStatus,plan_status,DESCP,USER_REAL_NAME,COLLECT_GROUP_ID,COLLECT_GROUP,"
							+ " DAY_EDIT_GROUP_ID,DAY_EDIT_GROUP,COLLECT_PLAN_START_DATE,COLLECT_PLAN_END_DATE,DAY_EDIT_PLAN_START_DATE, "
							+ " DAY_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,TASK_ID,NAME, task_type,"
							+ " PLAN_START_DATE,PLAN_END_DATE from T  WHERE 1=1 ";
				}

			}

			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockId".equals(key)) {
						selectSql += " and T.block_id=" + conditionJson.getInt(key);
					}
					if ("createUserName".equals(key)) {
						if (listType != null && "integrate".equals(listType)) {
							selectSql += " and T.USER_REAL_NAME like '%" + conditionJson.getString(key) + "%'";
						}
					}
					if ("blockName".equals(key)) {
						selectSql += " and T.block_name like '%" + conditionJson.getString(key) + "%'";
					}
					if ("blockStatus".equals(key)) {
						String blockStatus = ((conditionJson.getJSONArray(key).toString()).replace('[', '('))
								.replace(']', ')');
						selectSql += " and T.blockStatus in " + blockStatus;
					}
					if ("taskName".equals(key)) {
						if (listType != null && "integrate".equals(listType)) {
							selectSql += " and T.NAME like '%" + conditionJson.getString(key) + "%'";
						}
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockName".equals(key)) {
						selectSql += (" order by T.block_name " + orderJson.getString("blockName"));
						break;
					}
					if ("planStatus".equals(key)) {
						selectSql += (" order by T.plan_status " + orderJson.getString("planStatus"));
						break;
					}
					if ("blockId".equals(key)) {
						selectSql += (" order by T.block_id " + orderJson.getString("blockId"));
						break;
					}
				}
			} else {
				selectSql += " order by T.block_id";
			}
			return BlockOperation.selectAllBlock(conn, selectSql, listType);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/*public List<HashMap> listByInfoId(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String inforId = json.getString("inforId");

			String selectSql = "select distinct i.block_id,t.block_name,t.city_id,b.status,k.name,to_char(k.plan_start_date, 'yyyymmdd') plan_start_date,"
					+ "to_char(k.plan_end_date, 'yyyymmdd') plan_end_date,"
					+ " to_char(k.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ "to_char(k.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date "
					+ " from infor_block_mapping i, block t, task k,block_man b where  i.block_id = t.block_id and i.block_id=b.block_id"
					+ " and t.city_id = k.city_id and k.latest = 1 and i.infor_id ='" + inforId + "'";
			String selectSqlNotOpen = "select t.block_id,t.block_name,t.city_id,0 status from infor_block_mapping i,block t where t.plan_status=0 and"
					+ " i.block_id=t.block_id and not exists （select 1 from block_man b where b.block_id=t.block_id）and  i.infor_id ='"
					+ inforId + "'";

			return BlockOperation.QuerylistByInfoId(conn, selectSql, selectSqlNotOpen);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	private String blockPushMsgByConn(Connection conn,List blockManIds) throws Exception {
		try {
			if (blockManIds.size()==0){
				return "";
			}
			String BlockIds = "(";
			BlockIds += StringUtils.join(blockManIds.toArray(), ",") + ")";
			String selectSql = "select DISTINCT m.block_man_id,m.block_man_name,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.collect_group_id) collectGroupLeader,(SELECT u.leader_id FROM User_Group u "
					+ "WHERE u.group_id=m.day_edit_group_id) dayEditGroupLeader from block_man m WHERE m.block_man_id in "
					+ BlockIds;
			//System.out.println(selectSql);
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(selectSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ResultSet rs = stmt.executeQuery();
			List<Object[]> msgContentList=new ArrayList<Object[]>();
			/*block创建/编辑/关闭
			1.分配的采集作业组组长
			2.分配的日编作业组组长
			block:XXX(block名称)内容发生变更，请关注*/
			String msgTitle="block发布";
			while (rs.next()) {
				String collectGroupLeader=rs.getString("COLLECTGROUPLEADER");
				String dayEditGroupLeader=rs.getString("DAYEDITGROUPLEADER");
				if(collectGroupLeader!=null && !collectGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[3];
					msgTmp[0]=collectGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="block:"+rs.getString("BLOCK_MAN_NAME")+"内容发生变更，请关注";
					msgContentList.add(msgTmp);
				}
				if(dayEditGroupLeader!=null && !dayEditGroupLeader.isEmpty()){
					Object[] msgTmp=new Object[3];
					msgTmp[0]=dayEditGroupLeader;
					msgTmp[1]=msgTitle;
					msgTmp[2]="block:"+rs.getString("BLOCK_MAN_NAME")+"内容发生变更，请关注";
					msgContentList.add(msgTmp);
				}
			}
			
			if(msgContentList.size()>0){
				blockPushMsgByMsg(conn,msgContentList);
			}

			BlockOperation.updateMainBlock(conn, blockManIds);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("发布失败，原因为:" + e.getMessage(), e);
		}
		return "发布成功";

	}

	public String blockPushMsg(List blockManIds) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			this.blockPushMsgByConn(conn,blockManIds);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("发布失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		return "发布成功";

	}

	private void blockPushMsgByMsg(Connection conn,
			List<Object[]> msgContentList) throws Exception {
		String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID IN (4, 5)";
		List<Integer> userIdList=UserInfoOperation.getUserListBySql(conn, userSql);
		Object[][] msgList=new Object[msgContentList.size()][3];
		int num=0;
		for(Object[] msgContent:msgContentList){
			msgList[num]=msgContent;
			num+=1;
		}
		MessageOperation.batchInsert(conn,msgList);		
	}

	public Page list(int stage, JSONObject condition, JSONObject order, int currentPageNum,int pageSize, int snapshot) throws Exception {
		// TODO Auto-generated method stub
		
		Connection conn = null;		
		try {
			conn = DBConnector.getInstance().getManConnection();
			//返回部分字段
			if(snapshot==1){
				return this.listBySnapshot(conn,stage, condition, order, currentPageNum, pageSize);
				}
			else{
				return this.listByAll(conn,condition, order, currentPageNum, pageSize);
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private Page listByAll(Connection conn, JSONObject conditionJson,
			JSONObject order, int currentPageNum, int pageSize) throws Exception {
		String conditionSql="";
		String statusSql="";
		if(null!=conditionJson && !conditionJson.isEmpty()){
			Iterator keys = conditionJson.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if("blockManName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("taskId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.TASK_ID ="+conditionJson.getInt(key);}
				if("cityId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.CITY_ID ="+conditionJson.getInt(key);}
				if("blockManId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_ID ="+conditionJson.getInt(key);}
				if("createUserName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.CREATE_USER_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("taskName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("blockPlanStatus".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_PLAN_STATUS ="+conditionJson.getInt(key);}				
			}
		}	
		if(!statusSql.isEmpty()){//有非status
			conditionSql+=" and ("+statusSql+")";}
		
		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
		long pageEndNum = currentPageNum * pageSize;
		String selectSql = "";
		String selectPart="";
		String wherePart="";
		selectSql="WITH TASK_PLAN AS"
				+ " (SELECT T.TASK_ID, 3 PLAN_STATUS"
				+ "    FROM TASK T, BLOCK_MAN BM"
				+ "   WHERE T.TASK_ID = BM.TASK_ID"
				+ "     AND NOT EXISTS (SELECT 1"
				+ "            FROM BLOCK_MAN BMM"
				+ "           WHERE BMM.TASK_ID = T.TASK_ID"
				+ "             AND BMM.STATUS <> 0)"
				+ "  UNION ALL"
				+ "  SELECT T.TASK_ID, 2 PLAN_STATUS"
				+ "    FROM TASK T"
				+ "   WHERE NOT EXISTS"
				+ "   (SELECT 1 FROM BLOCK_MAN BMM WHERE BMM.TASK_ID = T.TASK_ID)"
				+ "  UNION ALL"
				+ "  SELECT T.TASK_ID, 2 PLAN_STATUS"
				+ "    FROM TASK T, BLOCK_MAN BM"
				+ "   WHERE T.TASK_ID = BM.TASK_ID"
				+ "     AND EXISTS (SELECT 1"
				+ "            FROM BLOCK_MAN BMM"
				+ "           WHERE BMM.TASK_ID = T.TASK_ID"
				+ "             AND BMM.STATUS <> 0)),"
				+ " MAN_LIST AS"
				+ " (SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.DESCP BLOCK_DESCP,"
				+ "                  T.TASK_ID,"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"
				+ "                  TT.CITY_ID,"
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				/*
				 * 记录默认排序原则：
				 * ①根据状态排序：开启>草稿>未规划>100%(已完成)>已关闭 
				 *    用order_status来表示这个排序的先后顺序。分别是开启0>草稿1>未规划2>100%(已完成)3>已关闭4
				 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
				 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
				 */
				+ "                  CASE TT.STATUS"
				+ "                      WHEN 1 THEN CASE TP.PLAN_STATUS WHEN 2 THEN 0"
                + "                       when 3 then 3 end "
                + "                         when 2 then 1"
                + "                           when 0 then 4 end order_status,"
				+ "                  S.PERCENT,"
				+ "                  S.DIFF_DATE,"
				+ "                  TO_CHAR(T.COLLECT_PLAN_START_DATE, 'YYYYMMDD') COLLECT_PLAN_START_DATE,"
				+ "                  TO_CHAR(T.COLLECT_PLAN_END_DATE, 'YYYYMMDD') COLLECT_PLAN_END_DATE,"
				+ "                  T.COLLECT_GROUP_ID,"
				+ "                  S.COLLECT_PERCENT,"
				+ "                  S.COLLECT_DIFF_DATE,"
				+ "                  S.COLLECT_PROGRESS,"
				/*+ "                  CASE NVL(ST.STAGE, 999)"
				+ "                    WHEN 0 THEN 1"
				+ "                    ELSE 0 END COLLECT_ASSIGN_STATUS,"*/
				+ "                  GC.GROUP_NAME COLLECT_GROUP_NAME,"
				+ "                  TO_CHAR(T.DAY_EDIT_PLAN_START_DATE, 'YYYYMMDD') DAY_EDIT_PLAN_START_DATE,"
				+ "                  TO_CHAR(T.DAY_EDIT_PLAN_END_DATE, 'YYYYMMDD') DAY_EDIT_PLAN_END_DATE,"
				+ "                  T.DAY_EDIT_GROUP_ID,"
				+ "                  S.DAILY_PERCENT,"
				+ "                  S.DAILY_DIFF_DATE,"
				+ "                  S.DAILY_PROGRESS,"
				/*+ "                  CASE NVL(ST.STAGE, 999)"
				+ "                    WHEN 1 THEN 1"
				+ "                    ELSE 0 END DAILY_ASSIGN_STATUS,"*/
				+ "                  GE.GROUP_NAME DAY_EDIT_GROUP_NAME,"
				+ "                  TT.TASK_TYPE,"
				+ "                  I.USER_REAL_NAME CREATE_USER_NAME,"
				+ "                  T.CREATE_USER_ID"
				+ "    FROM BLOCK                     B,"
				+ "         BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                GC,"
				+ "         USER_GROUP                GE,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				//+ "         SUBTASK                   ST,"
				+ "         USER_INFO I,"
				+ "         TASK_PLAN                 TP"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.CREATE_USER_ID=I.USER_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND B.BLOCK_ID = T.BLOCK_ID"
				+ "     AND T.COLLECT_GROUP_ID = GC.GROUP_ID(+)"
				+ "     AND T.DAY_EDIT_GROUP_ID = GE.GROUP_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.TASK_ID = TP.TASK_ID(+)"
				//+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID(+)"
				+ "     AND TT.LATEST = 1"
				+ "  UNION ALL"
				+ "  SELECT DISTINCT 0,"
				+ "                  '---',"
				+ "                  '---',"
				+ "                  0,"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"
				+ "                  C.CITY_ID,"
				+ "                  0 STATUS,"
				+ "                  B.PLAN_STATUS block_plan_status,"
				+ "                  2 order_status,"
				+ "                  0,"
				+ "                  0,"
				+ "                  '---' COLLECT_PLAN_START_DATE,"
				+ "                  '---' COLLECT_PLAN_END_DATE,"
				+ "                  0 COLLECT_GROUP_ID,"
				+ "                  0 COLLECT_PERCENT,"
				+ "                  0 COLLECT_DIFF_DATE,"
				+ "                  0 COLLECT_PROGRESS,"
				//+ "                  0 COLLECT_ASSIGN_STATUS,"
				+ "                  '---' COLLECT_GROUP_NAME,"
				+ "                  '---' DAY_EDIT_PLAN_START_DATE,"
				+ "                  '---' DAY_EDIT_PLAN_END_DATE,"
				+ "                  0 DAY_EDIT_GROUP_ID,"
				+ "                  0 DAILY_PERCENT,"
				+ "                  0 DAILY_DIFF_DATE,"
				+ "                  0 DAILY_PROGRESS,"
				//+ "                  0 DAILY_ASSIGN_STATUS,"
				+ "                  '---' DAY_EDIT_GROUP_NAME,"
				+ "                  1 TASK_TYPE,"
				+ "                  '---',"
				+ "                  0"
				+ "    FROM BLOCK B, CITY C"
				+ "   WHERE B.CITY_ID = C.CITY_ID"
				+ "     AND B.PLAN_STATUS = 0"
				+ "     AND C.CITY_ID<>100002),"
				+ " FINAL_TABLE AS"
				+ " (SELECT *"
				+ "    FROM MAN_LIST"
				+ "    WHERE 1=1"
				+ conditionSql+""
				+ " order by man_list.order_status asc,man_list.diff_date desc,man_list.percent desc,man_list.block_man_name,man_list.block_name)"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
		return BlockOperation.getAllQuery(conn, selectSql,currentPageNum,pageSize);
	}

	public Page listBySnapshot(Connection conn,int stage, JSONObject conditionJson, JSONObject order, int currentPageNum,int pageSize) throws Exception {
		
		String conditionSql="";
		String statusSql="";
		if(null!=conditionJson && !conditionJson.isEmpty()){
			Iterator keys = conditionJson.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if("blockManName".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.BLOCK_MAN_NAME LIKE '%"+conditionJson.getString(key)+"%'";}
				if("groupId".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.GROUP_ID ="+conditionJson.getInt(key);}
				if("planStatus".equals(key)){
					conditionSql=conditionSql+" AND MAN_LIST.STATUS =1 AND MAN_LIST.PLAN_STATUS="+conditionJson.getInt(key);}
				
				if ("assignStatus".equals(key)) {
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.ASSIGN_STATUS="+conditionJson.getInt(key);}
				if ("progress".equals(key)) {
					if(!statusSql.isEmpty()){statusSql+=" or ";}
					statusSql+=" MAN_LIST.Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
				if ("diffDate".equals(key)) {
					JSONArray diffDateArray=conditionJson.getJSONArray(key);
					for(Object diffDate:diffDateArray){
						if((int) diffDate==1){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date>0";
						}
						if((int) diffDate==0){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date=0";
						}
						if((int) diffDate==-1){
							if(!statusSql.isEmpty()){statusSql+=" or ";}
							statusSql+=" MAN_LIST.diff_date<0";
						}
						}
					}
			}
		}	
		if(!statusSql.isEmpty()){//有非status
			conditionSql+=" and ("+statusSql+")";}
		
		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
		long pageEndNum = currentPageNum * pageSize;
		String selectSql = "";
		String selectPart="";
		String wherePart="";
		String stagePart="";
		if(stage==0){
			selectPart="                  TO_CHAR(T.COLLECT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
					+ "                  TO_CHAR(T.COLLECT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
					+ "                  T.COLLECT_GROUP_ID GROUP_ID,"
					+ "                  S.COLLECT_PERCENT PERCENT,"
					+ "                  S.COLLECT_DIFF_DATE DIFF_DATE,"
					+ "                  S.COLLECT_PROGRESS progress,"
					/*+ "                  CASE NVL(ST.STAGE, 999)"
					+ "                    WHEN 0 THEN 1"
					+ "                    ELSE 0 END ASSIGN_STATUS,"*/
					+ "                  G.GROUP_NAME,";
			wherePart="     AND T.COLLECT_GROUP_ID = G.GROUP_ID(+)";
			stagePart="STAGE=0";
		}
		if(stage==1){
			selectPart="                  TO_CHAR(T.DAY_EDIT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
					+ "                  TO_CHAR(T.DAY_EDIT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
					+ "                  T.DAY_EDIT_GROUP_ID GROUP_ID,"
					+ "                  S.DAILY_PERCENT PERCENT,"
					+ "                  S.DAILY_DIFF_DATE DIFF_DATE,"
					+ "                  S.DAILY_PROGRESS progress,"
					/*+ "                  CASE NVL(ST.STAGE, 999)"
					+ "                    WHEN 1 THEN 1"
					+ "                    ELSE 0 END ASSIGN_STATUS,"*/
					+ "                  G.GROUP_NAME,";
			wherePart="     AND T.DAY_EDIT_GROUP_ID = G.GROUP_ID(+)";
			stagePart="STAGE=1";
		}
		selectSql="WITH MAN_LIST AS"
				//未分配子任务
				+ " (SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"	
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  0 ASSIGN_STATUS,"
				+ "                  2 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				//+ "     AND T.STATUS=1"
				+ "     AND (EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT."+stagePart
						+ " GROUP BY STT.BLOCK_MAN_ID HAVING COUNT(DISTINCT STT.STATUS)=2)"
						+ " OR NOT EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT."+stagePart+"))"
				+wherePart					
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "  UNION ALL"
				//分配子任务，且子任务都是关闭状态==〉已完成
				+ " SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"	
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  1 ASSIGN_STATUS,"
				+ "                  3 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         SUBTASK                   ST,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.STATUS=1"
				+ "     AND ST."+stagePart
				+wherePart		
				+ "     AND NOT EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT.STATUS=1 AND STT."+stagePart+")"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID"
				+ "  UNION ALL"
				//分配子任务，且存在非关子任务==〉作业中
				+ " SELECT DISTINCT T.BLOCK_MAN_ID,"
				+ "                  T.BLOCK_MAN_NAME,"
				+ "                  T.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"	
				+ "                  T.STATUS BLOCK_STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  1 ASSIGN_STATUS,"
				+ "                  2 PLAN_STATUS,"
				+selectPart					
				+ "                  TT.TASK_TYPE"
				+ "    FROM BLOCK_MAN                 T,"
				+ "         TASK                      TT,"
				+ "         USER_GROUP                G,"
				+ "         FM_STAT_OVERVIEW_BLOCKMAN S,"
				+ "         SUBTASK                   ST,"
				+ "         BLOCK                   B"
				+ "   WHERE T.TASK_ID = TT.TASK_ID"
				+ "     AND T.BLOCK_ID=B.BLOCK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.STATUS=1"
				+ "     AND ST."+stagePart
				+wherePart		
				+ "     AND EXISTS(SELECT 1 FROM SUBTASK STT WHERE STT.BLOCK_MAN_ID=T.BLOCK_MAN_ID AND STT.STATUS<>0 AND STT."+stagePart+")"
				+ "     AND T.BLOCK_MAN_ID = S.BLOCK_MAN_ID(+)"
				+ "     AND T.BLOCK_MAN_ID = ST.BLOCK_MAN_ID(+)"
				+ "  UNION ALL"
				//未规划block
				+ "  SELECT DISTINCT 0,"
				+ "                  '---',"
				+ "                  B.BLOCK_ID,"
				+ "                  B.BLOCK_NAME,"				
				+ "                  0 STATUS,"
				+ "                  B.PLAN_STATUS BLOCK_PLAN_STATUS,"
				+ "                  0 ASSIGN_STATUS,"
				+ "                  1 PLAN_STATUS,"
				+ "                  '---' PLAN_START_DATE,"
				+ "                  '---' PLAN_END_DATE,"
				+ "                  0 GROUP_ID,"
				+ "                  0 PERCENT,"
				+ "                  0 DIFF_DATE,"
				+ "                  0 PROGRESS,"
				+ "                  '---' GROUP_NAME,"
				+ "                  1 TASK_TYPE"
				+ "    FROM BLOCK B, CITY C"
				+ "   WHERE B.CITY_ID = C.CITY_ID"
				+ "     AND B.PLAN_STATUS = 0"
				+ "     AND C.CITY_ID<>100002),"
				+ " FINAL_TABLE AS"
				+ " (SELECT *"
				+ "    FROM MAN_LIST"
				+ "    WHERE 1=1"
				+ conditionSql+")"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
		return BlockOperation.getSnapshotQuery(conn, selectSql,currentPageNum,pageSize);
	}
	

}
