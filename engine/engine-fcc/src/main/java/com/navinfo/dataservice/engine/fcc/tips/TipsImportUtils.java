package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.ParseException;

/**
 * tips导入辅助工具，如rowkey生成，source生成
 * 
 * @author lilei3774
 * 
 */
public class TipsImportUtils {

	public static int ThreadCount = 5;

	public static int QueueSize = 20000;

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
	
	public static String generateFeedback() {
		JSONObject json = new JSONObject();

		json.put("f_array", new JSONArray());
		
		return json.toString();
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
			String date, String type, String deep, JSONObject g_location,
			JSONObject g_guide, String feedback) throws Exception {
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

		json.put("feedback", feedback);
		
		json.put("s_reliability", 100);

		return json;
	}

	public static JSONObject connectLinks(List<Geometry> geoms)
			throws ParseException, JSONException {
		
		GeometryFactory factory = new GeometryFactory();
		
		MultiLineString multiline = factory.createMultiLineString(geoms.toArray(new LineString[0]));
		
		JSONObject json = GeoTranslator.jts2Geojson(multiline);
		
		return json;
	}

	public static String generateSolrWkt(String sourceType, JSONObject deep,
			JSONObject g_location, JSONArray feedbacks) throws Exception {
		List<Geometry> geos = new ArrayList<Geometry>();

		GeometryFactory factory = new GeometryFactory();

		if (sourceType.equals("1501")) {

			JSONObject gSLoc = deep.getJSONObject("gSLoc");

			JSONObject gELoc = deep.getJSONObject("gELoc");

			Geometry g1 = GeoTranslator.geojson2Jts(gSLoc);

			Geometry g2 = GeoTranslator.geojson2Jts(gELoc);

			Geometry g3 = g1.union(g2);

			Geometry g = factory.createMultiPoint(g3.getCoordinates());

			geos.add(g);
		} else {

			Geometry g = GeoTranslator.geojson2Jts(g_location);

			if (!g.isValid()) {
				throw new Exception("invalid g_location");
			}

			geos.add(g);
		}

		for (int i = 0; i < feedbacks.size(); i++) {
			JSONObject feedback = feedbacks.getJSONObject(i);

			if (feedback.getInt("type") == 6) {
				// 草图
				JSONArray content = feedback.getJSONArray("content");

				for (int j = 0; j < content.size(); j++) {

					JSONObject geo = content.getJSONObject(i);

					Geometry g = GeoTranslator.geojson2Jts(geo
							.getJSONObject("geo"));

					geos.add(g);
				}

				break;
			}
		}

		if (geos.size() == 1) {
			return GeoTranslator.jts2Wkt(geos.get(0));
		} else {
			Geometry[] gArray = new Geometry[geos.size()];

			for (int i = 0; i < geos.size(); i++) {
				gArray[i] = geos.get(i);
			}

			Geometry g = factory.createGeometryCollection(gArray);

			return GeoTranslator.jts2Wkt(g);
		}

	}
}
