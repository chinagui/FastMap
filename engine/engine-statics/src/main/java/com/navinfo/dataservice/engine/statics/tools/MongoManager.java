package com.navinfo.dataservice.engine.statics.tools;

import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

public class MongoManager {

	private static Logger log = Logger.getLogger(MongoManager.class);

	private static final MongoManager instance = new MongoManager();

	private static MongoClient mongo = null;

	private static final String host = SystemMessage.getString("config", "mongo_host");
	private static final Integer port = Integer.valueOf(SystemMessage.getString("config", "mongo_port"));

	/**
	 * 私有化
	 */
	private MongoManager() {
	}

	/**
	 * 单例
	 * 
	 * @return
	 */
	public static MongoManager getInstance() {

		return instance;

	}

	/**
	 * 初始化MongoDB
	 */
	private void init() {

		try {

			mongo = new MongoClient(host, port);
			log.info("MongoDB init success!");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取DB对象
	 * 
	 * @return
	 */
	public MongoClient getMongoInstance() {

		try {

			if (mongo == null) {

				init();

			}
			return mongo;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}