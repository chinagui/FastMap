package com.navinfo.dataservice.dao.shm;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/** 
 * @ClassName: RedisPipeline
 * @author xiaoxiaowen4127
 * @date 2016年7月28日
 * @Description: RedisPipeline.java
 */
public class RedisPipeline {
	protected Jedis jedis;
	protected Pipeline pipeline;
    public RedisPipeline(Jedis jedis){
    	this.jedis=jedis;
    	pipeline=jedis.pipelined();
    }
	public List<Object> syncExec(){
		if(pipeline!=null){
			return pipeline.syncAndReturnAll();
		}
		return null;
	}
}
