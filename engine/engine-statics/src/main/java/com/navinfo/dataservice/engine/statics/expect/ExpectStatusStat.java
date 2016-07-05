package com.navinfo.dataservice.engine.statics.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;

public class ExpectStatusStat implements Runnable {
	private Logger log = LogManager.getLogger(this.getClass());
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	private List<BlockMan> blocks;
	private MongoDao md;

	public ExpectStatusStat(CountDownLatch cdl, List<BlockMan> blocks, String db_name,
			String col_name, String stat_date, String stat_time) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;
		this.stat_time = stat_time;
		this.blocks = blocks;
		this.md = new MongoDao(db_name);
	}

	private List<Document> doStat() {

		List<Document> list = new ArrayList<Document>();

		Map<Integer, Integer> poiCollectMap = StatInit.getLatestExpectStat(
				db_name, PoiCollectExpectMain.col_name_block, "block_id",
				stat_date);
		Map<Integer, Integer> poiDailyMap = StatInit.getLatestExpectStat(
				db_name, PoiDailyExpectMain.col_name_block, "block_id",
				stat_date);
		Map<Integer, Integer> roadCollectMap = StatInit.getLatestExpectStat(
				db_name, RoadCollectExpectMain.col_name_block, "block_id",
				stat_date);
		Map<Integer, Integer> roadDailyMap = StatInit.getLatestExpectStat(
				db_name, RoadDailyExpectMain.col_name_block, "block_id",
				stat_date);

		for(BlockMan block : blocks){
			
			int blockId = block.getBlockId();

			int poiCollectPercent = 0;
			int poiDailyPercent = 0;
			int roadCollectPercent = 0;
			int roadDailyPercent=0;
			
			if(poiDailyMap.containsKey(blockId)){
				poiCollectPercent=poiCollectMap.get(blockId);
			}
			
			if(poiDailyMap.containsKey(blockId)){
				poiDailyPercent=poiDailyMap.get(blockId);
			}
			
			if(poiDailyMap.containsKey(blockId)){
				roadCollectPercent=roadCollectMap.get(blockId);
			}
			
			if(poiDailyMap.containsKey(blockId)){
				roadDailyPercent=roadDailyMap.get(blockId);
			}
			
			int status = 1;

			if (poiCollectPercent < 0 || poiDailyPercent < 0
					|| roadCollectPercent < 0 || roadDailyPercent < 0) {
				status = -1;
			}

			Document doc = new Document();

			doc.put("block_id", blockId);

			doc.put("status", status);

			doc.put("stat_time", stat_time);

			list.add(doc);
		}

		return list;
	}

	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task");

			List<Document> list = doStat();

			if (list.size() > 0) {
				md.insertMany(col_name, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();
	}

}
