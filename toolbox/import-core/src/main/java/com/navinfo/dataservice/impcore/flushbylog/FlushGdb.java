package com.navinfo.dataservice.impcore.flushbylog;

import java.io.FileInputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.datalock.lock.FmMesh4Lock;
import com.navinfo.dataservice.datalock.lock.MeshLockManager;
import com.navinfo.dataservice.commons.util.StringUtils;

public class FlushGdb {

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static StringBuilder logDetailQuery = new StringBuilder(
			" where com_sta = 0 ");

	private static Connection sourceConn;

	private static Connection destConn;

	private static Properties props;

	private static List<Integer> meshes = new ArrayList<Integer>();

	private static long stopTime = 0;

	private static WKT wktUtil = new WKT();

	private static List<String> logDetails = new ArrayList<String>();

	public static FlushResult copXcopyHistory(String[] args) {
		FlushResult result = new FlushResult();

		try {
			result = flush(args);

			sourceConn.commit();

			destConn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	public static FlushResult fmgdb2gdbg(String[] args) {

		FlushResult result = new FlushResult();
		try {
			result = flushNoMesh(args);

			sourceConn.commit();

			destConn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	public static FlushResult prjMeshCommit(String[] args) {

		FlushResult result = new FlushResult();
		try {
			result = flush(args);

			sourceConn.commit();

			destConn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	public static FlushResult prjMeshReturnHistory(String[] args) {

		FlushResult flushResult = new FlushResult();

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

			props = new Properties();

			props.load(new FileInputStream(args[0]));

			stopTime = Long.parseLong(props.getProperty("stopTime"));

			Scanner scanner = new Scanner(new FileInputStream(args[1]));

			while (scanner.hasNextLine()) {
				meshes.add(Integer.parseInt(scanner.nextLine()));
			}

			int userId = Integer.valueOf(args[2]);

			logDetailQuery.append(" and op_dt <= to_date('" + stopTime
					+ "','yyyymmddhh24miss')");

			int meshSize = meshes.size();

			Set<Integer> setMesh = new HashSet<Integer>();

			for (int m : meshes) {
				setMesh.add(m);
			}

			int prjId = Integer.parseInt(props.getProperty("project_id"));

			MeshLockManager man = new MeshLockManager(MultiDataSourceFactory
					.getInstance().getManDataSource());

			man.lock(prjId, userId, setMesh, FmMesh4Lock.TYPE_GIVE_BACK);

			logDetailQuery.append(" and mesh_id in (");

			for (int i = 0; i < meshSize; i++) {

				logDetailQuery.append(meshes.get(i));
				if (i < (meshSize - 1)) {
					logDetailQuery.append(",");
				}
			}

			logDetailQuery.append(") order by op_dt ");

			init();

			flushData(flushResult);

			moveLog(flushResult);

			sourceConn.commit();

			destConn.commit();

			man.unlock(prjId, setMesh, FmMesh4Lock.TYPE_GIVE_BACK);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return flushResult;

	}

	public static FlushResult flush(String[] args) {

		FlushResult flushResult = new FlushResult();

		try {

			props = new Properties();

			props.load(new FileInputStream(args[0]));

			stopTime = Long.parseLong(props.getProperty("stopTime"));

			Scanner scanner = new Scanner(new FileInputStream(args[1]));

			while (scanner.hasNextLine()) {
				meshes.add(Integer.parseInt(scanner.nextLine()));
			}

			logDetailQuery.append(" and op_dt <= to_date('" + stopTime
					+ "','yyyymmddhh24miss')");

			int meshSize = meshes.size();

			logDetailQuery.append(" and mesh_id in (");

			for (int i = 0; i < meshSize; i++) {

				logDetailQuery.append(meshes.get(i));
				if (i < (meshSize - 1)) {
					logDetailQuery.append(",");
				}
			}

			logDetailQuery.append(") order by op_dt ");

			init();

			flushData(flushResult);

			moveLog(flushResult);

			updateLogDetailCk();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return flushResult;
	}

	public static FlushResult flushNoMesh(String[] args) {

		FlushResult flushResult = new FlushResult();

		try {

			props = new Properties();

			props.load(new FileInputStream(args[0]));

			stopTime = Long.parseLong(props.getProperty("stopTime"));

			// Scanner scanner = new Scanner(new FileInputStream(args[1]));

			// while (scanner.hasNextLine()) {
			// meshes.add(Integer.parseInt(scanner.nextLine()));
			// }

			logDetailQuery.append(" and op_dt <= to_date('" + stopTime
					+ "','yyyymmddhh24miss')");

			// int meshSize = meshes.size();
			//
			// logDetailQuery.append(" and mesh_id in (");
			//
			// for (int i = 0; i < meshSize; i++) {
			//
			// logDetailQuery.append(meshes.get(i));
			// if (i < (meshSize - 1)) {
			// logDetailQuery.append(",");
			// }
			// }

			// logDetailQuery.append(") order by op_dt ");

			logDetailQuery.append(" order by op_dt ");

			init();

			flushData(flushResult);

			moveLog(flushResult);

			updateLogDetailCk();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return flushResult;

	}

	private static void init() throws SQLException {

		sourceConn = DriverManager.getConnection(
				"jdbc:oracle:thin:@" + props.getProperty("sourceDBIp") + ":"
						+ 1521 + ":" + "orcl",
				props.getProperty("sourceDBUsername"),
				props.getProperty("sourceDBPassword"));

		destConn = DriverManager.getConnection(
				"jdbc:oracle:thin:@" + props.getProperty("destDBIp") + ":"
						+ 1521 + ":" + "orcl",
				props.getProperty("destDBUsername"),
				props.getProperty("destDBPassword"));

		sourceConn.setAutoCommit(false);

		destConn.setAutoCommit(false);
	}

	private static void flushData(FlushResult flushResult) throws Exception {

		Statement sourceStmt = sourceConn.createStatement();

		ResultSet rs = sourceStmt.executeQuery("select * from log_detail "
				+ logDetailQuery.toString());

		rs.setFetchSize(1000);

		while (rs.next()) {

			flushResult.addTotal();

			int op_tp = rs.getInt("op_tp");

			String rowId = rs.getString("row_id");

			if (op_tp == 1) {// 新增

				flushResult.addInsertTotal();

				if (insertData(rs) == 0) {
					flushResult.addInsertFailed();

					flushResult.addInsertFailedRowId(rowId);
				}

			} else if (op_tp == 3) { // 修改

				flushResult.addUpdateTotal();

				if (updateData(rs) == 0) {
					flushResult.addUpdateFailed();

					flushResult.addUpdateFailedRowId(rowId);
				}

			} else if (op_tp == 2) { // 删除

				flushResult.addDeleteTotal();

				if (deleteData(rs) == 0) {
					flushResult.addDeleteFailed();

					flushResult.addDeleteFailedRowId(rowId);
				}
			}

		}

	}

	private static void moveLog(FlushResult flushResult) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");

		String dbLinkName = "dblink_" + sdf.format(new Date());

		String sqlCreateDblink = "create database link "
				+ dbLinkName
				+ "  connect to "
				+ props.getProperty("destDBUsername")
				+ " identified by "
				+ props.getProperty("destDBPassword")
				+ "  using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = "
				+ props.getProperty("destDBIp")
				+ " )(PORT = 1521 )))(CONNECT_DATA = (SERVICE_NAME = orcl )))'";

		Statement stmt = sourceConn.createStatement();

		stmt.execute(sqlCreateDblink);

		String moveSql = "insert into log_detail@" + dbLinkName
				+ " select * from log_detail where";

		Clob clob=null;
		if(logDetails.size()>1000){
			clob=sourceConn.createClob();
			clob.setString(1, StringUtils.collection2String(logDetails, ","));
			moveSql+= " row_id IN (select column_value from table(clob_to_table(?)))";
		}else{
			moveSql+= " row_id IN ("+StringUtils.collection2String(logDetails, ",")+")";
		}
		
		int logMoved = stmt.executeUpdate(moveSql);

		flushResult.setLogMoved(logMoved);

		String sqlDropDblink = "drop database link " + dbLinkName;

		stmt.execute(sqlDropDblink);

	}

	private static void updateLogDetailCk() throws Exception {

		Statement stmt = sourceConn.createStatement();

		String sql = "update log_detail set com_dt = sysdate,com_sta=1 where";
		
		Clob clob=null;
		if(logDetails.size()>1000){
			clob=sourceConn.createClob();
			clob.setString(1, StringUtils.collection2String(logDetails, ","));
			sql+= " row_id IN (select column_value from table(clob_to_table(?)))";
		}else{
			sql+= " row_id IN ("+StringUtils.collection2String(logDetails, ",")+")";
		}

		stmt.execute(sql);

		stmt.close();
	}

	private static int insertData(ResultSet rs) {

		StringBuilder sb = new StringBuilder("insert into ");

		PreparedStatement pstmt = null;

		try {
			String logRowId = rs.getString("row_id");

			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = rs.getString("tb_nm").toLowerCase();

			sb.append(tableName);

			sb.append(" (");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				if (++tmpPos < keySize) {
					sb.append(it.next());

					sb.append(",");
				} else {
					sb.append(it.next());
				}
			}

			sb.append(",u_record) ");

			sb.append("values(");

			it = json.keys();

			tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();

				sb.append(":");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",1)");

			it = json.keys();

			tmpPos = 0;

			pstmt = destConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {
					
					if(tableName.equals("ck_exception")){
						
						if("create_date".equalsIgnoreCase(keyName) || "update_date".equalsIgnoreCase(keyName))
						{
							Timestamp ts = new Timestamp( DateUtils.stringToLong(valObj.toString(), "yyyy-MM-dd HH:mm:ss"));
									
							pstmt.setTimestamp(tmpPos, ts);
						}
						else{
							pstmt.setObject(tmpPos, valObj);
						}
					}
					else{
						pstmt.setObject(tmpPos, valObj);
					}
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
	
						STRUCT s = JGeometry.store(jg, destConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}

			int result = pstmt.executeUpdate();

			logDetails.add("hextoraw('"+logRowId+"')");

			return result;
		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private static int updateData(ResultSet rs) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {
			String logRowId = rs.getString("row_id");

			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = rs.getString("tb_nm").toLowerCase();

			sb.append(tableName);

			sb.append(" set ");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();

				Object valObj = json.get(keyName);

				sb.append(keyName);

				sb.append("=:");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",u_record=3 where row_id = hextoraw('");

			sb.append(rs.getString("tb_row_id"));

			sb.append("')");

			it = json.keys();

			tmpPos = 0;

			pstmt = destConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {

					pstmt.setObject(tmpPos, valObj);
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
	
						STRUCT s = JGeometry.store(jg, destConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}

			int result = pstmt.executeUpdate();

			logDetails.add("hextoraw('"+logRowId+"')");

			return result;

		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private static int deleteData(ResultSet rs) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {

			String logRowId = rs.getString("row_id");

			String sql = "update " + rs.getString("tb_nm")
					+ " set u_record = 2 where row_id =hextoraw('"
					+ rs.getString("tb_row_id") + "')";

			pstmt = destConn.prepareStatement(sql);

			int result = pstmt.executeUpdate();

			logDetails.add("hextoraw('"+logRowId+"')");

			return result;

		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

	}

}