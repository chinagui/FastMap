package com.navinfo.dataservice.engine.statics.roadcollect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

public class RoadCollectMain {

	private static Logger log = null;
	public static final String col_name_grid = "fm_stat_collect_road_grid";
	public static final String col_name_block = "fm_stat_collect_road_block";
	public static final String col_name_city = "fm_stat_collect_road_city";

	private String db_name;
	private String stat_date;
	private String stat_time;
	// tips 库
	private String col_name_tips_grid = "track_length_grid_stat";
	private String col_name_tips_block = "track_length_block_stat";
	private String col_name_tips_city = "track_length_city_stat";
	// 季度库
	public static String col_name_seasion_grid = "road_season_grid_stat";
	public static String col_name_seasion_block = "road_season_block_stat";
	public static String col_name_seasion_city = "road_season_city_stat";

	public RoadCollectMain(String dbn, String stat_time) {
		this.db_name = dbn;
		this.stat_date = stat_time.substring(0, 8);
		this.stat_time = stat_time;
	}

	/**
	 * 统计结果mongo结果库初始化
	 */
	public void initMongoDb(String db_name) {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();
		// grid
		Iterator<String> iter_grid = md.listCollectionNames().iterator();
		boolean flag_grid = true;
		while (iter_grid.hasNext()) {
			if (iter_grid.next().equalsIgnoreCase(col_name_grid)) {
				flag_grid = false;
				break;
			}
		}

		if (flag_grid) {
			md.createCollection(col_name_grid);
			md.getCollection(col_name_grid).createIndex(new BasicDBObject("grid_id", 1));
			md.getCollection(col_name_grid).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection " + col_name_grid + " ok");
			log.info("-- -- create mongo index on " + col_name_grid + "(grid_id，stat_date) ok");
		}

		// block
		Iterator<String> iter_block = md.listCollectionNames().iterator();
		boolean flag_block = true;
		while (iter_block.hasNext()) {
			if (iter_block.next().equalsIgnoreCase(col_name_block)) {
				flag_block = false;
				break;
			}
		}

		if (flag_block) {
			md.createCollection(col_name_block);
			md.getCollection(col_name_block).createIndex(new BasicDBObject("block_id", 1));
			md.getCollection(col_name_block).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection " + col_name_block + " ok");
			log.info("-- -- create mongo index on " + col_name_block + "(block_id，stat_date) ok");
		}

		// city
		Iterator<String> iter_city = md.listCollectionNames().iterator();
		boolean flag_city = true;
		while (iter_city.hasNext()) {
			if (iter_city.next().equalsIgnoreCase(col_name_city)) {
				flag_city = false;
				break;
			}
		}

		if (flag_city) {
			md.createCollection(col_name_city);
			md.getCollection(col_name_city).createIndex(new BasicDBObject("city_id", 1));
			md.getCollection(col_name_city).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection " + col_name_city + " ok");
			log.info("-- -- create mongo index on " + col_name_city + "(city_id，stat_date) ok");
		}
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name_grid, query);
		mdao.deleteMany(col_name_block, query);
		mdao.deleteMany(col_name_city, query);

	}

	/**
	 * 构建 grid 数据
	 * 
	 * @return List<Document>
	 */
	public List<Document> build_grid() {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Double> mapTips = StatInit.getTrackTipsStat(db_name, col_name_tips_grid, "grid_id", stat_date);
		Map<String, Double> mapSeason = StatInit.getTrackSeasonStat(db_name, col_name_seasion_grid, "grid_id");

		for (Entry<String, Double> entry : mapSeason.entrySet()) {
			Document json = new Document();
			String grid_id = entry.getKey();
			double total = entry.getValue();
			double finish = (mapTips.get(grid_id) == null ? 0 : mapTips.get(grid_id));

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
				road.put("percent", StatUtil.formatDouble(finish / total * 100));
			}

			// ------------------------------
			json.put("road", road);
			json_list.add(json);
		}
		return json_list;
	}

	/**
	 * 构建 block 数据
	 * 
	 * @return List<Document>
	 */
	public List<Document> build_block() {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Double> mapTips = StatInit.getTrackTipsStat(db_name, col_name_tips_block, "block_id", stat_date);
		Map<String, Double> mapSeason = StatInit.getTrackSeasonStat(db_name, col_name_seasion_block, "block_id");

		for (Entry<String, Double> entry : mapSeason.entrySet()) {
			Document json = new Document();
			String block_id = entry.getKey();
			double total = entry.getValue();
			double finish = (mapTips.get(block_id) == null ? 0 : mapTips.get(block_id));

			// ------------------------------
			json.put("block_id", block_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			if (finish == 0 || total == 0) {
				road.put("percent", 0);
			} else {
				road.put("percent", StatUtil.formatDouble(finish / total * 100));
			}

			// ------------------------------
			json.put("road", road);
			json_list.add(json);
		}
		return json_list;
	}

	/**
	 * 构建 city 数据
	 * 
	 * @return List<Document>
	 */
	public List<Document> build_city() {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Double> mapTips = StatInit.getTrackTipsStat(db_name, col_name_tips_city, "city_id", stat_date);
		Map<String, Double> mapSeason = StatInit.getTrackSeasonStat(db_name, col_name_seasion_city, "city_id");

		for (Entry<String, Double> entry : mapSeason.entrySet()) {
			Document json = new Document();
			String city_id = entry.getKey();
			double total = entry.getValue();
			double finish = (mapTips.get(city_id) == null ? 0 : mapTips.get(city_id));

			// ------------------------------
			json.put("city_id", city_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			if (finish == 0 || total == 0) {
				road.put("percent", 0);
			} else {
				road.put("percent", StatUtil.formatDouble(finish / total * 100));
			}

			// ------------------------------
			json.put("road", road);
			json_list.add(json);
		}
		return json_list;
	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat() {
		log = LogManager.getLogger(RoadCollectMain.class);

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 统计数据如mongo
			MongoDao md = new MongoDao(db_name);
			log.info("-- begin stat:" + col_name_grid);
			md.insertMany(col_name_grid, build_grid());
//			log.info("-- begin stat:" + col_name_block);
//			md.insertMany(col_name_block, build_block());
//			log.info("-- begin stat:" + col_name_city);
//			md.insertMany(col_name_city, build_city());

			log.info("-- end stat");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
