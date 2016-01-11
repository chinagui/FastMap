package com.navinfo.dataservice.commons.database.mongo;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

public class MongoDbFactory {
	private static Logger log = Logger.getLogger(MongoDbFactory.class);
	private Map<String,MongoClient> mongoClientMap = new HashMap<String,MongoClient>();//所有mongo对象的单例池
	
	private static class SingletonHolder{
		private static final MongoDbFactory INSTANCE = new MongoDbFactory();
	}
	public static final MongoDbFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public String getKey(String host,int port){
		return host+":"+port;
	}
	/**
	 * key:host+":"+port,value:Mongo对象
	 * @param host
	 * @param port
	 * @return
	 */
	public MongoClient getMongoInstance(String host,int port)throws UnknownHostException,Exception{
		if(StringUtils.isNotEmpty(host)&&StringUtils.isNotEmpty(String.valueOf(port))){
			MongoClient mongoClient=null;
			String keyStr=getKey(host,port);
			synchronized(this){
				if(mongoClientMap.containsKey(keyStr)){
					mongoClient = mongoClientMap.get(keyStr);
				}else{
					log.info("未找到缓存的MongoClient，新建一个mongo连接。host="+host+",port="+port);
					mongoClient = new MongoClient(host,port);
					//加入mongo池
					mongoClientMap.put(keyStr, mongoClient);
				}
			}
			return mongoClient;
		}else{
			throw new UnknownHostException ("MongoClient对象获取失败：host或port为空");
		}
	}
	
}
