package com.navinfo.dataservice.commons.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class LogPidService {

	private static final Logger logger = Logger.getLogger(LogPidService.class);

	private Connection conn;

	private Statement stmt;

	private ResultSet resultSet;


	private String opSql = "select SEQ_LOG_OPERATION_OP_ID.nextval pid from dual ";

	private String obSql = "select SEQ_LOG_OBJECT_OB_LGID.nextval pid from dual ";

	private String ckSql = "select SEQ_CK_EXCEPTION.nextval pid from dual ";
	
	public LogPidService(Connection conn){
		this.conn = conn;
	}


	public int generateOpPid() throws Exception {
		
		int pid = -1;
		
		try {
			stmt = conn.createStatement();

			resultSet = stmt.executeQuery(opSql);

			resultSet.next();
			
			pid = resultSet.getInt(1);
			
			return pid;
		} catch (Exception e) {
			
			throw e;
		}finally{
			try{
				resultSet.close();
			}catch(Exception e){
				
			}
			
			try{
				stmt.close();
			}catch(Exception e){
				
			}
		}

	}

	public int generateOBPid() throws Exception {int pid = -1;
	
	try {
		stmt = conn.createStatement();

		resultSet = stmt.executeQuery(obSql);

		resultSet.next();
		
		pid = resultSet.getInt(1);
		
		return pid;
	} catch (Exception e) {
		
		throw e;
	}finally{
		try{
			resultSet.close();
		}catch(Exception e){
			
		}
		
		try{
			stmt.close();
		}catch(Exception e){
			
		}
	}}

	public int generateExceptionPid() throws Exception {int pid = -1;
	
	try {
		stmt = conn.createStatement();

		resultSet = stmt.executeQuery(ckSql);

		resultSet.next();
		
		pid = resultSet.getInt(1);
		
		return pid;
	} catch (Exception e) {
		
		throw e;
	}finally{
		try{
			resultSet.close();
		}catch(Exception e){
			
		}
		
		try{
			stmt.close();
		}catch(Exception e){
			
		}
	}}

}
