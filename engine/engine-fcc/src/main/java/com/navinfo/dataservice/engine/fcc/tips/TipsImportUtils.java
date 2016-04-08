package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * tips导入辅助工具，如rowkey生成，source生成
 * 
 * @author lilei3774
 * 
 */
public class TipsImportUtils {
	
	public static int ThreadCount=5;
	
	public static int QueueSize=20000;

	/**
	 * 根据类型、位置、唯一ID组合ROWKEY
	 * 
	 * @param lonlat
	 * @param uniqId
	 * @param type
	 * @return
	 */
	public static String generateRowkey(String uniqId, String type) {

		StringBuilder rowkey = new StringBuilder();

		rowkey.append("11");

		rowkey.append(type);

		rowkey.append(uniqId);

		return rowkey.toString();
	}

	public static String generateSource(String type) {
		JSONObject sourcejson = new JSONObject();

		sourcejson.put("s_featureKind", 2);
		sourcejson.put("s_project", JSONObject.fromObject(null));
		sourcejson.put("s_sourceCode", 11);
		sourcejson.put("s_sourceId", JSONObject.fromObject(null));
		sourcejson.put("s_sourceType", type);
		sourcejson.put("s_reliability", 100);
		sourcejson.put("s_sourceProvider", 0);

		return sourcejson.toString();
	}

	public static String generateTrack(String date) {

		JSONObject track = new JSONObject();

		track.put("t_lifecycle", 0);

		track.put("t_command", 0);

		track.put("t_date", date);

		JSONArray trackinfoarray = new JSONArray();

		JSONObject trackinfo = new JSONObject();

		trackinfo.put("stage", 0);
		trackinfo.put("date", date);
		trackinfo.put("handler", 0);

		trackinfoarray.add(trackinfo);

		track.put("t_trackInfo", trackinfoarray);

		return track.toString();
	}

	// 组装solr索引
	public static JSONObject assembleSolrIndex(String rowkey, int stage,
			String date, String type,  String deep, JSONObject g_location, JSONObject g_guide)
			throws Exception {
		JSONObject json = new JSONObject();

		json.put("id", rowkey);

		json.put("stage", stage);

		json.put("t_operateDate", date);

		json.put("t_date", date);

		json.put("t_lifecycle", 0);

		json.put("t_command", 0);

		json.put("handler", 0);

		json.put("s_sourceType", type);

		json.put("s_sourceCode", 11);

		json.put("g_location", g_location);

		json.put("g_guide", g_guide);

		json.put("wkt",
				GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(g_location)));
		
		json.put("deep", deep);

		return json;
	}
	
	public static JSONObject connectLinks(List<Geometry> geoms)
			throws ParseException {
		JSONObject json = new JSONObject();

		json.put("type", "LineString");

		Geometry geom = geoms.get(0);

		for (int i = 1; i < geoms.size(); i++) {
			geom = geom.union(geoms.get(i));
		}

		Coordinate[] cs = geom.getCoordinates();

		List<double[]> ps = new ArrayList<double[]>();

		for (Coordinate c : cs) {
			double[] p = new double[2];

			p[0] = c.x;

			p[1] = c.y;

			ps.add(p);
		}

		json.put("coordinates", ps);

		return json;
	}
}
