package com.navinfo.dataservice.expcore.snapshot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;
import com.fastmap.NdsSqliteEncryptor;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
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

		Connection sqliteConn = null;
		Statement stmt = null;
		String zipfile = null;
		try {
			File file = new File(dir + "/tmp");
	
			if (file.exists()) {
				FileUtil.deleteDirectory(file);
			}
	
			file.mkdirs();
	
			// load the sqlite-JDBC driver using the current class loader
			Class.forName("org.sqlite.JDBC");
	
			//Connection sqliteConn = null;
	
			// enabling dynamic extension loading
			// absolutely required by SpatiaLite
			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);
	
			// create a database connection
			sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
					+ "/tmp/gdbdata_une.sqlite", config.toProperties());
			stmt = sqliteConn.createStatement();
			stmt.setQueryTimeout(30); // set timeout to 30 sec.
	
			// loading SpatiaLite
			stmt.execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");
	
			// enabling Spatial Metadata
			// using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and
			// GEOMETRY_COLUMNS
			stmt.execute("SELECT InitSpatialMetadata()");
	
			sqliteConn.setAutoCommit(false);
	
			String operateDate = StringUtils.getCurrentTime();
	
			System.out.println("exporting region_index: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			RegionIndexExporter.run(sqliteConn, stmt, meshes,dir);
			
			System.out.println("exporting rdline: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			RdLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			System.out.println("exporting rdnode: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			RdNodeExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			System.out.println("exporting bkline: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			BkLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			System.out.println("exporting bkface: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			BkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			System.out.println("exporting rdlinegsc : time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			RdGscExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			System.out.println("exporting adface :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			AdFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			//********2017.09.26 zl*********
			System.out.println("exporting gdb_cmkFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbCmkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			System.out.println("exporting gdb_luFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbLuFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			sqliteConn.close();
			
			System.out.println("......Start......");
			//获取 NdsSqliteEncryptor 实例
			NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
			try {
				//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
				String gdbmm = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbSqlitePassword);
	//			System.out.println("密码: "+gdbmm);
				encryptor.encryptDataBase(dir + "/tmp/gdbdata_une.sqlite",dir + "/tmp/gdbdata.sqlite", gdbmm);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			System.out.println("....加密成功..End......");
	
			//删除原有sqlite 数据库
			File fileOld = new File(dir + "/tmp/gdbdata_une.sqlite");
			if(fileOld.exists() && fileOld.isFile()){
				fileOld.delete();
				System.out.println(" 删除未加密sqlite 数据库成功!");
			}
			
			zipfile = dir + "/" + operateDate + ".zip";
			// 压缩文件
			ZipUtils.zipFile(dir + "/tmp/", zipfile);
	
			FileUtil.deleteDirectory(file);
			
			return zipfile;
		} catch (Exception e) {
				System.err.println("导出gdb 数据报错:"+e.getMessage());	
				return zipfile;
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
