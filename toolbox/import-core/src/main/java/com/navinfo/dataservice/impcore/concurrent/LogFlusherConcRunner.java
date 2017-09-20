package com.navinfo.dataservice.impcore.concurrent;

import com.navinfo.dataservice.commons.database.OracleSchema;

/** 
 * @ClassName: LogFlusherConcRunner
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: LogFlusherConcRunner.java
 */
public class LogFlusherConcRunner {
	protected boolean ignoreError=false;
	protected OracleSchema logSchema;
	protected OracleSchema tarSchema;
}
