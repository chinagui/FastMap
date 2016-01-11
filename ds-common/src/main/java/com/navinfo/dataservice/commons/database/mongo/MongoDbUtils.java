package com.navinfo.dataservice.commons.database.mongo;


import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClient;
import com.navinfo.dataservice.commons.exception.DataSourceException;

public class MongoDbUtils {
	
	private static Logger log = Logger.getLogger(MongoDbUtils.class);
		
	public static MongoCollection<Document> getCollection(String host,int port,String dbName,String collName){
		log.debug("获取mongoDb数据库信息");
		MongoClient mongoClient;
		MongoDatabase db;
		MongoCollection<Document> collection;
		try {
			mongoClient = MongoDbFactory.getInstance().getMongoInstance(host, port);
			db=mongoClient.getDatabase(dbName);
			collection=db.getCollection(collName);
		} 
		 catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new DataSourceException("获取MongoDB数据集合失败。dbName="+dbName+",collName:"+collName);
		}
		 return collection;
	}
	
	
	/**
	 * 获取指定的DB
	 * @return
	 */
	public static MongoDatabase getDbByName(String host,int port,String dbName){
		log.debug("=============================MongoDBINFO==========================");
		log.debug("host:"+host);
		log.debug("port:"+port);
		log.debug("dbName:"+dbName);
		MongoClient mongoClient;
		MongoDatabase db;
		try {
			mongoClient = MongoDbFactory.getInstance().getMongoInstance(host, port);
			db=mongoClient.getDatabase(dbName);
		} 
		 catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new DataSourceException("获取MongoDB数据库失败！dbName="+dbName);
		}
		 return db;
	}
	
    public static void main(String[] args){
    	try{
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

	
	
	

}
