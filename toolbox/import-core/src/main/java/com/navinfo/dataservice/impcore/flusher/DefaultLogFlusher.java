package com.navinfo.dataservice.impcore.flusher;

import java.sql.Connection;
import java.util.Random;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.NaviListUtils;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.dataservice.impcore.flushbylog.LogReader;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: DefaultLogFlusher 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午4:00:36 
* @Description: TODO
*  
*/
public class DefaultLogFlusher extends LogFlusher {

	public DefaultLogFlusher(OracleSchema logSchema, OracleSchema tarSchema, boolean ignoreError,String tempTable) {
		super(logSchema, tarSchema, ignoreError);
		this.tempTable=tempTable;
	}

	protected String tempTable;
	protected String tempFailLogTable;

	@Override
	public FlushResult flush() throws Exception {
		Connection logConn = null;
		Connection tarConn = null;
		try{
			logConn = logSchema.getPoolDataSource().getConnection();
			tempFailLogTable = createFailueLogTempTable(logConn);
			tarConn = tarSchema.getPoolDataSource().getConnection();
			FlushResult result = LogFlushUtil.getInstance().flush(logConn, tarConn, selectLogSql(),this.ignoreError);
			recordFailLog2Temptable(result,logConn);
			result.setTempFailLogTable(tempFailLogTable);
			logConn.commit();
			if(result.isSuccess()||ignoreError){
				tarConn.commit();
			}else{
				throw new Exception("履历刷库过程中有履历刷库失败，请检查表"+tempFailLogTable+"中错误日志");
			}
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(logConn);
			DbUtils.rollbackAndCloseQuietly(tarConn);
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(logConn);
			DbUtils.closeQuietly(tarConn);
		}
	}
	/**
	 * DDL语句，会提交事务，注意使用
	 * @throws Exception
	 */
	protected String createFailueLogTempTable(Connection conn)throws Exception{
		StringBuilder sb = new StringBuilder();
		String table = "TEMP_FAIL_LOG_"+new Random().nextInt(1000000);
		sb.append("CREATE TABLE ");
		sb.append(table);
		sb.append("(OP_ID RAW(16),ROW_ID RAW(16))");
		run.execute(conn, sb.toString());
		return table;
	}
	
	protected String selectLogSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT L.* FROM LOG_DETAIL L,");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID ORDER BY T.OP_DT");
		return sb.toString();
	}
	
	protected void recordFailLog2Temptable(FlushResult flushResult,Connection conn) throws Exception{
		if (flushResult.isSuccess()) return ;
		String sql = "insert into "+tempFailLogTable+" values(?,?)";
		Object[][] batchParams = NaviListUtils.toArrayMatrix(flushResult.getFailedLog());
		run.batch(conn, sql, batchParams);
	}

}
