package com.navinfo.dataservice.engine.statics.roadcollect;

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
import com.navinfo.dataservice.engine.statics.tools.StatInit;

public class RoadCollectMain {

	private static Logger log = null;
	private static CountDownLatch countDownLatch = null;
	private static final String col_name = "fm_stat_collect_poi";
	private String db_name;
	private String stat_date;

	public RoadCollectMain(String dbn, String stat_date) {
		this.db_name = dbn;
		this.stat_date = stat_date;
	}

	/**
	 * 统计结果mongo结果库初始化
	 */
	public void initMongoDb(String db_name) {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();
		Iterator<String> iter = md.listCollectionNames().iterator();
		boolean flag = true;
		while (iter.hasNext()) {
			if (iter.next().equalsIgnoreCase(col_name)) {
				flag = false;
				break;
			}
		}

		if (flag) {
			md.createCollection(col_name);
			md.getCollection(col_name).createIndex(new BasicDBObject("grid_id", 1));
			md.getCollection(col_name).createIndex(new BasicDBObject("stat_date", 1));
			log.info("-- -- create mongo collection "+col_name+" ok");
			log.info("-- -- create mongo index on "+col_name+"(grid_id，stat_date) ok");
		}

		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name, query);

	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat() {
		log = LogManager.getLogger(RoadCollectMain.class);

		log.info("-- poi 日库统计开始");

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 初始化 datahub环境
			StatInit.initDatahubDb();
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
				executorService.submit(new RoadCollectStat(countDownLatch, db_id, db_name, col_name, stat_date));
			}

			countDownLatch.await();
			executorService.shutdown();

			log.info("-- poi 日库统计结束");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
