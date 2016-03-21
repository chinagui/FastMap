package com.navinfo.dataservice.commons.util;

import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class Log4jUtils {

	/**
	 * @return 主机名
	 */
	public static String getHostname() {
		return System.getProperty("user.name");
	}

	/**
	 * @return logId
	 */
	public static String genLogid() {
		return UUID.randomUUID().toString().substring(16);

	}
	
	
	/**
	 * 记录错误信息，logid和异常记录到MDC中
	 * @param logger
	 * @param logid
	 * @param msg
	 * @param e
	 */
	public static void error(Logger logger, String logid, String msg, Exception e){
		
		MDC.put("logid", logid);

		MDC.put("trace", ExceptionUtils.getStackTrace(e).replace('\'', '"'));

		logger.error(msg, e);
		
	}
	
}
