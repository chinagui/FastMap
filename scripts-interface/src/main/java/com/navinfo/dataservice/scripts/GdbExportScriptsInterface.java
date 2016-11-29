package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.expcore.snapshot.GdbDataExporter;
import com.navinfo.navicommons.database.QueryRunner;

public class GdbExportScriptsInterface {

	private static Map<Integer, Map<Integer, Set<Integer>>> getProvinceMeshList(String type) throws SQLException {
	
		Connection conn = DBConnector.getInstance().getManConnection();

		QueryRunner runner = new QueryRunner();
		String sql = null;
		if("month".equals(type)){
			sql = "select a.region_id,a.monthly_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.monthly_db_id is not null order by region_id,admincode";
		}else if("day".equals(type)){
			sql = "select a.region_id,a.daily_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.daily_db_id is not null order by region_id,admincode";
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
			String path = args[0];
			
			if (!path.endsWith("/")) {
				path += "/";
			}
			String type="month";
			if(args.length==2){
				type = args[1];
			}
			

			JobScriptsInterface.initContext();

			Map<Integer, Map<Integer, Set<Integer>>> map = getProvinceMeshList(type);

			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");

			for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : map
					.entrySet()) {

				int dbId = entry.getKey();
				
				System.out.println("export dbId : " + dbId);

				Map<Integer, Set<Integer>> data = entry.getValue();

				DbInfo dbinfo = datahub.getDbById(dbId);

				DbConnectConfig connConfig = DbConnectConfig
						.createConnectConfig(dbinfo.getConnectParam());

				DataSource datasource = MultiDataSourceFactory.getInstance()
						.getDataSource(connConfig);

				Connection conn = datasource.getConnection();

				for (Map.Entry<Integer, Set<Integer>> en : data.entrySet()) {

					int admincode = en.getKey();
//					if(admincode!=420000){
//						continue;
//					}
					
					System.out.println("export admincode "+admincode+" ...");
					
					Set<Integer> meshes = en.getValue();

					String output = path + admincode / 10000;

					String filename = GdbDataExporter.run(conn, output, meshes);
					
					System.out.println("export admincode "+admincode+" success: "+filename);
				}
			}

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}

}