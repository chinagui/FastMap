package com.navinfo.dataservice.commons.log;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.exception.LogInitException;
import com.navinfo.dataservice.commons.job.DSJob;
import com.navinfo.navicommons.database.QueryRunner;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-8-1
 * Time: 下午4:18
 * 每个任务一个日志文件
 */
public class DSJobLogger {
    private static Logger log = Logger.getLogger(DSJobLogger.class);
    private static ThreadLocal<Logger> logThreadLocal = new ThreadLocal<Logger>();

    /**
     * 为每个job初始化一个日志记录器，使得每个job的日志记录在各自单独的文件中
     *
     * @param task
     * @return
     * @throws IOException
     */
    public static Logger createLogger(DSJob job) throws IOException {
        String output = "logs" + File.separator
                + job.getTaskId() + File.separator
                + job.getJobType() + File.separator
                + job.getJobId() + ".log";
        Logger logger = DynamicOutputLogger.getFileLogger(job.getJobId(),
                output);
        log.debug("create task log:" + output);
        logThreadLocal.set(logger);
        return logger;
    }

    /**
     * 取得当前线程变量中已经初始化的日志系统
     *
     * @return
     */
    public static Logger getLogger() {
        Logger logger = logThreadLocal.get();
        if (logger == null) {
            throw new LogInitException("当前线程变量中不存在已初始化的Logger，请先调用方法createVmTaskLogger初始化日志类");
        }
        return logger;
    }

    /**
     * 取得当前线程变量中已经初始化的日志系统  ，如果当前线程变量中不存在这样的日志，则使用参数输入的日志记录器
     *
     * @param log
     * @return
     */
    public static Logger getLogger(Logger log) {
        Logger logger = logThreadLocal.get();
        if (logger == null) {
            logger = log;
        }
        return logger;
    }

    /**
     * 根据任务号，获取当前任务的日志记录器
     *
     * @param taskId
     * @return
     */
    public static Logger getLogger(String taskId) {
        Logger logger = DynamicOutputLogger.getLogger(taskId);
        if (!logger.getAllAppenders().hasMoreElements()) {
            throw new LogInitException("请先调用方法createVmTaskLogger初始化日志类");
            /*log.warn("没有设置将单个任务输出到单独文件的日志系统，使用默认的日志系统");
            logger = Logger.getLogger("default");*/
        }
        return logger;
    }

    /**
     * 每个任务执行结束后，销毁这个任务的日志记录器
     *
     * @param taskId
     */
    public static void destroyVmTaskLogger(String taskId) {
        logThreadLocal.remove();
        DynamicOutputLogger.removeLogger(taskId);
    }

    public static void writeOperateLog(Connection conn,QueryRunner runner,String taskType,String taskStatus,String userName) throws Exception{
    	String configValue=SystemConfig.getSystemConfig().getValue("taskStatus.troggle.log.enable");
    	//默认是写日志的，如果配置了false则不写
    	if("false".equals(configValue)){
    		return;
    	}
		String writeLogSql = "insert into task_exec_operate_log values(?,?,null,?,SYSDATE)";
		//往日志写入一条开关日志
		runner.update(conn,writeLogSql, taskType,taskStatus,userName);
    }


}
