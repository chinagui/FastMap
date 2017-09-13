package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.navicommons.database.TransactionalDataSource;

/** 
* @ClassName: LogReader 
* @author Xiao Xiaowen 
* @date 2016年4月21日 下午6:10:26 
* @Description: TODO
*/
public class LogReaderDay2Month {
	private TransactionalDataSource sourceDataSource;
	private Connection sourceDbConn;
	private ResultSet rs;
	private Statement st;
	private String logQuerySql;

	public LogReaderDay2Month(TransactionalDataSource sourceDataSource,String logQuerySql) {
		
		this.sourceDataSource = sourceDataSource;
		this.logQuerySql = logQuerySql;
	}
	public ResultSet read() throws Exception{
		Statement sourceStmt = sourceDbConn.createStatement();
		ResultSet rs = sourceStmt.executeQuery(logQuerySql);
		rs.setFetchSize(1000);
		this.rs = rs;
		this.st = sourceStmt;
		return rs;
	}
	/**
	 * 只清理读取过程中的资源；但是不关闭输入的conn，需要调用方进习conn的关闭
	 */
	public void close(){
		DbUtils.closeQuietly(rs);
		DbUtils.closeQuietly(st);
		sourceDataSource.giveBackConnection(sourceDbConn);
	}
	public void open() throws SQLException {
		sourceDbConn = sourceDataSource.getConnection();
		sourceDbConn.setAutoCommit(false);

	}
	

}
