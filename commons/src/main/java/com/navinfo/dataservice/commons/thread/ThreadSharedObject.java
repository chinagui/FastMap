package com.navinfo.dataservice.commons.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/** 
* @ClassName: ThreadSharedObject 
* @author Xiao Xiaowen 
* @date 2016年11月21日 下午9:26:11 
* @Description: TODO
*/
public class ThreadSharedObject {
    private static final Logger logger = Logger.getLogger(ThreadSharedObject.class);
    private Object lock = new Object();
    public Object waitLock = new Object();
    private int successTaskNum = 0;
    private int failTaskNum = 0;
    private int totalTaskNum = 0;
    private List<Exception> exceptionList=new ArrayList<Exception>();//供子线程记录异常

    public int getTotalTaskNum() {
        return totalTaskNum;
    }

    public ThreadSharedObject(int totalTaskNum) {
        this.totalTaskNum = totalTaskNum;
    }

    public void executeSuccess() {
        synchronized (lock) {
            successTaskNum++;
            //logger.debug("已完成线程数 "+(successTaskNum+failTaskNum));
            if (successTaskNum + failTaskNum == totalTaskNum) {
                notifyMainThread();
            }
        }
    }

    public void executeFail() {
        synchronized (lock) {
            failTaskNum++;
            logger.debug("已完成线程数 " + (successTaskNum + failTaskNum) + " 总线程数" + totalTaskNum);
            if (successTaskNum + failTaskNum == totalTaskNum) {
                notifyMainThread();
            }
        }
    }

    public void executeFailAndNotifyMainThread() {
        synchronized (lock) {
            failTaskNum++;
            logger.debug("子线程失败，已完成线程数 " + (successTaskNum + failTaskNum) + " 总线程数" + totalTaskNum);
            notifyMainThread();
        }
    }

    public void notifyMainThread() {
        synchronized (waitLock) {
            waitLock.notify();
        }
    }

    public boolean execFinished() {
        synchronized (lock) {
            //logger.debug("successTaskNum="+successTaskNum+" failTaskNum="+failTaskNum+" totalTaskNum="+totalTaskNum);
            //logger.debug("已完成线程数 "+(successTaskNum+failTaskNum));
            return successTaskNum + failTaskNum == totalTaskNum;
        }
    }

    public boolean haveFailThread() {
        synchronized (lock) {
            return failTaskNum > 0;
        }
    }
    /**
     * 子线程可通过此方法增加异常
     * @param e
     */
    public synchronized void addException(Exception e){
    	exceptionList.add(e);
    }
    public List<Exception> getExceptionList(){
    	return exceptionList;
    }
}
