package com.navinfo.dataservice.commons.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-12-20 Time: 下午5:00
 */
public class MultiThreadExecute {
	protected Logger log = Logger.getLogger(this.getClass());

	public MultiThreadExecute() {

	}

	/**
	 * 创建线程池
	 * 
	 * @return
	 */
	protected VMThreadPoolExecutor createThreadPool() {
		int poolSize = 10;
		try {
			VMThreadPoolExecutor executePoolExecutor = new VMThreadPoolExecutor(
					poolSize, poolSize, 3, TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
			return executePoolExecutor;
		} catch (Exception e) {
			log.error("初始化线程池错误:", e);

		}
		return null;
		
	}
}
