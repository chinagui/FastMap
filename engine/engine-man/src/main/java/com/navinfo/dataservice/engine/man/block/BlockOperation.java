package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class BlockOperation {
	private static Logger log = LoggerRepos.getLogger(BlockOperation.class);

	public BlockOperation() {
		// TODO Auto-generated constructor stub
	}

	public static List<HashMap> queryBlockBySql(Connection conn, String selectSql) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>() {
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while (rs.next()) {
						HashMap map = new HashMap<String, Integer>();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("planningStatus", rs.getInt("PLAN_STATUS"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						try {
							STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						list.add(map);
					}
					return list;
				}

			};
			return run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}

	public static List<HashMap> queryProduceBlock(Connection conn, String selectSql, final JSONObject json)
			throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>() {
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while (rs.next()) {
						HashMap map = new HashMap<String, Integer>();
						// block下grid日完成度为100%，block才可出品
						try {
							if (BlockOperation.checkGridFinished(rs.getInt("BLOCK_ID"), json.getInt("stage"),
									json.getInt("stage"))) {
								map.put("blockId", rs.getInt("BLOCK_ID"));
								map.put("blockName", rs.getInt("BLOCK_NAME"));
								STRUCT struct = (STRUCT) rs.getObject("geometry");
								try {
									String clobStr = GeoTranslator.struct2Wkt(struct);
									map.put("geometry", Geojson.wkt2Geojson(clobStr));
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								list.add(map);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					return list;
				}

			};
			return run.query(conn, selectSql, rsHandler, json.getString("wkt"));
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}

	public static Page queryBlockByGroup(final Connection conn, String selectSql, final int stage,
			final int currentPageNum, int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					int totalCount = 0;
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("blockStatus", rs.getInt("STATUS"));
						map.put("planStartDate", rs.getString("PLAN_START_DATE"));
						map.put("planEndDate", rs.getString("PLAN_END_DATE"));
						map.put("assignStatus", rs.getInt("flag"));
						map.put("type", rs.getInt("TASK_TYPE"));
						map.put("finishPercent",
								calculateBlockFinishPercent(conn, rs.getInt("BLOCK_ID"), rs.getInt("STATUS"), stage));

						if (totalCount == 0) {
							totalCount = rs.getInt("TOTAL_RECORD_NUM_");
						}
						list.add(map);
					}
					page.setResult(list);
					page.setTotalCount(totalCount);
					return page;
				}

			};
			return  run.query(currentPageNum, pageSize, conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * 判断block下grid是否完成度为100%
	 * 
	 * @param blockId
	 * @param stage
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static boolean checkGridFinished(int blockId, int stage, int type) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String sqlByblockId = "select grid_id from grid where block_id=" + blockId;

			PreparedStatement stmt = conn.prepareStatement(sqlByblockId);
			ResultSet rs = stmt.executeQuery();
			List<String> gridList = new ArrayList();
			while (rs.next()) {
				gridList.add(String.valueOf(rs.getInt(1)));
			}
			// 调用统计模块，查询grid完成度,若不为100%，返回false
			StaticsApi statics = (StaticsApi) ApplicationContextUtil.getBean("staticsApi");
			List<GridStatInfo> GridStatList = new ArrayList();
			// stage:作业阶段（0采集、1日编、2月编）
			if (0 == stage) {
				GridStatList = statics.getLatestCollectStatByGrids(gridList);
			}
			if (1 == stage) {
				GridStatList = statics.getLatestDailyEditStatByGrids(gridList);
			}
			if (2 == stage) {
				GridStatList = statics.getLatestMonthlyEditStatByGrids(gridList);
			}
			GridStatList = statics.getLatestCollectStatByGrids(gridList);

			// type:0 POI，1POI&Road
			for (int i = 0; i < GridStatList.size(); i++) {
				GridStatInfo statInfo = GridStatList.get(i);
				if (0 == type && statInfo.getPercentPoi() != 100) {
					return false;
				}
				if (1 == type && (statInfo.getPercentRoad() != 100 || statInfo.getPercentPoi() != 100)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param conn
	 * @param blockIdList
	 * @return 根据BlockId列表获取其中所有可关闭的block blockIdList
	 * @throws Exception
	 */
	public static List<Integer> getBlockListReadyToClose(Connection conn, List<Integer> blockManIdList) throws Exception {
		// TODO Auto-generated method stub

		try {
			QueryRunner run = new QueryRunner();

			String BlockIds = "(";
			BlockIds += StringUtils.join(blockManIdList.toArray(), ",") + ")";

			String selectSql = "select distinct b.block_man_id "
					+ ",listagg(st.status , ',') within group(order by b.block_man_id) as status" + " from subtask st"
					+ ", block_man b " + " where st.block_man_id = b.block_man_id"
					+ " and b.block_man_id in " + BlockIds + " group by b.block_man_id";

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						String[] s = rs.getString("status").split(",");
						List<String> status = (List<String>) Arrays.asList(s);
						if (!status.contains("1")) {
							list.add(rs.getInt("block_man_id"));
						}

					}
					return list;
				}

			};

			List<Integer> blockList = run.query(conn, selectSql, rsHandler);
			return blockList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param blockList
	 * @throws Exception
	 */
	public static void closeBlockByBlockIdList(Connection conn, List<Integer> blockManList) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			if (!blockManList.isEmpty()) {
				String BlockIds = "(";
				BlockIds += StringUtils.join(blockManList.toArray(), ",") + ")";
				// 更新block_man
				String updateSql = "update block_man" + " set status = 0" + " where latest = 1 and block_man_id in " + BlockIds;
				run.update(conn, updateSql);
				
				updateSql="UPDATE block C"
						+ "   SET C.PLAN_STATUS = 2"
						+ " WHERE NOT EXISTS (SELECT 1"
						+ "          FROM block_man T"
						+ "         WHERE T.block_ID = C.block_ID"
						+ "           AND T.STATUS <> 0"
						+ "			  AND T.LATEST=1)"
						+ "   AND C.PLAN_STATUS = 1";
				run.update(conn, updateSql);				
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	/*
	 * 分页 查询block list
	 */
	public static Page selectBlockList(Connection conn, String selectSql, List<Object> values, final int currentPageNum,
			final int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					int totalCount = 0;
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("descp", rs.getString("DESCP"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("createUserName", rs.getString("USER_REAL_NAME"));
						map.put("collectPlanStartDate", rs.getString("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getString("COLLECT_PLAN_END_DATE"));
						map.put("collectGroupId", rs.getInt("COLLECT_GROUP_ID"));
						map.put("collectGroup", rs.getString("COLLECT_GROUP"));
						map.put("dayEditPlanStartDate", rs.getString("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getString("DAY_EDIT_PLAN_END_DATE"));
						map.put("dayEditGroupId", rs.getInt("DAY_EDIT_GROUP_ID"));
						map.put("dayEditGroup", rs.getString("DAY_EDIT_GROUP"));
						map.put("dayProducePlanStartDate", rs.getString("DAY_PRODUCE_PLAN_START_DATE"));
						map.put("dayProducePlanEndDate", rs.getString("DAY_PRODUCE_PLAN_END_DATE"));
						map.put("monthEditPlanStartDate", rs.getString("MONTH_EDIT_PLAN_START_DATE"));
						map.put("monthEditPlanEndDate", rs.getString("MONTH_EDIT_PLAN_END_DATE"));
						map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
						map.put("monthEditGroup", rs.getString("MONTH_EDIT_GROUP"));
						map.put("monthProducePlanStartDate", rs.getString("MONTH_PRODUCE_PLAN_START_DATE"));
						map.put("monthProducePlanEndDate", rs.getString("MONTH_PRODUCE_PLAN_END_DATE"));
						map.put("status", rs.getInt("STATUS"));
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("taskName", rs.getString("NAME"));
						map.put("taskPlanStartDate", rs.getString("PLAN_START_DATE"));
						map.put("taskPlanEndDate", rs.getString("PLAN_END_DATE"));
						map.put("taskMonthStartDate", rs.getString("TASK_START_DATE"));
						map.put("taskMonthEndDate", rs.getString("TASK_END_DATE"));
						if (totalCount == 0) {
							totalCount = rs.getInt("TOTAL_RECORD_NUM_");
						}
						list.add(map);
					}
					page.setResult(list);
					page.setTotalCount(totalCount);
					return page;
				}

			};
			if (null == values || values.size() == 0) {
				return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler);
			}
			return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}

	/*
	 * 不分页 查询block list
	 */
	public static List selectAllBlock(final Connection conn, String selectSql, 
			String listType) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			List<HashMap> blockList = new ArrayList<HashMap>();
			if (listType == null || "snapshot".equals(listType)) {
				ResultSetHandler<List> rsHandler = new ResultSetHandler<List>() {
					public List handle(ResultSet rs) throws SQLException {
						List<HashMap> list = new ArrayList<HashMap>();
						while (rs.next()) {
							HashMap map = new HashMap();
							map.put("blockId", rs.getInt("BLOCK_ID"));
							map.put("blockName", rs.getString("BLOCK_NAME"));
							map.put("blockStatus", rs.getInt("blockStatus"));
							map.put("planStatus", rs.getInt("plan_status"));
							map.put("finishPercent",
									calculateBlockFinishPercent(conn, rs.getInt("BLOCK_ID"), rs.getInt("blockStatus"),10));
							list.add(map);
						}
						return list;
					}
				};
				blockList = run.query(conn, selectSql, rsHandler);
			} else {
				ResultSetHandler<List> rsHandler = new ResultSetHandler<List>() {

					public List handle(ResultSet rs) throws SQLException {
						List<HashMap> list = new ArrayList<HashMap>();
						while (rs.next()) {
							HashMap map = new HashMap();
							map.put("blockId", rs.getInt("BLOCK_ID"));
							map.put("blockName", rs.getString("BLOCK_NAME"));
							map.put("blockStatus", rs.getInt("blockStatus"));
							map.put("planStatus", rs.getInt("plan_status"));
							map.put("descp", rs.getString("DESCP"));
							map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
							map.put("createUserName", rs.getString("USER_REAL_NAME"));
							map.put("collectPlanStartDate", rs.getString("COLLECT_PLAN_START_DATE"));
							map.put("collectPlanEndDate", rs.getString("COLLECT_PLAN_END_DATE"));
							map.put("collectGroupId", rs.getInt("COLLECT_GROUP_ID"));
							map.put("collectGroup", rs.getString("COLLECT_GROUP"));
							map.put("dayEditPlanStartDate", rs.getString("DAY_EDIT_PLAN_START_DATE"));
							map.put("dayEditPlanEndDate", rs.getString("DAY_EDIT_PLAN_END_DATE"));
							map.put("dayEditGroupId", rs.getInt("DAY_EDIT_GROUP_ID"));
							map.put("dayEditGroup", rs.getString("DAY_EDIT_GROUP"));
							map.put("dayProducePlanStartDate", rs.getString("DAY_PRODUCE_PLAN_START_DATE"));
							map.put("dayProducePlanEndDate", rs.getString("DAY_PRODUCE_PLAN_END_DATE"));
							map.put("taskId", rs.getInt("TASK_ID"));
							map.put("taskName", rs.getString("NAME"));
							map.put("taskType", rs.getString("task_type"));
							map.put("taskPlanStartDate", rs.getString("PLAN_START_DATE"));
							map.put("taskPlanEndDate", rs.getString("PLAN_END_DATE"));
							map.put("finishPercent",
									calculateBlockFinishPercent(conn, rs.getInt("BLOCK_ID"), rs.getInt("blockStatus"),10));
							list.add(map);
						}
						return list;
					}
				};
				blockList = run.query(conn, selectSql, rsHandler);
			}
			return blockList;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * 开启和未开启的block都返回
	 * 
	 * @param conn
	 * @param selectSql
	 * @param selectSqlNotOpen
	 * @return
	 * @throws Exception
	 */
	/*public static List<HashMap> QuerylistByInfoId(Connection conn, String selectSql, String selectSqlNotOpen)
			throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			List<HashMap> resultList = new ArrayList<HashMap>();

			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>() {
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while (rs.next()) {
						HashMap map = new HashMap<String, Integer>();
						map.put("blockId", rs.getInt("block_id"));
						map.put("blockName", rs.getString("block_name"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("taskName", rs.getString("name"));
						map.put("taskPlanStartDate", rs.getString("plan_start_date"));
						map.put("taskPlanEndDate", rs.getString("plan_end_date"));
						map.put("taskMonthStartDate", rs.getString("month_edit_plan_start_date"));
						map.put("taskMonthEndDate", rs.getString("month_edit_plan_end_date"));
						map.put("status", rs.getInt("status"));
						map.put("cityId", rs.getString("city_id"));

						list.add(map);
					}
					return list;
				}

			};

			List<HashMap> list = run.query(conn, selectSql, rsHandler);

			ResultSetHandler<List<HashMap>> rsHandlerNoOpen = new ResultSetHandler<List<HashMap>>() {
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while (rs.next()) {
						HashMap map = new HashMap<String, Integer>();
						map.put("blockId", rs.getInt("block_id"));
						map.put("blockName", rs.getString("block_name"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("status", rs.getInt("status"));
						map.put("cityId", rs.getInt("city_id"));
						list.add(map);
					}
					return list;
				}

			};
			List<HashMap> listNotOpen = run.query(conn, selectSqlNotOpen, rsHandlerNoOpen);

			list.addAll(listNotOpen);

			return list;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}*/

	/**
	 * @param conn
	 * @param blockList
	 * @throws Exception
	 */
	public static void openBlockByBlockIdList(Connection conn, List<Integer> blockList) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			if (!blockList.isEmpty()) {
				String BlockIds = "(";
				BlockIds += StringUtils.join(blockList.toArray(), ",") + ")";

				String updateSql = "update block" + " set plan_status = 1" + " where block_id in " + BlockIds;

				run.update(conn, updateSql);
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param blockList
	 * @throws Exception
	 */
	public static List queryOperationBlocks(Connection conn, JSONArray blockArray) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			List<Integer> updateBlockList = new ArrayList<Integer>();
			List<Integer> blockList = new ArrayList<Integer>();

			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				blockList.add(block.getInt("blockId"));
			}
			String BlockIds = "(";
			BlockIds += StringUtils.join(blockList.toArray(), ",") + ")";

			String selectSql = "select block_id from block_man where status!=0 and block_id in " + BlockIds;

			PreparedStatement stmt = conn.prepareStatement(selectSql);
			ResultSet rs = stmt.executeQuery();
			List<String> gridList = new ArrayList();
			while (rs.next()) {
				updateBlockList.add(rs.getInt(1));
			}

			return updateBlockList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param blockList
	 * @throws Exception
	 */
	public static void updateMainBlock(Connection conn, List<Integer> blockManIds) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			if (!blockManIds.isEmpty()) {
				String BlockIds = "(";
				BlockIds += StringUtils.join(blockManIds.toArray(), ",") + ")";

				String updateSql = "update block_man" + " set status = 1" + " where block_man_id in " + BlockIds +" and status!=0";

				run.update(conn, updateSql);
			}

			// 发布消息

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * 得到分页后的数据
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @param data
	 * @return
	 */
	public List getPagedList(int pageNum, int pageSize, List<HashMap> data) {
		int fromIndex = (pageNum - 1) * pageSize;
		if (fromIndex >= data.size()) {
			return Collections.emptyList();
		}

		int toIndex = pageNum * pageSize;
		if (toIndex >= data.size()) {
			toIndex = data.size();
		}
		return data.subList(fromIndex, toIndex);
	}

	/**
	 * block完成度：统计原则：block关联的所有子任务完成度，子任务A完成度*(1/子任务个数N)+子任务B完成度*(1/子任务个数N)+...+
	 * 子任务N完成度*(1/子任务个数N)
	 * 
	 * @param conn
	 * @param blockId
	 * @param blockStatus
	 * @return
	 * @throws SQLException
	 */
	public static String calculateBlockFinishPercent(Connection conn, int blockId, int blockStatus, int stage)
			throws SQLException {
		if (blockStatus != 1) {
			return "---";
		}

		String selectFinishPercentSql = "  SELECT distinct f.subtask_id,f.finish_percent FROM subtask s,subtask_finish f  WHERE s.subtask_id=f.subtask_id AND s.status=1 AND s.block_id="
				+ blockId;
		String selectSubTaskCount = " SELECT COUNT(1) total FROM subtask s WHERE s.status=1 and s.block_id=" + blockId;

		if (stage == 0 || stage == 1) {
			selectFinishPercentSql += " and s.stage=" + stage;
			selectSubTaskCount += " and s.stage=" + stage;
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(selectSubTaskCount);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rs = stmt.executeQuery();
		int subtaskCount = 0;
		while (rs.next()) {
//			subtaskCount = rs.getInt("record_");
			subtaskCount = rs.getInt("total");
		}
		if (subtaskCount == 0) {
			return "0%";
		}
		float finishPercent = 0;
		PreparedStatement stmt1 = conn.prepareStatement(selectFinishPercentSql);
		ResultSet rs1 = stmt1.executeQuery();
		while (rs1.next()) {
			finishPercent += rs1.getInt("finish_percent") / (subtaskCount * 100);
		}
		return String.valueOf(finishPercent * 100) + "%";
	}

	/**
	 * @param conn
	 * @param blockArray
	 * @return
	 * @throws Exception 
	 */
	public static Map<Integer, String> queryBlockNameByBlocks(Connection conn, JSONArray blockArray) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			List<Integer> updateBlockList = new ArrayList<Integer>();
			List<Integer> blockList = new ArrayList<Integer>();

			for (int i = 0; i < blockArray.size(); i++) {
				JSONObject block = blockArray.getJSONObject(i);
				blockList.add(block.getInt("blockId"));
			}
			String BlockIds = "(";
			BlockIds += StringUtils.join(blockList.toArray(), ",") + ")";

			String selectSql = "select b.block_id,b.block_name from block b where b.block_id in " + BlockIds;

			Map<Integer,String> blockNameMap = new HashMap<Integer,String>();
			PreparedStatement stmt = conn.prepareStatement(selectSql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				blockNameMap.put(rs.getInt("block_id"), rs.getString("block_name"));
			}

			return blockNameMap;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	public static Page getSnapshotQuery(Connection conn, String selectSql,final int currentPageNum,final int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					page.setPageSize(pageSize);
					int totalCount = 0;
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
						map.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("blockStatus", rs.getInt("BLOCK_STATUS"));
						map.put("blockPlanStatus", rs.getInt("BLOCK_PLAN_STATUS"));
						map.put("planStartDate", rs.getString("PLAN_START_DATE"));
						map.put("planEndDate", rs.getString("PLAN_END_DATE"));
						map.put("assignStatus", rs.getInt("ASSIGN_STATUS"));
						map.put("groupId", rs.getInt("GROUP_ID"));
						map.put("groupName", rs.getString("GROUP_NAME"));
						map.put("diffDate", rs.getInt("DIFF_DATE"));
						map.put("progress", rs.getInt("PROGRESS"));						
						map.put("type", rs.getInt("TASK_TYPE"));
						map.put("percent",rs.getInt("PERCENT"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(map);
					}
					page.setResult(list);
					page.setTotalCount(totalCount);
					return page;
				}

			};
			return  run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}
	public static Page getAllQuery(Connection conn, String selectSql,final int currentPageNum,final int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					page.setPageSize(pageSize);
					int totalCount = 0;
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("blockManId", rs.getInt("BLOCK_MAN_ID"));
						map.put("blockManName", rs.getString("BLOCK_MAN_NAME"));
						map.put("blockDescp", rs.getString("BLOCK_DESCP"));
						
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockStatus", rs.getInt("BLOCK_STATUS"));
						map.put("blockPlanStatus", rs.getInt("BLOCK_PLAN_STATUS"));
						map.put("diffDate", rs.getInt("DIFF_DATE"));
						map.put("percent",rs.getInt("PERCENT"));
						
						map.put("collectPlanStartDate", rs.getString("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getString("COLLECT_PLAN_END_DATE"));
						//map.put("collectAssignStatus", rs.getInt("COLLECT_ASSIGN_STATUS"));
						map.put("collectGroupId", rs.getInt("COLLECT_GROUP_ID"));
						map.put("collectGroupName", rs.getString("COLLECT_GROUP_NAME"));
						map.put("collectDiffDate", rs.getInt("COLLECT_DIFF_DATE"));
						map.put("collectProgress", rs.getInt("COLLECT_PROGRESS"));
						map.put("collectPercent",rs.getInt("COLLECT_PERCENT"));
						
						map.put("dayEditPlanStartDate", rs.getString("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getString("DAY_EDIT_PLAN_END_DATE"));
						//map.put("dailyAssignStatus", rs.getInt("DAILY_ASSIGN_STATUS"));
						map.put("dayEditGroupId", rs.getInt("DAY_EDIT_GROUP_ID"));
						map.put("dayEditGroupName", rs.getString("DAY_EDIT_GROUP_NAME"));
						map.put("dailyDiffDate", rs.getInt("DAILY_DIFF_DATE"));
						map.put("dailyProgress", rs.getInt("DAILY_PROGRESS"));
						map.put("dailyPercent",rs.getInt("DAILY_PERCENT"));
						map.put("type", rs.getInt("TASK_TYPE"));
						
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createUserName", rs.getString("CREATE_USER_NAME"));
						
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(map);
					}
					page.setResult(list);
					page.setTotalCount(totalCount);
					return page;
				}

			};
			return  run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}
}
