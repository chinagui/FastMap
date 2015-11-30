package com.navinfo.dataservice.expcore.sql.handler;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-5-9
 * Time: 下午7:19
 */
public class UncaughtThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected Logger log = Logger.getLogger(this.getClass());
    private List<Throwable> exceptions;
    private CountDownLatch doneSignal;

    private static Object lock = new Object();

    public UncaughtThreadExceptionHandler(List<Throwable> exceptions,
                                          CountDownLatch doneSignal) {
        this.exceptions = exceptions;
        this.doneSignal = doneSignal;
    }

    public void uncaughtException(Thread t, Throwable e) {
        log.error("Thread:" + t.getName() + " throw exception," + e.getMessage(), e);
        saveException(e);
        log.info("唤醒主线程");
        notifyMainThread();

    }


    private void saveException(Throwable e) {
        synchronized (lock) {
            exceptions.add(e);
        }
    }

    private void notifyMainThread() {
        synchronized (lock) {
            while (doneSignal.getCount() > 0)
                doneSignal.countDown();
        }
    }

}
