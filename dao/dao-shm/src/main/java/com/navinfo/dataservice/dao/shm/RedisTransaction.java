package com.navinfo.dataservice.dao.shm;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/** 
 * @ClassName: RedisTransaction
 * @author xiaoxiaowen4127
 * @date 2016年7月28日
 * @Description: RedisTransaction.java
 */
public class RedisTransaction {
	protected Jedis jedis;
	protected Transaction trans;
	public RedisTransaction(Jedis jedis){
		this.jedis=jedis;
		trans = jedis.multi();
	}
	public List<Object> commitTransaction(){
		if(trans!=null){
			return  trans.exec();
		}
		return null;
	}
}
