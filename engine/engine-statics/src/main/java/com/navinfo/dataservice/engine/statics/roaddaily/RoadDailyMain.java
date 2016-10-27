package com.navinfo.dataservice.engine.statics.roaddaily;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.exception.ServiceException;

public class RoadDailyMain {

	private static Logger log = null;
	private static CountDownLatch countDownLatch = null;
	public static final String col_name_grid = "fm_stat_daily_road_grid";
	public static final String col_name_block = "fm_stat_daily_road_block";
	public static final String col_name_city = "fm_stat_daily_road_city";

	private String db_name;
	private String stat_date;
	private String stat_time;
	// tips 库

	private String col_name_tips_block = "fm_stat_daily_tips_block";
	private String col_name_tips_city = "fm_stat_daily_tips_city";
	// 季度库

	public static final String col_name_seasion_block = "tips_season_block_stat";
	public static final String col_name_seasion_city = "tips_season_city_stat";

	public RoadDailyMain(String dbn, String stat_time) {
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

		// -------------------
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name_grid, query);
		mdao.deleteMany(col_name_block, query);
		mdao.deleteMany(col_name_city, query);

	}

	/**
	 * 构建 block 级别数据
	 */
	public List<Document> build_block() throws ServiceException {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Integer> mapSeason = StatInit.getTipsFinishOfSeason(db_name, col_name_seasion_block, "block_id");
		Map<String, Integer> mapTips = StatInit.getTipsFinishOfDaily(db_name, col_name_tips_block, "block_id", stat_date);
		Map<String, Integer> mapCheck = StatInit.getCheckFromDaily(db_name, col_name_grid, "block", stat_date);
		for (Entry<String, Integer> entry : mapSeason.entrySet()) {
			Document json = new Document();
			String block_id = entry.getKey();
			Integer total = entry.getValue();
			Integer finish = (mapTips.get(block_id) == null ? 0 : mapTips.get(block_id));

			// ------------------------------
			json.put("block_id", block_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			int checkResult = (mapCheck.get(block_id) == null ? 0 : mapCheck.get(block_id));
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

	/**
	 * 构建 city 级别数据
	 */
	public List<Document> build_city() throws ServiceException {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Integer> mapSeason = StatInit.getTipsFinishOfSeason(db_name, col_name_seasion_city, "city_id");
		Map<String, Integer> mapTips = StatInit.getTipsFinishOfDaily(db_name, col_name_tips_city, "city_id", stat_date);
		Map<String, Integer> mapCheck = StatInit.getCheckFromDaily(db_name, col_name_grid, "city", stat_date);
		for (Entry<String, Integer> entry : mapSeason.entrySet()) {
			Document json = new Document();
			String city_id = entry.getKey();
			Integer total = entry.getValue();
			Integer finish = (mapTips.get(city_id) == null ? 0 : mapTips.get(city_id));

			// ------------------------------
			json.put("city_id", city_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			int checkResult = (mapCheck.get(city_id) == null ? 0 : mapCheck.get(city_id));
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

	public void runStat() {

		String xx = System.getProperty("user.dir") + File.separator + "config" + File.separator + "log4j.properties";
		PropertyConfigurator.configure(xx);

		log = LogManager.getLogger("stat");

		log.info("-- begin stat --");

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 获得 大区库的db_id
			List<Integer> ListDbId = OracleDao.getDbIdDaily();

			int dbid_cnt = ListDbId.size();
			Iterator<Integer> iter = ListDbId.iterator();

			ExecutorService executorService = Executors.newCachedThreadPool();
			// 计数器，线程数
			countDownLatch = new CountDownLatch(dbid_cnt);

			while (iter.hasNext()) {
				int db_id = iter.next();

				log.info("-- -- 创建统计进程 db_id：" + db_id);
				executorService.submit(new RoadDailyStat(countDownLatch, db_id, db_name, col_name_grid, stat_date, stat_time));
			}

			countDownLatch.await();
			executorService.shutdown();
			log.info("-- -- finish all Thread stat of grid");
			// 根据 grid 结果 汇总 block 和 city
			MongoDao md = new MongoDao(db_name);
//			log.info("-- begin stat:" + col_name_block);
//			md.insertMany(col_name_block, build_block());
//			log.info("-- begin stat:" + col_name_city);
//			md.insertMany(col_name_city, build_city());

			log.info("-- end stat --");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat2() {
		log = LogManager.getLogger(RoadDailyMain.class);

		log.info("-- begin stat:" + col_name_grid);

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 统计数据如mongo

			log.info("-- end stat --");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
