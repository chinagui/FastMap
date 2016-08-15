package com.navinfo.dataservice.engine.edit.zhangyuntao.other.synchronize;

/**
 * @Title: SynchronizedExample.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月12日 上午9:54:16
 * @version: v1.0
 */
public class SynchronizedExample extends Thread {

	public SynchronizedExample() {
	}
	
	@Override
	public void run(){
		int i = 0;
		while (i < 10) {
			print1();
			i++;
			System.out.println(i);
		}
	}
	
	public synchronized void print1(){
		System.out.println("print1");
	}
	
	public static void main(String[] args) {
		SynchronizedExample ep1 = new SynchronizedExample();
		SynchronizedExample ep2 = new SynchronizedExample();
		ep1.start();
//		try {
//			ep2.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		ep2.start();
	}
}
