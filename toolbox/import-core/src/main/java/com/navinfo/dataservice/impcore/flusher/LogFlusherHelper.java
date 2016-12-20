package com.navinfo.dataservice.impcore.flusher;

import java.sql.Connection;
import java.util.Random;

import com.navinfo.navicommons.database.QueryRunner;

public class LogFlusherHelper {
	public static  String createFailueLogTempTable(Connection conn)throws Exception{
		StringBuilder sb = new StringBuilder();
		String table = "TEMP_FAIL_LOG_"+new Random().nextInt(1000000);
		sb.append("CREATE TABLE ");
		sb.append(table);
		sb.append("(OP_ID RAW(16),ROW_ID RAW(16))");
		new QueryRunner().execute(conn, sb.toString());
		return table;
	}
}
