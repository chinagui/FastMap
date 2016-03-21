package com.navinfo.navicommons.chain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.chain.impl.ContextBase;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.SystemGlobals;
import com.navinfo.navicommons.exception.DMSException;
import com.navinfo.navicommons.thread.ThreadPoolFactory;

/**
 * User: liuqing
 * Date: 2010-10-29
 * Time: 10:46:02
 */
public class ChainContext extends ContextBase {
    protected Logger log = Logger.getLogger(this.getClass());
    private ThreadPoolExecutor poolExecutor;

    public ThreadPoolExecutor getPoolExecutor(int poolSize) {
        if (poolExecutor == null) {
            poolExecutor = createThreadPool(poolSize);
        }
        return poolExecutor;
    }

    public ThreadPoolExecutor getPoolExecutor() {
        if (poolExecutor == null) {
            int poolSize = SystemGlobals.getIntValue("threadPool.corePoolSize", 10);
            poolExecutor = getPoolExecutor(poolSize);
        }
        return poolExecutor;
    }


    protected ThreadPoolExecutor createThreadPool(int poolSize) {
        ThreadPoolExecutor poolExecutor = null;
        try {
            poolExecutor = ThreadPoolFactory.getInstance(poolSize).getThreadPoolExecutor();
        } catch (IOException e) {
            throw new DMSException("初始化线程池错误", e);
        }
        return poolExecutor;
    }


    /**
     * 停止所有线程标识
     */
    private volatile boolean shutdownApp = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock lock2 = new ReentrantLock();
    private final List<Exception> threadExceptionList = new ArrayList<Exception>();

    public boolean isAppRunning() {
        lock2.lock();  // block until condition holds
        try {
            return !shutdownApp;
        } finally {
            lock2.unlock();
        }

    }

    public void shutdownApp(Exception e) {
        lock.lock();  // block until condition holds
        try {
            shutdownApp = true;
            threadExceptionList.add(e);
            //唤醒主线程
            //waitMainThread(doneSignal);
            //

        } finally {
            lock.unlock();
        }

    }

    private void waitMainThread(CountDownLatch doneSignal) {
        if (doneSignal != null) {
            //出现异常，强制激活主线程
            while (doneSignal.getCount() > 1) {
                doneSignal.countDown();
            }
        }
    }

    public List<Exception> getThreadExceptionList() {
        return threadExceptionList;
    }


}
