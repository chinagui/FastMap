package com.navinfo.dataservice.engine.man.block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import oracle.sql.CLOB;

public class BlockOperation {
	private static Logger log = LoggerRepos.getLogger(BlockOperation.class);

	public BlockOperation() {
		// TODO Auto-generated constructor stub
	}

	public static List<HashMap> queryBlockBySql(Connection conn, String selectSql, String wkt) throws Exception {
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

						CLOB clob = (CLOB) rs.getObject("geometry");
						String clobStr = DataBaseUtils.clob2String(clob);
						try {
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						list.add(map);
					}
					return list;
				}

			};
			return run.query(conn, selectSql, rsHandler, wkt);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:" + e.getMessage(), e);
		}
	}

	public static List<HashMap> queryProduceBlock(Connection conn, String selectSql, List<Object> values)
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
							if (BlockOperation.checkGridFinished(rs.getInt("BLOCK_ID"))) {
								map.put("blockId", rs.getInt("BLOCK_ID"));
								map.put("blockName", rs.getInt("BLOCK_NAME"));
								CLOB clob = (CLOB) rs.getObject("geometry");
								String clobStr = DataBaseUtils.clob2String(clob);
								try {
									map.put("geometry", Geojson.wkt2Geojson(clobStr));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
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
			if (null == values || values.size() == 0) {
				return run.query(conn, selectSql, rsHandler);
			}
			return run.query(conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:" + e.getMessage(), e);
		}
	}

	public static List<HashMap> queryBlockByGroup(Connection conn, String selectSql, List<Object> values)
			throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>() {
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while (rs.next()) {
						HashMap map = new HashMap<String, Integer>();
						System.out.println(rs.getInt("BLOCK_ID"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("planStartDate", rs.getDate("planStartDate"));
						map.put("planEndDate", rs.getDate("planEndDate"));
						map.put("descp", rs.getString("DESCP"));

						list.add(map);
					}
					return list;
				}

			};
			if (null == values || values.size() == 0) {
				return run.query(conn, selectSql, rsHandler);
			}
			return run.query(conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:" + e.getMessage(), e);
		}
	}

	public static boolean checkGridFinished(int blockId) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String sqlByblockId = "select grid_id from grid where block_id=" + blockId;

			PreparedStatement stmt = conn.prepareStatement(sqlByblockId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int grid_id = rs.getInt(1);
				// 调用统计模块，查询grid完成度,若不为100%，返回false
				// TODO
				return false;
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
	public static List<Integer> getBlockListReadyToClose(Connection conn, List<Integer> blockIdList) throws Exception {
		// TODO Auto-generated method stub

		try {
			QueryRunner run = new QueryRunner();

			String BlockIds = "(";
			BlockIds += StringUtils.join(blockIdList.toArray(), ",") + ")";

			String selectSql = "select distinct b.block_id " + "from subtask st" + ", block b "
					+ " where st.block_id = b.block_id" + " and st.status = 0" + " and b.plan_status = 1"
					+ " and b.block_id in " + BlockIds;

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						list.add(rs.getInt("block_id"));
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
	public static void closeBlockByBlockIdList(Connection conn, List<Integer> blockList) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			if (!blockList.isEmpty()) {
				String BlockIds = "(";
				BlockIds += StringUtils.join(blockList.toArray(), ",") + ")";

				String updateSql = "update block" + " set plan_status = 2" + " where block_id in " + BlockIds;

				run.update(conn, updateSql);
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:" + e.getMessage(), e);
		}
	}

	/*
	 * 查询block list
	 */
	public static Page selectBlockList(Connection conn, String selectSql, List<Object> values, final int currentPageNum,
			final int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					page.setPageSize(pageSize);
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("descp", rs.getString("DESCP"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("createUserName", rs.getString("USER_REAL_NAME"));
						map.put("collectPlanStartDate", rs.getString("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getString("COLLECT_PLAN_END_DATE"));
						map.put("collectGroup", rs.getString("COLLECT_GROUP"));
						map.put("dayEditPlanStartDate", rs.getString("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getString("DAY_EDIT_PLAN_END_DATE"));
						map.put("dayEditGroup", rs.getString("DAY_EDIT_GROUP"));
						map.put("dayProducePlanStartDate", rs.getString("DAY_PRODUCE_PLAN_START_DATE"));
						map.put("dayProducePlanEndDate", rs.getString("DAY_PRODUCE_PLAN_END_DATE"));
						map.put("monthEditPlanStartDate", rs.getString("MONTH_EDIT_PLAN_START_DATE"));
						map.put("monthEditPlanEndDate", rs.getString("MONTH_EDIT_PLAN_END_DATE"));
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

						list.add(map);
					}
					page.setResult(list);
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

}
