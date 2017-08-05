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
import com.navinfo.dataservice.dao.mq.MsgPublisher;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

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
	
	public void write(JSONObject messageJSON) throws Exception{
		String jobType=messageJSON.getString("jobType");
		String timestamp=messageJSON.getString("timestamp");
		log.info("start write:jobType="+jobType+",timestamp="+timestamp);
		
		write2Mongo(timestamp,messageJSON.getJSONObject("statResult"));	
		write2Other(timestamp,messageJSON.getJSONObject("statResult"));
		pushEndMsg(jobType,timestamp);
		log.info("end write:jobType="+jobType+",timestamp="+timestamp);
	}
	/**
	 * 重写该方法，增加其他数据库的写入。例如调用写入oracle的方法
	 * @param messageJSON
	 * @throws Exception 
	 */
	public void write2Other(String timestamp,JSONObject messageJSON) throws Exception {}

	/**
	 * 统计信息写入mongo库
	 * @param messageJSON
	 */
	public void write2Mongo(String timestamp,JSONObject messageJSON){
		log.info("start write2Mongo");
		for(Object collectionNameTmp:messageJSON.keySet()){
			String collectionName=String.valueOf(collectionNameTmp);
			//初始化统计collection
			initMongoDb(collectionName,timestamp);
			//统计信息入库
			Document resultDoc=new Document();
			resultDoc.put("timestamp",timestamp);
			resultDoc.put("content",messageJSON.getJSONArray(collectionName));
	
			MongoDao md = new MongoDao(dbName);
			md.insertOne(collectionName, resultDoc);
		}
		log.info("end write2Mongo");
	}
	
	/**
	 * 统计结果mongo结果库初始化
	 * 1.判断是否有collection，如果没有就自动创建，并建立默认索引，有特殊索引需求，单独继承该类
	 * 2.删除时间点相同的重复统计数据
	 * @param collectionName
	 */
	public void initMongoDb(String collectionName,String timestamp) {
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
		
		// 删除时间点相同的重复统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",timestamp="+timestamp);
		BasicDBObject query = new BasicDBObject();
		query.put("timestamp", timestamp);
		mdao.deleteMany(collectionName, query);
	}
	
	/**
	 * 有特殊索引需求则需重写改方法
	 * @param md
	 * @param collectionName
	 */
	public void  createMongoSelfIndex(MongoDatabase md,String collectionName){}
	/**
	 * 发送任务结束消息
	 * {'jobType':'','timestamp':'20170523190000'}
	 * @param jobName
	 * @throws Exception 
	 */
	public void pushEndMsg(String jobType,String timestamp) throws Exception{
		log.info(jobType+" end(execute+write)");
		JSONObject msg=new JSONObject();
		msg.put("jobType", jobType);
		msg.put("timestamp", timestamp);
		MsgPublisher.publish2WorkQueue("stat_job_end", msg.toString());
	}
}
