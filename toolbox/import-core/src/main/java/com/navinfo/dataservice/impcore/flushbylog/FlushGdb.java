package com.navinfo.dataservice.impcore.flushbylog;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import oracle.spatial.util.WKT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.commit.AllFeatureLogFlusher;

public class FlushGdb {

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static StringBuilder logDetailQuery = new StringBuilder(
			"SELECT * FROM LOG_DETAIL where com_sta = 0 ");

	private static Connection sourceConn;

	private static Connection destConn;

	private static Properties props;

	private static long stopTime = 0;

	private static WKT wktUtil = new WKT();

	private static List<String> logDetails = new ArrayList<String>();

	/**
	 * 批处理结果入项目库
	 * @param args
	 * @return
	 */
	public static FlushResult copXcopyHistory(String[] args) {
		return flushByGrids(args);
	}

	public static FlushResult fmgdb2gdbg(String[] args) {

		return flushAll(args);
	}

	/**
	 * 履历刷库，传入截止时间和grids参数
	 * @param args
	 * @return
	 */
	public static FlushResult prjMeshCommit(String[] args) {

		return flushByGrids(args);

	}

	/**
	 * 项目还履历
	 * @param args
	 * @return
	 */
	public static FlushResult prjMeshReturnHistory(String[] args) {

		FlushResult flushResult = new FlushResult();
		Scanner scanner = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

			props = new Properties();

			props.load(new FileInputStream(args[0]));

			stopTime = Long.parseLong(props.getProperty("stopTime"));

			scanner = new Scanner(new FileInputStream(args[1]));

			List<Integer> meshes = new ArrayList<Integer>();
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
			
			DatalockApi datalock = (DatalockApi)ApplicationContextUtil.getBean("datalockApi");
//			datalock.lock(prjId, userId, setMesh, FmEditLock.TYPE_GIVE_BACK);

			logDetailQuery.append(" and mesh_id in (");

			for (int i = 0; i < meshSize; i++) {

				logDetailQuery.append(meshes.get(i));
				if (i < (meshSize - 1)) {
					logDetailQuery.append(",");
				}
			}

			logDetailQuery.append(") order by op_dt ");

			init();

			flushData(flushResult,"SELECT * FROM LOG_DETAIL "+logDetailQuery.toString());

			//moveLog(flushResult);

			sourceConn.commit();

			destConn.commit();

//			datalock.unlock(prjId, setMesh, FmEditLock.TYPE_GIVE_BACK);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				sourceConn.rollback();

				destConn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}finally{
			if(scanner!=null){
				scanner.close();
			}
		}

		return flushResult;

	}
	
	

	public static FlushResult flushByGrids(String[] args) {
		try {
			String configPropFile = args[0];
			String gridFile = args[1];
			props = new Properties();
			props.load(new FileInputStream(configPropFile));			
			DbInfo srcDbInfo = genDbInfo(
					props.getProperty("sourceDBUsername"),
					props.getProperty("sourceDBPassword"),
					props.getProperty("sourceDBIp")
					);
			DbInfo targetDbInfo = genDbInfo(
					props.getProperty("destDBUsername"),
					props.getProperty("destDBPassword"),
					props.getProperty("destDBIp")
					);
			LogFlusher flusher = new AllFeatureLogFlusher(
					Integer.valueOf(props.getProperty("regionId")).intValue(),
					srcDbInfo, 
					targetDbInfo, 
					extraceGridFromInFile(gridFile), 
					props.getProperty("stopTime"));
			return flusher.perform();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * @param args 
	 * 第一个参数：properties文件所在的路径。properties文件规格如下
	 * sourceDBUsername=
	 * sourceDBPassword=
	 * sourceDBIp=
	 * destDBUsername=
	 * destDBPassword=
	 * destDBIp=
	 * regionId=
	 * stopTime= 格式为yyyymmddhhMMss 
	 * @return
	 */
	public static FlushResult flushAll(String[] args) {
		try {
			String configPropFile = args[0];
			props = new Properties();
			props.load(new FileInputStream(configPropFile));			
			DbInfo srcDbInfo = genDbInfo(
					props.getProperty("sourceDBUsername"),
					props.getProperty("sourceDBPassword"),
					props.getProperty("sourceDBIp")
					);
			DbInfo targetDbInfo = genDbInfo(
					props.getProperty("destDBUsername"),
					props.getProperty("destDBPassword"),
					props.getProperty("destDBIp")
					);
			LogFlusher flusher = new AllFeatureLogFlusher(
					Integer.valueOf(props.getProperty("regionId")).intValue(),
					srcDbInfo, 
					targetDbInfo, 
					null, 
					props.getProperty("stopTime")
					);
			return flusher.perform();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	private static List<Integer> extraceGridFromInFile(String inFile) throws Exception {
		if (StringUtils.isEmpty(inFile)){
			return null;
		}
		Scanner scanner = null;
		FileInputStream gridFile =null; 
		try{
			gridFile = new FileInputStream(inFile);		
			List<Integer> grids = new ArrayList<Integer>();
			scanner = new Scanner(gridFile);
			while (scanner.hasNextLine()) {
				grids.add(Integer.parseInt(scanner.nextLine()));
			}
			return grids;
		}finally{
			org.apache.commons.io.IOUtils.closeQuietly(gridFile);
			if(scanner!=null)scanner.close();
		}
	}

	private static DbInfo genDbInfo(String dbUserName,String dbPwd,String ip) {
		DbInfo srcDbInfo = new DbInfo();
		srcDbInfo.setDbUserName(dbUserName);
		srcDbInfo.setDbUserPasswd(dbPwd);
		DbServer dbServer =new DbServer();
		dbServer.setIp(ip);
		dbServer.setPort(1521);
		srcDbInfo.setDbName("orcl");
		srcDbInfo.setDbServer(dbServer );
		return srcDbInfo;
	}
	private static boolean flushData(FlushResult flushResult,String logQuerySql) throws Exception {
		Statement sourceStmt =null;
		ResultSet rs =null;
		try{
			sourceStmt = sourceConn.createStatement();

			rs = sourceStmt.executeQuery(logQuerySql);

			rs.setFetchSize(1000);
			LogWriter logWriter = new LogWriter(destConn);
			while (rs.next()) {

				flushResult.addTotal();

				int opType = rs.getInt("op_tp");
				String rowId = rs.getString("row_id");
				String opId = rs.getString("op_id");
				String newValue = rs.getString("new");
				String tableName = rs.getString("tb_nm");
				String tableRowId = rs.getString("tb_row_id");

				EditLog editLog = new EditLog(opType, rowId, opId, rowId,newValue, tableName, tableRowId);
				ILogWriteListener listener = new LogWriteListener(flushResult);
				logWriter.write(editLog , listener );

			}
			if(flushResult.getFailedTotal()>0){
				return false;
			}
			return true;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(sourceStmt);
		}
		
	}


	

	

}