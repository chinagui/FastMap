package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.expcore.snapshot.GdbDataUnzipForAdd;
import com.navinfo.navicommons.database.QueryRunner;

public class GdbUnzipForAddScriptsInterface {
	public static Map<Integer, Map<Integer, Set<Integer>>> getProvinceMeshList(String type,int regionId) throws SQLException {
		Connection conn = DBConnector.getInstance().getManConnection();

		QueryRunner runner = new QueryRunner();
		String sql = null;
		String regionSql = "";
		if(regionId>0){
			regionSql = " and a.region_id = "+regionId;
		}
		if("month".equals(type)){
			sql = "select a.region_id,a.monthly_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.monthly_db_id is not null "+regionSql+" order by region_id,admincode";
		}else if("day".equals(type)){
			sql = "select a.region_id,a.daily_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.daily_db_id is not null "+regionSql+" order by region_id,admincode";
		}
		ResultSetHandler<Map<Integer, Map<Integer, Set<Integer>>>> rsh = new ResultSetHandler<Map<Integer, Map<Integer, Set<Integer>>>>() {

			@Override
			public Map<Integer, Map<Integer, Set<Integer>>> handle(ResultSet rs)
					throws SQLException {
				if (rs != null) {
					Map<Integer, Map<Integer, Set<Integer>>> data = new HashMap<Integer, Map<Integer, Set<Integer>>>();

					while (rs.next()) {

						int dbId = rs.getInt("db_id");

						int adminCode = rs.getInt("admincode");

						int mesh = rs.getInt("mesh");

						Map<Integer, Set<Integer>> map = null;
						if (data.containsKey(dbId)) {
							map = data.get(dbId);
						} else {
							map = new HashMap<Integer, Set<Integer>>();
						}

						Set<Integer> meshes = null;
						if (map.containsKey(adminCode)) {
							meshes = map.get(adminCode);
						} else {
							meshes = new HashSet<Integer>();
						}

						meshes.add(mesh);

						map.put(adminCode, meshes);

						data.put(dbId, map);

					}
					return data;
				}
				return null;
			}
		};

		return runner.query(conn, sql, rsh);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			String zipType = args[0];
			
			String path = args[1];
			
			if (!path.endsWith("/")) {
				path += "/";
			}
			String type="month";
			if(args.length>2){
				type = args[2];
			}
			
			int regionId = 0;
			if(args.length==4){
				regionId = Integer.valueOf(args[3]);
			}
			
			System.out.println("gdb begin time :"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			
			JobScriptsInterface.initContext();

			Map<Integer, Map<Integer, Set<Integer>>> map = getProvinceMeshList(type,regionId);


			for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : map
					.entrySet()) {

				int dbId = entry.getKey();
				
				System.out.println("export dbId : " + dbId);

				Map<Integer, Set<Integer>> data = entry.getValue();


				for (Map.Entry<Integer, Set<Integer>> en : data.entrySet()) {

					int admincode = en.getKey();
					
					System.out.println("export admincode "+admincode+" ...");
					
					String output = path + admincode / 10000;
					//解压及解密
					if ("1".equals(zipType)){
//					    String sqliteFile = "/data/resources/17win/download_prep/basedata/13/gdbdata_une.sqlite";
						String sqliteFile = GdbDataUnzipForAdd.unzip(output);
						if(StringUtils.isEmpty(sqliteFile)){
							System.out.println(admincode+",没有要增量的sqlite数据库!,"+output);
							continue;
						}
					}
					if ("2".equals(zipType)){
						//加密及压缩
						String filename = GdbDataUnzipForAdd.zip(output);
						System.out.println("export admincode "+admincode+" success: "+filename);
					}
				}
			}

			System.out.println("gdb end time :"+DateUtils.dateToString(new Date(),DateUtils.DATE_DEFAULT_FORMAT));
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
}
