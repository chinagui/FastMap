package com.navinfo.dataservice.impcore.concurrent;

import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.flushbylog.EditLog;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.ILogWriteListener;
import com.navinfo.dataservice.impcore.flushbylog.LogReader;
import com.navinfo.dataservice.impcore.flushbylog.LogWriteListener;
import com.navinfo.dataservice.impcore.flushbylog.LogWriter;
import com.navinfo.dataservice.impcore.flushbylog.ThreadSharedObjectExtResult;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 
 * @ClassName: LogFlushRunner
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: LogFlushRunner.java
 */
public abstract class LogFlushRunner implements Runnable{
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected LogReader reader;
	protected LogWriter writer;//初始化writer时决定是否忽略新增错误，删除错误，修改错误
	protected ThreadSharedObjectExtResult sharedResults;
	protected QueryRunner run;
	public int totalCount = 0;
	public long totalStartTime = 0;
	public LogFlushRunner(ThreadSharedObjectExtResult sharedResults){
		run = new QueryRunner();
	}

	public abstract void initLogReader(Connection conn,String tempOpTable)throws Exception;
	public abstract void intiLogWriter(Connection conn)throws Exception;
	private FlushResult flush()throws Exception{
		int index = 0;
		ResultSet rs = reader.read();
		try{
			rs.setFetchSize(1000);
			FlushResult flushResult =new FlushResult();
			while (rs.next()) {
				flushResult .addTotal();
				int opType = rs.getInt("op_tp");
				String rowId = rs.getString("row_id");
				String opId = rs.getString("op_id");
				String newValue = rs.getString("new");
				String tableName = rs.getString("tb_nm");
				String tableRowId = rs.getString("tb_row_id");

				EditLog editLog = new EditLog(opType, rowId, opId, rowId,newValue, tableName, tableRowId);
				ILogWriteListener listener = new LogWriteListener(flushResult);
				writer.write(editLog , listener);
				index++;
				if (index % 10000 == 0) {
					totalCountPlus(10000);
				}
			}
			totalCountPlus(index % 10000);
			sharedResults.addFlushResult(flushResult);
			return flushResult;
		}finally{
			DbUtils.closeQuietly(rs);
		}
	}
	@Override
	public void run() {
		try{
			totalStartTime = System.currentTimeMillis();
			sharedResults.addFlushResult(flush());
			sharedResults.executeSuccess();
		}catch(Exception e){
			log.error("刷库线程（"+Thread.currentThread().getName()+")执行失败，原因："+e.getMessage(),e);
			log.debug("已终止线程：" + Thread.currentThread().getName());
			sharedResults.addException(e);
			sharedResults.executeFailAndNotifyMainThread();
		}finally{
			//release resources
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				log.warn("关闭履历写入器时出错:", e);
			}
		}
	}
	void totalCountPlus(int i) {
		totalCount += i;
		long cost = System.currentTimeMillis() - totalStartTime;
		log.debug("总进度：已完成 " + totalCount + ",速度 "
				+ (int) (totalCount / ((cost + 0.1) / 1000)) + "/sec.");
	}
}
