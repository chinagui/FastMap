package com.navinfo.dataservice.engine.statics.writer;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONObject;

/**
 * 子任务数据写入oracle
 * @ClassName SubtaskWriter
 * @author Han Shaoming
 * @date 2017年8月1日 下午1:53:28
 * @Description TODO
 */
public class SubtaskWriter extends DefaultWriter {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	
	public void write2Other(String timestamp,JSONObject messageJSON) {
		log.info("start write2Oracle");
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
		log.info("end write2Oracle");
	}
}
