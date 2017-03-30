package com.navinfo.dataservice.engine.statics.poimonthly;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;

public class PoiMonthlyMain {
	public static final String col_name_grid = "fm_stat_monthly_poi_grid";
	public static final String col_name_block = "fm_stat_monthly_poi_block";
	public static final String col_name_city = "fm_stat_monthly_poi_city";
	private static Logger log = null;
	private static CountDownLatch countDownLatch = null;
	private String db_name;
	private String stat_date;
	private String stat_time;

	public PoiMonthlyMain(String dbn, String stat_time) {
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

		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name_grid, query);

	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat() {
		log = LogManager.getLogger(PoiMonthlyMain.class);

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
				executorService.submit(new PoiMonthlyStat(countDownLatch, db_id, db_name, col_name_grid, stat_date, stat_time));
			}

			countDownLatch.await();
			executorService.shutdown();
			//log.info("all sub task finish");
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
