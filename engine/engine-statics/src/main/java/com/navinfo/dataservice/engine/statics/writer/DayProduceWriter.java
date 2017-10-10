package com.navinfo.dataservice.engine.statics.writer;

import java.util.Iterator;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;


/**
 * 
 * @ClassName ProductMonitorWriter
 * @author Han Shaoming
 * @date 2017年9月23日 下午1:08:55
 * @Description TODO
 */

public class DayProduceWriter extends DefaultWriter {
	/**
	 * @param collectionName
	 * @param timestamp
	 * @param 
	 */
	public void initMongoDbByDate(String collectionName,String timestamp) {
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
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp) ok");
		}
		
		
		String dateStr = timestamp.substring(0,8); 
		// 删除当天的统计数据
		log.info("删除当天的统计数据 mongo "+collectionName+",timestamp="+timestamp+" ,dateStr="+dateStr);
		Pattern pattern = Pattern.compile("^"+dateStr);
		BasicDBObject query = new BasicDBObject("timestamp", pattern);		
		mdao.deleteMany(collectionName, query);
	}
}
