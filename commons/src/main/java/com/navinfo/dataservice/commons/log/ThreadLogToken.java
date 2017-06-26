package com.navinfo.dataservice.commons.log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 
* @ClassName: ThreadLogToken 
* @author Xiao Xiaowen 
* @date 2017年6月8日 下午10:07:17 
* @Description: TODO
 */
public class ThreadLogToken {

    private ThreadLocal<String> tlToken = new ThreadLocal<String>();

	private ThreadLogToken(){
		
	}
 
	private volatile static ThreadLogToken instance;
	
	public static ThreadLogToken getInstance(){
		if(instance==null){
			synchronized(ThreadLogToken.class){
				if(instance==null){
					instance=new ThreadLogToken();
				}
			}
		}
		return instance;
	}

    public void set(String token) {
    	tlToken.set(token);
    }

    public String get() {
    	String token = tlToken.get();
    	if(token==null){
            long head = System.currentTimeMillis()%86400000;
            long body = Thread.currentThread().getId();
            int foot = ThreadLocalRandom.current().nextInt(1000);
            token = head + "_" + body + "_" + foot;
            tlToken.set(token);
    	}
        return token;
    }

    public void remove() {
    	tlToken.remove();
    }
}