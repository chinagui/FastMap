package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

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

		MultiLineString multiline = factory.createMultiLineString(geoms
				.toArray(new LineString[0]));

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
			
		}  
		//8002的g_location是feedback中坐标的第一个点，可以不取，如果取了，则坐标相交，solr计算结果返回错误
		else  if(! sourceType.equals("8002") ){

			Geometry g = GeoTranslator.geojson2Jts(g_location);
			
			int glen=g.getNumGeometries();
			
			for (int i = 0; i < glen; i++) {
				
				if (!g.isValid()) {
					throw new Exception("invalid g_location");
				}
				
				geos.add(g.getGeometryN(i));
				
			}
		}

		for (int i = 0; i < feedbacks.size(); i++) {
			JSONObject feedback = feedbacks.getJSONObject(i);

			if (feedback.getInt("type") == 6) {
				// 草图
				JSONArray content = feedback.getJSONArray("content");

				for (int j = 0; j < content.size(); j++) {

					JSONObject geo = content.getJSONObject(j);

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
			/**
			 * 20161026修改，如果复杂几何中存在相同的坐标，则保留一个，否则solr计算wkt相交有问题（
			 * 和王磊确认目前采集端不限制多线几何重复或者自相交的情况）
			 **/
			Geometry[] gArray = null;
			// 去重处理
			Set<Geometry> gSet = new TreeSet<Geometry>();
			for (int i = 0; i < geos.size(); i++) {
				gSet.add(geos.get(i));
			}

			gArray = new Geometry[gSet.size()];

			Iterator<Geometry> it = gSet.iterator();// 先迭代出来
			int i = 0;
			while (it.hasNext()) {// 遍历
				gArray[i] = it.next();
				i++;
			}
			Geometry g = factory.createGeometryCollection(gArray);

			return GeoTranslator.jts2Wkt(g);
		}

	}

	public static void main(String[] args) {
		String wkt = "LINESTRING (116.47573 40.01949, 116.47576 40.01948, 116.4758 40.01946, 116.47583 40.01945, 116.47585 40.01943)";
		try {
			Set<Geometry> gList = new TreeSet<Geometry>();
			Geometry m = GeoTranslator.wkt2Geometry(wkt);
			Geometry m2 = GeoTranslator.wkt2Geometry(wkt);
			gList.add(m);
			gList.add(m2);
			System.out.println(gList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
