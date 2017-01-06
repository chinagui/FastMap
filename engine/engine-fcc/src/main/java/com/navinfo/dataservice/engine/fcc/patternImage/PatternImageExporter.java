package com.navinfo.dataservice.engine.fcc.patternImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import oracle.sql.BLOB;

import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ZipUtils;

public class PatternImageExporter {

	private void exportImage2Sqlite(Connection sqliteConn, String sql)
			throws Exception {

		String insertSql = "insert into meta_JVImage values("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Statement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

			pstmt = conn.createStatement();

			resultSet = pstmt.executeQuery(sql);

			resultSet.setFetchSize(5000);

			int count = 0;

			while (resultSet.next()) {

				String name = resultSet.getString("file_name");

				String format = resultSet.getString("format");

				BLOB blob = (BLOB) resultSet.getBlob("file_content");

				if(blob == null){
					continue;
				}
				
				InputStream is = blob.getBinaryStream();
				int length = (int) blob.length();
				byte[] content = new byte[length];
				is.read(content);
				is.close();

				String bType = resultSet.getString("b_type");

				String mType = resultSet.getString("m_type");

				prep.setString(1, name);

				prep.setString(2, format);

				prep.setBinaryStream(3, new ByteArrayInputStream(content),
						content.length);

				prep.setString(4, bType);

				prep.setString(5, mType);

				prep.setInt(6, 0);

				prep.setString(7, "");

				prep.setString(8, "");

				prep.setString(9, "");

				prep.setInt(10, 0);

				prep.executeUpdate();

				count += 1;

				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}

			sqliteConn.commit();
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
	}

	private Connection createSqlite(String dir) throws Exception {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;

		// enabling dynamic extension loading
		// absolutely required by SpatiaLite
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		// create a database connection
		sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
				+ "/image.sqlite", config.toProperties());
		Statement stmt = sqliteConn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		sqliteConn.setAutoCommit(false);

		stmt.execute("create table meta_JVImage(name text, format text, content Blob, bType text, mType text, userId integer, operateDate text, uploadDate text, downloadDate text, status integer)");

		return sqliteConn;
	}

	public void export2SqliteByNames(String dir, Set<String> names)
			throws Exception {

		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();

		Connection sqliteConn = createSqlite(dir);

		String sql = "select * from sc_model_match_g where file_name in (";

		int i=0;
		for(String name:names){
			if (i > 0) {
				sql += ",";
			}

			sql += "'" + name + "'";
			
			i++;
		}

		sql += ")";

		exportImage2Sqlite(sqliteConn, sql);

		sqliteConn.close();
	}
	
	public String export2SqliteByDate(String path, String date)
			throws Exception {
		Date curdate = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String currentDate = sdf.format(curdate);

		String dir = path + "/" + currentDate;

		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();

		Connection sqliteConn = createSqlite(dir);

		String sql = "select * from sc_model_match_g where b_type in ('2D','3D') and update_time > to_date('"
				+ date + "','yyyymmddhh24miss')";

		exportImage2Sqlite(sqliteConn, sql);

		sqliteConn.close();

		ZipUtils.zipFile(dir, path + "/" + currentDate + ".zip");

		FileUtil.deleteDirectory(new File(dir));

		return currentDate + ".zip";
	}

	public void export2Sqlite(String path) throws Exception {

		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String currentDate = sdf.format(date);

		String dir = path + "/" + currentDate;

		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();

		Connection sqliteConn = createSqlite(dir);

		String sql = "select * from sc_model_match_g where b_type in ('2D','3D')";

		exportImage2Sqlite(sqliteConn, sql);

		sqliteConn.close();

		ZipUtils.zipFile(dir, path + "/" + currentDate + ".zip");

		FileUtil.deleteDirectory(new File(dir));
	}

	public static void main(String[] args) throws Exception {

		String path = "./";

		PatternImageExporter exporter = new PatternImageExporter();

		// exporter.export2Sqlite(path);

//		JSONArray a = new JSONArray();
//		a.add("03311011");
//		a.add("03514013");
//		exporter.export2SqliteByNames(path, a);
//
//		System.out.println("done");
	}
}
