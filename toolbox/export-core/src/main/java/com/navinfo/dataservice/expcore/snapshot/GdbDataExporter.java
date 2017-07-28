package com.navinfo.dataservice.expcore.snapshot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;
import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;
import com.fastmap.NdsSqliteEncryptor;
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
				+ "/tmp/gdbdata_une.sqlite", config.toProperties());
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

		sqliteConn.close();
		
		System.out.println("......Start......");
		//获取 NdsSqliteEncryptor 实例
		NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
		try {
			//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
			encryptor.encryptDataBase(dir + "/tmp/gdbdata_une.sqlite",dir + "/tmp/gdbdata.sqlite", PropConstant.gdbSqlitePassword);
			
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
		
		String zipfile = dir + "/" + operateDate + ".zip";
		// 压缩文件
		ZipUtils.zipFile(dir + "/tmp/", zipfile);

		FileUtil.deleteDirectory(file);
		
		return zipfile;

	}
	public static void main(String[] args) {
		/*String sql = "select a.*, display_text.name, styleFactors1.types,styleFactors2.lane_types,"
				+ "speedlimits.from_speed_limit,speedlimits.to_speed_limit,forms.forms   "
				+ "from rd_link a,"
				+ "(select a.link_pid,listagg(B.NAME,'/') within group(order by name_class,seq_num) name "
					+ "from rd_link_name a, rd_name b "
					+ "where a.name_groupid = b.name_groupid AND a.NAME_CLASS in (1,2) and b.lang_code = 'CHI' and a.u_record != 2 and a.name_type != 15 "
					+ "group by link_pid) display_text,"
					+ "(select link_pid,"
					+ "listagg(type, ',') within group(order by type) types   "
					+ "from (select a.link_pid, type from rd_link_limit a  "
						//+ "where (type in (0, 4, 5, 6, 10) or (type=2 and vehicle=2147483784)) and a.u_record != 2)  group by link_pid) styleFactors1, "
						//**********zl 2017.02.17 增加遗漏条件 "存在TYPE=2且VEHICLE=2147483786（步行者、急救车、配送卡车）且TIME_DOMAIN为空"
							+ "where (type in (0, 4, 5, 6, 10) or (type=2 and vehicle=2147483784) or (type=2 and VEHICLE=2147483786 and TIME_DOMAIN is null)) and a.u_record != 2)  group by link_pid) styleFactors1, "
						//***********************
					+ "(select link_pid, listagg(lane_type, ',') within group(order by lane_type) lane_types  from rd_lane a "
							+ "where a.u_record != 2 group by link_pid) styleFactors2,"
						+ "(select link_pid, from_speed_limit, to_speed_limit from rd_link_speedlimit a  where speed_type = 0 and a.u_record != 2) speedlimits, "
						+ "(select link_pid, listagg(form_of_way, ',') within group(order by form_of_way) forms  from rd_link_form "
							+ "where u_record != 2  group by link_pid) forms  where a.link_pid = display_text.link_pid(+)    and a.link_pid = styleFactors1.link_pid(+) "
							+ " and a.link_pid = styleFactors2.link_pid(+)    and a.link_pid = speedlimits.link_pid(+)    and a.link_pid = forms.link_pid(+) "
							+ " and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";
		
		System.out.println(sql);*/
	}
}
