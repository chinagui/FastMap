package com.navinfo.dataservice.FosEngine.patternimg;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Date;
import java.util.Scanner;

import net.sf.json.JSONObject;

public class UploadPatternImage {
	private Connection conn;

	public UploadPatternImage(Connection conn) {
		this.conn = conn;
	}

	public void run(String fileName) throws Exception {
		
		Date currentDate = new Date();
		
		String sql = "insert into SC_MODEL_MATCH_G values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		int counter=0;
		
		Scanner scanner = new Scanner(new FileInputStream(fileName));
		
		while (scanner.hasNextLine()) {
			
			String line = scanner.nextLine();

			JSONObject json = JSONObject.fromObject(line);

			pstmt.setNull(1, Types.INTEGER);

			pstmt.setString(2, "");

			pstmt.setString(3, "");

			pstmt.setString(4, "");

			pstmt.setString(5, "");

			pstmt.setString(6, json.getString("bType"));

			pstmt.setString(7, json.getString("mType"));

			pstmt.setString(8, "");

			pstmt.setString(9, json.getString("name"));

			pstmt.setString(10, "");

			pstmt.setString(11, json.getString("format"));

			pstmt.setString(12, "");

			pstmt.setDate(13, (java.sql.Date)currentDate);

			pstmt.setString(14, "");

			pstmt.setString(15, "");
			
			pstmt.setString(16, "");
			
			ByteArrayInputStream stream = new ByteArrayInputStream(json.getString("content").getBytes());
			
			pstmt.setBlob(17, stream);
			
			pstmt.setInt(18, 0);
			
			pstmt.setDate(19, (java.sql.Date)currentDate);
			
			pstmt.executeUpdate();
			
			counter++;

			if (counter % 100 == 0) {
				conn.commit();
			}

		}
		
		conn.commit();

		conn.close();

	}
}
