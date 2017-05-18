package com.navinfo.dataservice.engine.statics.writer;

import java.util.Iterator;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.database.sql.StringUtil;

/**
 * 统计结果，默认格式为
 * String类型，内容为：{"jobName":"",statDate:"yyyyMMddHHmmss",content:[{具体统计值...}]}
 * 默认存储到mongo库，具体db为：sys库配置的mongo统计的db ，collection为：jobName的同名collection
 * 存储内容为：{statDate:"yyyyMMddHHmmss",content:[{具体统计值...}]}
 * 
 * 重点需要重写的方法：write2Other定制化写入其他类型数据库等，createMongoSelfIndex定制化创建mongo库索引
 * @author zhangxiaoyi
 *
 */
public class DefaultWriter {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	public void write(JSONObject messageJSON){
		String jobName=messageJSON.getString("jobName");
		String statDate=messageJSON.getString("statDate");
		log.info("write:jobName="+jobName+",statDate="+statDate);
		
		write2Mongo(messageJSON);	
		write2Other(messageJSON);
	}
	/**
	 * 重写该方法，增加其他数据库的写入。例如调用写入oracle的方法
	 * @param messageJSON
	 */
	public void write2Other(JSONObject messageJSON) {}

	/**
	 * 统计信息写入mongo库
	 * @param messageJSON
	 */
	public void write2Mongo(JSONObject messageJSON){
		log.info("start write2Mongo");
		String jobName=messageJSON.getString("jobName");
		String statDate=messageJSON.getString("statDate");
		JSONObject resultMsg=new JSONObject();
		resultMsg.putAll(messageJSON);
		resultMsg.remove("jobName");
		//根据驼峰获取collectionName jobName="poiNameStat" 返回：poi_name_stat
		String collectionName=StringUtil.propertyToDB(jobName);
		//初始化统计collection
		initMongoDb(collectionName,statDate);
		//统计信息入库
		Document resultDoc=new Document();
		resultDoc.putAll(resultMsg);

		MongoDao md = new MongoDao(dbName);
		md.insertOne(collectionName, resultDoc);
		log.info("end write2Mongo");
	}
	
	/**
	 * 统计结果mongo结果库初始化
	 * 1.判断是否有collection，如果没有就自动创建，并建立默认索引，有特殊索引需求，单独继承该类
	 * 2.删除时间点相同的重复统计数据
	 * @param collectionName
	 */
	public void initMongoDb(String collectionName,String statDate) {
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
					new BasicDBObject("statDate", 1));
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(statDate) ok");
		}
		
		// 删除时间点相同的重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("statDate", statDate);
		mdao.deleteMany(collectionName, query);
	}
	
	/**
	 * 有特殊索引需求则需重写改方法
	 * @param md
	 * @param collectionName
	 */
	public void  createMongoSelfIndex(MongoDatabase md,String collectionName){}
}
