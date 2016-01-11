package com.navinfo.dataservice.impcore.flushbylog;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import net.sf.json.JSONObject;

public class FlushGdb {

	private static StringBuilder logDetailQuery = new StringBuilder(" where is_ck = 0 ");
	
	private static Connection sourceConn;
	
	private static Connection destConn;
	
	private static Properties props;
	
	private static List<Integer> meshes = new ArrayList<Integer>();
	
	private static long stopTime = 0;
	
	public static void main(String[] args) {
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			props = new Properties();
			
			stopTime = Long.parseLong(props.getProperty("stopTime"));
			
			props.load(new FileInputStream(args[0]));
			
			Scanner scanner = new Scanner(new FileInputStream(args[1]));
			
			while(scanner.hasNextLine()){
				meshes.add(Integer.parseInt(scanner.nextLine()));
			}
			
			logDetailQuery.append(" and op_dt <= to_date('"+stopTime+"','yyyymmddhh24miss')");
			
			int meshSize = meshes.size();
			
			logDetailQuery.append(" and mesh_id in (");
			
			for(int i=0;i<meshSize;i++){
				
				logDetailQuery.append(meshes.get(i));
				if (i < (meshSize - 1)){
					logDetailQuery.append(",");
				}
			}
			
			logDetailQuery.append(")");
			
			init();
			
			flushData();
			
			moveLog();
			
			updateLogDetailCk();
			
			sourceConn.commit();
			
			destConn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			try{
				sourceConn.rollback();
				
				destConn.rollback();
			}catch(Exception e1){
				e1.printStackTrace();
			}
		}
		
	}
	
	private static void init() throws SQLException{
		
		sourceConn = DriverManager.getConnection("", props.getProperty("sourceDBUsername"), props.getProperty("sourceDBPassword"));
		
		destConn = DriverManager.getConnection("", props.getProperty("destDBUsername"), props.getProperty("destDBPassword"));
		
		sourceConn.setAutoCommit(false);
		
		destConn.setAutoCommit(false);
	}
	
	private static void flushData() throws Exception
	{
		Statement sourceStmt = sourceConn.createStatement();
		
		Statement destStmt = destConn.createStatement();
		
		ResultSet logrs = sourceStmt.executeQuery("select * from log_detail "+logDetailQuery.toString());
		
		logrs.setFetchSize(1000);
		
		List<String> listSql = new ArrayList<String>();
		
		int num = 0;
		
		while(logrs.next()){
			
			listSql.add(assembleDataSql(logrs));
			
			num++;
			
			if (num > 1000){
				
				for(String s : listSql){
					destStmt.addBatch(s);
				}
				
				destStmt.executeBatch();
				
				listSql.clear();
				
				num = 0;
			}
			
		}
		
		for(String s : listSql){
			destStmt.addBatch(s);
		}
		
		destStmt.executeBatch();
		
		listSql.clear();
	}
	
	
	
	private static void moveLog() throws Exception{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
		
		String dbLinkName = "dblink_"+sdf.format(new Date());
		
		String sqlCreateDblink = "";
		
		Statement stmt = sourceConn.createStatement();
		
		stmt.execute(sqlCreateDblink);
		
		String moveSql = "insert into log_detail@"+dbLinkName+" select * from log_detail "+logDetailQuery.toString();
		
		stmt.executeUpdate(moveSql);
		
		String sqlDropDblink = "";
		
		stmt.execute(sqlDropDblink);
	}
	
	private static void updateLogDetailCk() throws Exception{
		
		Statement stmt = sourceConn.createStatement();
		
		String sql = "update log_detail set is_ck = 0 ";
		
		stmt.execute(sql);
		
		stmt.close();
	}
	
	private static String assembleDataSql(ResultSet rs) throws Exception{
		
		int op_tp = rs.getInt("op_tp");
		
		if (op_tp == 1){
			String newValue = rs.getString("new");
			
			JSONObject json = JSONObject.fromObject(newValue);
			
			StringBuilder sb = new StringBuilder("insert into ");
			
			sb.append(rs.getString("tb_nm"));
			
			sb.append(" (");
			
			Iterator<String> it = json.keys();
			
			int keySize = json.keySet().size();
			
			int tmpPos = 0;
			
			while(it.hasNext()){
				if (++tmpPos < keySize){
					sb.append(it.next());
					
					sb.append(",");
				}else{
					sb.append(it.next());
				}
			}
			
			sb.append(") ");
			
			sb.append("values(");
			
			it = json.keys();
			
			tmpPos = 0;
			
			while(it.hasNext()){
				String keyName = it.next();
				
				Object valObj = json.get(keyName);
				
				if (valObj instanceof String){
					
					if (!"geometry".equals(keyName)){
						sb.append("'");
						
						sb.append(valObj.toString());
						
						sb.append("'");
					}else{
						sb.append("sdo_geometry('");
						
						sb.append(valObj.toString());
						
						sb.append("',8307)");
					}
					
				}else{
					sb.append(valObj);
				}
				
				if (++tmpPos < keySize){
					sb.append(it.next());
					
					sb.append(",");
				}
			}
			
			sb.append(")");
			
			return sb.toString();
		}else if (op_tp == 2){

			String newValue = rs.getString("new");
			
			JSONObject json = JSONObject.fromObject(newValue);
			
			StringBuilder sb = new StringBuilder("update ");
			
			sb.append(rs.getString("tb_nm"));
			
			sb.append(" set ");
			
			Iterator<String> it = json.keys();
			
			int keySize = json.keySet().size();
			
			int tmpPos = 0;
			
			while(it.hasNext()){
				String keyName = it.next();
				
				Object valObj = json.get(keyName);
				
				sb.append(keyName);
				
				sb.append("=");
				
				if (valObj instanceof String){
					
					if (!"geometry".equals(keyName)){
						
						sb.append("'");
						
						sb.append(valObj.toString());
						
						sb.append("'");
					}else{
						sb.append("sdo_geometry('");
						
						sb.append(valObj.toString());
						
						sb.append("',8307)");
					}
					
				}else{
					sb.append(valObj);
				}
				
				if (++tmpPos < keySize){
					sb.append(it.next());
					
					sb.append(",");
				}
			}
			
			sb.append(")");
			
			return sb.toString();
		
		}else if (op_tp == 3){
			return "update "+ rs.getString("tb_nm") +" set u_record = 3 where row_id ="+ rs.getString("row_id");
		}
		
		return null;
	}

}
