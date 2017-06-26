package com.navinfo.dataservice.impcore.flusher;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;

public class CopVersion2MonLogFlusher {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private boolean ignoreError=false;
	private Connection logConn;
	private Connection tarConn;
	private OracleSchema logSchema;
	private String type;
	public  CopVersion2MonLogFlusher(OracleSchema logSchema,Connection logConn,Connection tarConn,boolean ignoreError,String type){
		this.logConn = logConn;
		this.tarConn = tarConn;
		this.ignoreError=ignoreError;
		this.logSchema=logSchema;
		this.type=type;
	}
	public FlushResult flush()throws Exception{
		FlushResult flushResult = LogFlushUtil.getInstance().flush(logConn, tarConn, selectLogSql(),ignoreError,type);
		String failLogTempTable = LogFlusherHelper.createFailueLogTempTable(logSchema.getPoolDataSource().getConnection());
		flushResult.setTempFailLogTable(failLogTempTable);
		log.info("将错误日志记录到错误日志temp表中："+failLogTempTable);
		flushResult.recordFailLog2Temptable(logConn);//
		return flushResult;
	}
	private String selectLogSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT L.* FROM LOG_DETAIL L");
		return sb.toString();
	}
}
