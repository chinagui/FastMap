package com.navinfo.dataservice.scripts.translate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Title: Test
 * @Package: com.navinfo.dataservice.scripts.translate
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/9/2017
 * @Version: V1.0
 */
public class Test {
    public static void main(String[] args) {
        System.out.println("main thread start...");
        Thread thread = new MyThread();
        thread.start();
        thread.setDaemon(true);
        //
        //ThreadPoolExecutor executor = new ThreadPoolExecutor(20,20,3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
        //executor.execute(new Runnable() {
        //    @Override
        //    public void run() {
        //        System.out.println("child thread start...");
        //        for(int i = 0; i < 100000; i++) {
        //            System.out.println(i);
        //        }
        //        System.out.println("child thread end...");
        //    }
        //});

        System.out.println("main thread end...");
        //System.exit(0);
    }
}
