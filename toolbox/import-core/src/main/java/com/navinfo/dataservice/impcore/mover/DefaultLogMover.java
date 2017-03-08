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
public class DefaultLogMover extends LogMover {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection moveConn;
	private boolean autoCommit;
	public DefaultLogMover(OracleSchema logSchema, OracleSchema tarSchema,String tempTable,String tempFailLogTable) {
		this(logSchema, tarSchema,tempTable,tempFailLogTable,null,true);
	}
	public DefaultLogMover(OracleSchema logSchema, OracleSchema tarSchema,String tempTable,String tempFailLogTable,Connection moveConn,boolean autoCommit) {
		super(logSchema, tarSchema);
		this.tempTable=tempTable;
		this.tempFailLogTable=tempFailLogTable;
		this.moveConn = moveConn;
		this.autoCommit = autoCommit;
	}
	protected String tempTable;
	protected String tempFailLogTable;
	protected String dbLinkName;
	protected String tarTempTable;
	@Override
	public LogMoveResult move() throws Exception {
		Connection conn = null;
		DbLinkCreator cr = new DbLinkCreator();
		try{
			LogMoveResult  result = new LogMoveResult();
			//create db link
			dbLinkName = tarSchema.getConnConfig().getUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, logSchema.getPoolDataSource(), tarSchema.getConnConfig().getUserName(), tarSchema.getConnConfig().getUserPasswd(), tarSchema.getConnConfig().getServerIp(), String.valueOf(tarSchema.getConnConfig().getServerPort()), tarSchema.getConnConfig().getServiceName());
			tarTempTable = LogFlushUtil.getInstance().createTempTable(tarSchema.getPoolDataSource().getConnection());
			
			if(moveConn==null){
				moveConn=logSchema.getPoolDataSource().getConnection();
			}
			conn = moveConn;
			doMove(conn, result);
			return result;
		}catch(Exception e){
			if(autoCommit){
				DbUtils.rollbackAndCloseQuietly(conn);
			}
			throw e;
		}finally{
//			if(cr!=null) cr.drop(dbLinkName, false, logSchema.getPoolDataSource());
			if(autoCommit){
				DbUtils.commitAndCloseQuietly(conn);
			}
			
		}
	}
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
		run.update(conn,dayReleaseSql());
	}
	protected String tempTableMoveSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into "+tarTempTable+"@");
		sb.append(dbLinkName);
		sb.append(" select * from ");
		sb.append(tempTable);
		return sb.toString();
	}
//	protected String actionSql(){
//		StringBuilder sb = new StringBuilder();
//		sb.append("insert into log_action@");
//		sb.append(dbLinkName);
//		sb.append(" select la.* from log_action la where la.act_id in (select distinct lp.act_id from log_operation lp where lp.op_id in (select t.op_id from ");
//		sb.append(tempTable);
//		sb.append(" t");
//		if(StringUtils.isNotEmpty(tempFailLogTable)){
//			sb.append(" where NOT EXISTS(SELECT 1 FROM ");
//			sb.append(tempFailLogTable);
//			sb.append(" f WHERE f.OP_ID=t.OP_ID)");
//		}
//		sb.append(" ))");
//		return sb.toString();
//	}
	protected String actionSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO log_action@");
		sb.append(dbLinkName);
		sb.append("tt USING (select la.* from log_action la where la.act_id in (select distinct lp.act_id from log_operation lp where lp.op_id in (select t.op_id from ");
		sb.append(tempTable);
		sb.append(" t");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" where NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.OP_ID=t.OP_ID)");
		}
		sb.append(" )) ) TP");
		sb.append(" ON (TP.ACT_ID = TT.ACT_ID)");
		sb.append(" WHEN NOT MATCHED THEN INSERT");
		sb.append(" (ACT_ID, US_ID, OP_CMD, SRC_DB, STK_ID) VALUES");
		sb.append(" (TP.ACT_ID, TP.US_ID, TP.OP_CMD, TP.SRC_DB, TP.STK_ID)");
		return sb.toString();
	}
	protected String detailSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("insert into log_detail@");
		sb.append(dbLinkName);
		sb.append(" select l.* from log_detail l,");
		sb.append(tempTable);
		sb.append(" t where l.op_id=t.op_id");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.row_id=l.row_Id)");
		}
		return sb.toString();
	}
	protected String gridSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_DETAIL_GRID@");
		sb.append(dbLinkName);
		sb.append(" SELECT P.* FROM LOG_DETAIL_GRID P,LOG_DETAIL L,");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID AND L.ROW_ID=P.LOG_ROW_ID");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.row_id=l.row_Id)");
		}
		return sb.toString();
				
	}
	
	protected String operationSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_OPERATION@");
		sb.append(dbLinkName);
		sb.append("(OP_ID,ACT_ID,OP_DT,OP_SEQ) SELECT T.OP_ID,T.ACT_ID,SYSDATE,LOG_OP_SEQ.NEXTVAL@"+dbLinkName+" FROM (SELECT L.OP_ID,L.ACT_ID FROM LOG_OPERATION L,");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID  ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" F WHERE F.OP_ID=T.OP_ID)");
		}
		sb.append(" ORDER BY L.OP_DT) T");
		return sb.toString();
	}

	protected String dayReleaseSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_DAY_RELEASE@");
		sb.append(dbLinkName);
		sb.append("(OP_ID) SELECT distinct L.OP_ID FROM ");
		sb.append(tempTable);
		sb.append(" T,LOG_DETAIL L WHERE T.OP_ID=L.OP_ID");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" F WHERE F.ROW_ID=L.ROW_ID)");
		}
		return sb.toString();
	}
}
