package com.navinfo.dataservice.expcore.snapshot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;

/**
 * 
 * @ClassName GdbDataExporterForAdd
 * @author Han Shaoming
 * @date 2017年10月16日 下午4:41:09
 * @Description TODO
 */
public class GdbDataExporterForAdd {
	/**
	 * 导出道路底图到spatialite
	 * 
	 * @param oa
	 * @param dir
	 * @throws Exception
	 */
	public static void run(Connection conn, String dir, Set<Integer> meshes)
			throws Exception {

		Connection sqliteConn = null;
		Statement stmt = null;
		try {
			//判断相应省份的文件是否存在
			File provinceFile = new File(dir);
			if (!provinceFile.exists() || provinceFile.listFiles().length < 1) {
				return;
			}else{
				boolean flag =false;
				for (File file : provinceFile.listFiles()) {
					if(file.isFile() && file.getName().endsWith(".zip")){flag = true;}
				}
				if(!flag){return;}
			}
			//判断是否有文件
			String localUnzipDir = dir+File.separator+"tmp";
			String sqliteFile = null;
			File unzipFile = new File(localUnzipDir);
			if(unzipFile.exists() && unzipFile.listFiles().length >0){
				sqliteFile = localUnzipDir+File.separator+unzipFile.listFiles()[0].getName();
			}
			if(StringUtils.isEmpty(sqliteFile)){return;}
			
			// load the sqlite-JDBC driver using the current class loader
			Class.forName("org.sqlite.JDBC");
			//Connection sqliteConn = null;
	
			// enabling dynamic extension loading
			// absolutely required by SpatiaLite
			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);
	
			// create a database connection
			sqliteConn = DriverManager.getConnection("jdbc:sqlite:"+ dir+ "/tmp/gdbdata_une.sqlite", config.toProperties());
			sqliteConn.setAutoCommit(false);
			stmt = sqliteConn.createStatement();
			stmt.setQueryTimeout(30); // set timeout to 30 sec.
	
			// loading SpatiaLite
			System.out.println("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");
			stmt.execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");
			sqliteConn.commit();
//			NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
			// enabling Spatial Metadata
			// using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and
			// GEOMETRY_COLUMNS
//			stmt.execute("SELECT InitSpatialMetadata()");
	
	
			String operateDate = StringUtils.getCurrentTime();
			
			stmt.execute("DELETE FROM gdb_bkFace WHERE kind=106;");
			sqliteConn.commit();
			System.out.println("删除gdb_bkFace中的数据成功!");
			
			//********2017.09.26 zl*********
			System.out.println("exporting gdb_cmkFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbCmkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			System.out.println("exporting gdb_luFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbLuFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			sqliteConn.close();
			
		} catch (Exception e) {
			if(sqliteConn != null){
				DbUtils.rollbackAndCloseQuietly(sqliteConn);
			}
			System.err.println("导出gdb 数据报错:"+e);	
		}finally {
			if(stmt != null ){
				DbUtils.closeQuietly(stmt);
			}	
			if(sqliteConn != null){
				DbUtils.closeQuietly(sqliteConn);
			}
		}
	}
	
}
