package com.navinfo.dataservice.commons.geom;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeo;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeoJSONReader;
import org.mapfish.geo.MfGeoJSONWriter;
import org.mapfish.geo.MfGeometry;

import com.navinfo.dataservice.commons.constant.LoggerConstant;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Geojson的辅助类
 */
public class Geojson {

	private static Logger logger = Logger.getLogger(Geojson.class);

	private static final WKTWriter wktWriter = new WKTWriter();

	private static final MfGeoFactory mfFactory = new MfGeoFactory() {

		@Override
		public MfFeature createFeature(String id, MfGeometry geometry,
				org.json.JSONObject properties) {

			return new MyFeature(id, geometry, properties);
		}

	};

	private static final MfGeoJSONReader reader = new MfGeoJSONReader(mfFactory);

	private static class MyFeature extends MfFeature {
		private final String id;
		private final MfGeometry geometry;
		private final org.json.JSONObject properties;

		public MyFeature(String id, MfGeometry geometry,
				org.json.JSONObject properties) {
			this.id = id;
			this.geometry = geometry;
			this.properties = properties;
		}

		public String getFeatureId() {
			return id;
		}

		public MfGeometry getMfGeometry() {
			return geometry;
		}

		@Override
		public void toJSON(JSONWriter builder) throws JSONException {
			throw new RuntimeException("Not implemented");
		}

	}

	/**
	 * Oracle几何struct转Geojson
	 * 
	 * @param struct
	 * @return
	 * @throws Exception
	 */
	public static JSONObject spatial2Geojson(STRUCT struct) throws Exception {
		JGeometry geom = JGeometry.load(struct);

		String w = new String(new WKT().fromJGeometry(geom));

		WKTReader reader = new WKTReader();

		Geometry g = reader.read(w);

		JSONStringer stringer = new JSONStringer();

		MfGeoJSONWriter builder = new MfGeoJSONWriter(stringer);

		builder.encodeGeometry(g);

		JSONObject geometry = JSONObject.fromObject(stringer.toString());

		return geometry;
	}

	public static JSONArray lonlat2Pixel(double lon, double lat, int z,
			double px, double py) {
		JSONArray ja = new JSONArray();

		ja.add((int) (MercatorProjection.longitudeToPixelX(lon, (byte) z) - px));

		ja.add((int) (MercatorProjection.latitudeToPixelY(lat, (byte) z) - py));

		return ja;
	}

	public static JSONObject link2Pixel(JSONObject geojson, double px, double py,
			int z) throws Exception {

		JSONArray coords = new JSONArray();

		JSONArray jaCoords = geojson.getJSONArray("coordinates");

		for (int i = 0; i < jaCoords.size(); i++) {
			JSONArray ja = jaCoords.getJSONArray(i);

			coords.add(lonlat2Pixel(ja.getDouble(0), ja.getDouble(1), z, px, py));
		}

		geojson.put("coordinates", coords);

		return geojson;
	}

	public static JSONObject face2Pixel(JSONObject geojson, double px, double py,
			int z) throws Exception {

		JSONArray coords = new JSONArray();

		JSONArray jaCoords = geojson.getJSONArray("coordinates");

		for (int i = 0; i < jaCoords.size(); i++) {

			JSONArray lineCoords = new JSONArray();

			JSONArray ja = jaCoords.getJSONArray(i);

			for (int j = 0; j < ja.size(); j++) {

				JSONArray ja2 = ja.getJSONArray(j);

				lineCoords.add(lonlat2Pixel(ja2.getDouble(0), ja2.getDouble(1),
						z, px, py));
			}

			coords.add(lineCoords);
		}

		geojson.put("coordinates", coords);

		return geojson;
	}

	/**
	 * wkt转Geojson
	 * 
	 * @param wkt
	 * @return
	 * @throws Exception
	 */
	public static JSONObject wkt2Geojson(String wkt) throws Exception {

		WKTReader reader = new WKTReader();

		Geometry g = reader.read(wkt);

		JSONStringer stringer = new JSONStringer();

		MfGeoJSONWriter builder = new MfGeoJSONWriter(stringer);

		builder.encodeGeometry(g);

		JSONObject geometry = JSONObject.fromObject(stringer.toString());

		return geometry;
	}

	/**
	 * Geojson转wkt
	 * 
	 * @param geojson
	 * @return
	 * @throws JSONException
	 */
	public static String geojson2Wkt(String geojson) throws JSONException {

		MfGeo result = reader.decode(geojson);

		MfGeometry geom = (MfGeometry) result;

		Geometry jts = geom.getInternalGeometry();

		return wktWriter.write(jts);
	}

	/**
	 * 单点几何数组转Geojson
	 * 
	 * @param point
	 * @return
	 */
	public static JSONObject point2Geojson(double[] point) {

		JSONObject json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point);

		return json;
	}

	public static void coord2Pixel(JSONObject geojson, int z, double px,
			double py) {

		String type = geojson.getString("type");

		JSONArray ja = geojson.getJSONArray("coordinates");

		JSONArray coords = new JSONArray();

		switch (type) {
		case "Point":
			geojson.put("coordinates",
					lonlat2Pixel(ja.getDouble(0), ja.getDouble(1), z, px, py));
			break;

		case "LineString":

			for (int i = 0; i < ja.size(); i++) {
				JSONArray a = ja.getJSONArray(i);

				coords.add(lonlat2Pixel(a.getDouble(0), a.getDouble(1), z, px,
						py));
			}

			geojson.put("coordinates", coords);
			break;

		case "Polygon":
		case "MultiLineString":
			for (int i = 0; i < ja.size(); i++) {

				JSONArray line = ja.getJSONArray(i);

				JSONArray newline = new JSONArray();

				for (int j = 0; j < line.size(); j++) {
					JSONArray point = line.getJSONArray(j);

					point.add(lonlat2Pixel(point.getDouble(0),
							point.getDouble(1), z, px, py));
					
					newline.add(point);
				}

				coords.add(newline);
			}
			geojson.put("coordinates", coords);
			break;
		}
	}

	public static void point2Pixel(JSONObject geojson, int z, double px,
			double py) {

		JSONArray ja = geojson.getJSONArray("coordinates");

		geojson.put("coordinates",
				lonlat2Pixel(ja.getDouble(0), ja.getDouble(1), z, px, py));
	}

	/**
	 * 对象转json时将JTS几何转为Geojson的帮助方法
	 * 
	 * @param scale
	 * @param precision
	 * @return
	 */
	public static JsonConfig geoJsonConfig(final double scale,
			final int precision) {
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(Geometry.class,
				new JsonValueProcessor() {

					@Override
					public Object processObjectValue(String arg0, Object arg1,
							JsonConfig arg2) {

						try {
							if(arg1 != null)
							{
								return GeoTranslator.jts2Geojson((Geometry) arg1,
										scale, precision);
							}
							else
							{
								return "";
							}
						} catch (JSONException e) {
							logger.error(LoggerConstant.errorGeometry, e);
							return null;
						}
					}

					@Override
					public Object processArrayValue(Object value,
							JsonConfig arg1) {

						return value;
					}
				});

		return jsonConfig;
	}

	public static void main(String[] args) throws Exception {

		System.out
				.println(wkt2Geojson("POLYGON ((116.56526 39.96671, 116.56522 39.96706, 116.56516 39.96757, 116.56512 39.96807, 116.5651 39.96849, 116.5649 39.97102, 116.56465 39.97433, 116.56458 39.97504, 116.56452 39.9758, 116.56451 39.97595, 116.56447 39.97674, 116.56441 39.9778, 116.56429 39.97937, 116.56419 39.98058, 116.56415 39.98121, 116.56411 39.9815, 116.56406 39.98195, 116.564 39.98218, 116.56385 39.9825, 116.5638 39.9826, 116.56372 39.98277, 116.56368 39.98284, 116.5636 39.98296, 116.56338 39.98329, 116.56266 39.98409, 116.56211 39.98468, 116.56191 39.98488, 116.56117 39.98566, 116.56096 39.98588, 116.56017 39.98669, 116.56012 39.98674, 116.55976 39.98714, 116.55885 39.98813, 116.55454 39.99266, 116.55413 39.99306, 116.55378 39.99344, 116.54911 39.99844, 116.54866 39.9989, 116.54859 39.99897, 116.54763 40.0, 116.54742 40.0, 116.54773 39.9997, 116.54847 39.99889, 116.55075 39.99643, 116.55204 39.99506, 116.55322 39.99383, 116.55426 39.99271, 116.55447 39.9925, 116.55494 39.99198, 116.5555 39.99141, 116.5556 39.9913, 116.55613 39.99073, 116.55671 39.99012, 116.55737 39.98941, 116.55758 39.9892, 116.55825 39.98849, 116.55891 39.9878, 116.55972 39.98693, 116.5605 39.98613, 116.56126 39.98532, 116.56187 39.98467, 116.56198 39.98456, 116.56254 39.98392, 116.56273 39.98372, 116.56323 39.98314, 116.56344 39.98291, 116.56356 39.9827, 116.5637 39.9825, 116.56395 39.98184, 116.56405 39.98012, 116.56414 39.97876, 116.56424 39.97756, 116.56434 39.97619, 116.5644 39.97524, 116.56442 39.97475, 116.5645 39.97377, 116.56462 39.97194, 116.56469 39.971, 116.56478 39.96989, 116.56488 39.96861, 116.56498 39.96724, 116.56501 39.96681, 116.56526 39.96671))"));
		System.out
				.println(wkt2Geojson("LINESTRING (116.70469 29.62948, 116.70546 29.62938, 116.70612 29.62905, 116.70652 29.62904, 116.70707 29.62933, 116.70753 29.62913, 116.70818 29.62879)"));
	}
}
