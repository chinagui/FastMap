package com.navinfo.dataservice.diff.scanner;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: LogOperationGenerator 
* @author Xiao Xiaowen 
* @date 2016年4月25日 上午11:33:08 
* @Description: TODO
*/
public class LogActionGenerator {
	protected Logger log = LoggerRepos.getLogger(LogActionGenerator.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    public LogActionGenerator(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }
    
    public String generate(long userId,String opCmd,long taskId)throws SQLException{
    	Connection conn = null;
    	try{
    		conn = diffServer.getPoolDataSource().getConnection();
    		String sql = "INSERT INTO LOG_ACTION(ACT_ID,US_ID,OP_CMD,STK_ID) VALUES(?,?,?,?)";
    		String actId = UuidUtils.genUuid();
    		runner.update(conn, sql,actId,userId,opCmd,taskId);
    		conn.commit();
    		return actId;
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		DbUtils.rollbackAndCloseQuietly(conn);
    		throw e;
    	}finally{
    		DbUtils.closeQuietly(conn);
    	}
    }
}
