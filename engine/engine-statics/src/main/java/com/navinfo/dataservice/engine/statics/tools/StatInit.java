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
	 * 初始化tips库统计结果
	 */
	public static Map<String, Double> getGridTipsStat(String db_name, String col_name, String stat_date) {
		Map<String, Double> map = new HashMap<String, Double>();

		Pattern pattern = Pattern.compile("^" + stat_date + ".*$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject();
		query.put("stat_time", pattern);

		MongoCursor<Document> iter = new MongoDao(db_name).find(col_name, query).iterator();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			map.put(json.getString("grid_id"), json.getDouble("track_length"));
		}
		return map;
	}
}
