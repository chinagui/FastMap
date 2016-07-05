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
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.season.RoadSeasonMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

public class RoadCollectExpectStat implements Runnable {
	private Logger log = LogManager.getLogger(RoadCollectExpectStat.class);

	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	private List<BlockMan> blocks;
	private MongoDao md;

	public RoadCollectExpectStat(CountDownLatch cdl, List<BlockMan> blocks,
			String db_name, String col_name, String stat_date, String stat_time) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;
		this.stat_time = stat_time;
		this.blocks = blocks;
		this.md = new MongoDao(db_name);
	}

	private Map<String, Double> getBlockStat(String blockId) {
		Map<String, Double> map = new HashMap<String, Double>();

		MongoCursor<Document> iter = md.find(RoadCollectMain.col_name_block,
				Filters.eq("block_id", blockId)).iterator();

		while (iter.hasNext()) {

			JSONObject doc = JSONObject.fromObject(iter.next());

			String statDate = doc.getString("stat_date");

			JSONObject data = doc.getJSONObject("road");

			double finish = data.getDouble("finish");

			map.put(statDate, finish);

		}

		return map;
	}

	private List<Document> doStat() {

		List<Document> list = new ArrayList<Document>();
		
		Map<String, Double> mapSeason = StatInit.getTrackSeasonStat(db_name, RoadCollectMain.col_name_seasion_block, "block_id");
		
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

			Map<String, Double> map = getBlockStat(blockId);

			long dayCount = TimestampUtils.getDiff(startDate, endDate,
					TimeUnit.DAYS) + 1;

			double total = 0;

			if (mapSeason.containsKey(blockId)) {
				total = mapSeason.get(blockId);
			}

			double step = 0;

			if (total > 0) {
				step = StatUtil.formatDouble(total / dayCount);
			}

			double expectFinish = 0;
			double finish = 0;

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

				doc.put("finish", finish);

				doc.put("expect", expectFinish);

				doc.put("percent", percent);

				doc.put("total", total);

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
