package com.navinfo.dataservice.impcore.concurrent;

import java.sql.Connection;

import com.navinfo.dataservice.impcore.flushbylog.LogReader;
import com.navinfo.dataservice.impcore.flushbylog.LogWriter;
import com.navinfo.dataservice.impcore.flushbylog.ThreadSharedObjectExtResult;

/** 
 * @ClassName: Day2MonTableConcRunner
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: Day2MonTableConcRunner.java
 */
public class Day2MonTableConcRunner extends LogFlushRunner {

	protected String tableName;
	public Day2MonTableConcRunner(ThreadSharedObjectExtResult sharedResults,String tableName) {
		super(sharedResults);
		this.tableName=tableName;
	}

	@Override
	public void initLogReader(Connection conn,String tempOpTable) throws Exception {
		String sql = "SELECT L.* FROM LOG_DETAIL L," + tempOpTable + " T WHERE L.OP_ID=T.OP_ID AND L.TB_NM = '"+tableName+"'";
		reader = new LogReader(conn,sql);
	}

	@Override
	public void intiLogWriter(Connection conn) throws Exception {
		//LogWriter改动太大，暂用原来的
		writer = new LogWriter(conn,true,"day2MonSync");
	}

}
