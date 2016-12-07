package com.navinfo.dataservice.impcore.mover;

import java.sql.Connection;

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
	public DefaultLogMover(OracleSchema logSchema, OracleSchema tarSchema,String tempTable,String tempFailLogTable) {
		super(logSchema, tarSchema);
		this.tempTable=tempTable;
		this.tempFailLogTable=tempFailLogTable;
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
			conn = logSchema.getPoolDataSource().getConnection();
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
			return result;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
//			if(cr!=null) cr.drop(dbLinkName, false, logSchema.getPoolDataSource());
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private String tempTableMoveSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into "+tarTempTable+"@");
		sb.append(dbLinkName);
		sb.append(" select * from ");
		sb.append(tempTable);
		return sb.toString();
	}
	protected String actionSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("insert into log_action@");
		sb.append(dbLinkName);
		sb.append(" select l.* from log_action l where l.op_id in (select op_id from ");
		sb.append(tempTable);
		sb.append(" t)");
//		if(StringUtils.isNotEmpty(tempFailLogTable)){
//			sb.append(" WHERE NOT EXISTS(SELECT 1 FROM ");
//			sb.append(tempFailLogTable);
//			sb.append(" f WHERE f.row_id=l.row_Id)");
//		}
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
		sb.append("(OP_ID,US_ID,OP_CMD,OP_DT) SELECT L.OP_ID,L.US_ID,L.OP_CMD,L.OP_DT FROM LOG_OPERATION L,");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID  ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" F WHERE F.ROW_ID=D.ROW_ID)");
		}
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
