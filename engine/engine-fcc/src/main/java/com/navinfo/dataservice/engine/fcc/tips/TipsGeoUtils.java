package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.AngleCalculator.LngLatPoint;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.navinfo.nirobot.common.utils.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @ClassName: TipsGeoUtils.java
 * @author y
 * @date 2017-1-3 下午7:46:12
 * @Description: 与tips业务相关的一些坐标计算
 * 
 */
public class TipsGeoUtils {

	/***
	 * 线跨图幅打断
	 * 
	 * @param map
	 * @param result
	 * @throws Exception
	 */
	public static List<Geometry> cutGeoByMeshes(Geometry geo)
			throws Exception {

		List<Geometry> resultGeosList = new ArrayList<Geometry>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		// 不跨图幅
		if (meshes.size() == 1) {
			return null;
		}
		// 跨图幅
		else {
			Iterator<String> it = meshes.iterator();
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
						GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),
								1, 5));
				if (geomInter instanceof GeometryCollection) {
					int geoNum = geomInter.getNumGeometries();
					for (int i = 0; i < geoNum; i++) {
						Geometry subGeo = geomInter.getGeometryN(i);
						if (subGeo instanceof LineString) {
							subGeo = GeoTranslator.geojson2Jts(
									GeoTranslator.jts2Geojson(subGeo), 1, 5);

							resultGeosList.addAll(getCutLinksWithMesh(subGeo));
						}
					}
				} else {
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);
					resultGeosList.addAll(getCutLinksWithMesh(geomInter));
				}
			}
		}
		return resultGeosList;
	}

	/***
	 * 跨图幅打断后的线是有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 
	 * @param g
	 * @param maps
	 * @throws Exception
	 */
	private static List<Geometry> getCutLinksWithMesh(Geometry g) throws Exception {
		List<Geometry> geos = new ArrayList<Geometry>();
		if (g != null) {

			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				geos.add(g);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					geos.add(g.getGeometryN(i));
				}
			}
		}
		return geos;
		
		
	}
	
	
    
    /**
	 * 找某条link上离指定点位最近的两个形状点
	 * 
	 * @param guidePoint
	 * @param line
	 * @param rdLink
	 * @param geoPoint
	 * @return LineString 返回两形状点组成的线
	 * @throws Exception
	 */
	public LineString getLineComposedByRecentTwoPoint(Geometry line,
			Point guidePoint) throws Exception {
		Double minDis = null;
		LineString newLine = null; // 离引导坐标最近的两个形状点的组成的线段

		int geoNum = line.getNumGeometries();
		for (int i = 0; i < geoNum; i++) {
			Geometry subGeo = line.getGeometryN(i);
			if (subGeo instanceof LineString) {
				Coordinate[] c_array = subGeo.getCoordinates();
				int num = -1; // 存放离的最近的形状点的顺序号
				for (int k = 0; k < c_array.length; k++) {
					double tmpDis = GeometryUtils.getDistance(
							guidePoint.getCoordinate(), c_array[k]);
					if (minDis == null || tmpDis < minDis) {
						minDis = tmpDis;
						num = k;
					}
				}
				Coordinate c_start = null;
				Coordinate c_end = null;

				if (num != -1) {
					if (num == 0) {
						c_start = c_array[0];
						c_end = c_array[1];
					} else if (num == c_array.length - 1) {
						c_start = c_array[c_array.length - 2];
						c_end = c_array[num];
					} else {
						double dis_last = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num - 1]);
						double dis_next = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num + 1]);
						if (dis_last < dis_next) {
							c_start = c_array[num - 1];
							c_end = c_array[num];
						} else {
							c_start = c_array[num];
							c_end = c_array[num + 1];
						}
					}

					Coordinate[] coordinates = new Coordinate[] { c_start,
							c_end };

					newLine = JtsGeometryFactory.createLineString(coordinates);
				}
			}
		}

		return newLine;
	}
	
	
    /**
	 * 找某条link上离指定点位最近的两个形状点
	 * 
	 * @param guidePoint
	 * @param line
	 * @param rdLink
	 * @param geoPoint
	 * @return LineString 返回两形状点组成的线
	 * @throws Exception
	 */
	public static List<Point> getRecentTwoPoint(Geometry line,
			Point guidePoint) throws Exception {
		List<Point> pointList=new ArrayList<Point>();
		Double minDis = null;
		int geoNum = line.getNumGeometries();
		for (int i = 0; i < geoNum; i++) {
			Geometry subGeo = line.getGeometryN(i);
			if (subGeo instanceof LineString) {
				Coordinate[] c_array = subGeo.getCoordinates();
				int num = -1; // 存放离的最近的形状点的顺序号
				for (int k = 0; k < c_array.length; k++) {
					double tmpDis = GeometryUtils.getDistance(
							guidePoint.getCoordinate(), c_array[k]);
					if (minDis == null || tmpDis < minDis) {
						minDis = tmpDis;
						num = k;
					}
				}
				Coordinate c_start = null;
				Coordinate c_end = null;

				if (num != -1) {
					if (num == 0) {
						c_start = c_array[0];
						c_end = c_array[1];
					} else if (num == c_array.length - 1) {
						c_start = c_array[c_array.length - 2];
						c_end = c_array[num];
					} else {
						double dis_last = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num - 1]);
						double dis_next = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num + 1]);
						if (dis_last < dis_next) {
							c_start = c_array[num - 1];
							c_end = c_array[num];
						} else {
							c_start = c_array[num];
							c_end = c_array[num + 1];
						}
					}

					Point point1=JtsGeometryFactory.createPoint(c_start);
					
					Point point2=JtsGeometryFactory.createPoint(c_end);
					
					pointList.add(point1);
					pointList.add(point2);
				}
			}
		}

		return pointList;
	}
	
	
	/**
	 * @Description:计算，打断后组成线和立交几何，左右2米的一个几何弧端
	 * @param g_location  测线几何
	 * @param g_location2 立交点
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-8-15 上午10:22:01
	 */
	public static  Geometry getGscLineGeo(Geometry lineGeo, Point gscPointGeo) throws Exception {
		//1.获取线 离点最近的两个形状点
		List<Point> recentTwoPoint= TipsGeoUtils.getRecentTwoPoint(lineGeo,gscPointGeo);
		
		//2.两个形状点分别和交点，组成一条线
		Point point1=recentTwoPoint.get(1);
		Coordinate[] coordinates = new Coordinate[] { 
				gscPointGeo.getCoordinate(),point1.getCoordinate() };
		Geometry newLine1 = JtsGeometryFactory.createLineString(coordinates);
		//3.计算线的如果长度<=2，则直接取这个点，如果大于2.则截图测线2m
		if(newLine1.getLength()>2){
		    Point newPoint = null;
	        LngLatPoint pointGscLngLa = new LngLatPoint(gscPointGeo.getX(), gscPointGeo.getY());
	        LngLatPoint point1LngLa= new LngLatPoint(point1.getX(), point1.getY());
	        double angle=AngleCalculator.getAngle(pointGscLngLa, point1LngLa); //角度就是 立交点和测线形状点的角度
	        LngLatPoint lnglatPoint = AngleCalculator.getMyLatLng(pointGscLngLa, 2/1000, angle);
	        Coordinate coordinate = new Coordinate(lnglatPoint.m_Longitude, lnglatPoint.m_Latitude);
	        newPoint = JtsGeometryFactory.createPoint(coordinate);
	        point1=newPoint;
		}
		
		Point point2=recentTwoPoint.get(1);
		Coordinate[] coordinates2 = new Coordinate[] { 
				gscPointGeo.getCoordinate(),point2.getCoordinate() };
		Geometry newLine2 = JtsGeometryFactory.createLineString(coordinates2);
		if(newLine2.getLength()>2){
		    Point newPoint = null;
	        LngLatPoint pointGscLngLa = new LngLatPoint(gscPointGeo.getX(), gscPointGeo.getY());
	        LngLatPoint point2LngLa= new LngLatPoint(point2.getX(), point2.getY());
	        double angle=AngleCalculator.getAngle(pointGscLngLa, point2LngLa); //角度就是 立交点和测线形状点的角度
	        LngLatPoint lnglatPoint = AngleCalculator.getMyLatLng(pointGscLngLa, 2/1000, angle);
	        Coordinate coordinate = new Coordinate(lnglatPoint.m_Longitude, lnglatPoint.m_Latitude);
	        newPoint = JtsGeometryFactory.createPoint(coordinate);
	        point2=newPoint;
		}
		
		Coordinate[] coordinatesResult = new Coordinate[] { 
				point1.getCoordinate(),point2.getCoordinate() };
		Geometry resultLine = JtsGeometryFactory.createLineString(coordinatesResult);
		
		return resultLine;
	}
	
	

}
