package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class DatabaseOperator {

	public DatabaseOperator() {
		// TODO Auto-generated constructor stub
	}
	
	public List<Object> exeSelect(Connection conn,String sql) throws Exception{
		PreparedStatement pstmt = conn.prepareStatement(sql);		
		ResultSet resultSet = pstmt.executeQuery();
		List<Object> resultList=new ArrayList<Object>();
		resultList=settleResultSet(resultSet);
		releaseSource(pstmt,resultSet);
		return resultList;
		}
	
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		while (resultSet.next()){
			resultList.add(resultSet.getString(1));
		} 
		return resultList;
	}
	
	private void releaseSource(Statement stmt,ResultSet resultSet) throws SQLException{
		resultSet.close();
		stmt.close();
	}
	
	public static void main(String[] args) throws Exception{
		String sql="select a.link_pid from rd_link a";
		Connection conn=DBConnector.getInstance().getConnectionById(11);
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(conn, sql);
		System.out.println("end");
	}

}
