package com.navinfo.dataservice.impcore.deepinfo;

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

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.exception.DataErrorException;

public class DeepInfoImporter {

	private Logger logger = LoggerRepos.getLogger(this.getClass());
	
	private String[] tables = new String[]{"IX_POI_PARKING","IX_POI_GASSTATION","IX_POI_BUSINESSTIME","IX_POI_CARRENTAL","IX_POI_HOTEL","IX_POI_DETAIL","IX_POI_RESTAURANT"};

	private void clearDeepInfoTables(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();
		
		for(String table:tables){

			stmt.executeUpdate("TRUNCATE TABLE "+table);
		}

		stmt.close();
	}

	private void resetUrecord(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();
		
		for(String table:tables){

			stmt.executeUpdate("UPDATE "+table+" SET U_RECORD=0");
		}

		stmt.close();
	}

	public void run(String filePath, String outPath) throws Exception {
		File file = new File(filePath);

		InputStreamReader read = new InputStreamReader(
				new FileInputStream(file));

		BufferedReader reader = new BufferedReader(read);

		String line;

		Connection conn = DBConnector.getInstance().getMkConnection();

		conn.setAutoCommit(false);

		clearDeepInfoTables(conn);

		String querySql = "select 1 from ix_poi where pid=?";

		PreparedStatement pstmt = conn.prepareStatement(querySql);

		Statement stmt = conn.createStatement();

		ResultSet rs = null;

		List<Integer> pids = new ArrayList<Integer>();

		int parkingCount = 0;

		int gasCount = 0;
		
		int businessTimeCount = 0;
		int carRentalCount=0;
		int hotelCount=0;
		int detailCount=0;
		int restaurantCount=0;

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
				//gas
				int res = GasStationImporter.run(conn, stmt, poi);

				if (res > 0) {
					cache++;
					gasCount++;
				}
				//parking
				res = ParkingImporter.run(conn, stmt, poi);

				if (res > 0) {
					cache++;
					parkingCount++;
				}
				
				//business time
				res = BusinessTimeImporter.run(conn,stmt,poi);
				if(res>0){
					cache++;
					businessTimeCount++;
				}
				
				//car rental
				res = CarRentalImporter.run(conn,stmt,poi);
				if(res>0){
					cache++;
					carRentalCount++;
				}
				
				//hotel
				res = HotelImporter.run(conn,stmt,poi);
				if(res>0){
					cache++;
					hotelCount++;
				}
				
				//detail
				res = DetailImporter.run(conn,stmt,poi);
				if(res>0){
					cache++;
					detailCount++;
				}
				
				//restaurant
				res = RestaurantImporter.run(conn,stmt,poi);
				if(res>0){
					cache++;
					restaurantCount++;
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

		stmt.close();

		pstmt.close();

		resetUrecord(conn);

		conn.commit();

		conn.close();

		PrintWriter pw = new PrintWriter(outPath);

		for (Integer pid : pids) {
			pw.println(pid);
		}

		pw.flush();

		pw.close();

		logger.info("total:" + total + ",not found:" + notfound);

		logger.info("IX_POI_PARKING count:" + parkingCount);

		logger.info("IX_POI_GASSTATION count:" + gasCount);

		logger.info("IX_POI_BUSINESSTIME count:" + businessTimeCount);

		logger.info("IX_POI_CARRENTAL count:" + carRentalCount);

		logger.info("IX_POI_HOTEL count:" + hotelCount);

		logger.info("IX_POI_DETAIL count:" + detailCount);

		logger.info("IX_POI_RESTAURANT count:" + restaurantCount);

		logger.info("DONE.");
		
		reader.close();
	}
}
