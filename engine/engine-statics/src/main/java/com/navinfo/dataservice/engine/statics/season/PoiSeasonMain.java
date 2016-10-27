package com.navinfo.dataservice.engine.statics.season;

import java.io.File;
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
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;

public class PoiSeasonMain {

	private static Logger log = null;
	private static CountDownLatch countDownLatch = null;
	public static final String col_name_grid = "poi_season_grid_stat";
	public static final String col_name_block = "poi_season_block_stat";
	public static final String col_name_city = "poi_season_city_stat";
	private String db_name;
	private String stat_date;

	public PoiSeasonMain(String dbn, String stat_date) {
		this.db_name = dbn;
		this.stat_date = stat_date;
	}

	/**
	 * 统计结果mongo结果库初始化
	 */
	private void initMongoDb(String db_name) {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();

		md.getCollection(col_name_grid).drop();
		md.createCollection(col_name_grid);
		BasicDBObject index1 = new BasicDBObject();
		index1.put("grid_id", 1);
		index1.put("unique", true);
		md.getCollection(col_name_grid).createIndex(index1);
		log.info("-- -- finish init collection:" + col_name_grid);
		// ----------------------------
		md.getCollection(col_name_block).drop();
		md.createCollection(col_name_block);
		BasicDBObject index2 = new BasicDBObject();
		index2.put("block_id", 1);
		index2.put("unique", true);
		md.getCollection(col_name_block).createIndex(index2);
		log.info("-- -- finish init collection:" + col_name_block);
		// ----------------------------
		md.getCollection(col_name_city).drop();
		md.createCollection(col_name_city);
		BasicDBObject index3 = new BasicDBObject();
		index3.put("city_id", 1);
		index3.put("unique", true);
		md.getCollection(col_name_city).createIndex(index3);
		log.info("-- -- finish init collection:" + col_name_city);
	}

	/**
	 * 根据季度 grid 统计结果生成 block维度统计，并插入到 mongo
	 */
	private void buildBlockStat() {
		log.info("-- -- building block data from grid：" + col_name_block);
		try {
			Map<String, String> gridBlockMap = OracleDao.getGrid2Block();

			Map<String, Integer> resultMap = new HashMap<String, Integer>();
			MongoDao md = new MongoDao(db_name);
			MongoCursor<Document> iter1 = md.find(col_name_grid, null).iterator();
			while (iter1.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter1.next());
				String grid_id = json.getString("grid_id");
				String block_id = gridBlockMap.get(grid_id);
				Integer total = json.getInt("total");
				if (resultMap.containsKey(block_id)) {
					resultMap.put(block_id, resultMap.get(block_id) + total);
				} else {
					resultMap.put(block_id, total);
				}
			}

			List<Document> backList = new ArrayList<Document>();

			for (Iterator<String> iter2 = resultMap.keySet().iterator(); iter2.hasNext();) {
				String key = iter2.next();
				Integer total = resultMap.get(key);
				Document doc = new Document();
				doc.put("block_id", key);
				doc.put("total", total);
				doc.put("stat_date", stat_date);
				backList.add(doc);
			}
			md.insertMany(col_name_block, backList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据季度grid统计结果生成city维度统计，并插入到mongo
	 */
	private void buildCityStat() {
		log.info("-- -- building city data from grid：" + col_name_city);
		try {
			Map<String, String> gridCityMap = OracleDao.getGrid2City();

			Map<String, Integer> resultMap = new HashMap<String, Integer>();
			MongoDao md = new MongoDao(db_name);
			MongoCursor<Document> iter1 = md.find(col_name_grid, null).iterator();
			while (iter1.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter1.next());
				String grid_id = json.getString("grid_id");
				String city_id = gridCityMap.get(grid_id);
				Integer total = json.getInt("total");
				if (resultMap.containsKey(city_id)) {
					resultMap.put(city_id, resultMap.get(city_id) + total);
				} else {
					resultMap.put(city_id, total);
				}
			}

			List<Document> backList = new ArrayList<Document>();

			for (Iterator<String> iter2 = resultMap.keySet().iterator(); iter2.hasNext();) {
				String key = iter2.next();
				Integer total = resultMap.get(key);
				Document doc = new Document();
				doc.put("city_id", key);
				doc.put("total", total);
				doc.put("stat_date", stat_date);
				backList.add(doc);
			}

			md.insertMany(col_name_city, backList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runStat() {

		String xx = System.getProperty("user.dir") + File.separator + "config" + File.separator + "log4j.properties";
		PropertyConfigurator.configure(xx);

		log = LogManager.getLogger("stat");

		log.info("-- begin stat:" + col_name_grid);

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 获得 大区库的db_id
			List<Integer> ListDbId = OracleDao.getDbIdMonth();

			int dbid_cnt = ListDbId.size();
			Iterator<Integer> iter = ListDbId.iterator();

			ExecutorService executorService = Executors.newCachedThreadPool();
			// 计数器，线程数
			countDownLatch = new CountDownLatch(dbid_cnt);

			while (iter.hasNext()) {
				int db_id = iter.next();

				log.info("-- -- 创建统计进程 db_id：" + db_id);
				executorService.submit(new PoiSeasonStat(countDownLatch, db_id, db_name, col_name_grid, stat_date));
			}

			countDownLatch.await();
			executorService.shutdown();
			log.info("-- -- finish all Thread stat of grid");
			// 所有大区库统计完成后，进行派生block数据
			buildBlockStat();
			// 所有大区库统计完成后，进行派生city数据
			buildCityStat();
			log.info("-- end stat --");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
