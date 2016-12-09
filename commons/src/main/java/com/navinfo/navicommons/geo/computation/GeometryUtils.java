package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONObject;

public class GeometryUtils {
	private static double EARTH_RADIUS = 6378137;
	private static double metersPerDegree = 2.0 * Math.PI * EARTH_RADIUS
			/ 360.0;
	private static double radiansPerDegree = Math.PI / 180.0;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double convert2Degree(double distance) {
		return distance / metersPerDegree;
	}

	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000.0;
		return s;
	}

	public static double getDistance(Coordinate coord1, Coordinate coord2) {
		return getDistance(coord1.y, coord1.x, coord2.y, coord2.x);
	}

	/**
	 * 通过两个点生成一个线段
	 * 
	 * @param p1
	 *            点1
	 * @param p2
	 *            点2
	 * @return line的Geo
	 * @throws Exception
	 */
	public static Geometry getLineFromPoint(double[] p1, double[] p2)
			throws Exception {
		StringBuilder sb = new StringBuilder("LINESTRING (" + p1[0]);

		sb.append(" " + p1[1]);

		sb.append(",");

		sb.append(p2[0]);

		sb.append(" " + p2[1]);

		sb.append(")");

		Geometry line = new WKTReader().read(sb.toString());

		return line;
	}

	/**
	 * 是否是逆时针方向
	 * 
	 * @param ring
	 * @return
	 * @throws Exception
	 */
	public static boolean IsCCW(Coordinate[] ring) throws Exception {

		// # of points without closing endpoint
		int nPts = ring.length - 1;

		// check that this is a valid ring - if not, simply return a dummy value
		if (nPts < 3) {
			return false;
		}

		// algorithm to check if a Ring is stored in CCW order
		// find highest point
		Coordinate hip = ring[0];
		int hii = 0;

		for (int i = 1; i <= nPts; i++) {
			Coordinate p = ring[i];

			if (p.y > hip.y) {
				hip = p;
				hii = i;
			}
		}

		// find different point before highest point
		int iPrev = hii;
		if (iPrev > 0) {
			do {
				iPrev = (iPrev - 1) % nPts;
			} while (ring[iPrev].equals(hip) && iPrev != hii);
		} else // 宋慧星修改
		{
			iPrev = nPts;
			while (ring[iPrev].equals(hip) && iPrev != hii) {
				iPrev = (iPrev - 1) % nPts;
			}
		}

		// find different point after highest point
		int iNext = hii;
		do {
			iNext = (iNext + 1) % nPts;
		} while (ring[iNext].equals(hip) && iNext != hii);

		Coordinate prev = ring[iPrev];
		Coordinate next = ring[iNext];

		if (prev.equals(hip) || next.equals(hip) || prev.equals(next)) {
			throw new Exception(
					"degenerate ring (does not contain 3 different points)");
		}

		// translate so that hip is at the origin.
		// This will not affect the area calculation, and will avoid
		// finite-accuracy errors (i.e very small vectors with very large
		// coordinates)
		// This also simplifies the discriminant calculation.
		double prev2x = prev.x - hip.x;
		double prev2y = prev.y - hip.y;
		double next2x = next.x - hip.x;
		double next2y = next.y - hip.y;

		// compute cross-product of vectors hip->next and hip->prev
		// (e.g. area of parallelogram they enclose)
		Double disc = next2x * prev2y - next2y * prev2x;

		/*
		 * If disc is exactly 0, lines are collinear. There are two possible
		 * cases: (1) the lines lie along the x axis in opposite directions (2)
		 * the line lie on top of one another
		 * 
		 * (2) should never happen, so we're going to ignore it! (Might want to
		 * assert this)
		 * 
		 * (1) is handled by checking if next is left of prev ==> CCW
		 */
		if (disc == 0.0) {
			return (prev.x > next.x); // poly is CCW if prev x is right of next
										// x
		} else {
			return (disc > 0.0); // if area is positive, points are ordered CCW
		}

	}

	public static double getLinkLength(Geometry g) {

		double length = 0;

		Coordinate[] coords = g.getCoordinates();

		for (int i = 0; i < coords.length - 1; i++) {

			Coordinate p1 = coords[i];

			Coordinate p2 = coords[i + 1];

			length += getDistance(p1.y, p1.x, p2.y, p2.x);

		}

		length = Math.round(length * 10000) / 10000.0;

		return length;
	}

	public static double getLinkLength(String wkt) throws ParseException {

		WKTReader reader = new WKTReader();

		Geometry g = reader.read(wkt);

		return getLinkLength(g);
	}

	/**
	 * 获取自相交线的交点
	 * 
	 * @param geometryList
	 *            自相交线的线段几何的集合
	 * @return 自相交线的交点
	 * @throws Exception
	 */
	public static Geometry getIntersectGeoBySingleLine(
			List<Geometry> geometryList) throws Exception {

		StringBuilder sb = new StringBuilder("MULTIPOINT (");

		List<Coordinate> coors = new ArrayList<>();

		List<Geometry> geoList = new ArrayList<>();

		for (int i = 0; i < geometryList.size() - 1; i++) {

			Geometry tmp1 = geometryList.get(i);

			for (int j = i; j < geometryList.size() - 1; j++) {

				Geometry tmp2 = geometryList.get(j + 1);

				if (tmp1.intersects(tmp2)) {
					Geometry interGeo = tmp1.intersection(tmp2);

					Coordinate coor = interGeo.getCoordinate();

					if (!tmp1.touches(tmp2)) {
						coors.add(coor);
					} else {
						if (geoList.contains(interGeo) && !coors.contains(coor)) {
							coors.add(coor);
						} else {
							geoList.add(interGeo);
						}
					}
				}
			}
		}

		for (Coordinate coor : coors) {
			sb.append(coor.x + " ");

			sb.append(coor.y + ",");
		}

		if (sb.toString().contains(",")) {
			sb.deleteCharAt(sb.lastIndexOf(","));
		}

		sb.append(")");

		return getMulPointByWKT(sb.toString());
	}

	/**
	 * 获取空间多条线的交点几何（立交和平交都适用，可能有多个交点）
	 * 
	 * @param geometryList
	 *            线的几何的集合
	 * @return 多条线的交点几何
	 */
	public static Geometry getIntersectsGeo(List<Geometry> geometryList) {

		Geometry geo0 = geometryList.get(0);

		Geometry geo1 = geometryList.get(1);

		Geometry result = GeoTranslator
				.transform(geo0.intersection(geo1), 1, 0);

		for (int i = 1; i < (geometryList.size() - 1); i++) {
			Geometry tmp1 = geometryList.get(i);

			Geometry tmp2 = geometryList.get(i + 1);

			if (tmp1.intersects(tmp2)) {

				Geometry interGeo = tmp1.intersection(tmp2);

				// 距离大于0代表不相交
				if (result.distance(interGeo) > 1) {

					result = null;

					break;
				}
			} else {
				result = null;
				break;
			}
		}

		return result;
	}

	/*
	 * 功能：获取线段geo的中点坐标 返回值格式：String pointWkt = "Point ("+x+" "+y+")";
	 */
	public static Geometry getMidPointByLine(Geometry geo) throws Exception {
		Coordinate[] cs = geo.getCoordinates();

		int midP = (int) Math.round(cs.length / 2d);
		double x = 0;
		double y = 0;
		if (cs.length % 2 == 0) {
			x = cs[midP - 1].x;
			y = cs[midP - 1].y;
		} else {
			x = cs[midP].x;
			y = cs[midP].y;
		}

		Geometry pointWkt = GeoTranslator.point2Jts(x, y);
		return pointWkt;

	}

	/*
	 * 根据geo返回一个中心点，用于web端定位
	 */
	public static Geometry getPointFromGeo(Geometry geo) throws Exception {
		Geometry pointGeo;
		if (geo instanceof Point) {
			pointGeo = geo;
		} else if (geo instanceof LineString) {
			pointGeo = getMidPointByLine(geo);
		} else {
			pointGeo = geo.getCentroid();
		}
		return pointGeo;
	}

	/**
	 * 获取一条link自相交的点 （可能有多个点）
	 * 
	 * @param geometry
	 * @return
	 * @throws Exception
	 */
	public static Geometry getInterPointFromSelf(Geometry geometry)
			throws Exception {

		Coordinate coorArray[] = geometry.getCoordinates();

		List<Geometry> geometryList = new ArrayList<>();

		for (int i = 0; i < coorArray.length - 1; i++) {
			Coordinate coor1 = coorArray[i];

			Coordinate coor2 = coorArray[i + 1];

			double p1[] = { coor1.x, coor1.y };

			double p2[] = { coor2.x, coor2.y };

			Geometry line = getLineFromPoint(p1, p2);

			geometryList.add(line);
		}

		Geometry geo = getIntersectGeoBySingleLine(geometryList);

		geo = GeoTranslator.transform(geo, 1, 0);

		return geo;
	}

	/**
	 * 获取交点和link的关系：是否是起点、终点、形状点（1：起点 2:终点 0：形状点）
	 * 
	 * @param geometries
	 *            需要对比的几何
	 * @param standGeo
	 *            作为对比的几何
	 * @return Map<Integer,Integer> value:start_end 标识
	 * @throws JSONException
	 */
	public static Integer getStartOrEndType(Coordinate[] coors,
			Geometry standGeo) throws JSONException {
		int flag = 0;
		if (coors[0].equals(standGeo.getCoordinate())) {

			flag = 1;
		} else if (coors[coors.length - 1].equals(standGeo.getCoordinate())) {

			flag = 2;
		} else {
			flag = 0;
		}
		return flag;
	}

	public static Geometry getPolygonByWKT(String wkt) throws ParseException {

		WKTReader reader = new WKTReader();

		Polygon polygon = (Polygon) reader.read(wkt);

		return polygon;
	}

	/**
	 * create multiPoint by wkt
	 * 
	 * @return
	 */
	public static MultiPoint getMulPointByWKT(String wkt) throws ParseException {

		WKTReader reader = new WKTReader();

		MultiPoint mpoint = (MultiPoint) reader.read(wkt);

		return mpoint;
	}

	public static double[] getCoordinate(Geometry geo) {

		Coordinate[] coords = geo.getCoordinates();

		double[] points = new double[coords.length * 2];

		for (int i = 0; i < coords.length; i++) {

			points[2 * i] = coords[i].x;

			points[2 * i + 1] = coords[i].y;
		}

		return points;
	}

	public static double getCalculateArea(Geometry g) {
		double area = 0.0;
		Coordinate[] coordinates = g.getCoordinates();
		List<double[]> points = new ArrayList<double[]>();
		if (coordinates.length > 2) {
			for (Coordinate c : coordinates) {
				double[] point = { c.x, c.y };
				points.add(point);
			}
			area = PlanarPolygonAreaMeters(points);

		}
		return area;
	}

	/**
	 * @Description:TODO 平面多边形面积
	 * @param points
	 *            double[0] longitude; double[1] latitude
	 * @return
	 */
	public static double PlanarPolygonAreaMeters(List<double[]> points) {

		double a = 0.0;
		for (int i = 0; i < points.size(); ++i) {
			int j = (i + 1) % points.size();
			double xi = points.get(i)[0] * metersPerDegree
					* Math.cos(points.get(i)[1] * radiansPerDegree);
			double yi = points.get(i)[1] * metersPerDegree;
			double xj = points.get(j)[0] * metersPerDegree
					* Math.cos(points.get(j)[1] * radiansPerDegree);
			double yj = points.get(j)[1] * metersPerDegree;
			a += xi * yj - xj * yi;
		}
		return Math.abs(a / 2.0);
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

		Coordinate last = null;

		for (Coordinate c : cs) {

			if (c.equals(last)) {
				continue;
			}

			last = c;

			double[] p = new double[2];

			p[0] = c.x;

			p[1] = c.y;

			ps.add(p);
		}

		json.put("coordinates", ps);

		return json;
	}

	/**
	 * @Description:TODO 两个面是否相交
	 * @param String
	 *            scrWkt,Geometry geom
	 * @return boolean
	 * @throws ParseException
	 */
	public static boolean IsIntersectPolygon(String scrWkt, String clobStr)
			throws ParseException {

		Geometry srcGeom = GeometryUtils.getPolygonByWKT(scrWkt);
		Geometry Geom = GeometryUtils.getPolygonByWKT(clobStr);
		return srcGeom.intersects(Geom);
	}

	public static LineString getBuffer(Coordinate[] coordinates, double distance) {

		GeometryFactory geometryFactory = new GeometryFactory();

		MultiPoint MultiPoint = geometryFactory.createMultiPoint(coordinates);

		ConvexHull hull = new ConvexHull(MultiPoint);

		Geometry geosRing = hull.getConvexHull();

		Geometry buff = geosRing.buffer(distance);

		Polygon myPolygon = (Polygon) buff;

		LineString exteriorRing = myPolygon.getExteriorRing();

		return exteriorRing;

	}

	/**
	 * 计算点与移动后的线之间最近点的坐标
	 * 
	 * @param point
	 * @param geom
	 * @return
	 */
	public static Coordinate GetNearestPointOnLine(Coordinate point,
			Geometry geom) {
		Coordinate[] coll = geom.getCoordinates();

		Coordinate targetPoint = new Coordinate();

		if (coll.length < 2) {
			return null;
		}

		double minDistance = 0;

		targetPoint = coll[0];

		minDistance = GeometryUtils.getDistance(point, targetPoint);

		for (int i = 0; i < coll.length - 1; i++) {
			Coordinate point1 = new Coordinate();
			Coordinate point2 = new Coordinate();
			Coordinate pedalPoint = new Coordinate();

			point1 = coll[i];
			point2 = coll[i + 1];

			pedalPoint = GetPedalPoint(point1, point2, point);

			boolean isPointAtLine = IsPointAtLineInter(point1, point2,
					pedalPoint);

			// 如果在线上
			if (isPointAtLine) {
				double pedalLong = GeometryUtils.getDistance(point, pedalPoint);
				if (pedalLong < minDistance) {
					minDistance = pedalLong;
					targetPoint = pedalPoint;
				}
			} else {
				// 计算与点1的最小距离
				double long1 = GeometryUtils.getDistance(point1, point);
				// 计算与点2的最小距离
				double long2 = GeometryUtils.getDistance(point2, point);
				if (long1 <= long2) {
					if (long1 < minDistance) {
						minDistance = long1;
						targetPoint = point1;
					}
				} else {
					if (long2 < minDistance) {
						minDistance = long2;
						targetPoint = point2;
					}
				}
			}
		}
		return targetPoint;
	}
	
	
	/**
	 * 计算点与的线垂足点位
	 * 
	 * @param point
	 * @param geom
	 * @return
	 */
	public static Coordinate getLinkPedalPointOnLine(Coordinate point,
			Geometry geom) {
		Coordinate[] coll = geom.getCoordinates();

		Coordinate targetPoint = new Coordinate();

		if (coll.length < 2) {
			return null;
		}

		double minDistance = 0;

		targetPoint = null;

		minDistance = Double.MAX_VALUE;

		for (int i = 0; i < coll.length - 1; i++) {
			Coordinate point1 = new Coordinate();
			Coordinate point2 = new Coordinate();
			Coordinate pedalPoint = new Coordinate();

			point1 = coll[i];
			point2 = coll[i + 1];

			pedalPoint = GetPedalPoint(point1, point2, point);

			boolean isPointAtLine = IsPointAtLineInter(point1, point2,
					pedalPoint);

			// 如果在线上
			if (isPointAtLine) {
				double pedalLong = GeometryUtils.getDistance(point, pedalPoint);
				if (pedalLong < minDistance) {
					minDistance = pedalLong;
					targetPoint = pedalPoint;
				}
			} 
		}
		return targetPoint;
	}


	/**
	 * 计算垂足点
	 */
	public static Coordinate GetPedalPoint(Coordinate point1,
			Coordinate point2, Coordinate point) {
		Coordinate targetPoint = new Coordinate();

		double x1, x2, y1, y2;
		x1 = point1.x;
		y1 = point1.y;
		x2 = point2.x;
		y2 = point2.y;

		if (x1 == x2 && y1 == y2) {
			return null;
		} else if (x1 == x2) {
			targetPoint.x = x1;
			targetPoint.y = point.y;
		} else if (y1 == y2) {
			targetPoint.x = point.x;
			targetPoint.y = y1;
		} else {
			double k = (y2 - y1) / (x2 - x1);
			double x = (k * k * x1 + k * (point.y - y1) + point.x)
					/ (k * k + 1);
			double y = k * (x - x1) + y1;

			targetPoint.x = x;
			targetPoint.y = y;
		}
		return targetPoint;
	}

	/**
	 * 计算点位在link的位置（side = 1：左侧，side=2：右侧，side = 3：link上）
	 * 
	 * @param point
	 *            需要计算的点位坐标
	 * @param link
	 *            link的几何
	 * @param guideGeo
	 *            点在线上的垂足点位
	 * @return 位置信息
	 * @throws Exception
	 */
	public static int calulatPointSideOflink(Geometry point, Geometry link,
			Geometry guideGeo) throws Exception {
		int side = 0;

		// 如果poi点位在线上则更新side为3，否则计算左右
		if (point.distance(link) <= 1) {
			side = 3;
		} else {
			// poi的位置点
			DoublePoint doublePoint = new DoublePoint(point.getCoordinate().x,
					point.getCoordinate().y);

			Coordinate cor[] = link.getCoordinates();

			for (int i = 0; i < cor.length - 1; i++) {

				Coordinate cor1 = cor[i];

				Coordinate cor2 = cor[i + 1];

				// 判断点是否在线段上
				boolean isIntersection = GeoTranslator.isIntersectionInLine(
						new double[] { cor1.x, cor1.y },
						new double[] { cor2.x, cor2.y },
						new double[] { guideGeo.getCoordinate().x,
								guideGeo.getCoordinate().y });
				if (isIntersection) {

					DoublePoint startPoint = new DoublePoint(cor1.x, cor1.y);

					DoublePoint endPoint = new DoublePoint(cor2.x, cor2.y);

					DoubleLine doubleLine = new DoubleLine(startPoint, endPoint);

					boolean flag = CompLineUtil.isRightSide(doubleLine,
							doublePoint);

					if (flag) {
						side = 2;

					} else {
						side = 1;
					}
					break;
				}
			}
		}

		return side;
	}

	/**
	 * 判断点point是否在point1和point2组成的线上
	 */
	private static boolean IsPointAtLineInter(Coordinate point1,
			Coordinate point2, Coordinate point) {
		boolean result = false;

		LineSegment lineSegment = new LineSegment(point1, point2);

		if (lineSegment.distance(point) > 1) {

			return result;
		}

		double x1, x2, y1, y2, x, y;

		x1 = point1.x;
		y1 = point1.y;
		x2 = point2.x;
		y2 = point2.y;
		x = point.x;
		y = point.y;

		if (x >= min(x1, x2) && x <= max(x1, x2) && y >= min(y1, y2)
				&& y <= max(y1, y2)) {
			result = true;
		}
		return result;
	}

	/**
	 * 判断两点的最小值
	 * 
	 * @param x1
	 * @param x2
	 * @return
	 */
	private static double min(double x1, double x2) {
		if (x1 > x2)
			return x2;
		else
			return x1;
	}

	/**
	 * 判断两点的最大值
	 * 
	 * @param x1
	 * @param x2
	 * @return
	 */
	private static double max(double x1, double x2) {
		if (x1 < x2)
			return x2;
		else
			return x1;
	}

	/**
	 * 计算在直线线上的距离端点距离为dist的点的坐标
	 * 
	 * @author zhaokk
	 * @param coord
	 *            墨卡托坐标
	 * @param next
	 *            墨卡托坐标
	 * @param dist
	 *            要获取的点距离coord点的距离（米）
	 * @return 经纬度坐标
	 */
	public static Coordinate getPointOnLineSegmentByDistance(Coordinate coord,
			Coordinate next, double dist) {
		Coordinate result = new Coordinate();

		double distance = Math.sqrt(Math.pow(next.x - coord.x, 2)
				+ Math.pow(next.y - coord.y, 2));

		if (coord.x != next.x) {
			double k = (coord.y - next.y) / (coord.x - next.x);

			double c = coord.y - k * coord.x;

			result.x = coord.x + (dist / distance / 100000)
					* (next.x - coord.x);

			result.y = k * result.x + c;
		} else {
			// 与x轴垂直

			result.x = coord.x;

			if (coord.y < next.y) {
				result.y = coord.y + dist / 100000;
			} else {
				result.y = coord.y - dist / 100000;
			}
		}

		return result;
	}

	/**
	 * 计算在线上的距离端点距离为dist的点的坐标
	 * 
	 * @author zhaokk
	 * @param dist
	 *            要获取的点距离coord点的距离（米）
	 * @return 经纬度坐标
	 * @throws Exception
	 */
	public static Coordinate getPointOnLineStringDistance(
			LineString lineString, double dist) throws Exception {
		Coordinate c = null;
		double length = 0.0;
		for (int i = 1; i < lineString.getCoordinates().length; i++) {
			Coordinate prePoint = lineString.getCoordinates()[i - 1];
			Coordinate currentPoint = lineString.getCoordinates()[i];
			Geometry g = getLineFromPoint(
					new double[] { prePoint.x, prePoint.y }, new double[] {
							currentPoint.x, currentPoint.y });
			double currentLength = getLinkLength(g);
			length += getLinkLength(g);
			if (Math.abs(dist - length) < 1) {
				c = currentPoint;
				break;
			}
			if (length > dist) {
				c = getPointOnLineSegmentByDistance(prePoint,
						currentPoint, dist + currentLength - length);
				break;
			}
		}

		return c;

	}

	public static void main(String[] args) throws Exception {

		WKTReader r = new WKTReader();

		String test1 = " LINESTRING (116.05539 39.87195, 116.05554 39.87162,116.05578 39.87162, 116.05567 39.87190)";

		String test2 = "LINESTRING (116.05524 39.87189, 116.05571 39.87167,116.05580 39.87190)";

		String test3 = "LINESTRING (116.04920 39.86528, 116.04987 39.86426,116.05038 39.86348)";
		String test4 = "LINESTRING (116.3902 39.9983, 116.3903 39.9984, 116.3905 39.9983, 116.3905 39.9984, 116.3907 39.9983, 116.3907 39.9985, 116.3909 39.9983, 116.3908 39.9985, 116.3911 39.9983, 116.3911 39.9985)";
		Geometry g1 = r.read(test1);

		Geometry g2 = r.read(test2);

		Geometry g3 = r.read(test3);
		Geometry g4 = r.read(test4);
		System.out.println(getLinkLength(g4));
		Coordinate c = getPointOnLineStringDistance((LineString) g4,
				82.89500000000001);
		System.out.println(c.x);
		System.out.println(c.y);
		// 116.39053, 39.99844
	}
}