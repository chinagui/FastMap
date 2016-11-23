package com.navinfo.dataservice.commons.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** 
* @ClassName: MultiThreadTest 
* @author Xiao Xiaowen 
* @date 2016年11月22日 下午2:35:38 
* @Description: TODO
*/
public class MultiThreadTest {
	public static void main(String[] args) {
		try{
			VMThreadPoolExecutor threadPoolExecutor = new VMThreadPoolExecutor(10,
	        		10,
					3,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
			final CountDownLatch latch = new CountDownLatch(20);
			threadPoolExecutor.addDoneSignal(latch);
			for(int i=0;i<20;i++){
				threadPoolExecutor.execute(new TestThread(i,latch));
			}
			latch.await();
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
