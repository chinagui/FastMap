package com.navinfo.dataservice.engine.statics.poicollect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Geometry;

public class PoiCollectStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	// 季度库
	private String col_name_seasion_grid = "poi_season_grid_stat";

	public PoiCollectStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date, String stat_time) {
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

	public Map<String, Integer> getPois() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();

			String sql = "select ip.geometry from ix_poi ip, poi_edit_status pes where ip.pid = pes.pid and pes.is_upload=1";
			return run.query(conn, sql, new ResultSetHandler<Map<String, Integer>>() {

				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {

					Map<String, Integer> map = new HashMap<String, Integer>();
					while (rs.next()) {
						try {

							STRUCT struct = (STRUCT) rs.getObject("geometry");
							Geometry geo = GeoTranslator.struct2Jts(struct);
							String grid_id = CompGridUtil.point2Grids(geo.getCoordinate().x, geo.getCoordinate().y)[0];
							if (map.containsKey(grid_id)) {
								map.put(grid_id, map.get(grid_id) + 1);
							} else {
								map.put(grid_id, 1);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					return map;
				}
			}

			);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	public List<Document> doStatPoi(Map<String, Integer> map) {
		List<Document> backList = new ArrayList<Document>();

		Map<String, Integer> mapSeason = StatInit.getPoiSeasonStat(db_name, col_name_seasion_grid, "grid_id");

		for (Entry<String, Integer> entry : mapSeason.entrySet()) {

			String grid_id = entry.getKey();
			int total = entry.getValue();
			int finish = (map.get(grid_id) == null ? 0 : map.get(grid_id));

			Document json = new Document();
			// ------------------------------
			json.put("grid_id", grid_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			if (finish == 0 || total == 0) {
				road.put("percent", 0);
			} else {
				road.put("percent", StatUtil.formatDouble((double) finish / total * 100));
			}

			// ------------------------------
			json.put("poi", road);
			backList.add(json);
		}
		return backList;
	}

	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task" + conn);
			Map<String, Integer> ja = getPois();
			new MongoDao(db_name).insertMany(col_name, doStatPoi(ja));

		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();

	}

}
