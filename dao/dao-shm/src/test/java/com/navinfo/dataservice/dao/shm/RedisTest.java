package com.navinfo.dataservice.dao.shm;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.RandomUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Hello world!
 *
 */
public class RedisTest 
{
	protected static Logger log = Logger.getLogger(RedisTest.class);
    public static void main( String[] args )
    {
    	JedisPoolConfig config = new JedisPoolConfig();
    	log.info("starting...");
    	JedisPool pool=new JedisPool(config, "192.168.4.188",6379,20000);
    	Jedis jedis = null;
    	try{
    		jedis = pool.getResource();
    		testSearch(jedis);
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
    		if(jedis!=null){
    			jedis.close();
    		}
    	}
//    	for(int i=0;i<10;i++){
//        	new Thread(){
//        		@Override
//    			public void run(){
//        	    	Jedis jedis = null;
//        	    	try{
//        	    		jedis = pool.getResource();
//        	    		test2(jedis);
//        	    	}catch(Exception e){
//        	    		e.printStackTrace();
//        	    	}finally{
//        	    		if(jedis!=null){
//        	    			jedis.close();
//        	    		}
//        	    	}
//        		}
//        	}.start();
//    	}
    }
    public static void testSearch(Jedis jedis)throws Exception{

		long t1 = System.currentTimeMillis();
		log.info(t1);
		Set<String> set = jedis.keys("rd_name_cn:9999*:*北京*");
		log.info("size:"+set.size());
		Pipeline pl = jedis.pipelined();
		for(String ke:set){
			pl.hmget(ke, "type","id");
		}
		List<Object> list = pl.syncAndReturnAll();
		//List<String> list = jedis.hvals("rd_name:8600*");
		System.out.println(System.currentTimeMillis()-t1);
		System.out.println(list);
    }
    public static void test2(Jedis jedis)throws Exception{

		long t1 = System.currentTimeMillis();
		log.info(t1);
		for(int i=0;i<1000000;i++){
			String type = getPrefixCn(i);
			String name = RandomUtil.nextString(4);
			String content = RandomUtil.nextString(36);
			String key = "rd_name_cn:"+i+":"+type+":"+name;
			Map<String,String> hash = new HashMap<String,String>();
			hash.put("id", String.valueOf(i));
			hash.put("type", type);
			hash.put("name", name);
			hash.put("content", content);
			jedis.hmset(key, hash);
			if(i%10000==0){
				log.info("key:"+key);
			}
		}
		System.out.println(System.currentTimeMillis()-t1);
    }
    public static void testUnifiedName(Jedis jedis)throws Exception {

		log.debug("geting res");
		long t1 = System.currentTimeMillis();
		log.info(t1);
		for(int i=0;i<1000000;i++){
			String type = getPrefix(i);
			String name = RandomUtil.nextString(4);
			String content = RandomUtil.nextString(36);
			JSONObject json = new JSONObject();
			json.put("type", type);
			json.put("name", name);
			json.put("content", content);
			Map<String,String> hash = new HashMap<String,String>();
			hash.put(String.valueOf(i), json.toString());
			jedis.hmset("rd_name", hash);
			if(i%10000==0){
				log.info("key:"+i);
			}
		}
		System.out.println(System.currentTimeMillis()-t1);
    }
    public static String getPrefix(int i){
    	int j = i%10;
    	if(j==0){
    		return "AAA";
    	}else if(j==1){
    		return "BBB";
    	}else if(j==2){
    		return "CCC";
    	}else if(j==3){
    		return "DDD";
    	}else if(j==4){
    		return "EEE";
    	}else if(j==5){
    		return "FFF";
    	}else if(j==6){
    		return "GGG";
    	}else if(j==7){
    		return "HHH";
    	}else if(j==8){
    		return "III";
    	}else if(j==9){
    		return "JJJ";
    	}
    	return "ZZZ";
    }

    public static String getPrefixCn(int i){
    	int j = i%10;
    	if(j==0){
    		return "北京";
    	}else if(j==1){
    		return "北京市";
    	}else if(j==2){
    		return "大北京市";
    	}else if(j==3){
    		return "天津";
    	}else if(j==4){
    		return "天津唐山";
    	}else if(j==5){
    		return "河北明天";
    	}else if(j==6){
    		return "雾霾";
    	}else if(j==7){
    		return "唐山唐山";
    	}else if(j==8){
    		return "一片雾霾";
    	}else if(j==9){
    		return "一片森林";
    	}
    	return "中国";
    }
}
