package com.navinfo.dataservice.dao.shm;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/** 
 * @ClassName: RedisConnector
 * @author xiaoxiaowen4127
 * @date 2016年7月25日
 * @Description: RedisConnector.java
 */
public class RedisConnector {
	private RedisConnector(){}
	private volatile static RedisConnector instance;

	public static RedisConnector getInstance() {
		if (instance == null) {
			synchronized (RedisConnector.class) {
				if (instance == null) {
					instance = new RedisConnector();
				}
			}
		}
		return instance;
	}
	private JedisPoolConfig jConf;
	private JedisPool pool;
	
	/**
	 * Jedis连接用完需要close()
	 * @return
	 */
	public Jedis getConnection(){
		if(pool==null){
			synchronized(this){
				if(pool==null){
					//...完善从sysconfig读配置，连接池大小默认
					//...设置密码
					jConf=new JedisPoolConfig();
					pool=new JedisPool(jConf, "192.168.4.188",6379,20000);
				}
			}
		}
		return pool.getResource();
	}
}
