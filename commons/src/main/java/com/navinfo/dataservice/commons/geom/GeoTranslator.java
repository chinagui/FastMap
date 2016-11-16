package com.navinfo.dataservice.commons.geom;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeo;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeoJSONReader;
import org.mapfish.geo.MfGeoJSONWriter;
import org.mapfish.geo.MfGeometry;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * 几何的转换类
 */
public class GeoTranslator {

	public static final double dPrecisionGeo = 1.0;
	
	private static final GeometryFactory geoFactory = new GeometryFactory();

	private static final MfGeoFactory mfFactory = new MfGeoFactory() {

		@Override
		public MfFeature createFeature(String id, MfGeometry geometry,
				org.json.JSONObject properties) {
			return new MyFeature(id, geometry, properties);
		}

	};

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

	private static final MfGeoJSONReader mfreader = new MfGeoJSONReader(
			mfFactory);

	/**
	 * 点p0是否在点p1和p2的线上
	 * 
	 * @param p1
	 *            线起点
	 * @param p2
	 *            线终点
	 * @param p0
	 *            点
	 * @return True 在线上； False 不在线上
	 * @throws Exception
	 */
	public static boolean isIntersection(double[] p1, double[] p2, double[] p0)
			throws Exception {
		boolean flag = false;

		StringBuilder sb2 = new StringBuilder("Point (" + p0[0]);

		sb2.append(" " + p0[1]);

		sb2.append(")");

		Geometry line = GeometryUtils.getLineFromPoint(p1, p2);

		Geometry point = new WKTReader().read(sb2.toString());

		if (line.distance(point) <= 1) {
			flag = true;
		}

		return flag;
	}

	/**
	 * 点p0是否在点p1和p2的线上，不包含端点
	 * 
	 * @param p1
	 *            线起点
	 * @param p2
	 *            线终点
	 * @param p0
	 *            点
	 * @return True 在线上； False 不在线上
	 * @throws Exception
	 */
	public static boolean isIntersectionInLine(double[] p1, double[] p2,
			double[] p0) throws Exception {
		boolean flag = false;

		if (p1 == p0 || p2 == p0) {
			return flag;
		}

		StringBuilder sb2 = new StringBuilder("Point (" + p0[0]);

		sb2.append(" " + p0[1]);

		sb2.append(")");

		Geometry line = GeometryUtils.getLineFromPoint(p1, p2);

		Geometry point = new WKTReader().read(sb2.toString());

		if (line.distance(point) <= 1) {
			flag = true;
		}

		return flag;
	}

	/**
	 * 点p0是否在点p1和p2的线上(墨卡托坐标)
	 * 
	 * @param p1
	 *            线起点
	 * @param p2
	 *            线终点
	 * @param p0
	 *            点
	 * @return True 在线上； False 不在线上
	 * @throws Exception
	 */
	public static boolean isIntersectionMer(double[] p1, double[] p2,
			double[] p0) throws Exception {
		boolean flag = false;

		StringBuilder sb = new StringBuilder("LINESTRING ("
				+ MercatorProjection.longitudeToMetersX(p1[0]));

		sb.append(" " + MercatorProjection.latitudeToMetersY(p1[1]));

		sb.append(",");

		sb.append(MercatorProjection.longitudeToMetersX(p2[0]));

		sb.append(" " + MercatorProjection.latitudeToMetersY(p2[1]));

		sb.append(")");

		StringBuilder sb2 = new StringBuilder("Point ("
				+ MercatorProjection.longitudeToMetersX(p0[0]));

		sb2.append(" " + MercatorProjection.latitudeToMetersY(p0[1]));

		sb2.append(")");

		Geometry line = new WKTReader().read(sb.toString());

		Geometry point = new WKTReader().read(sb2.toString());

		if (line.intersects(point)) {
			flag = true;
		}

		return flag;
	}

	/**
	 * Oracle几何体转JTS几何
	 * 
	 * @param struct
	 *            Oracle几何
	 * @return JTS几何
	 * @throws Exception
	 */
	public static Geometry struct2Jts(STRUCT struct) throws Exception {

		JGeometry geom = JGeometry.load(struct);

		String w = new String(new WKT().fromJGeometry(geom));

		WKTReader reader = new WKTReader();

		Geometry g = reader.read(w);

		return g;
	}

	/**
	 * Oracle几何体转wkt几何
	 * 
	 * @param struct
	 *            Oracle几何
	 * @return wkt几何
	 * @throws Exception
	 */
	public static String struct2Wkt(STRUCT struct) throws Exception {

		JGeometry geom = JGeometry.load(struct);

		String w = new String(new WKT().fromJGeometry(geom));

		return w;
	}
	
	/**
	 * wkt几何转Oracle几何体
	 * 
	 * @param struct
	 *            Oracle几何
	 * @return wkt几何
	 * @throws Exception
	 */
	public static STRUCT wkt2Struct(Connection conn,String wkt) throws Exception {
		JGeometry geom = GeoTranslator.wkt2JGrometry(wkt);
		STRUCT struct = JGeometry.store(geom, ConnectionUtil.getObject(conn));
		return struct;
	}

	/**
	 * Oracle几何体转JTS几何，按比例缩放，并截取精度
	 * 
	 * @param struct
	 *            Oracle几何
	 * @param scale
	 *            缩放比例
	 * @param precision
	 *            截取精度
	 * @return JTS几何
	 * @throws Exception
	 */
	public static Geometry struct2Jts(STRUCT struct, double scale, int precision)
			throws Exception {

		Geometry g = struct2Jts(struct);

		g = transform(g, scale, precision);

		return g;
	}

	public static JGeometry wkt2JGrometry(String wktStr)
			throws GeometryExceptionWithContext {

		WKT wkt = new WKT();

		JGeometry geom = wkt.toJGeometry(wktStr.getBytes());

		geom.setSRID(8307);

		return geom;
	}

	public static Geometry wkt2Geometry(String wkt) throws Exception {
		GeometryFactory geometryFactory = new GeometryFactory();
		WKTReader reader = new WKTReader(geometryFactory);
		Geometry geometry = reader.read(wkt);
		return geometry;
	}

	public static JGeometry Jts2JGeometry(Geometry jts, double scale,
			int precision) throws GeometryExceptionWithContext {

		WKT wkt = new WKT();

		String str = GeoTranslator.jts2Wkt(jts, scale, precision);

		JGeometry geom = wkt.toJGeometry(str.getBytes());

		geom.setSRID(8307);

		return geom;
	}

	/**
	 * JTS几何转Wkt
	 * 
	 * @param g
	 *            JTS几何
	 * @return Wkt
	 */
	public static String jts2Wkt(Geometry g) {

		String wkt = new WKTWriter().write(g);

		return wkt;
	}

	/**
	 * JTS几何转Wkt，按比例缩放，并截取精度
	 * 
	 * @param g
	 *            JTS几何
	 * @param scale
	 *            缩放比例
	 * @param precision
	 *            截取精度
	 * @return Wkt
	 */
	public static String jts2Wkt(Geometry g, double scale, int precision) {

		g = transform(g, scale, precision);

		String wkt = new WKTWriter().write(g);

		return wkt;
	}

	/**
	 * Geojson转JTS几何
	 * 
	 * @param json
	 *            Geojson
	 * @return JTS几何
	 * @throws JSONException
	 */
	public static Geometry geojson2Jts(JSONObject json) throws JSONException {

		MfGeo result = mfreader.decode(json.toString());

		MfGeometry geom = (MfGeometry) result;

		Geometry jts = geom.getInternalGeometry();

		return jts;
	}

	/**
	 * Geojson转JTS几何，按比例缩放，并截取精度
	 * 
	 * @param json
	 *            Geojson几何
	 * @param scale
	 *            缩放比例
	 * @param precision
	 *            截取精度
	 * @return JTS几何
	 * @throws JSONException
	 */
	public static Geometry geojson2Jts(JSONObject json, double scale,
			int precision) throws JSONException {

		MfGeo result = mfreader.decode(json.toString());

		MfGeometry geom = (MfGeometry) result;

		Geometry jts = geom.getInternalGeometry();

		jts = transform(jts, scale, precision);

		return jts;
	}

	/**
	 * JTS转Geojson，按比例缩放，并截取精度
	 * 
	 * @param jts
	 *            JTS几何
	 * @param scale
	 *            缩放比例
	 * @param precision
	 *            截取精度
	 * @return Geojson
	 * @throws JSONException
	 */
	public static JSONObject jts2Geojson(Geometry jts, double scale,
			int precision) throws JSONException {

		jts = transform(jts, scale, precision);

		JSONStringer stringer = new JSONStringer();

		MfGeoJSONWriter builder = new MfGeoJSONWriter(stringer);

		builder.encodeGeometry(jts);

		JSONObject geometry = JSONObject.fromObject(stringer.toString());

		return geometry;
	}

	/**
	 * JTS几何转Geojson
	 * 
	 * @param jts
	 *            JTS几何
	 * @return Geojson
	 * @throws JSONException
	 */
	public static JSONObject jts2Geojson(Geometry jts) throws JSONException {

		JSONStringer stringer = new JSONStringer();

		MfGeoJSONWriter builder = new MfGeoJSONWriter(stringer);

		builder.encodeGeometry(jts);

		JSONObject geometry = JSONObject.fromObject(stringer.toString());

		return geometry;
	}

	/**
	 * JTS几何按比例缩放，并截取精度
	 * 
	 * @param geom
	 *            JTS几何
	 * @param scale
	 *            缩放比例
	 * @param precision
	 *            截取精度
	 * @return JTS几何
	 */
	public static Geometry transform(Geometry geom, double scale, int precision) {

		// 防止开发人员在同一级别对Geometry重复缩放，捕获catch后按代码修改前逻辑缩放Geometry。
		try {
			Coordinate flagCoordinate = geom.getCoordinate();

			if (flagCoordinate == null && geom.getCoordinates().length > 0) {

				flagCoordinate = geom.getCoordinates()[0];
			}
			if (flagCoordinate != null) {

				if (scale > 1) {
					if (flagCoordinate.x > 180 || flagCoordinate.y > 90) {
						return geom;
					}
				} else if (scale < 1) {
					if (flagCoordinate.x < 180 || flagCoordinate.y < 90) {
						return geom;
					}
				}
			}
		} catch (Exception e) {
			// 不处理异常 继续向下执行
		}

		AffineTransformation aff = new AffineTransformation();

		aff = aff.scale(scale, scale);

		geom = aff.transform(geom);

		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(
				new PrecisionModel(Math.pow(10, precision)));

		geom = reducer.reduce(geom);

		return geom;
	}

	/**
	 * JTS几何转JSON坐标数组
	 * 
	 * @param geom
	 *            JTS几何
	 * @return JSON坐标数组
	 */
	public static JSONArray jts2JSONArray(Geometry geom) {

		JSONArray array = new JSONArray();

		Coordinate[] coords = geom.getCoordinates();

		for (Coordinate coord : coords) {

			array.add(coord.x);

			array.add(coord.y);
		}

		return array;
	}

	/**
	 * 获取wkt的最小外包矩形
	 * 
	 * @param wkt
	 *            wkt
	 * @return 坐标数组
	 * @throws ParseException
	 */
	public static double[] getMBR(String wkt) throws ParseException {

		double[] mbr = new double[4];

		WKTReader wktReader = new WKTReader();

		Coordinate[] cs = wktReader.read(wkt).getCoordinates();

		double minLon = 180;

		double minLat = 90;

		double maxLon = 0;

		double maxLat = 0;

		for (Coordinate c : cs) {

			double lon = c.x;

			if (minLon > lon) {
				minLon = lon;
			}

			if (maxLon < lon) {
				maxLon = lon;
			}

			double lat = c.y;

			if (minLat > lat) {
				minLat = lat;
			}

			if (maxLat < lat) {
				maxLat = lat;
			}
		}

		mbr[0] = minLon;

		mbr[1] = minLat;

		mbr[2] = maxLon;

		mbr[3] = maxLat;

		return mbr;

	}

	public static Geometry point2Jts(double x, double y) throws JSONException {

		Coordinate coordinate = new Coordinate(x, y);

		Point point = geoFactory.createPoint(coordinate);

		return point;
	}

	public static double calAngle(double x11, double y11, double x12,
			double y12, double x21, double y21, double x22, double y22) {
		// double k1 = (y11-y12)/(x11-x12);
		// double k2 = (y21-y22)/(x21-x22);
		// double angle1 = Math.abs(Math.atan(k1)/(Math.PI/180));
		// double angle2 = Math.abs(Math.atan(k2)/(Math.PI/180));
		//
		// return Math.round(Math.abs(angle1-angle2));

		double a = Math.atan2(y11 - y12, x11 - x12);

		double b = Math.atan2(y21 - y22, x21 - x22);

		return Math.round(Angle.toDegrees(b - a));
	}

	/**
	 * 线几何组成面的几何
	 * 
	 * @param List
	 *            <Geometry> 线几何
	 * @return 面几何
	 */
	public static Geometry getCalLineToPython(List<Geometry> gList)
			throws Exception {
		Coordinate[] c = null;
		List<Coordinate> list = new ArrayList<Coordinate>();
		for (Geometry g : gList) {
			c = (Coordinate[]) ArrayUtils.addAll(c, g.getCoordinates());
		}
		for (int i = 0; i < c.length; i++) {

			if (!list.contains(c[i])) {
				list.add(c[i]);
			}
		}
		list = getOrderCoordinate(list, gList);
		list.add(c[0]);
		Coordinate[] c1 = new Coordinate[list.size()];
		for (int i = 0; i < list.size(); i++) {
			c1[i] = list.get(i);
		}

		return geoFactory.createPolygon(c1);

	}

	/**
	 * 线几何按逆序组成面的几何
	 * 
	 * @param List
	 *            <Geometry> 线几何
	 * @return 面几何
	 */
	public static Geometry getPolygonToPoints(Coordinate[] c) throws Exception {
		return geoFactory.createPolygon(c);

	}

	public static List<Coordinate> getOrderCoordinate(
			List<Coordinate> coordinates, List<Geometry> gList)
			throws JSONException {

		List<Coordinate> rlist = new ArrayList<Coordinate>();
		Coordinate currentCoordinate = coordinates.get(0);
		rlist.add(currentCoordinate);
		int size = coordinates.size();
		while (rlist.size() != size) {
			coordinates.removeAll(rlist);
			for (Coordinate coordinate : coordinates) {
				if (isGoordinateToLine(currentCoordinate, coordinate, gList)) {
					rlist.add(coordinate);
					currentCoordinate = coordinate;
					break;
				}
			}

		}
		return rlist;
	}

	public static boolean isGoordinateToLine(Coordinate c1, Coordinate c2,
			List<Geometry> gList) {
		Coordinate[] coordinates = new Coordinate[2];
		coordinates[0] = c1;
		coordinates[1] = c2;
		for (Geometry g : gList) {
			LineString line = geoFactory.createLineString(g.getCoordinates());
			if (line.contains(geoFactory.createLineString(coordinates))
					|| line.contains(geoFactory.createLineString(coordinates)
							.reverse())) {
				return true;
			}
		}
		return false;
	}

	/***
	 * zhaokk LineString 加入形状点 组成一个新的link
	 * 
	 * @throws Exception
	 */
	public static LineString getReformLineString(LineString lineString,
			Set<Point> points) throws Exception {

		
		 Geometry geometry = GeoTranslator.transform(lineString, 100000, 5);
		 List<Coordinate> coordinates = new ArrayList<Coordinate>();
		 Collections.addAll( coordinates, geometry.getCoordinates());
		for (Point point : points) {
			point = (Point) GeoTranslator.transform(point, 100000, 5);
			// 扩大100000倍保持精度
			double lon = point.getX();
			double lat = point.getY();

			for (int i = 0; i < coordinates.size(); i++) {

				Coordinate cs = coordinates.get(i);
				Coordinate ce = coordinates.get(i+1);
				// 是否在形状点上
				if (Math.abs(lon - ce.x) < 0.0000001
						&& Math.abs(lat - ce.y) < 0.0000001) {
					continue;

				}
				// 是否在线段上
				if (GeoTranslator.isIntersection(new double[] { cs.x, cs.y },
						new double[] { ce.x, ce.y }, new double[] { lon, lat })) {
					coordinates.add(i+1,point.getCoordinate());
					break;

				}
			}

		}

		// 返回linestring
		Coordinate[] c = (Coordinate[]) coordinates
				.toArray(new Coordinate[coordinates.size()]);
		return (LineString) GeoTranslator.transform(
				geoFactory.createLineString(c), 0.00001, 5);

	}

	/***
	 * 按顺序返回线上对应的形状点 zhaokk
	 * 
	 * @param lineString
	 * @param points
	 * @return
	 * @throws Exception
	 */
	public static List<Point> getOrderPoints(LineString line,
			Set<Point> points) throws Exception {
		List<String> str = new ArrayList<String>();
		List<Point> list = new ArrayList<Point>();
		//LineString line = getReformLineString(lineString, points);
		Map<Integer, Point> map = new TreeMap<Integer, Point>();
		for (Point point : points) {
			for (int i = 0; i < line.getCoordinates().length; i++) {
				Coordinate c = line.getCoordinates()[i];
				if (point.getX() == c.x && point.getY() == c.y) {
					map.put(i, point);
					break;
				}
			}
		}
		list.addAll(map.values());

		return list;

	}

	
	/**
	 * 点p0是否在点p1和p2的线上
	 * 
	 * @param p1
	 *            线起点
	 * @param p2
	 *            线终点
	 * @param p0
	 *            点
	 * @return True 在线上； False 不在线上
	 * @throws Exception
	 */
	public static boolean isIntersection(Coordinate p1, Coordinate p2,
			Coordinate p0) throws Exception {
		
		boolean flag = false;

		Coordinate[] coordinates = new Coordinate[2];

		coordinates[0] = p1;

		coordinates[1] = p2;

		LineString line = geoFactory.createLineString(coordinates);

		Point point = geoFactory.createPoint(p0);

		if (line.distance(point) <= 1) {
			flag = true;
		}

		return flag;
	}
	
	
	public static long round(double d) {
		return d < 0 ? (long) d : (long) (d + 0.5);
	}

	public static boolean isPointEquals(Point point1, Point point2) {
		return isPointEquals(point1.getX(), point1.getY(), point2.getX(),
				point2.getY());
	}

	public static boolean isPointEquals(Coordinate c1, Coordinate c2) {
		return isPointEquals(c1.x, c1.y, c2.x, c2.y);
	}

	public static boolean isPointEquals(double x1, double y1, double x2,
			double y2) {
		x1 = round(x1);
		y1 = round(y1);
		x2 = round(x2);
		y2 = round(y2);

		if (Math.abs(x1 - x2) >= dPrecisionGeo)
			return false;

		if (Math.abs(y1 - y2) >= dPrecisionGeo)
			return false;

		return true;
	}
	
	


	public static void main(String[] args) throws Exception {
		Point point = (Point)wkt2Geometry("POINT (116.38636 40.00512)");
		Point point1 = (Point)wkt2Geometry("POINT (116.38617 40.00511)");
		Point point2 = (Point)wkt2Geometry("POINT (116.38602 40.0051)");
		Set<Point> points = new HashSet<Point>();
		points.add(point);
		points.add(point1);
		points.add(point2);
		LineString line = (LineString)wkt2Geometry("LineString(116.38586 40.00509, 116.38647 40.00512)");
		//LINESTRING (116.38586 40.00509, 116.38602 40.0051, 116.38617 40.00511, 116.38636 40.00512, 116.38647 40.00512)
		LineString s = getReformLineString(line, points);
		System.out.println(s);
		
		//[POINT (116.38636 40.00512), POINT (116.38617 40.00511), POINT (116.38602 40.0051)]

		//LINESTRING (116.38586 40.00509, 116.38636 40.00512, 116.38617 40.00511, 116.38602 40.0051, 116.38647 40.00512)
	}
}
