package com.navinfo.dataservice.engine.fcc.tips.speedLimit;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;

public class RdSpeedLimitTipsBuilder {

	private static final WKT wkt = new WKT();

	private static String sql = "select a.pid,a.link_pid,a.tollgate_flag,a.direct,a.speed_value, "
			+ "a.speed_flag,a.capture_flag,a.limit_src,"
			+ "a.geometry point_geom,b.geometry link_geom  "
			+ "from rd_speedlimit a,rd_link b where a.link_pid = b.link_pid";

	private static String type = "1101";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab
			) throws Exception {

		SolrBulkUpdater solrConn = new SolrBulkUpdater(TipsImportUtils.QueueSize,TipsImportUtils.ThreadCount);
		Statement stmt = null;

		ResultSet resultSet = null;
		try{
			stmt = fmgdbConn.createStatement();
	
			resultSet = stmt.executeQuery(sql);
	
			resultSet.setFetchSize(5000);
	
			List<Put> puts = new ArrayList<Put>();
	
			int num = 0;
	
			String uniqId = null;
	
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String date = sdf.format(new Date());
	
			while (resultSet.next()) {
				num++;
	
				uniqId = resultSet.getString("pid");
	
				String rowkey = TipsImportUtils.generateRowkey(uniqId, type);
	
				String source = TipsImportUtils.generateSource(type);
	
				String track = TipsImportUtils.generateTrack(date);
	
				JSONObject geometry = generateGeometry(resultSet);
	
				String deep = generateDeep(resultSet);
				
				String feedback = TipsImportUtils.generateFeedback();
	
				Put put = new Put(rowkey.getBytes());
	
				put.addColumn("data".getBytes(), "source".getBytes(),
						source.getBytes());
	
				put.addColumn("data".getBytes(), "track".getBytes(),
						track.getBytes());
	
				put.addColumn("data".getBytes(), "geometry".getBytes(), geometry
						.toString().getBytes());
	
				put.addColumn("data".getBytes(), "deep".getBytes(), deep.getBytes());
				
				put.addColumn("data".getBytes(), "feedback".getBytes(), feedback.getBytes());
	
				puts.add(put);
	
				JSONObject solrIndexJson = TipsImportUtils.assembleSolrIndex(
						rowkey, 0, date, type, deep.toString(),
						geometry.getJSONObject("g_location"),
						geometry.getJSONObject("g_guide"), "[]");
	
				solrConn.addTips(solrIndexJson);
	
				if (num % 5000 == 0) {
					htab.put(puts);
	
					puts.clear();
	
				}
			}
	
			htab.put(puts);
	
			solrConn.commit();
			
			solrConn.close();
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(stmt);
		}

	}

	private static JSONObject generateGeometry(ResultSet resultSet)
			throws Exception {

		STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String pointWkt = new String(wkt.fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String linkWkt = new String(wkt.fromJGeometry(geom2));

		int direct = resultSet.getInt("direct");

		double[] point = DisplayUtils.calSpeedLimitPos(linkWkt, pointWkt,
				direct);

		JSONObject json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point);

		JSONObject geometry = new JSONObject();

		geometry.put("g_location", json);

		geometry.put("g_guide", Geojson.wkt2Geojson(pointWkt));

		return geometry;
	}

	private static String generateDeep(ResultSet resultSet) throws Exception {

		JSONObject jsonDeep = new JSONObject();

		jsonDeep.put("id", resultSet.getString("pid"));

		JSONObject jsonF = new JSONObject();

		jsonF.put("id", resultSet.getString("link_pid"));

		jsonF.put("type", 1);

		jsonDeep.put("f", jsonF);

		jsonDeep.put("agl", calAngle(resultSet));

		jsonDeep.put("toll", resultSet.getInt("tollgate_flag"));

		jsonDeep.put("rdDir", resultSet.getInt("direct"));

		jsonDeep.put("value", resultSet.getInt("speed_value") / 10);

		jsonDeep.put("se", resultSet.getInt("speed_flag"));

		jsonDeep.put("flag", resultSet.getInt("capture_flag"));

		jsonDeep.put("src", resultSet.getInt("limit_src"));

		return jsonDeep.toString();
	}

	// 计算角度
	private static double calAngle(ResultSet resultSet) throws Exception {

		double angle = 0;

		STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		double[] point = geom1.getFirstPoint();

		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		int ps = geom2.getNumPoints();

		int startIndex = 0;

		for (int i = 0; i < ps - 1; i++) {
			double sx = geom2.getOrdinatesArray()[i * 2];

			double sy = geom2.getOrdinatesArray()[i * 2 + 1];

			double ex = geom2.getOrdinatesArray()[(i + 1) * 2];

			double ey = geom2.getOrdinatesArray()[(i + 1) * 2 + 1];

			if (isBetween(sx, ex, point[0]) && isBetween(sy, ey, point[1])) {
				startIndex = i;
				break;
			}
		}

		StringBuilder sb = new StringBuilder("LINESTRING (");

		sb.append(geom2.getOrdinatesArray()[startIndex * 2]);

		sb.append(" ");

		sb.append(geom2.getOrdinatesArray()[startIndex * 2 + 1]);

		sb.append(", ");

		sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2]);

		sb.append(" ");

		sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2 + 1]);

		sb.append(")");

		angle = DisplayUtils.calIncloudedAngle(sb.toString(),
				resultSet.getInt("direct"));

		return angle;

	}

	private static boolean isBetween(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}

}
