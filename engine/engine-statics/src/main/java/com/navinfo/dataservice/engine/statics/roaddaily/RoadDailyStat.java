package com.navinfo.dataservice.engine.statics.roaddaily;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class RoadDailyStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
//	private String col_name_seasion_grid = "tips_season_grid_stat";
//	private String col_name_tips_grid = "fm_stat_daily_tips_grid";
	private String col_name_tips_grid = "fm_stat_tips_grid";
	
	public RoadDailyStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date, String stat_time) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;
		this.stat_time = stat_time;
		this.log = LogManager.getLogger(String.valueOf(db_id));
		try {
			this.conn = DBConnector.getInstance().getConnectionById(db_id);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 根据 grid 表返回 hashmap： key（grid_id）=value（check_count）
	 */

	public Map<String, Integer> getGridCheckResult() throws ServiceException {

		try {
			QueryRunner run = new QueryRunner();
			String sql = "select grid_id, count (*) cnt from ni_val_exception_grid nvg, ck_result_object cro "
					+ "where nvg.md5_code = cro.md5_code and table_name not like 'IX%' group by grid_id";
			return run.query(conn, sql, new ResultSetHandler<Map<String, Integer>>() {

				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> map = new HashMap<String, Integer>();
					while (rs.next()) {
						map.put(String.valueOf(rs.getInt("grid_id")), rs.getInt("cnt"));
					}
					return map;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 构建 grid 级别数据
	 */
	public List<Document> build_grid() throws ServiceException {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Map<String,Integer>> mapTips = StatInit.getTipsTotalAndFinishOfDaily(db_name, col_name_tips_grid, "grid_id", stat_date);
//		Map<String, Integer> mapSeason  = StatInit.getTipsFinishOfSeason(db_name, col_name_seasion_grid, "grid_id");
//		Map<String, Integer> mapTips = StatInit.getTipsFinishOfDaily(db_name, col_name_tips_grid, "grid_id", stat_date);
		Map<String, Integer> mapCheck = getGridCheckResult();
		for (Entry<String, Map<String,Integer>> entry : mapTips.entrySet()) {
			Document json = new Document();
			String grid_id = entry.getKey();
			Map<String, Integer> map = entry.getValue();
			Integer total = map.get("total");
			Integer finish = map.get("finish");	
//			Integer total = entry.getValue();
//			Integer finish = (mapTips.get(grid_id) == null ? 0 : mapTips.get(grid_id));

			// ------------------------------
			json.put("grid_id", grid_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			int checkResult = (mapCheck.get(grid_id) == null ? 0 : mapCheck.get(grid_id));
			if (finish == 0 || total == 0) {
				road.put("percent", new Double(0));
				road.put("check_wrong_num", 0);
				road.put("all_percent", new Double(0));
			} else {
				Double percent = StatUtil.formatDouble(finish / total * 100);
				road.put("percent", percent);
				road.put("check_wrong_num", checkResult);
				if (checkResult > 0) {
					road.put("all_percent", StatUtil.formatDouble(percent * 0.9));
				} else {
					road.put("all_percent", StatUtil.formatDouble(percent * 0.9 + 10));
				}

			}

			// ------------------------------
			json.put("road", road);
			json_list.add(json);
		}
		return json_list;
	}
	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task" + conn);

			new MongoDao(db_name).insertMany(col_name, build_grid());
		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();

	}
	
	

}
