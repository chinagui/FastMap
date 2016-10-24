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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.impcore.exception.DataErrorException;

public class DeepInfoImporter {

	private Logger logger = LoggerRepos.getLogger(this.getClass());

	private void clearDeepInfoTables(Connection conn) throws SQLException {

		String parkingSql = "truncate table ix_poi_parking";

		String gasstationSql = "truncate table ix_poi_gasstation";

		Statement stmt = conn.createStatement();

		stmt.executeUpdate(parkingSql);

		stmt.executeUpdate(gasstationSql);

		stmt.close();
	}

	private void resetUrecord(Connection conn) throws SQLException {

		String parkingSql = "update ix_poi_parking set u_record=0";

		String gasstationSql = "update ix_poi_gasstation set u_record=0";

		Statement stmt = conn.createStatement();

		stmt.executeUpdate(parkingSql);

		stmt.executeUpdate(gasstationSql);

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

		Result result = new Result();

		List<Integer> pids = new ArrayList<Integer>();

		int parkingCount = 0;

		int gasCount = 0;

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
				int res = GasStationImporter.run(result, conn, stmt, poi);

				if (res > 0) {
					cache++;
					gasCount++;
				}
				//parking
				res = ParkingImporter.run(result, conn, stmt, poi);

				if (res > 0) {
					cache++;
					parkingCount++;
				}
				//

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

		logger.info("not found pid count:" + notfound);

		logger.info("IX_POI_Parking count:" + parkingCount);

		logger.info("IX_POI_GASSTATION count:" + gasCount);

		logger.info("DONE.");
	}
}
