package com.navinfo.dataservice.impcore.concurrent;

import java.sql.Connection;

import com.navinfo.dataservice.impcore.flushbylog.LogReader;
import com.navinfo.dataservice.impcore.flushbylog.LogWriter;
import com.navinfo.dataservice.impcore.flushbylog.ThreadSharedObjectExtResult;

/** 
 * @ClassName: Day2MonRowConcLogFlusher
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: Day2MonRowConcLogFlusher.java
 */
public class Day2MonRowConcLogFlusher extends AbstractLogFlusher {

	protected int rownumStart=0;
	protected int rownumSize = 0;
	public Day2MonRowConcLogFlusher(ThreadSharedObjectExtResult sharedResults,int rownumStart,int rownumSize) {
		super(sharedResults);
		this.rownumStart = rownumStart;
		this.rownumSize = rownumSize;
	}

	@Override
	public void initLogReader(Connection conn, String tempOpTable) throws Exception {
		String sql = "select L.* from LOG_DETAIL L, " + tempOpTable + " T       \n"
				+ "      where L.OP_ID=T.OP_ID AND L.TB_NM                 \n"
				+ "          ORDER BY T.OP_SEQ) a    \n"
				+ "         where rn >" + rownumStart + "                    \n"
				+ "        AND rn <= " + (rownumStart + rownumSize) + " \n";
		reader = new LogReader(conn,sql);
	}


	@Override
	public void intiLogWriter(Connection conn) throws Exception {
		writer = new LogWriter(conn,true,"day2MonSync");
	}

}
