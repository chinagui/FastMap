package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class InitializeMKPoiFlag {
	private static Logger log = LoggerRepos.getLogger(InitializeMKPoiFlag.class);
	private static QueryRunner run = new QueryRunner();

	public static void main(String[] args) throws Exception {
		try {
			// String path = args[0];

			String path = "C:/Users/fhx/Desktop/110000_poi_info.txt";
			JobScriptsInterface.initContext();

			getHBaseDataInfo(path, "not_find_pid.txt");
		} catch (Exception e) {
			log.info("Oops,something is wrong....");

			e.printStackTrace();
		}
	}

	private static void getHBaseDataInfo(String filepath, String outpath) throws Exception {

		HbasePoiInfo hbaseInfo = new HbasePoiInfo();

		InputStreamReader read = new InputStreamReader(new FileInputStream(filepath));

		BufferedReader reader = new BufferedReader(read);

		String line;

		int cache = 0;

		PrintWriter pw = new PrintWriter(outpath);

		log.info("start reading poi_info.txt...");

		List<PoiInfo> poiInfoes = new ArrayList<>();

		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMkConnection();

			createTable(conn, hbaseInfo);

			while ((line = reader.readLine()) != null) {
				JSONObject poiObj = JSONObject.fromObject(line);

				PoiInfo poiInfo = hbaseInfo.resolveResultInfo(poiObj);

				poiInfoes.add(poiInfo);

				cache++;

				if (cache > 30000) {
					insertDataIntoPoiFlag(conn, poiInfoes, hbaseInfo);

					for (Integer pid : hbaseInfo.notFindPoi) {
						pw.println(pid);
					}

					cache = 0;
					hbaseInfo.notFindPoi.clear();
					poiInfoes.clear();
				}
			}

			if (cache > 0) {
				insertDataIntoPoiFlag(conn, poiInfoes, hbaseInfo);

				for (Integer pid : hbaseInfo.notFindPoi) {
					pw.println(pid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			DbUtils.rollbackAndClose(conn);
		} finally {

			DbUtils.commitAndClose(conn);

			conn.close();

			pw.flush();

			pw.close();

			reader.close();
		}
	}

	private static void createTable(Connection conn, HbasePoiInfo hbaseInfo) throws Exception {
		String existPoiFlag = String.format("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'IX_POI_FLAG_METHOD'");

		int PoiFlagCount = run.queryForInt(conn, existPoiFlag);
		if (PoiFlagCount == 1) {
			log.info("母库已存在IX_POI_FLAG_METHOD子表");
		} else {
			String createPoiFlag = hbaseInfo.createTable;
			run.execute(conn, createPoiFlag);
			log.info("母库创建IX_POI_FLAG_METHOD:成功");
		}
	}

	private static void insertDataIntoPoiFlag(Connection conn, List<PoiInfo> poiInfoes, HbasePoiInfo hbaseInfo)
			throws Exception {
		log.info("insert datas into IX_POI_FLAG_METHOD...");

		List<Integer> allPoi = new ArrayList<>();
		for (PoiInfo poiInfo : poiInfoes) {
			allPoi.add(poiInfo.getPid());
		}

		log.info("finding poi not exist in ix_poi...");
		List<Integer> findPoi = hbaseInfo.getFoundPoi(conn, allPoi, "PID", "IX_POI");

		// 批量插入
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO IX_POI_FLAG_METHOD VALUES (?,?,?, 0, 0, 0, 0, 0, 0, ?, 0, null, 0, null, null, ?)");
		PreparedStatement pst = conn.prepareStatement(sql.toString());

		for (PoiInfo poiInfo : poiInfoes) {
			if (!findPoi.contains(poiInfo.getPid())) {
				hbaseInfo.notFindPoi.add(poiInfo.getPid());
				continue;
			}

			pst.setInt(1, poiInfo.getPid());
			pst.setInt(2, poiInfo.getVerifyRecord());
			pst.setInt(3, poiInfo.getSourceRecord());
			pst.setInt(4, poiInfo.getFieldVerification());
			pst.setString(5, UuidUtils.genUuid());
			pst.addBatch();
			log.info("insert poi:" + poiInfo.getPid());
		}
		pst.executeBatch();
	}
}
