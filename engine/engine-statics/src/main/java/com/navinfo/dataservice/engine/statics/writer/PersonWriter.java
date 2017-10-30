package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 任务数据写入oracle
 * @ClassName TaskWriter
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:46:58
 * @Description TODO
 */
public class PersonWriter extends DefaultWriter {
	
	/**
	 * 统计结果mongo结果库初始化
	 * 1.判断是否有collection，如果没有就自动创建，并建立默认索引，有特殊索引需求，单独继承该类
	 * 2.删除时间点相同的重复统计数据
	 * @param collectionName
	 */
	public void initMongoDb(String collectionName,String timestamp,JSONObject identifyJson) {
		log.info("init mongo "+collectionName);
		MongoDao mdao = new MongoDao(dbName);
		MongoDatabase md = mdao.getDatabase();
		// 初始化 col_name_grid
		Iterator<String> iter_grid = md.listCollectionNames().iterator();
		boolean flag_grid = true;
		while (iter_grid.hasNext()) {
			if (iter_grid.next().equalsIgnoreCase(collectionName)) {
				flag_grid = false;
				break;
			}
		}

		if (flag_grid) {
			md.createCollection(collectionName);
			md.getCollection(collectionName).createIndex(
					new BasicDBObject("timestamp", 1));
			md.getCollection(collectionName).createIndex(
					new BasicDBObject("workDay", 1));
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp,workDay) ok");
		}
		
		// 删除时间点相同的重复统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",identifyJson="+identifyJson);
		BasicDBObject query = new BasicDBObject();
		query.putAll(identifyJson);
		mdao.deleteMany(collectionName, query);
	}
}
