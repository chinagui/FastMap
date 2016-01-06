package org.navinfo.dataservice.meta;

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

import net.sf.json.JSONArray;
import oracle.sql.BLOB;

import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.commons.util.ZipUtils;

public class PatternImageExporter {

	private Connection conn;

	public PatternImageExporter(Connection conn) {
		this.conn = conn;
	}

	private void exportImage2Sqlite(Connection sqliteConn, String sql) throws Exception {

		String insertSql = "insert into meta_JVImage values("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		Statement pstmt = conn.createStatement();

		ResultSet resultSet = pstmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		int count = 0;

		while (resultSet.next()) {

			String name = resultSet.getString("file_name");

			String format = resultSet.getString("format");

			BLOB blob = (BLOB) resultSet.getBlob("file_content");

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
	}
	
	private Connection createSqlite(String dir) throws Exception{
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
	
	public String export2SqliteByNames(String path, JSONArray names) throws Exception{
		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String currentDate = sdf.format(date);
		
		String dir = path + "/" + currentDate;
		
		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();
		
		Connection sqliteConn = createSqlite(dir);
		
		String sql = "select * from sc_model_match_g where file_name in (";
		
		for(int i=0;i<names.size();i++){
			String name = names.getString(i);
			
			if(i>0){
				name+=",";
			}
			
			sql+="'"+name+"'";
		}
		
		sql+=")";
		
		exportImage2Sqlite(sqliteConn, sql);

		sqliteConn.close();

		ZipUtils.zipFile(dir, path + "/" + currentDate + ".zip");
		
		FileUtil.deleteDirectory(new File(dir));
		
		return currentDate+".zip";
	}
	
	public String export2SqliteByDate(String path, String date ) throws Exception{
		Date curdate = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String currentDate = sdf.format(curdate);
		
		String dir = path + "/" + currentDate;
		
		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();
		
		Connection sqliteConn = createSqlite(dir);
		
		String sql = "select * from sc_model_match_g where b_type in ('2D','3D') and update_time > to_date(:1,'yyyymmddhh24miss')";

		exportImage2Sqlite(sqliteConn, sql);

		sqliteConn.close();

		ZipUtils.zipFile(dir, path + "/" + currentDate + ".zip");
		
		FileUtil.deleteDirectory(new File(dir));
		
		return currentDate+".zip";
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

		String username = "mymeta3";

		String password = "mymeta3";

		String ip = "192.168.4.131";

		int port = 1521;

		String serviceName = "orcl";

		String path = "./";

		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
				+ port + ":" + serviceName, username, password);

		PatternImageExporter exporter = new PatternImageExporter(conn);
		
		//exporter.export2Sqlite(path);
		
		JSONArray a = new JSONArray();
		a.add("03311011");
		a.add("03514013");
		exporter.export2SqliteByNames(path, a);
		
		System.out.println("done");
	}
}
