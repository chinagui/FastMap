package com.navinfo.dataservice.impcore.flushbylog;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class FlushGdb {

	private static StringBuilder logDetailQuery = new StringBuilder(
			" where is_ck = 0 ");

	private static Connection sourceConn;

	private static Connection destConn;

	private static Properties props;

	private static List<Integer> meshes = new ArrayList<Integer>();

	private static long stopTime = 0;

	private static WKT wktUtil = new WKT();

	public static void flush(String[] args) {

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

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

			flushData();

			moveLog();

			updateLogDetailCk();

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

	private static void flushData() throws Exception {
		Statement sourceStmt = sourceConn.createStatement();

		ResultSet logrs = sourceStmt.executeQuery("select * from log_detail "
				+ logDetailQuery.toString());

		logrs.setFetchSize(1000);

		while (logrs.next()) {

			assembleDataSql(logrs);

		}
	}

	private static void moveLog() throws Exception {

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
				+ " select * from log_detail " + logDetailQuery.toString();

		stmt.executeUpdate(moveSql);

		String sqlDropDblink = "drop database link " + dbLinkName;

		stmt.execute(sqlDropDblink);
	}

	private static void updateLogDetailCk() throws Exception {

		Statement stmt = sourceConn.createStatement();

		String sql = "update log_detail set is_ck = 1 ";

		stmt.execute(sql);

		stmt.close();
	}

	private static void assembleDataSql(ResultSet rs) throws Exception {

		int op_tp = rs.getInt("op_tp");

		if (op_tp == 1) {
			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);

			StringBuilder sb = new StringBuilder("insert into ");

			sb.append(rs.getString("tb_nm"));

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

			sb.append(") ");

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

			sb.append(")");

			it = json.keys();

			tmpPos = 0;

			PreparedStatement pstmt = destConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equals(keyName)) {

					pstmt.setObject(tmpPos, valObj);
				} else {

					JGeometry jg = wktUtil.toJGeometry(valObj.toString().getBytes());
					
					jg.setSRID(8307);
					
					STRUCT s = JGeometry.store(jg, destConn);
					
					pstmt.setObject(tmpPos, s);
				}

			}
			
			pstmt.executeUpdate();

		} else if (op_tp == 3) {

			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);

			StringBuilder sb = new StringBuilder("update ");

			sb.append(rs.getString("tb_nm"));

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

			sb.append(" where row_id = hextoraw('");

			sb.append(rs.getString("tb_row_id"));

			sb.append("')");

			it = json.keys();

			tmpPos = 0;

			PreparedStatement pstmt = destConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equals(keyName)) {

					pstmt.setObject(tmpPos, valObj);
				} else {

					JGeometry jg = wktUtil.toJGeometry(valObj.toString().getBytes());
					
					jg.setSRID(8307);
					
					STRUCT s = JGeometry.store(jg, destConn);
					
					pstmt.setObject(tmpPos, s);
				}

			}
			
			pstmt.executeUpdate();
			
			pstmt.close();

		} else if (op_tp == 2) {
			String sql =  "update " + rs.getString("tb_nm")
					+ " set u_record = 2 where row_id =hextoraw('"
					+ rs.getString("tb_row_id") + "')";
			
			PreparedStatement pstmt = destConn.prepareStatement(sql);
			
			pstmt.executeUpdate();
			
			pstmt.close();
		}

	}

}
