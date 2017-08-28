package com.navinfo.dataservice.commons.geom;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeo;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeoJSONReader;
import org.mapfish.geo.MfGeoJSONWriter;
import org.mapfish.geo.MfGeometry;
import org.springframework.aop.ThrowsAdvice;

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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

/**
 * 几何的转换类
 */
public class GeoTranslator {

	public static final double dPrecisionGeo = 1.0;
	
	/**
	 * 几何坐标升级 乘10万（100000）
	 */
	public static final int geoUpgrade = 100000;
	/**
	 * 地图坐标精度单位 0.00001
	 */
	public static final double dPrecisionMap = 0.00001;

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
		
		if(Arrays.equals(p1, p0)||Arrays.equals(p2, p0))
		{
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
	 * 点p0是否在点c1和c2的线上，不包含端点
	 * 
	 * @param c1
	 *            线起点
	 * @param c2
	 *            线终点
	 * @param p0
	 *            点
	 * @return True 在线上； False 不在线上
	 * @throws Exception
	 */
	public static boolean isIntersectionInLine(Coordinate c1, Coordinate c2,
			Coordinate p0) throws Exception {

		if (isPointEquals(c1, p0) || isPointEquals(c2, p0)) {

			return false;
		}

		Coordinate[] coordinates = { c1, c2 };

		Geometry line = createLineString(coordinates);

		Geometry point = createPoint(p0);

		if (line.distance(point) <= 1) {
			
			return true;
		}

		return false;
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

		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(
				new PrecisionModel(Math.pow(10, precision)));
		
		// 防止开发人员在同一级别对Geometry重复缩放，捕获catch后按代码修改前逻辑缩放Geometry。
		try {
			Coordinate flagCoordinate = geom.getCoordinate();

			if (flagCoordinate == null && geom.getCoordinates().length > 0) {

				flagCoordinate = geom.getCoordinates()[0];
			}
			if (flagCoordinate != null) {

				if (scale > 1) {
					if (flagCoordinate.x > 180 || flagCoordinate.y > 90) {
						
						geom = reducer.reduce(geom);
						
						return geom;
					}
				} else if (scale < 1) {
					if (flagCoordinate.x < 180 || flagCoordinate.y < 90) {
						
						geom = reducer.reduce(geom);
						
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
	
	/**
	 * Creates a Point using the given Coordinate; a null Coordinate will create
	 * an empty Geometry.
	 */
	public static Geometry createPoint(Coordinate coordinate) throws JSONException {

		Point point = geoFactory.createPoint(coordinate);

		return point;
	}	
	
	public static LineString createLineString(List<Coordinate> coordinates)
			throws JSONException {

		Coordinate[] c = (Coordinate[]) coordinates
				.toArray(new Coordinate[coordinates.size()]);

		return createLineString(c);
	}

	public static LineString createLineString(Coordinate[] coordinates)
			throws JSONException {

		return geoFactory.createLineString(coordinates);
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
	 * @param gList<Geometry> 线几何
	 * @return 面几何
	 */
	public static Geometry getCalLineToPython(List<Geometry> gList) throws Exception {
		//gList已经是按照线的联通关系添加后的，只需要根据划线方向组好点的几何
		List<Coordinate> corList = new ArrayList<>();
		for(Coordinate cor : gList.get(0).getCoordinates())
		{
			corList.add(cor);
		}
		Coordinate endCor = corList.get(corList.size() -1);
		for (int i = 1;i<gList.size();i++) {
			Geometry lineGeo = gList.get(i);
			//线的第一个形状点是上一个线最后一个形状点，直接按顺序加入
			if(lineGeo.getCoordinates()[0].distance(endCor)<=1)
			{
				endCor = lineGeo.getCoordinates()[lineGeo.getCoordinates().length -1];
				
				for(int j= 1;j<lineGeo.getCoordinates().length;j++)
				{
					Coordinate lineCor = lineGeo.getCoordinates()[j];
					
					corList.add(lineCor);
				}
			}
			else
			{
				//线的最后一个形状点是上一条线的最后一个行政点，即划线方向不一致，对线进行反向排序后加入即可
				endCor = lineGeo.getCoordinates()[0];
				
				Geometry reverseLine = lineGeo.reverse();
				
				for(int j= 1;j<reverseLine.getCoordinates().length;j++)
				{
					Coordinate lineCor = reverseLine.getCoordinates()[j];
					
					corList.add(lineCor);
				}
			}
			
		}

		Geometry geometry = geoFactory.createPolygon((Coordinate[])corList.toArray(new Coordinate[corList.size()]));
		Set<Coordinate> set = new HashSet<>();
		for (Coordinate coordinate : geometry.getCoordinates()) {
		    set.add(coordinate);
        }
        if (set.size() < geometry.getCoordinates().length - 1) {
		    throw new Exception("如果创建的面跨图幅，并与图框线重叠的部分长度小于一个精度格，则不允许创建面");
        }
		return geometry;
	}
	
	/**
	 * 线几何组成面的几何,不进行部分重叠的检查
	 * @param gList<Geometry> 线几何
	 * @return 面几何
	 */
	public static Geometry getCalLineToPythonWithoutCheck(List<Geometry> gList) throws Exception {
		//gList已经是按照线的联通关系添加后的，只需要根据划线方向组好点的几何
		List<Coordinate> corList = new ArrayList<>();
		for(Coordinate cor : gList.get(0).getCoordinates())
		{
			corList.add(cor);
		}
		Coordinate endCor = corList.get(corList.size() -1);
		for (int i = 1;i<gList.size();i++) {
			Geometry lineGeo = gList.get(i);
			//线的第一个形状点是上一个线最后一个形状点，直接按顺序加入
			if(lineGeo.getCoordinates()[0].distance(endCor)<=1)
			{
				endCor = lineGeo.getCoordinates()[lineGeo.getCoordinates().length -1];
				
				for(int j= 1;j<lineGeo.getCoordinates().length;j++)
				{
					Coordinate lineCor = lineGeo.getCoordinates()[j];
					
					corList.add(lineCor);
				}
			}
			else
			{
				//线的最后一个形状点是上一条线的最后一个行政点，即划线方向不一致，对线进行反向排序后加入即可
				endCor = lineGeo.getCoordinates()[0];
				
				Geometry reverseLine = lineGeo.reverse();
				
				for(int j= 1;j<reverseLine.getCoordinates().length;j++)
				{
					Coordinate lineCor = reverseLine.getCoordinates()[j];
					
					corList.add(lineCor);
				}
			}
			
		}

		Geometry geometry = geoFactory.createPolygon((Coordinate[])corList.toArray(new Coordinate[corList.size()]));
		return geometry;
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
		Collections.addAll(coordinates, geometry.getCoordinates());
		for (Point point : points) {
			point = (Point) GeoTranslator.transform(point, 100000, 5);
			// 扩大100000倍保持精度
			double lon = point.getX();
			double lat = point.getY();

			for (int i = 0; i < coordinates.size()-1; i++) {

				Coordinate cs = coordinates.get(i);
				Coordinate ce = coordinates.get(i + 1);
				// 是否在形状点上
				if (Math.abs(lon - ce.x) < 0.0000001
						&& Math.abs(lat - ce.y) < 0.0000001) {
					continue;

				}
				// 是否在线段上
				if (GeoTranslator.isIntersection(new double[] { cs.x, cs.y },
						new double[] { ce.x, ce.y }, new double[] { lon, lat })) {
					coordinates.add(i + 1, point.getCoordinate());
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

	/**
	 * 
	 * @param link
	 * @param breakPoint
	 * @param pedalCoor
	 * @return
	 * @throws Exception
	 */
	public static LineString reformGeomtryByNode(LineString geometry, Set<Point> breakPoints) throws Exception {
		Coordinate[] coordinates = GeoTranslator.transform(geometry, 100000, 5).getCoordinates();
		List<Coordinate> coors = new ArrayList<Coordinate>();
		Collections.addAll(coors, coordinates);

		for(Point point: breakPoints){
			point = (Point) GeoTranslator.transform(point, 100000, 5);
			//打断点
			Coordinate breakPoint = new Coordinate(point.getX(),point.getY());
			
			//打断点对应的垂足点
			Coordinate pedalCoor = GeometryUtils.getLinkPedalPointOnLine(breakPoint, GeoTranslator.transform(geometry, 100000, 5));
			
			for (int i = 0; i < coors.size() - 1; i++) {
				Coordinate pointS = coors.get(i);
				Coordinate pointE = coors.get(i + 1);

				// 是否在形状点上
				if ((Math.abs(pedalCoor.x - pointE.x) < 0.0000001 && Math.abs(pedalCoor.y - pointE.y) < 0.0000001)
						|| (GeoTranslator.isIntersection(new double[] { pointS.x, pointS.y },
								new double[] { pointE.x, pointE.y }, new double[] { pedalCoor.x, pedalCoor.y }))) {
					coors.add(i + 1, breakPoint);
					break;
				}
			}//for
		}//for
		
		Coordinate[] c = (Coordinate[]) coors.toArray(new Coordinate[coors.size()]);
		return (LineString) GeoTranslator.transform(geoFactory.createLineString(c),0.00001, 5);
	}
	
	
	/***
	 * 按顺序返回线上对应的形状点 zhaokk
	 * 
	 * @param lineString
	 * @param points
	 * @return
	 * @throws Exception
	 */
	public static List<Point> getOrderPoints(LineString line, Set<Point> points)
			throws Exception {
		List<String> str = new ArrayList<String>();
		List<Point> list = new ArrayList<Point>();
		// LineString line = getReformLineString(lineString, points);
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

		LineString line = (LineString) transform(
				geoFactory.createLineString(coordinates), 100000, 5);

		Point point = (Point) transform(geoFactory.createPoint(p0), 100000, 5);
		

		if (line.distance(point) < dPrecisionGeo) {
			flag = true;
		}

		return flag;
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
			Coordinate p0,double precisionGeo) throws Exception {

		boolean flag = false;

		Coordinate[] coordinates = new Coordinate[2];

		coordinates[0] = p1;

		coordinates[1] = p2;
		
		int num0=String.valueOf(1/precisionGeo).length()-1;

		LineString line = (LineString) transform(
				geoFactory.createLineString(coordinates), 100000, 5+num0);

		Point point = (Point) transform(geoFactory.createPoint(p0), 100000, 5+num0);
		System.out.println("point1:"+p1.toString());
		System.out.println("point2:"+p2.toString());
		System.out.println("point0:"+p0.toString());
		System.out.println(line.distance(point));
		if (line.distance(point) < precisionGeo) {
			flag = true;
		}

		return flag;
	}
	
	/**
	 * 返回点距离线最近的距离
	 * @param lineCoordinates 线几何
	 * @param coord 点
	 * @return double 最新的距离
	 * @throws Exception
	 */
	public static double minDistince(Coordinate[] lineCoordinates, Coordinate coord,int precision) throws Exception {
		double minDis=10.0;
		Coordinate coorBefore=null;
		for(Coordinate linePoint:lineCoordinates){
			//交点与线的端点相同
			if(coord.equals(linePoint)){
				return 0.0;
			}
			//是否第一个点
			if(coorBefore==null){
				coorBefore=linePoint;
				continue;
			}
			//不是第一个点，判断交点是否在两点中间
			double myDis=distince(coorBefore, linePoint, coord, precision);
			if(minDis>myDis){
				minDis=myDis;
			}
			coorBefore=linePoint;
		}
		return minDis;
	}
	
	/**
	 * 点p0距离点p1和p2的线的距离
	 * 
	 * @param p1
	 *            线起点
	 * @param p2
	 *            线终点
	 * @param p0
	 *            点
	 * @return double 距离
	 * @throws Exception
	 */
	public static double distince(Coordinate p1, Coordinate p2,
			Coordinate p0,int precision) throws Exception {

		Coordinate[] coordinates = new Coordinate[2];

		coordinates[0] = p1;

		coordinates[1] = p2;

		LineString line = (LineString) transform(
				geoFactory.createLineString(coordinates), 100000, precision);

		Point point = (Point) transform(geoFactory.createPoint(p0), 100000, precision);
		return line.distance(point);
	}

	public static long round(double d) {
		return d < 0 ? (long) d : (long) (d + 0.5);
	}

	/**
	 * 判断两点是否坐标相同，两点坐标精度一致
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static boolean isPointEquals(Point point1, Point point2) {
		return isPointEquals(point1.getX(), point1.getY(), point2.getX(),
				point2.getY());
	}

	/**
	 * 判断两点是否坐标相同，两点坐标精度一致
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static boolean isPointEquals(Coordinate c1, Coordinate c2) {
		return isPointEquals(c1.x, c1.y, c2.x, c2.y);
	}

	/**
	 * 判断两点是否坐标相同，两点坐标精度一致
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static boolean isPointEquals(double x1, double y1, double x2,
			double y2) {

		if (x1 <= 180) {
			x1 = x1 * geoUpgrade;
			y1 = y1 * geoUpgrade;
		}
		if (x2 <= 180) {
			x2 = x2 * geoUpgrade;
			y2 = y2 * geoUpgrade;
		}
		// 7位精度
		// if (x1 >= 180 * geoUpgrade) {
		// x1 = x1 / 100;
		// y1 = y1 / 100;
		// }
		// if (x2 >= 180 * geoUpgrade) {
		// x2 = x2 / 100;
		// y2 = y2 / 100;
		// }

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
	
	/// <summary>
    /// 计算大地距离(单位:米) 输入坐标为真实坐标，形如（11648716，3983362）
    /// </summary>
    public static double CalculateSphereDistance(Point point1, Point point2)
    {
        if (isPointEquals(point1, point2))
        {
            return 0;
        }
        
		Coordinate start = transform(point1, dPrecisionMap, 5).getCoordinate();

		Coordinate stop = transform(point2, dPrecisionMap, 5).getCoordinate();

        double x1, x2, y1, y2;

        x1 = start.x;
        x2 = stop.x;
        y1 = start.y;
        y2 = stop.y;

        x1 = x1 * (Math.PI / 180.0);
        y1 = y1 * (Math.PI / 180.0);
        x2 = x2 * (Math.PI / 180.0);
        y2 = y2 * (Math.PI / 180.0);

        double R = 6367447.5;

        return 2 * R * Math.asin(0.5 * Math.sqrt(2 - 2 * Math.sin(y1) * Math.sin(y2) - 2 * Math.cos(y1) * Math.cos(y2) * Math.cos(x1 - x2)));
    }
    
    /**
	 * 点coord1，coord2必须在geo上，返回被点coord1，coord2切成多段的link列表。
	 * 点coord1，coord2在调用方法之前已经插入到geo中。相关判断在调用方法之前处理了
	 * @param geo
	 * @param coord1
	 * @param coord2
	 * @return
     * @throws Exception 
     * @throws JSONException 
	 */
	public static List<Geometry> splitGeoByPoint(Geometry geo,Coordinate coord1,Coordinate coord2) throws JSONException, Exception{		
		//点将线切成多段
		Coordinate[] lineCoordinates = geo.getCoordinates();
		
		List<Geometry> subLines=new ArrayList<Geometry>(); 
		List<Coordinate> tmpLine=new ArrayList<Coordinate>();
		Coordinate coorBefore=null;
		int num=0;
		for(Coordinate linePoint:lineCoordinates){
			//交点与线的端点相同
			if(coord1.equals(linePoint)||coord2.equals(linePoint)){
				num++;
				if(tmpLine.size()!=0){
					tmpLine.add(linePoint);
					subLines.add(GeoTranslator.createLineString(tmpLine));
					tmpLine=new ArrayList<Coordinate>();
				}
				tmpLine.add(linePoint);
				coorBefore=linePoint;
				continue;
			}
			//是否第一个点
			if(coorBefore==null){
				tmpLine.add(linePoint);
				coorBefore=linePoint;
				continue;
			}
			tmpLine.add(linePoint);
			coorBefore=linePoint;
		}
		if(tmpLine.size()>1){subLines.add(GeoTranslator.createLineString(tmpLine));}
		return subLines;
	}
	
	/**
	 * 前提：line与面有且只有2个交点，且线穿过面，相交部分在面内。
	 * 返回：面被line切成2个面，将切成的2个面返回
	 * @param line
	 * @param polygon
	 * @return
	 * @throws Exception
	 */
	public static List<Geometry> splitPolygonByLine(Geometry line,Geometry polygon,int precision) throws Exception{
		Geometry referGeoLine=GeoTranslator.createLineString(polygon.getCoordinates());
		//线是否穿过面
		Geometry interGeo=GeoTranslator.transform(referGeoLine.intersection(line),1,precision);
		
		if(interGeo==null||interGeo.getCoordinates().length==0){throw new Exception("线面没有交点");}
		if(interGeo.getCoordinates().length!=2){
			throw new Exception("线面交点大于2个，请重新画线");
		}
		
		//coord1，coord2为端点的线段
		Geometry midLine=GeoTranslator.transform(polygon.intersection(line),1,precision);
		//List<Coordinate> p1SEnodes=new ArrayList<>();
		MyGeometry unionGeo=GeoTranslator.addCoorToGeo(polygon, interGeo.getCoordinates()[0],null,precision);
		//List<Coordinate> p2SEnodes=new ArrayList<>();
		unionGeo=GeoTranslator.addCoorToGeo(unionGeo.getGeo(), interGeo.getCoordinates()[1],null,precision);
		boolean isIn=GeometryUtils.InteriorAnd2Intersection(midLine, unionGeo.getGeo());
		if(!isIn){
			throw new Exception("线不在面内，请重新划线");
		}
		Coordinate coord1 = interGeo.getCoordinates()[0];
		Coordinate coord2 = interGeo.getCoordinates()[1];
//		List<Geometry> subLines = GeoTranslator.splitGeoByPoint(line, coord1, coord2);
		
//		for(Geometry s:subLines){
//			Coordinate[] sCoors = s.getCoordinates();
//			Coordinate sStart = sCoors[0];
//			Coordinate sEnd = sCoors[sCoors.length-1];
//			if(GeoTranslator.isPointEquals(sStart,coord1)&&GeoTranslator.isPointEquals(sEnd,coord2)){
//				midLine=s;
//				continue;
//			}
//			if(GeoTranslator.isPointEquals(sStart,coord2)&&GeoTranslator.isPointEquals(sEnd,coord1)){
//				midLine=s;
//				continue;
//			}
//		}
		
		List<Geometry> subPolygonLines = GeoTranslator.splitGeoByPoint(unionGeo.getGeo(), coord1, coord2);
				
		List<Geometry> polygonLine1=new ArrayList<Geometry>();
		List<Geometry> polygonLine2=new ArrayList<Geometry>();
		List<Geometry> polygonLineTmp=polygonLine1;
		int i=1;
		
		for(Geometry s:subPolygonLines){
			Coordinate[] sCoors = s.getCoordinates();
			polygonLineTmp.add(s);
			if(sCoors[sCoors.length-1].equals(coord1)||sCoors[sCoors.length-1].equals(coord2)){
				if(i==1){
					Geometry orderMidLine=midLine;
					if(sCoors[sCoors.length-1].equals(orderMidLine.getCoordinates()[orderMidLine.getNumPoints()-1])){
						orderMidLine=orderMidLine.reverse();
					}
					polygonLineTmp.add(orderMidLine);
					polygonLineTmp=polygonLine2;i=2;
				}else{
					Geometry orderMidLine=midLine;
					if(sCoors[sCoors.length-1].equals(orderMidLine.getCoordinates()[orderMidLine.getNumPoints()-1])){
						orderMidLine=orderMidLine.reverse();
					}
					polygonLineTmp.add(orderMidLine);
					polygonLineTmp=polygonLine1;
				}
			}
		}
		
		//polygonLine1.add(subPolygonLines.get(0));
		//polygonLine1.add(midLine);
		Geometry p1 = GeoTranslator.getCalLineToPythonWithoutCheck(polygonLine1);
		
		//polygonLine2.add(subPolygonLines.get(1));
		//polygonLine2.add(midLine);
		Geometry p2 = GeoTranslator.getCalLineToPythonWithoutCheck(polygonLine2);
		
		List<Geometry> polygons=new ArrayList<Geometry>();
		polygons.add(p1);
		polygons.add(p2);
		
		return polygons;		
	}
	
	
	/**
	 * 点coord加到geo的形状点中
	 * 1.若有传入插入位置的前后形状点，则用前后形状点判断；
	 * 2.否则，按照最短距离取一个位置插入，距离相同随机取。并将前后点放入List<Coordinate> SENodes中
	 * 支持插入具体位置，支持自动计算插入位置（插入距离线段最近的位置）。若点插入则，返回几何+具体的位置
	 * @param geo
	 * @param coord
	 * @param SENodes 插入位置的前后形状点
	 * @return
     * @throws Exception 
     * @throws JSONException 
	 */
	public static MyGeometry addCoorToGeo(Geometry geo,Coordinate coord,List<Coordinate> SENodes,int precision) throws JSONException, Exception{
		//点将线切成多段
		Coordinate[] lineCoordinates = geo.getCoordinates();
		List<Coordinate> tmpLine=new ArrayList<Coordinate>();
		Coordinate coorBefore=null;
		double minDis=0.0;
		if(SENodes==null||SENodes.size()==0){
			minDis=minDistince(lineCoordinates, coord, precision);
		}
		List<Coordinate> mySENodes=new ArrayList<>();
		//点只能插入一次。
		boolean insertFlag=false;
		for(Coordinate linePoint:lineCoordinates){
			//交点与线的端点相同
			if(!insertFlag&&coord.equals(linePoint)){
				insertFlag=true;
				tmpLine.add(linePoint);
				coorBefore=linePoint;
				continue;
			}
			//是否第一个点
			if(coorBefore==null){
				tmpLine.add(linePoint);
				coorBefore=linePoint;
				continue;
			}
			/*不是第一个点，判断交点是否在两点中间。
			1.若有传入插入位置的前后形状点，则用前后形状点判断；
			2.否则，按照最短距离取一个位置插入，距离相同随机取。并将前后点放入List<Coordinate> SENodes中*/
			if(!insertFlag&&!coord.equals(coorBefore)){
				if(SENodes!=null&&SENodes.size()>0){
					if((coorBefore.equals(SENodes.get(0))&&linePoint.equals(SENodes.get(1)))
							||(coorBefore.equals(SENodes.get(1))&&linePoint.equals(SENodes.get(0)))){
						insertFlag=true;
						tmpLine.add(coord);
						tmpLine.add(linePoint);
						mySENodes.add(coorBefore);
						mySENodes.add(linePoint);
						coorBefore=linePoint;
						continue;
					}
				}else{
					double myDis=distince(coorBefore,linePoint, coord, precision);
					if(minDis==myDis){
						insertFlag=true;
						tmpLine.add(coord);
						tmpLine.add(linePoint);
						mySENodes.add(coorBefore);
						mySENodes.add(linePoint);
						coorBefore=linePoint;
						continue;
					}
				}
			}
			tmpLine.add(linePoint);
			coorBefore=linePoint;
		}
		List<Geometry> subLines=new ArrayList<Geometry>();
		subLines.add(GeoTranslator.createLineString(tmpLine));
		Geometry p = GeoTranslator.getCalLineToPythonWithoutCheck(subLines);
		MyGeometry myGeometry=new MyGeometry();
		myGeometry.setGeo(p);
		myGeometry.setSENodes(mySENodes);
		return myGeometry;
	}
	
	/**
	 * 处理geo，去除相邻的重复点.
	 * @param geo 线几何/面几何。
	 * @return
     * @throws Exception 
     * @throws JSONException 
	 */
	public static Geometry removeSameNode(Geometry geo) throws JSONException, Exception{
		Coordinate[] lineCoordinates = geo.getCoordinates();
		List<Coordinate> tmpLine=new ArrayList<Coordinate>();
		Coordinate coorBefore=null;
		for(Coordinate linePoint:lineCoordinates){
			if(linePoint.equals(coorBefore)){
				continue;
			}
			tmpLine.add(linePoint);
			coorBefore=linePoint;
		}
		Geometry lineGeo=GeoTranslator.createLineString(tmpLine);
		if(geo.getGeometryType().equals("Polygon")){
			List<Geometry> gList=new ArrayList<>();
			gList.add(lineGeo);
			Geometry pGeometry=GeoTranslator.getCalLineToPythonWithoutCheck(gList);
			return pGeometry;
		}
		return lineGeo;
	}

	public static void main(String[] args) throws Exception {
		Point point = (Point) wkt2Geometry("POINT (116.38636 40.00512)");
		Point point1 = (Point) wkt2Geometry("POINT (116.38617 40.00511)");
		Point point2 = (Point) wkt2Geometry("POINT (116.38602 40.0051)");
		Set<Point> points = new HashSet<Point>();
		points.add(point);
		points.add(point1);
		points.add(point2);
		LineString line = (LineString) wkt2Geometry("LineString(116.38586 40.00509, 116.38647 40.00512)");
		// LINESTRING (116.38586 40.00509, 116.38602 40.0051, 116.38617
		// 40.00511, 116.38636 40.00512, 116.38647 40.00512)
		LineString s = getReformLineString(line, points);
		System.out.println(s);

		// [POINT (116.38636 40.00512), POINT (116.38617 40.00511), POINT
		// (116.38602 40.0051)]

		// LINESTRING (116.38586 40.00509, 116.38636 40.00512, 116.38617
		// 40.00511, 116.38602 40.0051, 116.38647 40.00512)
	}
}
