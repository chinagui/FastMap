package com.navinfo.dataservice.FosEngine.tips.direct;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;

public class DirectTipsBuilder {

	private static final WKT wkt = new WKT();

	private static String sql = "select a.link_pid,a.direct,b.time_domain,a.geometry "
			+ "from rd_link a,rd_link_limit b "
			+ "where a.link_pid = b.link_pid "
			+ " and b.type=1 and time_domain is not null";

	private static String type = "1203";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,String solrUrl)
			throws Exception {
		SConnection solrConn = new SConnection(solrUrl,5000);
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;

		String uniqId = null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;

			uniqId = resultSet.getString("link_pid");

			String rowkey = TipsImportUtils.generateRowkey(uniqId, type);

			String source = TipsImportUtils.generateSource(type);

			String track = TipsImportUtils.generateTrack(date);

			String geometry = generateGeometry(resultSet);

			String deep = generateDeep(resultSet);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(),
					source.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(),
					track.getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(),
					geometry.getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.getBytes());

			puts.add(put);
			
			JSONObject solrIndexJson = assembleSolrIndex(rowkey, JSONObject.fromObject(geometry), 0, date, type, deep);

			solrConn.addTips(solrIndexJson);
			
			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

			}
		}

		htab.put(puts);

		solrConn.persistentData();
		
		solrConn.closeConnection();
	}

	private static String generateGeometry(ResultSet resultSet)
			throws SQLException, GeometryExceptionWithContext {
		STRUCT struct1 = (STRUCT) resultSet.getObject("geometry");

		JGeometry geom1 = JGeometry.load(struct1);

		int direct = resultSet.getInt("direct");

		double[] point = DisplayUtils.get1_4Point(geom1, direct);

		JSONObject json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point);

		JSONObject geometry = new JSONObject();

		geometry.put("g_location", json);

		geometry.put("g_guide", json);

		return geometry.toString();
	}

	private static String generateDeep(ResultSet resultSet) throws Exception {

		JSONObject json = new JSONObject();

		JSONObject jsonF = new JSONObject();

		jsonF.put("id", resultSet.getString("link_pid"));

		jsonF.put("type", 1);

		json.put("f", jsonF);

		STRUCT struct1 = (STRUCT) resultSet.getObject("geometry");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(wkt.fromJGeometry(geom1));

		int direct = resultSet.getInt("direct");

		json.put("agl", DisplayUtils.calIncloudedAngle(linkWkt, direct));

		json.put("dr", direct);
		
		json.put("time", resultSet.getString("time_domain"));

		return json.toString();
	}

	// 组装solr索引
	private static JSONObject assembleSolrIndex(String rowkey, JSONObject geom,
			int stage, String date, String type, String deep) throws Exception {
		JSONObject json = new JSONObject();

		json.put("id", rowkey);

		json.put("stage", stage);

		json.put("date", date);

		json.put("t_lifecycle", 0);

		json.put("t_command", 0);

		json.put("handler", 0);

		json.put("s_sourceType", type);

		json.put("s_sourceCode", 11);

		JSONObject geojson = geom.getJSONObject("g_location");

		json.put("g_location", geojson);

		json.put("g_guide", geom.getJSONObject("g_guide"));

		json.put("wkt",
				GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(geojson)));
		
		json.put("deep", deep);

		return json;
	}

}
