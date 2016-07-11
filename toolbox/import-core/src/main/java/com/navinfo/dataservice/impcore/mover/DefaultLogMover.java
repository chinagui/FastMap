package com.navinfo.dataservice.impcore.mover;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.RandomUtil;
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
	@Override
	public LogMoveResult move() throws Exception {
		
		Connection conn = null;
		try{
			//create db link
			DbLinkCreator cr = new DbLinkCreator();
			dbLinkName = tarSchema.getConnConfig().getUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, logSchema.getPoolDataSource(), tarSchema.getConnConfig().getUserName(), tarSchema.getConnConfig().getUserPasswd(), tarSchema.getConnConfig().getServerIp(), String.valueOf(tarSchema.getConnConfig().getServerPort()), tarSchema.getConnConfig().getServiceName());
			conn = logSchema.getPoolDataSource().getConnection();
			run.update(conn, operationSql());
			run.update(conn, detailSql());
			run.update(conn, gridSql());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
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
		sb.append(" T,LOG_DETAIL D WHERE L.OP_ID=T.OP_ID  AND L.OP_ID=D.OP_ID");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
		}
		return sb.toString();
	}

}
