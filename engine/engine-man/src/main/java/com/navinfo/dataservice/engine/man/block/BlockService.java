package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.CLOB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * @ClassName: BlockService
 * @author code generator
 * @date 2016-06-08 01:32:00
 * @Description: TODO
 */
@Service
public class BlockService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

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

	public List<HashMap> listByProduce(String wkt) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY from BLOCK t where sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
			List<Object> list = new ArrayList<Object>();
			list.add(wkt);
			return BlockOperation.queryProduceBlock(conn, selectSql, list);
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

			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']',
					')');

			String selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY,t.PLAN_STATUS from BLOCK t where PLAN_STATUS in "
					+ planningStatus;

			if (StringUtils.isNotEmpty(json.getString("snapshot"))) {
				if ("1".equals(json.getString("snapshot"))) {
					selectSql = "select t.BLOCK_ID,t.BLOCK_NAME,t.PLAN_STATUS from BLOCK t where PLAN_STATUS in "
							+ planningStatus;
				}
			}
			;
			if (!json.containsKey("relation") || ("intersect".equals(json.getString("relation")))) {
				selectSql += " and SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
			} else {
				if ("within".equals(json.getString("relation"))) {
					selectSql += " and sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
				}
			}

			return BlockOperation.queryBlockBySql(conn, selectSql, json.getString("wkt"));
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

			String selectSql = "select t.BLOCK_ID,t.CITY_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY,t.PLAN_STATUS from BLOCK t where t.BLOCK_ID=?";
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

			Format format = new SimpleDateFormat("yyyyMMdd");
			String time = format.format(DateUtilsEx.getCurDate());

			if (0 == stage) {
				selectSql = "select t.BLOCK_ID,t.COLLECT_PLAN_START_DATE as planStartDate,t.COLLECT_PLAN_END_DATE as planEndDate,t.DESCP from block_man t where t.COLLECT_PLAN_END_DATE>=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') and t.COLLECT_PLAN_START_DATE <=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') "
						+ "and t.COLLECT_GROUP_ID in " + groups;
			} else if (1 == stage) {
				selectSql = "select t.BLOCK_ID,t.DAY_EDIT_PLAN_START_DATE as planStartDate,t.DAY_EDIT_PLAN_END_DATE as planEndDate,t.DESCP from block_man t where t.DAY_EDIT_PLAN_END_DATE>=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') and t.DAY_EDIT_PLAN_START_DATE <=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') and t.DAY_EDIT_GROUP_ID in "
						+ groups;
			} else {
				selectSql = "select t.BLOCK_ID,t.MONTH_EDIT_PLAN_START_DATE as planStartDate,t.MONTH_EDIT_PLAN_END_DATE as planEndDate,t.DESCP from block_man t where t.MONTH_EDIT_PLAN_END_DATE>=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') and t.MONTH_EDIT_PLAN_START_DATE <=TO_DATE(?, 'YYYY/MM/DD-HH24:MI:SS') and t.MONTH_EDIT_GROUP_ID in "
						+ groups;
			}

			List<Object> list = new ArrayList<Object>();
			list.add(time);
			list.add(time);

			return BlockOperation.queryBlockByGroup(conn, selectSql, list);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<Integer> close(List<Integer> blockIdList) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();

			// 获取所有blockIdList中可以关闭的block
			List<Integer> blockReadyToClose = BlockOperation.getBlockListReadyToClose(conn, blockIdList);

			if (!blockReadyToClose.isEmpty()) {
				BlockOperation.closeBlockByBlockIdList(conn, blockReadyToClose);

				List<Integer> unClosedBlockList = new ArrayList<Integer>();
				for (int i = 0; i < blockIdList.size(); i++) {
					if (!blockReadyToClose.contains(blockIdList.get(i))) {
						unClosedBlockList.add(blockIdList.get(i));
					}
				}

				return unClosedBlockList;
			} else {
				return blockIdList;
			}

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
					+ " to_char(m.COLLECT_PLAN_START_DATE, 'yyyy-mm-dd') COLLECT_PLAN_START_DATE,"
					+ " to_char(m.COLLECT_PLAN_END_DATE, 'yyyy-mm-dd') COLLECT_PLAN_END_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_START_DATE, 'yyyy-mm-dd') DAY_EDIT_PLAN_START_DATE,"
					+ " to_char(m.DAY_EDIT_PLAN_END_DATE, 'yyyy-mm-dd') DAY_EDIT_PLAN_END_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_START_DATE, 'yyyy-mm-dd') MONTH_EDIT_PLAN_START_DATE,"
					+ " to_char(m.MONTH_EDIT_PLAN_END_DATE, 'yyyy-mm-dd') MONTH_EDIT_PLAN_END_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_START_DATE, 'yyyy-mm-dd') DAY_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.DAY_PRODUCE_PLAN_END_DATE, 'yyyy-mm-dd') DAY_PRODUCE_PLAN_END_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_START_DATE, 'yyyy-mm-dd') MONTH_PRODUCE_PLAN_START_DATE,"
					+ " to_char(m.MONTH_PRODUCE_PLAN_END_DATE, 'yyyy-mm-dd') MONTH_PRODUCE_PLAN_END_DATE,"
					+ " t.BLOCK_NAME," + " nvl(u.user_real_name, '') USER_REAL_NAME," + " m.STATUS," + " k.TASK_ID,"
					+ " k.NAME," + " to_char(k.PLAN_START_DATE, 'yyyy-mm-dd') PLAN_START_DATE,"
					+ " to_char(k.PLAN_END_DATE, 'yyyy-mm-dd') PLAN_END_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_START_DATE, 'yyyy-mm-dd') TASK_START_DATE,"
					+ " to_char(k.MONTH_EDIT_PLAN_END_DATE, 'yyyy-mm-dd') TASK_END_DATE"
					+ " from block_man m, block t, user_info u, task k, user_group u"
					+ " where m.block_id = t.block_id(+)" + "and m.create_user_id = u.user_id(+)"
					+ " and t.city_id = k.city_id(+)" + " and k.latest = 1" + "and m.collect_group_id = u.group_id(+)";
			if (null != conditionJson && !conditionJson.isEmpty()) {
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("blockId".equals(key)) {
						selectSql += " and t.block_id=" + conditionJson.getInt(key);
					}
					if ("createUserId".equals(key)) {
						selectSql += " and m.create_user_id=" + conditionJson.getInt(key);
					}
					if ("blockName".equals(key)) {
						selectSql += " and t.block_name like '%" + conditionJson.getString(key) + "%";
					}
				}
			}
			if (null != orderJson && !orderJson.isEmpty()) {
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("collectPlanEndDate".equals(key)) {
						selectSql += " order by t.COLLECT_PLAN_START_DATE";
						break;
					}
					if ("collectPlanEndDate ".equals(key)) {
						selectSql += " order by t.COLLECT_PLAN_END_DATE";
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

}
