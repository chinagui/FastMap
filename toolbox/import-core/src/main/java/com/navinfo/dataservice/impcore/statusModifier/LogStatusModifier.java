package com.navinfo.dataservice.impcore.statusModifier;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author MaYunFei
 * 2016年7月21日
 * 描述：import-coreLogStatusModifier.java
 */
public abstract class LogStatusModifier {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected OracleSchema logSchema;
	public LogStatusModifier(OracleSchema logSchema) {
		this.logSchema=logSchema;
	}
	public void execute() throws Exception {
		String sql = this.getStatusModSql();
		Connection conn = null;
		try{
			conn = logSchema.getPoolDataSource().getConnection();
			QueryRunner run= new QueryRunner();
			run.update(conn, sql);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void execute(Connection conn) throws Exception {
		String sql = this.getStatusModSql();
		try{
			QueryRunner run= new QueryRunner();
			run.update(conn, sql);
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	
	protected abstract  String getStatusModSql() ;
}

