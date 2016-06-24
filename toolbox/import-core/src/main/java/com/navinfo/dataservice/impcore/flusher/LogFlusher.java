package com.navinfo.dataservice.impcore.flusher;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: LogFlusher 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午1:44:53 
* @Description: TODO
*  
*/
public abstract class LogFlusher {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected boolean ignoreError=false;
	protected OracleSchema logSchema;
	protected OracleSchema tarSchema;
	protected QueryRunner run;
	public LogFlusher(OracleSchema logSchema,OracleSchema tarSchema,boolean ignoreError){
		this.logSchema=logSchema;
		this.tarSchema=tarSchema;
		this.ignoreError=ignoreError;
		run = new QueryRunner();
	}
	public abstract FlushResult flush()throws Exception;
}
