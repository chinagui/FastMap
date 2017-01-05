package com.navinfo.dataservice.expcore.snapshot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;

import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;

public class GdbDataExporter {
	/**
	 * 导出道路底图到spatialite
	 * 
	 * @param oa
	 * @param dir
	 * @throws Exception
	 */
	public static String run(Connection conn, String dir, Set<Integer> meshes)
			throws Exception {

		File file = new File(dir + "/tmp");

		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}

		file.mkdirs();

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;

		// enabling dynamic extension loading
		// absolutely required by SpatiaLite
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		// create a database connection
		sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
				+ "/tmp/gdbdata.sqlite", config.toProperties());
		Statement stmt = sqliteConn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		// loading SpatiaLite
		stmt.execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");

		// enabling Spatial Metadata
		// using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and
		// GEOMETRY_COLUMNS
		stmt.execute("SELECT InitSpatialMetadata()");

		sqliteConn.setAutoCommit(false);

		String operateDate = StringUtils.getCurrentTime();

		System.out.println("exporting rdline");
		
		RdLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);

		System.out.println("exporting rdnode");

		RdNodeExporter.run(sqliteConn, stmt, conn, operateDate, meshes);

		System.out.println("exporting bkline");

		BkLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);

		System.out.println("exporting bkface");

		BkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);

		System.out.println("exporting rdlinegsc");

		RdGscExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
		
		System.out.println("exporting adface");

		AdFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);

		sqliteConn.close();

		String zipfile = dir + "/" + operateDate + ".zip";
		// 压缩文件
		ZipUtils.zipFile(dir + "/tmp/", zipfile);

		FileUtil.deleteDirectory(file);
		
		return zipfile;

	}
}
