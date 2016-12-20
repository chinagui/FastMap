package com.navinfo.dataservice.impcore.flusher;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;

public class Day2MonLogFlusher {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private boolean ignoreError=false;
	private Connection logConn;
	private Connection tarConn;
	private String tempTable;
	private OracleSchema logSchema;
	public  Day2MonLogFlusher(OracleSchema logSchema,Connection logConn,Connection tarConn,boolean ignoreError,String logTempTable){
		this.logConn = logConn;
		this.tarConn = tarConn;
		this.tempTable = logTempTable;
		this.ignoreError=ignoreError;
		this.logSchema=logSchema;
	}
	public FlushResult flush()throws Exception{
		FlushResult flushResult = LogFlushUtil.getInstance().flush(logConn, tarConn, selectLogSql(),ignoreError);
		String failLogTempTable = LogFlusherHelper.createFailueLogTempTable(logSchema.getPoolDataSource().getConnection());
		flushResult.setTempFailLogTable(failLogTempTable);
		log.info("将错误日志记录到错误日志temp表中："+failLogTempTable);
		flushResult.recordFailLog2Temptable(logConn);//
		return flushResult;
	}
	private String selectLogSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT L.* FROM LOG_DETAIL L,");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID ORDER BY T.OP_DT");
		return sb.toString();
	}
}
