package com.navinfo.navicommons.concurrent;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-31
 */
public class CountDownService
{
//    private CountDownLatch begin =  new  CountDownLatch(1);
    private CountDownLatch end;
    private List<CountDownCommand> commands;
    private ExecutorService executorService;
    private static final transient Logger log = Logger.getLogger(CountDownService.class);
    private static final int DEFAULT_THREAD_COUNT = Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue("concurrent.thread.count","5"));
    private static final int MAX_THREAD_COUNT = 20;

    private Throwable throwable = null;
    private boolean exceptionFlag = false;

    public CountDownService(List<CountDownCommand> commands)
    {
        this(commands,DEFAULT_THREAD_COUNT);
    }

    public CountDownService(List<CountDownCommand> commands, int threadCount)
    {
        int count = threadCount;
        if(threadCount < 1)
            count = 1;
        else if(threadCount > MAX_THREAD_COUNT)
            count = MAX_THREAD_COUNT;
        executorService = Executors.newFixedThreadPool(count);
        this.commands = commands;
        this.end = new CountDownLatch(commands.size());
    }

    public void execute() throws ConcurrentException
    {
        //提交任务
        submitCommand();
        //开始执行
        beginService();
        //处理异常
        processException();
    }

    private void submitCommand()
    {
        for(CountDownCommand command : commands)
        {
            //设置计数锁
//            command.setBegin(begin);
            command.setEnd(end);
            command.setCountDownService(this);
            executorService.submit(command);
        }
    }

    private void beginService() throws ConcurrentException
    {
//        begin.countDown();
        try
        {
            end.await();
        } catch (InterruptedException e)
        {
            log.error(e);
            throw new ConcurrentException(e);
        }
        finally
        {
            executorService.shutdown();    
        }
    }

    void markException(Throwable e)
    {
        exceptionFlag = true;
        throwable = e;
    }

    void processException() throws ConcurrentException
    {
        if(exceptionFlag)
            throw new ConcurrentException(throwable);
    }

    public static abstract class CountDownCommand implements Runnable
    {
//        private CountDownLatch begin;
        private CountDownLatch end;
        private CountDownService countDownService;

        public void run()
        {
            try
            {
//                begin.await();
                doExecute();
            } catch (Throwable e)
            {
                countDownService.markException(e);
            } finally
            {
            	if(end!=null)
            		end.countDown();
            }
        }

        protected abstract void doExecute() throws Throwable;

//        private void setBegin(CountDownLatch begin) {
//            this.begin = begin;
//        }

        private void setEnd(CountDownLatch end) {
            this.end = end;
        }

        private void setCountDownService(CountDownService countDownService) {
            this.countDownService = countDownService;
        }
    }
}
