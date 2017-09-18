package com.navinfo.dataservice.impcore.concurrent;

import java.sql.Connection;

import com.navinfo.dataservice.impcore.flushbylog.LogReader;
import com.navinfo.dataservice.impcore.flushbylog.LogWriter;
import com.navinfo.dataservice.impcore.flushbylog.ThreadSharedObjectExtResult;

/** 
 * @ClassName: DefaultAllLogFlusher
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: DefaultAllLogFlusher.java
 */
public class DefaultAllLogFlusher extends AbstractLogFlusher {


	public DefaultAllLogFlusher(ThreadSharedObjectExtResult sharedResults) {
		super(sharedResults);
	}

	@Override
	public void initLogReader(Connection conn, String tempOpTable) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT L.* FROM LOG_DETAIL L,");
		sb.append(tempOpTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID ORDER BY T.OP_DT,T.OP_SEQ");
		reader = new LogReader(conn,sb.toString());
	}


	@Override
	public void intiLogWriter(Connection conn) throws Exception {
		//LogWriter改动太大，暂用原来的
		writer = new LogWriter(conn,true,"day2MonSync");
	}

}
