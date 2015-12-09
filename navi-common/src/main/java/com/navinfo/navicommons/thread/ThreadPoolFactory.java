package com.navinfo.navicommons.thread;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.SystemGlobals;

/**
 * 多线程任务，需要确保每个任务都成功才算整个功能成功。
 *
 * @author liuqing
 */
public class ThreadPoolFactory {

    private static final Logger logger = Logger.getLogger(ThreadPoolFactory.class);

    private static int defaultPoolSize= SystemGlobals.getIntValue("threadPool.corePoolSize", 10);

    private static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    private static ThreadPoolExecutor threadPool;


    private static ThreadPoolFactory instance;

    public static ThreadPoolFactory getInstance() throws IOException {
        return getInstance(null);
    }

    private static Integer poolSize;

    public static synchronized ThreadPoolFactory getInstance(Integer corePoolSize) throws IOException {
        poolSize = corePoolSize;
        if (poolSize == null) {
            poolSize =defaultPoolSize;
        }

        if (instance == null) {
            instance = new ThreadPoolFactory();
        }
        if (instance.getThreadPoolExecutor().isShutdown()) {
            instance = new ThreadPoolFactory();
        }
        return instance;
    }


    public void shutdown() {
        logger.info("强制关闭线程池");
        threadPool.shutdownNow();
    }

    private ThreadPoolFactory() throws IOException {
        createThreadPool();
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPool;
    }


    /**
     * 创建线程池，线程池的最大线程数和连接池最大连接数最好一致 当使用无界队列LinkedBlockingQueue时，maxPoolSize参数无效
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void createThreadPool() throws IOException {

        threadPool = new ThreadPoolExecutor(poolSize,
                poolSize,
                3,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void main(String args[]) {
        long t1 = System.currentTimeMillis();
        try {
            ThreadPoolFactory.getInstance();
        } catch (IOException e) {

            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);

    }

}
