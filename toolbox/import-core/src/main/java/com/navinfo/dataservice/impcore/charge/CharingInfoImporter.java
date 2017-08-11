package com.navinfo.dataservice.impcore.charge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.exception.DataErrorException;

public class CharingInfoImporter {

	private Logger logger = LoggerRepos.getLogger(this.getClass());

	private String[] tables = new String[] { "IX_POI_CHARGINGPLOT",
			"IX_POI_CHARGINGSTATION" };

	/*
	 * private void clearDeepInfoTables(Connection conn) throws SQLException {
	 * 
	 * Statement stmt = conn.createStatement();
	 * 
	 * for (String table : tables) {
	 * 
	 * stmt.executeUpdate("TRUNCATE TABLE " + table); }
	 * 
	 * stmt.close(); }
	 */
	private void resetUrecord(Connection conn) throws SQLException {
		Statement stmt =null;
		try{
			stmt = conn.createStatement();

			for (String table : tables) {

				stmt.executeUpdate("UPDATE " + table + " SET U_RECORD=0");
			}

		}finally{
			DbUtils.closeQuietly(stmt);
		}
		
	}

	public void run(String filePath, String outPath) throws Exception {
		InputStreamReader read = null;
		BufferedReader reader =null;
		Connection conn =null;
		PreparedStatement pstmt =null;
		Statement stmt =null;
		ResultSet rs = null;
		PrintWriter pw =null;
		try{
			File file = new File(filePath);

			read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);

			String line;

			conn = DBConnector.getInstance().getMkConnection();

			conn.setAutoCommit(false);

			// clearDeepInfoTables(conn);

			String querySql = "select 1 from ix_poi where pid=?";

			pstmt = conn.prepareStatement(querySql);

			stmt = conn.createStatement();


			List<Integer> pids = new ArrayList<Integer>();

			int plotCount = 0;

			int stationCount = 0;

			int total = 0;

			int notfound = 0;

			int cache = 0;

			while ((line = reader.readLine()) != null) {

				if (total % 10000 == 0) {
					logger.info("total:" + total + ",not found:" + notfound);
				}

				total++;

				JSONObject poi = JSONObject.fromObject(line);

				int pid = poi.getInt("pid");

				pstmt.setInt(1, pid);

				rs = pstmt.executeQuery();

				if (!rs.next()) {
					notfound++;
					pids.add(pid);
					rs.close();
					continue;
				}

				rs.close();

				try {
					// plot
					int res = CharingPlotImporter.run(conn, stmt, poi);

					if (res > 0) {
						cache++;
						plotCount++;
					}
					// station
					res = CharingStationImporter.run(conn, stmt, poi);

					if (res > 0) {
						cache++;
						stationCount++;
					}

				} catch (DataErrorException ex) {
					logger.error("pid " + pid + ":" + ex.getMessage());
				}

				if (cache > 5000) {
					stmt.executeBatch();
					cache = 0;
				}
			}

			if (cache > 0) {
				stmt.executeBatch();
			}

			resetUrecord(conn);

			conn.commit();


			pw = new PrintWriter(outPath);

			for (Integer pid : pids) {
				pw.println(pid);
			}

			pw.flush();


			logger.info("total:" + total + ",not found:" + notfound);

			logger.info("IX_POI_CHARGINGPLOT  count:" + plotCount);

			logger.info("IX_POI_CHARGINGSTATION count:" + stationCount);

			logger.info("DONE.");

			
		}finally{
			try{if(read!=null) read.close();}catch(Exception e){}
			try{if(reader!=null) reader.close();}catch(Exception e){}
			try{if(rs!=null) rs.close();}catch(Exception e){}
			try{if(stmt!=null) stmt.close();}catch(Exception e){}
			try{if(pstmt!=null) pstmt.close();}catch(Exception e){}
			try{if(pw!=null) pw.close();}catch(Exception e){}
		}
		
	}
}
