package com.navinfo.dataservice.commons.thread;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-5-11
 * Time: 下午4:32
 * 带异常处理的线程池
 */
public class VMThreadPoolExecutor extends ThreadPoolExecutor {

	protected Logger log = Logger.getLogger(this.getClass());

	private List<Throwable> exceptions = new ArrayList<Throwable>();
	protected List<CountDownLatch> doneSignals = new ArrayList<CountDownLatch>();

	public VMThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	@Override
	public void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		synchronized (this) {
			if (t == null)
				return;
			// 添加异常到队列
			log.error("添加异常到队列", t);
			exceptions.add(t);
			log.info("激活主线程");
			for (CountDownLatch doneSignal : doneSignals) {
				while (doneSignal.getCount() > 0)
					doneSignal.countDown();
			}

		}

	}

	public List<Runnable> shutdownNow() {
		if (!isShutdown()) {
			log.error("关闭线程池");

			try {
				log.debug("终止线程池");
				// 强制关闭线程会造成资源未释放，所以资源需要主线程管理
				List<Runnable> shutdownNow = super.shutdownNow();
				log.debug("终止线程池成功，终止任务数:" + shutdownNow.size());
				while (!isTerminated()) {
					log.debug("等待未执行完的任务：" + getActiveCount());
					try {
						Thread.sleep(2 * 1000);
					} catch (Exception e) {
						log.error("thread sleep error", e);
					}

				}
				log.debug("未执行完的任务数：" + getActiveCount());
			} catch (Exception e) {
				log.error("shutdownNow:", e);
			}

			// shutdown();
		}

		return new ArrayList<Runnable>();
	}

	public List<Throwable> getExceptions() {
		return exceptions;
	}

	public void addDoneSignal(CountDownLatch doneSignal) {
		this.doneSignals.add(doneSignal);
	}

	public void clearDoneSignals() {
		this.doneSignals.clear();
	}
}
