package com.navinfo.dataservice.engine.statics.roaddaily;

import java.util.ArrayList;
/**
 * 由于数据都来源于fcc，所以赵俊芳完成此统计
 * 此类暂时废弃
 */
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
@Deprecated
public class RoadDailyMain {

	private static Logger log = null;
	private static final String col_name = "fm_stat_daily_road";
	private String db_name;
	private String stat_date;
	private String stat_time;
	// tips 库
	private String col_name_tips = "track_length_grid_stat";
	// 季度库
	private String col_name_seasion_grid = "road_season_grid_stat";

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
			log.info("-- -- create mongo collection " + col_name + " ok");
			log.info("-- -- create mongo index on " + col_name + "(grid_id，stat_date) ok");
		}

		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);
		mdao.deleteMany(col_name, query);

	}

	public List<Document> getRdlink() {
		List<Document> json_list = new ArrayList<Document>();
		// 存储 从mongo提取的tips统计结果
		Map<String, Double> mapTips = StatInit.getTrackTipsStat(db_name, col_name_tips, "grid_id", stat_date);
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
			if (finish==0 || total==0){
				road.put("percent", 0);
			}else{
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
		log = LogManager.getLogger(RoadDailyMain.class);

		log.info("-- begin stat:" + col_name);

		try {
			// 初始化mongodb数据库
			initMongoDb(db_name);
			// 统计数据如mongo
			new MongoDao(db_name).insertMany(col_name, getRdlink());

			log.info("-- end stat:" + col_name);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
