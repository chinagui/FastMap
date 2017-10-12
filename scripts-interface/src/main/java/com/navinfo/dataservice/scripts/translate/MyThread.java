package com.navinfo.dataservice.scripts.translate;

/**
 * @Title: MyThread
 * @Package: com.navinfo.dataservice.scripts.translate
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/9/2017
 * @Version: V1.0
 */
public class MyThread extends Thread{

    @Override
    public void run() {
        System.out.println("child thread start...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("child thread end...");
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("child thread destroy...");
        super.finalize();
    }
}
