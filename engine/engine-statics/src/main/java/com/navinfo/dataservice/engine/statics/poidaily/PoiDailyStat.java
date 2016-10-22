package com.navinfo.dataservice.engine.statics.poidaily;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

public class PoiDailyStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	// 季度库
	private String col_name_seasion_grid = "poi_season_grid_stat";

	public PoiDailyStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date, String stat_time) {
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
	
	public Map<String, Integer> getUploadPois() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();

			String sql = "select ip.geometry from ix_poi ip, poi_edit_status pes where ip.row_id = pes.row_id and pes.is_upload=1";
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
		} 
//		finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}

	public Map<String, JSONObject> getPois() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();

			String sql = "select ip.pid,ip.geometry,ip.row_id from ix_poi ip, poi_edit_status pes where ip.row_id = pes.row_id and pes.status=3";
			return run.query(conn, sql, new ResultSetHandler<Map<String, JSONObject>>() {

				@Override
				public Map<String, JSONObject> handle(ResultSet rs) throws SQLException {

					Map<String, JSONObject> map = new HashMap<String, JSONObject>();
					while (rs.next()) {
						try {

							STRUCT struct = (STRUCT) rs.getObject("geometry");
							Geometry geo = GeoTranslator.struct2Jts(struct);
							String grid_id = CompGridUtil.point2Grids(geo.getCoordinate().x, geo.getCoordinate().y)[0];

							int pid = rs.getInt("pid");
							String row_id = rs.getString("row_id");
							JSONObject tmp = new JSONObject();
							tmp.put("pid", pid);
							tmp.put("row_id", row_id);
							if (map.containsKey(grid_id)) {
								JSONObject obj = map.get(grid_id);
								obj.put("finish", obj.getInt("finish") + 1);
								obj.put("finish_pids", obj.getJSONArray("finish_pids").element(tmp));
								map.put(grid_id, obj);

							} else {
								JSONObject obj = new JSONObject();
								obj.put("finish", 1);
								obj.put("finish_pids", new JSONArray().element(tmp));

								map.put(grid_id, obj);
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
		} 
//		finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}

//	public List<Document> doStatPoi(Map<String, JSONObject> map) throws ServiceException {
//		List<Document> backList = new ArrayList<Document>();
//
////		Map<String, Integer> mapSeason = StatInit.getPoiSeasonStat(db_name, col_name_seasion_grid, "grid_id");
//		Map<String, Integer> uploadPois = getUploadPois();
//
//		for (Entry<String, Integer> entry : uploadPois.entrySet()) {
//
//			String grid_id = entry.getKey();
//			int total = entry.getValue();
//			JSONObject obj = map.get(grid_id);
//			JSONArray finish_pids = new JSONArray();
//			int finish = 0;
//			if (obj != null) {
//				finish_pids = obj.getJSONArray("finish_pids");
//				finish = (Integer) obj.get("finish");
//			}
//
//			Document json = new Document();
//			// ------------------------------
//			json.put("grid_id", grid_id);
//			json.put("stat_date", stat_date);
//			json.put("stat_time", stat_time);
//			// ------------------------------
//			Document road = new Document();
//			road.put("total", total);
//			road.put("finish", finish);
//			road.put("finish_pids", finish_pids);
//			if (finish == 0 || total == 0) {
//				road.put("percent", 0);
//			} else {
//				road.put("percent", StatUtil.formatDouble((double) finish / total * 100));
//			}
//
//			// ------------------------------
//			json.put("poi", road);
//			backList.add(json);
//		}
//		return backList;
//	}

	public List<Document> doStatPoi() throws ServiceException {
		try{
			List<Document> backList = new ArrayList<Document>();
	
			Map<String, JSONObject> map = getPois();
			Map<String, Integer> uploadPois = getUploadPois();
	
			for (Entry<String, Integer> entry : uploadPois.entrySet()) {
	
				String grid_id = entry.getKey();
				int total = entry.getValue();
				JSONObject obj = map.get(grid_id);
				JSONArray finish_pids = new JSONArray();
				int finish = 0;
				if (obj != null) {
					finish_pids = obj.getJSONArray("finish_pids");
					finish = (Integer) obj.get("finish");
				}
	
				Document json = new Document();
				// ------------------------------
				json.put("grid_id", grid_id);
				json.put("stat_date", stat_date);
				json.put("stat_time", stat_time);
				// ------------------------------
				Document road = new Document();
				road.put("total", total);
				road.put("finish", finish);
				road.put("finish_pids", finish_pids);
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
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task" + conn);
//			Map<String, JSONObject> ja = getPois();
//			new MongoDao(db_name).insertMany(col_name, doStatPoi(ja));
			new MongoDao(db_name).insertMany(col_name, doStatPoi());
		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();

	}

}
