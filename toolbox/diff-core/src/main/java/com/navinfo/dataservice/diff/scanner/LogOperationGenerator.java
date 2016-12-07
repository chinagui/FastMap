package com.navinfo.dataservice.diff.scanner;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: LogOperationGenerator 
* @author Xiao Xiaowen 
* @date 2016年4月25日 上午11:33:08 
* @Description: TODO
*/
public class LogOperationGenerator {
	protected Logger log = LoggerRepos.getLogger(LogOperationGenerator.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    public LogOperationGenerator(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }
    
    public int generate(String actId)throws SQLException{
    	Connection conn = null;
    	try{
    		conn = diffServer.getPoolDataSource().getConnection();
    		String sql = "INSERT INTO LOG_OPERATION(OP_ID,ACT_ID,OP_DT,OP_SEQ) SELECT OP_ID,'"+actId+"' act_id,SYSDATE OP_DT,LOG_OP_SEQ.NEXTVAL OP_SEQ FROM LOG_DETAIL";
    		int count = runner.update(conn, sql);
    		conn.commit();
    		return count;
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		DbUtils.rollbackAndCloseQuietly(conn);
    		throw e;
    	}finally{
    		DbUtils.closeQuietly(conn);
    	}
    }
}
