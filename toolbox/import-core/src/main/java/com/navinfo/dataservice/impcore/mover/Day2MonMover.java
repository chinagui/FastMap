package com.navinfo.dataservice.impcore.mover;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

/** 
* @ClassName: DefaultLogMover 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午4:21:33 
* @Description: TODO
*  
*/
public class Day2MonMover extends DefaultLogMover {
	public Day2MonMover(OracleSchema logSchema, OracleSchema tarSchema, String tempTable, String tempFailLogTable) {
		super(logSchema, tarSchema, tempTable, tempFailLogTable);
	}

	@Override
	protected void doMove(Connection conn, LogMoveResult result) throws SQLException {
		result.setLogActionMoveCount(
				run.update(conn,actionSql()));
		result.setLogOperationMoveCount(
				run.update(conn, operationSql()));
		result.setLogDetailMoveCount(
				run.update(conn, detailSql()));
		result.setLogDetailGridMoveCount(
				run.update(conn, gridSql()));
		run.update(conn,tempTableMoveSql());
		result.setLogOperationTempTable(this.tarTempTable);//设置目标库的log_operation临时表(记录从日库搬移的log_operation的op_Id,op_dt)
	}
	@Override
	public void rollbackMove() throws Exception{
		Connection conn = tarSchema.getPoolDataSource().getConnection();
		try{
			run.update(conn, rollbackGridSql());
			run.update(conn, rollbackDetailSql());
			run.update(conn,rollbackActionSql());
			run.update(conn, rollbackOperationSql());			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

	private String rollbackGridSql() {
		return "delete from log_detail_grid a\r\n" + 
				"      where a.log_row_id in (select t2.row_id\r\n" + 
				"                               from "+tarTempTable+" t1, log_detail t2\r\n" + 
				"                              where t1.op_id = t2.op_id)";
	}

	private String rollbackDetailSql() {
		return "    delete from log_detail a \r\n" + 
				"      where a.op_id in (select op_id from "+tarTempTable+")";
	}

	private String rollbackOperationSql() {
		return "delete from log_operation  a\r\n" + 
				"      where a.op_id in (select op_id from "+tarTempTable+")";
	}

	private String rollbackActionSql() {
		return (" delete from log_action a\r\n" + 
				"      where exists\r\n" + 
				"      (select 1\r\n" + 
				"               from log_operation t\r\n" + 
				"              where t.act_id = a.act_id\r\n" + 
				"                and t.op_id in (select op_id from "+tarTempTable+"))");
	}

}
