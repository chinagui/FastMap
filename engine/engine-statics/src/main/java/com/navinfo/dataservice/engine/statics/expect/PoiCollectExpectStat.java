package com.navinfo.dataservice.engine.statics.expect;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.season.PoiSeasonMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;

public class PoiCollectExpectStat implements Runnable {
	private Logger log = LogManager.getLogger(this.getClass());
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	private List<BlockMan> blocks;
	private MongoDao md;

	public PoiCollectExpectStat(CountDownLatch cdl, List<BlockMan> blocks,
			String db_name, String col_name, String stat_date, String stat_time) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;
		this.stat_time = stat_time;
		this.md = new MongoDao(db_name);
		this.blocks = blocks;
	}

	private Map<String, Integer> getBlockStat(String blockId) {
		Map<String, Integer> map = new HashMap<String, Integer>();

		MongoCursor<Document> iter = md.find(PoiCollectMain.col_name_block,
				Filters.eq("block_id", blockId)).iterator();

		while (iter.hasNext()) {

			JSONObject doc = JSONObject.fromObject(iter.next());

			String statDate = doc.getString("stat_date");

			JSONObject data = doc.getJSONObject("poi");

			int finish = data.getInt("finish");

			map.put(statDate, finish);

		}

		return map;
	}

	private List<Document> doStat() {

		List<Document> list = new ArrayList<Document>();

		Map<String, Integer> mapSeason = StatInit.getPoiSeasonStat(db_name,
				PoiSeasonMain.col_name_block, "block_id",null);

		for (BlockMan block : blocks) {

			if (block.getCollectPlanStartDate() == null
					|| block.getCollectPlanEndDate() == null) {
				continue;
			}

			Timestamp startDate = (Timestamp) block.getCollectPlanStartDate();

			Timestamp endDate = (Timestamp) block.getCollectPlanEndDate();

			if (startDate.compareTo(endDate) > 0) {
				continue;
			}

			String blockId = block.getBlockId().toString();

			Map<String, Integer> map = getBlockStat(blockId);

			long dayCount = TimestampUtils.getDiff(startDate, endDate,
					TimeUnit.DAYS) + 1;

			int total = 0;

			if (mapSeason.containsKey(blockId)) {
				total = mapSeason.get(blockId);
			}

			int step = 0;

			if (total > 0) {
				step = (int) (total / dayCount);
			}

			int expectFinish = 0;
			int finish = 0;

			for (int i = 0; i < dayCount; i++) {
				expectFinish += step;

				Timestamp ts = TimestampUtils.addDays(startDate, i);

				String statDate = TimestampUtils.tsToStr(ts);

				if (statDate.compareTo(this.stat_date) > 0) {
					break;
				}

				if (map.containsKey(statDate)) {
					finish = map.get(statDate);
				}

				int percent = 0;

				if (expectFinish != 0) {
					percent = (int) ((finish - expectFinish)
							/ (double) expectFinish * 100);
				}

				Document doc = new Document();

				doc.put("block_id", block.getBlockId());

				doc.put("stat_date", statDate);

				doc.put("stat_time", stat_time);

				doc.put("finish", Double.valueOf(finish));

				doc.put("expect", Double.valueOf(expectFinish));

				doc.put("percent", percent);

				doc.put("total", Double.valueOf(total));

				list.add(doc);
			}
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
