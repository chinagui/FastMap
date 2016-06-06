package com.navinfo.dataservice.codegen.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

/** 
 * @ClassName: Table
 * @author MaYunFei
 * @date 下午1:52:17
 * @Description: Table.java
 */
public class Table {
	private String url;
	private String user;
	private String pwd;
	private String tableName;
	
	
	/**
	 * @param url
	 * @param user
	 * @param pwd
	 * @param tableName
	 */
	public Table(String url, String user, String pwd, String tableName) {
		super();
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		this.tableName = tableName;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the pwd
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private Connection createConnection() throws SQLException {
//		String url="jdbc:oracle:thin:@192.168.4.131:1521/orcl";
//		String user="fm_man";
//		String password="fm_man";
		return  DriverManager.getConnection(this.url,this.user,this.pwd);
	}
	/**
	 * @param tableName2
	 * @return
	 * @throws SQLException 
	 */
	public List<Column> getTableColumns() throws SQLException {
		org.apache.commons.dbutils.QueryRunner run = new org.apache.commons.dbutils.QueryRunner();
		Connection conn = this.createConnection();
		try{
			String sql ="select * from "+this.tableName +" where 1=2";
			ResultSetHandler<List<Column>> rsh=new ResultSetHandler<List<Column>>(){

				@Override
				public List<Column> handle(ResultSet rs) throws SQLException {
					if (rs==null){
						return null;
					}
					List<Column> columns = new ArrayList<Column>();
					ResultSetMetaData metaData = rs.getMetaData();
					int colCount = metaData.getColumnCount();
					
					for (int i=1;i<colCount+1;i++){
						String colName = metaData.getColumnName(i);
						int colType = metaData.getColumnType(i);
						Column col = new Column(colName,colType);
						columns.add(col);
					}
					return columns;
				}};
				return run.query(conn , sql, rsh);
		}finally{
			if (conn!=null){conn.close();}
		}
			
	}
}
