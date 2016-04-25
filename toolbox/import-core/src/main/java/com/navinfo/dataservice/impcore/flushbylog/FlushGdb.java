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
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.datalock.exception.LockException;
import com.navinfo.dataservice.datalock.lock.FmMesh4Lock;
import com.navinfo.dataservice.datalock.lock.MeshLockManager;
import com.navinfo.navicommons.database.QueryRunner;
//import com.navinfo.dataservice.commons.util.StringUtils;

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

	private static List<Integer> grids = new ArrayList<Integer>();

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
				grids.add(Integer.parseInt(scanner.nextLine()));
			}

			int userId = Integer.valueOf(args[2]);

			logDetailQuery.append(" and op_dt <= to_date('" + stopTime
					+ "','yyyymmddhh24miss')");

			int meshSize = grids.size();

			Set<Integer> setMesh = new HashSet<Integer>();

			for (int m : grids) {
				setMesh.add(m);
			}

			int prjId = Integer.parseInt(props.getProperty("project_id"));

			MeshLockManager man = new MeshLockManager(MultiDataSourceFactory
					.getInstance().getManDataSource());

			man.lock(prjId, userId, setMesh, FmMesh4Lock.TYPE_GIVE_BACK);

			logDetailQuery.append(" and mesh_id in (");

			for (int i = 0; i < meshSize; i++) {

				logDetailQuery.append(grids.get(i));
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
	private static String createTempTable()throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		String tempTable = "TEMP_LOG_OP_"+new Random().nextInt(1000000);
		sb.append("CREATE TABLE ");
		sb.append(tempTable);
		sb.append("(OP_ID VARCHAR2(32))");
		run.execute(sourceConn, sb.toString());
		return tempTable;
	}
	private static void dropTempTable(String tempTable){
		//
	}
	private static int prepareLog(String stopTime,List<Integer> grids,String tempTable)throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT T.OP_ID FROM LOG_DETAIL T,LOG_DETAIL_GRID P WHERE T.ROW_ID=P.ROW_ID AND COM_STA = 0 AND T.OP_DT<=TO_DATE('");
		sb.append(stopTime+ "','yyyymmddhh24miss') AND P.GRID_ID IN (");
		sb.append(StringUtils.join(grids, ","));
		sb.append(")");
		return run.update(sourceConn, sb.toString());
	}
	private static int extendLogByRowId(String tempTable)throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(tempTable);
		sb.append(" T USING (SELECT L.OP_ID FROM LOG_DETAIL L WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID)) P ON (T.OP_ID=P.OP_ID) WHEN NOT MATCHED THEN INSERT VALUES (P.OP_ID)");
		int result = run.update(sourceConn, sb.toString());
		if(result>0){
			return result+extendLogByRowId(tempTable);
		}
		return result;
	}
	private static void lockPreparedLog(String tempTable,int rowCount)throws LockException,SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STATUS=1 WHERE EXISTS (SELECT 1 FROM ");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.LOCK_STATUS=0");
		int result = run.update(sourceConn, sb.toString());
		if(result<rowCount){
			throw new LockException("部分履历已经被其他回库操作锁定,请稍候再试。");
		}
	}
	private static void unlockPreparedLog(String tempTable){
		try{
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STATUS=0 WHERE EXISTS (SELECT 1 FROM ");
			sb.append(tempTable);
			sb.append(" T WHERE L.OP_ID=T.OP_ID)");
			run.update(sourceConn, sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static String prepareAndLockLog(String tempTable,String stopTime,List<Integer> grids)throws LockException{
		//
		//Set<String> logOp
		try{
			int logOperationCount = 0;
			//2.select by conditions
			logOperationCount+=prepareLog(stopTime,grids,tempTable);
			//3.
			logOperationCount+=extendLogByRowId(tempTable);
			lockPreparedLog(tempTable,logOperationCount);
			sourceConn.commit();
			return tempTable;
		}catch(LockException e){
			throw e;
		}
		catch(SQLException e){
			throw new LockException("锁定要准备回库的履历时出现错误:"+e.getMessage(),e);
		}
	}

	public static FlushResult flush(String[] args) {

		FlushResult flushResult = new FlushResult();
		String tempTable = null;

		try {

			props = new Properties();

			props.load(new FileInputStream(args[0]));

			String stopTime = props.getProperty("stopTime");

			Scanner scanner = new Scanner(new FileInputStream(args[1]));

			while (scanner.hasNextLine()) {
				grids.add(Integer.parseInt(scanner.nextLine()));
			}

			tempTable = createTempTable();
			prepareAndLockLog(tempTable,stopTime,grids);

			logDetailQuery.append(" and op_dt <= to_date('" + stopTime
					+ "','yyyymmddhh24miss')");

			int meshSize = grids.size();

			logDetailQuery.append(" and mesh_id in (");

			for (int i = 0; i < meshSize; i++) {

				logDetailQuery.append(grids.get(i));
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
			if(StringUtils.isNotEmpty(tempTable)){
				unlockPreparedLog(tempTable);
			}
		}finally{
			dropTempTable(tempTable);
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

	private static void flushData(FlushResult flushResult,String logQuerySql) throws Exception {

		Statement sourceStmt = sourceConn.createStatement();

		ResultSet rs = sourceStmt.executeQuery(logQuerySql);

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
			clob.setString(1, StringUtils.join(logDetails, ","));
			moveSql+= " row_id IN (select column_value from table(clob_to_table(?)))";
		}else{
			moveSql+= " row_id IN ("+StringUtils.join(logDetails, ",")+")";
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
			clob.setString(1, StringUtils.join(logDetails, ","));
			sql+= " row_id IN (select column_value from table(clob_to_table(?)))";
		}else{
			sql+= " row_id IN ("+StringUtils.join(logDetails, ",")+")";
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