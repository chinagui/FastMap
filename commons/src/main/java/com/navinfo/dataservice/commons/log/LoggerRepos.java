package com.navinfo.dataservice.commons.log;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.DateUtils;



/** 
* @ClassName: LoggerRepos 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:12:36 
* @Description: 获取当前线程中的独立日志，如果没有则使用全局日志
* 独立日志以String-Logger的key-value格式存储
*/
public class LoggerRepos {
    private static Logger log = Logger.getLogger(LoggerRepos.class);
    private static ThreadLocal<Logger> logThreadLocal = new ThreadLocal<Logger>();


    /**
     * 取得当前线程变量中已经初始化的日志系统  ，如果当前线程变量中不存在这样的日志，则使用全局日志
     *
     * @param log
     * @return
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = logThreadLocal.get();
        if (logger == null) {
            logger = Logger.getLogger(clazz);
        }
        return logger;
    }

    /**
     * 取得当前线程变量中已经初始化的日志系统  ，如果当前线程变量中不存在这样的日志，则使用传入的原始日志
     * @param logName
     * @return
     */
    public static Logger getLogger(Logger originLogger) {
    	Logger logger = logThreadLocal.get();
    	if(logger == null){
    		return originLogger;
    	}
        return logger;
    }

    /**
     * 生成一个独立日志，键值为key
     * @param key
     * @return
     */
    public static Logger createLogger(String key) throws IOException{
    	log.info("create a log gine named:"+key);
    	String output = "logs/"+key+"-"+DateUtils.dateToString(new Date(),DateUtils.DATE_COMPACTED_FORMAT)+".log";
    	Logger logger = DynamicOutputLogger.getFileLogger(key,output);
        log.debug("create task log:" + output);
        logThreadLocal.set(logger);
        return logger;
    }
    /**
     * 根据键值，获取当前日志记录器,如果没有，则返回null
     * @param logName
     * @return
     */
    public static Logger getLogger(String key) {
    	Logger logger = DynamicOutputLogger.getLogger(key);
        if (!logger.getAllAppenders().hasMoreElements()) {
            return null;
        }
        return logger;
    }

    /**
     * 每个有独立线程执行结束后，销毁这个任务的日志记录器
     *
     * @param taskId
     */
    public static void destroyLogger(String key) {
        logThreadLocal.remove();
        DynamicOutputLogger.removeLogger(key);
    }

}
