package com.navinfo.dataservice.engine.statics.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.exception.ServiceException;

public class StatInit {

	/**
	 * 初始化datahub 连接环境
	 */
	public static void initDatahubDb() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	/**
	 * 获取 tips库中 track轨迹统计结果，并封装成map返回 支持 根据参数key返回 grid，block，city三种
	 */
	public static Map<String, Double> getTrackTipsStat(String db_name, String col_name, String key, String stat_date) {
		Map<String, Double> map = new HashMap<String, Double>();

		Pattern pattern = Pattern.compile("^" + stat_date + ".*$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject();
		query.put("stat_time", pattern);

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, query).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString(key), json.getDouble("track_length"));
		}
		return map;
	}

	/**
	 * 获取 tips库中 track轨迹统计结果，并封装成map返回 支持 根据参数key返回 grid，block，city三种
	 */
	public static Map<String, Double> getTrackSeasonStat(String db_name, String col_name, String key) {
		Map<String, Double> map = new HashMap<String, Double>();

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, null).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString(key), json.getDouble("total"));
		}
		return map;
	}

	/**
	 * 获取 tips库中 track轨迹统计结果，并封装成map返回 支持 根据参数key返回 grid，block，city三种
	 */
	public static Map<String, Integer> getPoiSeasonStat(String db_name, String col_name, String key) {
		Map<String, Integer> map = new HashMap<String, Integer>();

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, null).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString(key), json.getInt("total"));
		}
		return map;
	}

	/**
	 * 获取 tips 统计库中 获取tips完成量，并封装成map返回 支持 根据参数key返回 grid，block，city三种
	 */
	public static Map<String, Integer> getTipsFinishOfSeason(String db_name, String col_name, String key) {
		Map<String, Integer> map = new HashMap<String, Integer>();

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, null).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString(key), json.getInt("stat_count"));
		}
		return map;
	}

	/**
	 * 获取 tips 统计库中 获取tips完成量，并封装成map返回 支持 根据参数key返回 grid，block，city三种
	 */
	public static Map<String, Integer> getTipsFinishOfDaily(String db_name, String col_name, String key, String stat_date) {
		Map<String, Integer> map = new HashMap<String, Integer>();

		Pattern pattern = Pattern.compile("^" + stat_date + ".*$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", pattern);

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, query).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString(key), json.getInt("stat_count"));
		}
		return map;
	}

	/**
	 * 根据 日编道路grid统计结果 ，构建block，city 三种数据 ，并封装成map返回 支持 根据参数key返回 block，city三种
	 * 
	 * @throws ServiceException
	 */
	public static Map<String, Integer> getCheckFromDaily(String db_name, String col_name, String key, String stat_date) throws ServiceException {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Map<String, String> grid2blockORcity = new HashMap<String, String>();
		if (key == "block") {
			grid2blockORcity = OracleDao.getGrid2Block();
		} else if (key == "city") {
			grid2blockORcity = OracleDao.getGrid2City();
		}
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", stat_date);

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, query).iterator();

		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			String id = grid2blockORcity.get(json.getString("grid_id"));
			Integer cnt = json.getJSONObject("road").getInt("check_wrong_num");
			if (map.containsKey(id)) {
				map.put(id, map.get(id) + cnt);
			} else {
				map.put(id, cnt);
			}

		}
		return map;
	}

}
