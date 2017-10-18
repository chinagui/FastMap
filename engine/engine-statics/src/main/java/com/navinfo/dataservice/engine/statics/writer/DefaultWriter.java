package com.navinfo.dataservice.engine.statics.writer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.mq.MsgPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
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
		String identify=messageJSON.getString("identify");
		JSONObject identifyJson=messageJSON.getJSONObject("identifyJson");
		log.info("start write:jobType="+jobType+",timestamp="+timestamp+",identify="+identify);
		
		write2Mongo(timestamp,identifyJson,messageJSON.getJSONObject("statResult"));	
		write2Other(timestamp,messageJSON.getJSONObject("statResult"));
		pushEndMsg(jobType,timestamp,identify,identifyJson);
		try{
			log.info("start getLatestStatic");
			String staticMessage=getLatestStatic();		
			log.info("end getLatestStatic");
			if(!StringUtils.isEmpty(staticMessage)){
				pushWebSocket(staticMessage,jobType);
			}
		}catch (Exception e) {
			log.error("getLatestStatic or pushsocket error", e);
		}
		log.info("end write:jobType="+jobType+",timestamp="+timestamp+",identify="+identify);
	}
	public String getLatestStatic() throws Exception {
		// TODO Auto-generated method stub
		return null;
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
	public void write2Mongo(String timestamp,JSONObject identifyJson,JSONObject messageJSON){
		log.info("start write2Mongo");
		for(Object collectionNameTmp:messageJSON.keySet()){
			String collectionName=String.valueOf(collectionNameTmp);
			log.info("init "+collectionName);
			//初始化统计collection
			initMongoDb(collectionName,timestamp,identifyJson);
			//统计信息入库
			MongoDao md = new MongoDao(dbName);
			List<Document> docs=new ArrayList<>();
			for(Object tmp:messageJSON.getJSONArray(collectionName)){
				Document resultDoc=new Document();
				JSONObject jsonTmp = (JSONObject) tmp;
//				Iterator keyIter = jsonTmp.keys();
//				while(keyIter.hasNext()){
//					resultDoc.put(String.valueOf(keyIter.next()), jsonTmp.get(keyIter.next()));
//				}
				resultDoc.putAll(jsonTmp);
				resultDoc.put("timestamp",timestamp);
				docs.add(resultDoc);
			}
			log.info("insert "+collectionName+",size "+docs.size());
			md.insertMany(collectionName, docs);
		}
		log.info("end write2Mongo");
	}
	
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
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp) ok");
		}
		
		// 删除时间点相同的重复统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",identifyJson="+identifyJson);
		BasicDBObject query = new BasicDBObject();
		query.putAll(identifyJson);
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
	
	/**
	 * 发送任务结束消息
	 * {'jobType':'','timestamp':'20170523190000','identify':'{'timestamp':'20170523190000','workDay':'20170523'}'}
	 * 有些job的启动要通过identify来确认是否启动，并将identify中的参数传给job
	 * @param jobName
	 * @throws Exception 
	 */
	public void pushEndMsg(String jobType,String timestamp,String identify,JSONObject identifyJson) throws Exception{
		log.info(jobType+" end(execute+write)");
		JSONObject msg=new JSONObject();
		msg.put("jobType", jobType);
		msg.put("timestamp", timestamp);
		msg.put("identify", identify);
		msg.put("identifyJson", identifyJson);
		MsgPublisher.publish2WorkQueue("stat_job_end", msg.toString());
	}
	
	public void pushWebSocket(String staticMessage,String staticType) {
		try {
			log.info("start pushWebSocket");
            SysMsgPublisher.publishManStaticMsg(staticMessage,staticType);
            log.info("end pushWebSocket");
        } catch (Exception ex) {
            log.error("publishManJobMsg error:" + ExceptionUtils.getStackTrace(ex));
        }
	}
}
