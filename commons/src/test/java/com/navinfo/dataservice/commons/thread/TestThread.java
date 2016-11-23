package com.navinfo.dataservice.commons.thread;

import java.util.concurrent.CountDownLatch;

import com.navinfo.navicommons.exception.ThreadExecuteException;

/** 
* @ClassName: TestThread 
* @author Xiao Xiaowen 
* @date 2016年11月22日 下午4:33:52 
* @Description: TODO
*/
public class TestThread implements Runnable{

	protected int index=0;
	protected CountDownLatch latch=null;
	public TestThread(int index,CountDownLatch latch){
		this.index=index;
		this.latch=latch;
	}
	@Override
	public void run() {
		try{
			Thread.sleep(2000);
			if(index%5==0){
				throw new ThreadExecuteException("err");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			latch.countDown();
			//
			System.out.println("Over:"+index);
		}
	}
}
