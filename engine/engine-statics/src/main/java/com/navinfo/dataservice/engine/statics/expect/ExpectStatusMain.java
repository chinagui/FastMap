package com.navinfo.dataservice.engine.statics.expect;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;

public class ExpectStatusMain {
	private Logger log = LogManager.getLogger(ExpectStatusMain.class);

	private static CountDownLatch countDownLatch = null;

	public static final String col_name_block = "fm_stat_expect_status_block";

	private String db_name;
	private String stat_date;
	private String stat_time;

	public ExpectStatusMain(String dbn, String stat_time) {
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
			if (iter_grid.next().equalsIgnoreCase(col_name_block)) {
				flag_grid = false;
				break;
			}
		}

		if (flag_grid) {
			md.createCollection(col_name_block);
			md.getCollection(col_name_block).createIndex(
					new BasicDBObject("block_id", 1));
			log.info("-- -- create mongo collection " + col_name_block + " ok");
			log.info("-- -- create mongo index on " + col_name_block
					+ "(block_id) ok");
		}

		// 清空统计数据
		mdao.deleteMany(col_name_block, new Document());
	}

	/**
	 * 多线程执行所有大区库的统计
	 */
	public void runStat() {

		log.info("-- begin stat:" + col_name_block);

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			
			List<BlockMan> list = OracleDao.getBlockManList();

			ExecutorService executorService = Executors.newCachedThreadPool();
			// 计数器，线程数
			countDownLatch = new CountDownLatch(1);

			log.info("-- -- 创建统计进程 block_man_id：");
			executorService.submit(new ExpectStatusStat(countDownLatch, list,
					 db_name, col_name_block, stat_date, stat_time));

			countDownLatch.await();
			executorService.shutdown();

			log.info("all sub task finish");
			log.info("-- end stat:" + col_name_block);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
