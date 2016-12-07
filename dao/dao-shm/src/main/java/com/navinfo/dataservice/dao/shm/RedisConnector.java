package com.navinfo.dataservice.dao.shm;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.exception.ConnectionException;

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
	public Jedis getConnection()throws ConnectionException{
		if(pool==null){
			synchronized(this){
				if(pool==null){
					//...完善从sysconfig读配置，连接池大小默认
					//...设置密码
					jConf=new JedisPoolConfig();
					String conf = SystemConfigFactory.getSystemConfig().getValue("main.redis.config");
					if(StringUtils.isEmpty(conf)){
						throw new ConnectionException("Redis服务器连接参数未设置");
					}
					String[] confArray = conf.split(",");
					if(confArray.length!=4){
						throw new ConnectionException("Redis服务器连接参数设置错误");
					}
					pool=new JedisPool(jConf, confArray[0],Integer.parseInt(confArray[1]),Integer.parseInt(confArray[2]),confArray[3]);
				}
			}
		}
		return pool.getResource();
	}
	/**
	 * use jedis.close()
	 * @param jedis
	 */
	@Deprecated
	public void release(final Jedis jedis){
		if(jedis!=null){
			jedis.close();
		}
	}
	public static void main(String[] args) {
		Jedis j = null;
		try{
			j = RedisConnector.getInstance().getConnection();
			Set<String> re = j.keys("*");
			System.out.println(StringUtils.join(re,","));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(j!=null)j.close();
		}
	}
}
