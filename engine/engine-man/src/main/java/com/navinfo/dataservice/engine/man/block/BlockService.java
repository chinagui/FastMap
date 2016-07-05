package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.CLOB;

/**
 * @ClassName: BlockService
 * @author code generator
 * @date 2016-06-08 01:32:00
 * @Description: TODO
 */

public class BlockService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private BlockService() {
	}

	private static class SingletonHolder {
		private static final BlockService INSTANCE = new BlockService();
	}

	public static BlockService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void batchOpen(long userId, JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");

			String createSql = "insert into block_man (BLOCK_MAN_ID, CREATE_USER_ID,BLOCK_ID,COLLECT_GROUP_ID, COLLECT_PLAN_START_DATE,"
					+ "COLLECT_PLAN_END_DATE,DAY_EDIT_GROUP_ID,DAY_EDIT_PLAN_START_DATE,DAY_EDIT_PLAN_END_DATE,MONTH_EDIT_GROUP_ID,"
					+ "MONTH_EDIT_PLAN_START_DATE,MONTH_EDIT_PLAN_END_DATE,DAY_PRODUCE_PLAN_START_DATE,DAY_PRODUCE_PLAN_END_DATE,"
					+ "MONTH_PRODUCE_PLAN_START_DATE,MONTH_PRODUCE_PLAN_END_DATE,DESCP) "
					+ "values(BLOCK_MAN_SEQ.NEXTVAL,?,?,?,to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?,to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),?)";

			Object[][] param = new Object[blockArray.size()][];
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				Object[] obj = new Object[] { userId, block.getInt("blockId"), block.getInt("collectGroupId"),
						block.getString("collectPlanStartDate"), block.getString("collectPlanEndDate"),
						block.getInt("dayEditGroupId"), block.getString("dayEditPlanStartDate"),
						block.getString("dayEditPlanEndDate"), block.getInt("monthEditGroupId"),
						block.getString("monthEditPlanStartDate"), block.getString("monthEditPlanEndDate"),
						block.getString("dayProducePlanStartDate"), block.getString("dayProducePlanEndDate"),
						block.getString("monthProducePlanStartDate"), block.getString("monthProducePlanEndDate"),
						block.getString("descp") };
				param[i] = obj;
			}

			run.batch(conn, createSql, param);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public void batchUpdate(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONArray blockArray = json.getJSONArray("blocks");

			String createSql = "update block_man set COLLECT_GROUP_ID=?, COLLECT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "COLLECT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_GROUP_ID=?,DAY_EDIT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_EDIT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_EDIT_GROUP_ID=?,"
					+ "MONTH_EDIT_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_EDIT_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_PRODUCE_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),DAY_PRODUCE_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "MONTH_PRODUCE_PLAN_START_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'),MONTH_PRODUCE_PLAN_END_DATE=to_timestamp(?,'yyyy-mm-dd hh24:mi:ss.ff'), DESCP=? where BLOCK_ID=?";

			Object[][] param = new Object[blockArray.size()][];
			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				BlockMan bean = (BlockMan) JSONObject.toBean(block, BlockMan.class);
				Object[] obj = new Object[] { bean.getCollectGroupId(), bean.getCollectPlanStartDate(),
						bean.getCollectPlanEndDate(), bean.getDayEditGroupId(), bean.getDayEditPlanStartDate(),
						bean.getDayEditPlanEndDate(), bean.getMonthEditGroupId(), bean.getMonthEditPlanStartDate(),
						bean.getMonthEditPlanEndDate(), bean.getDayProducePlanStartDate(),
						bean.getDayProducePlanEndDate(), bean.getMonthProducePlanStartDate(),
						bean.getMonthProducePlanStartDate(), bean.getDescp(), bean.getBlockId() };
				param[i] = obj;
			}

			run.batch(conn, createSql, param);

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
			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY from BLOCK t where sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
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
			String wkt=json.getString("wkt");
			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']',
					')');

			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY,t.PLAN_STATUS,t.CITY_ID from BLOCK t where t.PLAN_STATUS in "
					+ planningStatus;

			if (StringUtils.isNotEmpty(json.getString("snapshot"))) {
				if ("1".equals(json.getString("snapshot"))) {
					selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.PLAN_STATUS,t.CITY_ID from BLOCK t where t.PLAN_STATUS in "
							+ planningStatus;
				}
			}
			;
			if (!json.containsKey("relation") || ("intersect".equals(json.getString("relation")))) {
				selectSql += " and SDO_ANYINTERACT(t.geometry,sdo_geometry('"+wkt+"',8307))='TRUE'";
			} else {
				if ("within".equals(json.getString("relation"))) {
					selectSql += " and sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry('"+wkt+"', 8307)), 'DISTANCE=0') = 'TRUE'";
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
			Block bean = (Block) JSONObject.toBean(obj, Block.class);

			String selectSql = "select t.BLOCK_ID,t.CITY_ID, t.BLOCK_NAME, t.GEOMETRY.get_wkt() as GEOMETRY,"
					+ " t.PLAN_STATUS, k.name taskName, b.collect_group_id, b.day_edit_group_id,"
					+ " b.month_edit_group_id, to_char(b.collect_plan_start_date, 'yyyymmdd') collect_plan_start_date, to_char(b.collect_plan_end_date, 'yyyymmdd') collect_plan_end_date,"
					+ " to_char(b.day_edit_plan_start_date, 'yyyymmdd') day_edit_plan_start_date, to_char(b.day_edit_plan_end_date, 'yyyymmdd') day_edit_plan_end_date, to_char(b.month_edit_plan_start_date, 'yyyymmdd') month_edit_plan_start_date,"
					+ " to_char(b.month_edit_plan_end_date, 'yyyymmdd') month_edit_plan_end_date,to_char(b.day_produce_plan_start_date, 'yyyymmdd') day_produce_plan_start_date,"
					+ " to_char(b.day_produce_plan_end_date, 'yyyymmdd') day_produce_plan_end_date,"
					+ " to_char(b.month_produce_plan_start_date, 'yyyymmdd') month_produce_plan_start_date,"
					+ " to_char(b.month_produce_plan_end_date, 'yyyymmdd') month_produce_plan_end_date"
					+ " from BLOCK t, BLOCK_MAN b, TASK k where t.BLOCK_ID = ?"
					+ " and t.block_id = b.block_id and t.city_id = k.city_id and k.latest = 1 and b.latest=1";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>() {
				public HashMap<String, Object> handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						CLOB clob = (CLOB) rs.getObject("GEOMETRY");
						String clobStr = DataBaseUtils.clob2String(clob);
						try {
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
						return map;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, bean.getBlockId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<HashMap> listByGroup(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String selectSql = null;
			int stage = json.getInt("stage");

			JSONArray groupIds = json.getJSONArray("groupIds");
			String groups = ((groupIds.toString()).replace('[', '(')).replace(']', ')');
			
			selectSql = "select b.BLOCK_ID,b.CITY_ID, b.BLOCK_NAME, b.GEOMETRY.get_wkt() as GEOMETRY,"
					+ " b.PLAN_STATUS from block_man t,block b,task k,subtask s where t.block_id=b.block_id and b.city_id=k.city_id and k.task_id=s.task_id and t.latest=1 and k.latest=1 and s.stage=? ";

			if (0 == stage) {
				selectSql += "and t.COLLECT_GROUP_ID in " + groups;
						
			} else if (1 == stage) {
				selectSql += "and t.DAY_EDIT_GROUP_ID in "+ groups;
			} else {
				selectSql += "and t.MONTH_EDIT_GROUP_ID in "+ groups;
			}

			return BlockOperation.queryBlockByGroup(conn, selectSql, stage);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public HashMap close(List<Integer> blockIdList) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			// 获取所有blockIdList中可以关闭的block
			List<Integer> blockReadyToClose = BlockOperation.getBlockListReadyToClose(conn, blockIdList);

			if (!blockReadyToClose.isEmpty()) {
				BlockOperation.closeBlockByBlockIdList(conn, blockReadyToClose);
			}

			HashMap unClosedBlocks = new HashMap();
			for (int i = 0; i < blockIdList.size(); i++) {
				if (!blockReadyToClose.contains(blockIdList.get(i))) {
					unClosedBlocks.put(blockIdList.get(i), "BLOCK内存在未完成作业，BLOCK无法关闭");
				}
			}

			return unClosedBlocks;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Page listAll(JSONObject conditionJson, JSONObject orderJson, int currentPageNum, int pageSize)
			throws Exception {
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
	}

	public List<HashMap> listByInfoId(JSONObject json) throws ServiceException {
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
	}

}
