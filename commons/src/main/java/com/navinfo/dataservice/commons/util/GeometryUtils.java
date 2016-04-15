package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryUtils {
	
	private static double EARTH_RADIUS = 6378137;
	private static double metersPerDegree = 2.0 * Math.PI * EARTH_RADIUS / 360.0; 
	private static double radiansPerDegree = Math.PI / 180.0; 
	private static double rad(double d)
	{
	    return d * Math.PI / 180.0;
	}

	public static double getDistance(double lat1, double lng1, double lat2, double lng2)
	{
	    double radLat1 = rad(lat1);
	    double radLat2 = rad(lat2);
	    double a = radLat1 - radLat2;
	    double b = rad(lng1) - rad(lng2);
	    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 
	     Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	    s = s * EARTH_RADIUS;
	    s = Math.round(s * 10000) / 10000.0;
	    return s;
	}
	
	public static double getDistance(Coordinate coord1, Coordinate coord2)
	{
	    return getDistance(coord1.y, coord1.x, coord2.y, coord2.x);
	}
	
	 /**
	  * 是否是逆时针方向
	 * @param ring
	 * @return
	 * @throws Exception 
	 */
	public static boolean IsCCW(Coordinate[] ring) throws Exception{
		 
		// # of points without closing endpoint
        int nPts = ring.length - 1;

        // check that this is a valid ring - if not, simply return a dummy value
        if (nPts < 3)
        {
            return false;
        }

        // algorithm to check if a Ring is stored in CCW order
        // find highest point
        Coordinate hip = ring[0];
        int hii = 0;

        for (int i = 1; i <= nPts; i++)
        {
            Coordinate p = ring[i];

            if (p.y > hip.y)
            {
                hip = p;
                hii = i;
            }
        }

        // find different point before highest point
        int iPrev = hii;
        if (iPrev > 0)
        {
            do
            {
                iPrev = (iPrev - 1) % nPts;
            } while (ring[iPrev].equals(hip) && iPrev != hii);
        }
        else //宋慧星修改
        {
            iPrev = nPts;
            while (ring[iPrev].equals(hip) && iPrev != hii)
            {
                iPrev = (iPrev - 1) % nPts;
            }
        }

        // find different point after highest point
        int iNext = hii;
        do
        {
            iNext = (iNext + 1) % nPts;
        } while (ring[iNext].equals(hip) && iNext != hii);

        Coordinate prev = ring[iPrev];
        Coordinate next = ring[iNext];

        if (prev.equals(hip) || next.equals(hip) || prev.equals(next))
        {
            throw new Exception("degenerate ring (does not contain 3 different points)");
        }

        // translate so that hip is at the origin.
        // This will not affect the area calculation, and will avoid
        // finite-accuracy errors (i.e very small vectors with very large coordinates)
        // This also simplifies the discriminant calculation.
        double prev2x = prev.x - hip.x;
        double prev2y = prev.y - hip.y;
        double next2x = next.x - hip.x;
        double next2y = next.y - hip.y;

        // compute cross-product of vectors hip->next and hip->prev
        // (e.g. area of parallelogram they enclose)
        Double disc = next2x * prev2y - next2y * prev2x;

        /* If disc is exactly 0, lines are collinear.  There are two possible cases:
                (1) the lines lie along the x axis in opposite directions
                (2) the line lie on top of one another
                
                (2) should never happen, so we're going to ignore it!
                    (Might want to assert this)
        
                (1) is handled by checking if next is left of prev ==> CCW
        */
        if (disc == 0.0)
        {
            return (prev.x > next.x); // poly is CCW if prev x is right of next x
        }
        else
        {
            return (disc > 0.0); // if area is positive, points are ordered CCW                 
        }
		 
	 }

	public static double getLinkLength(Geometry g){
		
		double length=0;
		
		Coordinate[] coords = g.getCoordinates();
		
		for(int i=0;i<coords.length-1;i++){
			
			Coordinate p1 = coords[i];
			
			Coordinate p2 = coords[i+1];
			
			length+=getDistance(p1.y, p1.x, p2.y, p2.x);
			
		}
		
		length = Math.round(length * 10000) / 10000.0;
		
		return length;
	}
	
	public static double getLinkLength(String wkt) throws ParseException{
		
		WKTReader reader = new WKTReader();
		
		Geometry g = reader.read(wkt);
		
		return getLinkLength(g);
	}

	public static Geometry getIntersectsGeo(List<Geometry> geometryList) {

		Geometry geo0 = geometryList.get(0);

		Geometry geo1 = geometryList.get(1);

		Geometry result = GeoTranslator.transform(geo0.intersection(geo1), 1, 5);

		for (int i = 1; i < (geometryList.size() - 1); i++) {
			Geometry tmp1 = geometryList.get(i);

			Geometry tmp2 = geometryList.get(i + 1);

			if (tmp1.intersects(tmp2)) {

				Geometry interGeo = tmp1.intersection(tmp2);

				// 距离大于0代表不相交
				if (result.distance(interGeo) > 0) {

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

	/**
	 * 获取交点和link的关系：是否是起点、终点、形状点（1：起点 2:终点 0：形状点）
	 * @param geometries 需要对比的几何
	 * @param standGeo 作为对比的几何
	 * @return Map<Integer,Integer> value:start_end 标识
	 */
	public static Map<Object,Integer> getStartOrEndType(List<Geometry> geometries, Geometry standGeo) 
	{
		Map<Object,Integer> startEndMap = new HashMap<>();
		
		for(Geometry compared : geometries)
		{
			if (compared.getBoundary().getGeometryN(0).equals(standGeo.getBoundary().getGeometryN(0))) {
				
				startEndMap.put(compared.getUserData(), 1);
			} else if (compared.getBoundary().getGeometryN(1).equals(standGeo)) {
				
				startEndMap.put(compared.getUserData(), 2);
			} else {
				
				startEndMap.put(compared.getUserData(), 0);
			}
		}
		return startEndMap;
	}

	public static Geometry getPolygonByWKT(String wkt) throws ParseException {

		WKTReader reader = new WKTReader();

		Polygon polygon = (Polygon) reader.read(wkt);

		return polygon;
	}

	public static void main(String[] args) throws Exception {

		WKTReader r = new WKTReader();

		String a = "LINESTRING (117.35746 39.13152, 117.35761 39.13144, 117.35788 39.13133, 117.35806 39.13128, 117.35824 39.13124, 117.35869 39.13117, 117.35908 39.13113, 117.35957 39.1311, 117.35984 39.1311, 117.36012 39.13112, 117.36057 39.13118, 117.36136 39.13142, 117.36189 39.13158, 117.36232 39.13173)";

		Geometry g = r.read(a);

		// System.out.println(GeometryUtils.getLinkLength(g));

		String test1 = " LINESTRING (116.05539 39.87195, 116.05554 39.87162, 116.05578 39.87162, 116.05567 39.87190)";

		String test2 = "LINESTRING (116.05524 39.87189, 116.05571 39.87167, 116.05580 39.87190)";

		String test3 = "LINESTRING (116.04920 39.86528, 116.04987 39.86426, 116.05038 39.86348)";

		Geometry g1 = r.read(test1);

		Geometry g2 = r.read(test2);

		Geometry g3 = r.read(test3);

		List<Geometry> list = new ArrayList<>();

		list.add(g1);

		list.add(g2);

		//list.add(g3);

		System.out.println(getIntersectsGeo(list));
		System.out.println(getIntersectsGeo(list).getUserData());
	}
	

	public static double getCalculateArea(Geometry g) { 
		double area =0.0;
		Coordinate[] coordinates = g.getCoordinates();
		List<double[]> points = new ArrayList<double[]>();
		if (coordinates.length > 2) {  
			for(Coordinate c :coordinates){
				double[] point = {c.x , c.y};
				points.add(point);
			}
			area = PlanarPolygonAreaMeters(points); 
			
		} return area;
	} 
	/** 
	* @Description:TODO
	平面多边形面积
	* @param points double[0] longitude; 
	double[1] latitude 
	* @return 
	*/ 
	public static double PlanarPolygonAreaMeters(List<double[]> points) { 

		double a = 0.0;  
		for (int i = 0; i < points.size(); ++i) { 
			int j = (i + 1) % points.size();  
			double xi = points.get(i)[0] * metersPerDegree * Math.cos(points.get(i)[1] * 
			radiansPerDegree);  
			double yi = points.get(i)[1] * metersPerDegree;  
			double xj = points.get(j)[0] * metersPerDegree * Math.cos(points.get(j)[1] * 
			radiansPerDegree);  
			double yj = points.get(j)[1] * metersPerDegree; 
		a += xi * yj - xj * yi; 
		}  
		return Math.abs(a / 2.0); 
	} 

}