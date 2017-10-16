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
	public static String run(Connection conn, String dir, Set<Integer> meshes)
			throws Exception {

		Connection sqliteConn = null;
		Statement stmt = null;
		String zipfile = null;
		try {
			//获取 NdsSqliteEncryptor 实例
			NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
			//判断相应省份的文件是否存在
			File provinceFile = new File(dir);
			if (!provinceFile.exists() || provinceFile.listFiles().length ==0) {
				return null;
			}
			//查询最新的压缩文件
			String localZipFile = getLastestInfo(dir,null);
			System.out.println("最新文件所在目录:"+localZipFile);
			//解压
			String localUnzipDir = dir+File.separator+"/tmp";
			ZipUtils.unzipFile(localZipFile,localUnzipDir);
			String sqliteFile = localUnzipDir+File.separator+"gdbdata.sqlite";
			System.out.println("更新文件所在目录:"+sqliteFile);
			//解密
			System.out.println("......Start...解密...");
			try {
				//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
				String gdbmm = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbSqlitePassword);
				encryptor.decryptDataBase(sqliteFile ,localUnzipDir+"/gdbdata_une.sqlite", gdbmm);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("....解密成功..End......");
			//删除原有sqlite 数据库
			File sqliteOld = new File(sqliteFile);
			if(sqliteOld.exists() && sqliteOld.isFile()){
				sqliteOld.delete();
				System.out.println(" 删除未加密sqlite 数据库成功!");
			}
			// load the sqlite-JDBC driver using the current class loader
			Class.forName("org.sqlite.JDBC");
	
			//Connection sqliteConn = null;
	
			// enabling dynamic extension loading
			// absolutely required by SpatiaLite
			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);
	
			// create a database connection
			sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + localUnzipDir+"/gdbdata_une.sqlite", config.toProperties());
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
	
//			System.out.println("exporting region_index: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//			
//			RegionIndexExporter.run(sqliteConn, stmt, meshes,dir);
//			
//			System.out.println("exporting rdline: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//			
//			RdLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
//	
//			System.out.println("exporting rdnode: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//	
//			RdNodeExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
//	
//			System.out.println("exporting bkline: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//	
//			BkLinkExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
//	
//			System.out.println("exporting bkface: time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//	
//			BkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
//	
//			System.out.println("exporting rdlinegsc : time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
//	
//			RdGscExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
//			
			System.out.println("exporting adface :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
	
			AdFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			//********2017.09.26 zl*********
			System.out.println("exporting gdb_cmkFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbCmkFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
			
			System.out.println("exporting gdb_luFace :time:"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			GdbLuFaceExporter.run(sqliteConn, stmt, conn, operateDate, meshes);
	
			sqliteConn.close();
			
			System.out.println("......Start......");
			
//			try {
//				//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
//				String gdbmm = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbSqlitePassword);
//	//			System.out.println("密码: "+gdbmm);
//				encryptor.encryptDataBase(dir + "/tmp/gdbdata_une.sqlite",dir + "/tmp/gdbdata.sqlite", gdbmm);
//				
//			} catch (Exception e) {
//				
//				e.printStackTrace();
//			}
			System.out.println("....加密成功..End......");
	
			//删除原有sqlite 数据库
//			File fileOld = new File(dir + "/tmp/gdbdata_une.sqlite");
//			if(fileOld.exists() && fileOld.isFile()){
//				fileOld.delete();
//				System.out.println(" 删除未加密sqlite 数据库成功!");
//			}
			
			zipfile = dir + "/" + operateDate + ".zip";
			// 压缩文件
			ZipUtils.zipFile(dir + "/tmp/", zipfile);
	
			FileUtil.deleteDirectory(new File(localUnzipDir));
			
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
	
	private static String getLastestInfo(String dir, String subdir) throws Exception{
		
		File file = new File(dir);
		
		File[] files = file.listFiles();
		
		long version = 0;
		
		for(File f:files){
			
			if(!f.isFile()){
				continue;
			}
			
			String name = f.getName();
			
			int index= name.indexOf(".");
			
			if(index==-1){
				continue;
			}
			
			long tmpVersion = Long.parseLong(name.substring(0, index));
			
			if (tmpVersion > version){
				version = tmpVersion;
			}
		}
		
		if(subdir != null){
			return dir+File.separator+subdir+File.separator+version+".zip";
		}
		else{
			return dir+File.separator+version+".zip";
		}
		
	}
}
