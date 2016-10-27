package com.navinfo.dataservice.engine.statics.poicollect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

public class PoiCollectMain {

	private static Logger log = null;
	private static CountDownLatch countDownLatch = null;
	public static final String col_name_grid = "fm_stat_collect_poi_grid";
	public static final String col_name_block = "fm_stat_collect_poi_block";
	public static final String col_name_city = "fm_stat_collect_poi_city";
	private String db_name;
	private String stat_date;
	private String stat_time;

	public PoiCollectMain(String dbn, String stat_time) {
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
		// 初始化 col_name_grid
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

		// 初始化 col_name_block
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
			log.info("-- -- create mongo collection " + col_name_grid + " ok");
			log.info("-- -- create mongo index on " + col_name_grid + "(block_id，stat_date) ok");
		}

		// 初始化 col_name_block
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
			log.info("-- -- create mongo collection " + col_name_grid + " ok");
			log.info("-- -- create mongo index on " + col_name_grid + "(city_id，stat_date) ok");
		}

		// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name_grid, query);
		mdao.deleteMany(col_name_block, query);
		mdao.deleteMany(col_name_city, query);

	}

	/**
	 * 根据当天 grid 统计结果生成 block维度统计，并插入到 mongo
	 */
	private void buildBlockStat() {
		log.info("-- -- building block data from grid：" + col_name_block);
		try {
			Map<String, String> gridBlockMap = OracleDao.getGrid2Block();

			Map<String, Integer[]> resultMap = new HashMap<String, Integer[]>();
			MongoDao md = new MongoDao(db_name);
			MongoCursor<Document> iter1 = md.find(col_name_grid, null).iterator();
			while (iter1.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter1.next());
				String grid_id = json.getString("grid_id");

				Integer total = json.getJSONObject("poi").getInt("total");
				Integer finish = json.getJSONObject("poi").getInt("finish");
				String block_id = gridBlockMap.get(grid_id);
				if (block_id == null) {
					continue;
				}
				if (resultMap.containsKey(block_id)) {
					Integer[] intArray = { resultMap.get(block_id)[0] + total, resultMap.get(block_id)[1] + finish };
					resultMap.put(block_id, intArray);
				} else {
					Integer[] intArray = { total, finish };
					resultMap.put(block_id, intArray);
				}
			}
			List<Document> backList = new ArrayList<Document>();

			for (Iterator<String> iter2 = resultMap.keySet().iterator(); iter2.hasNext();) {
				String key = iter2.next();
				Integer[] all = resultMap.get(key);
				Document doc = new Document();
				int total = all[0];
				int finish = all[1];

				doc.put("block_id", key);

				doc.put("stat_date", stat_date);
				doc.put("stat_time", stat_time);

				Document poi = new Document();
				poi.put("total", total);
				poi.put("finish", finish);
				if (finish == 0 || total == 0) {
					poi.put("percent", 0);
				} else {
					poi.put("percent", StatUtil.formatDouble((double) finish / total * 100));
				}
				doc.put("poi", poi);

				backList.add(doc);
			}

			md.insertMany(col_name_block, backList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据当天 grid 统计结果生成city维度统计，并插入到 mongo
	 */
	private void buildCityStat() {
		log.info("-- -- building city data from grid：" + col_name_city);
		try {
			Map<String, String> gridBlockMap = OracleDao.getGrid2City();

			Map<String, Integer[]> resultMap = new HashMap<String, Integer[]>();
			MongoDao md = new MongoDao(db_name);
			MongoCursor<Document> iter1 = md.find(col_name_grid, null).iterator();
			while (iter1.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter1.next());
				String grid_id = json.getString("grid_id");

				Integer total = json.getJSONObject("poi").getInt("total");
				Integer finish = json.getJSONObject("poi").getInt("finish");
				String city_id = gridBlockMap.get(grid_id);
				if (city_id == null) {
					continue;
				}
				if (resultMap.containsKey(city_id)) {
					Integer[] intArray = { resultMap.get(city_id)[0] + total, resultMap.get(city_id)[1] + finish };
					resultMap.put(city_id, intArray);
				} else {
					Integer[] intArray = { total, finish };
					resultMap.put(city_id, intArray);
				}
			}
			List<Document> backList = new ArrayList<Document>();

			for (Iterator<String> iter2 = resultMap.keySet().iterator(); iter2.hasNext();) {
				String key = iter2.next();
				Integer[] all = resultMap.get(key);
				Document doc = new Document();
				int total = all[0];
				int finish = all[1];

				doc.put("city_id", key);

				doc.put("stat_date", stat_date);
				doc.put("stat_time", stat_time);

				Document poi = new Document();
				poi.put("total", total);
				poi.put("finish", finish);
				if (finish == 0 || total == 0) {
					poi.put("percent", 0);
				} else {
					poi.put("percent", StatUtil.formatDouble((double) finish / total * 100));
				}
				doc.put("poi", poi);

				backList.add(doc);
			}

			md.insertMany(col_name_city, backList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat() {
		log = LogManager.getLogger(PoiCollectMain.class);

		log.info("-- begin stat:" + col_name_grid);

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
				executorService.submit(new PoiCollectStat(countDownLatch, db_id, db_name, col_name_grid, stat_date, stat_time));
			}

			countDownLatch.await();
			executorService.shutdown();
			log.info("all sub task finish");
//			// 所有大区进程统计完成后，开始汇总 block维度数据。
//			buildBlockStat();
//			// 所有大区进程统计完成后，开始汇总city维度数据。
//			buildCityStat();
			log.info("-- end stat:" + col_name_grid);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
