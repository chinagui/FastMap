package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class GeoHelper {
	
	//5位坐标精度的容忍值为。。0.000005
	private static int DECIMAL_NUM=5;
	private static double COORDINATE_TOLERANCE=Math.pow(10, -1 * (DECIMAL_NUM))/ 2;

	public GeoHelper() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 功能：获取线段geo的中点坐标
	 * 返回值格式：String pointWkt = "Point ("+x+" "+y+")";
	 */
	public static Geometry getMidPointByLine(Geometry geo) throws Exception{
		Coordinate[] cs = geo.getCoordinates();
		
		int midP = (int)Math.round(cs.length/2d);
		double x = 0;
		double y = 0;
		if (cs.length%2==0){
			x = cs[midP-1].x;
			y = cs[midP-1].y;
		}
		else{
			x = cs[midP].x;
			y = cs[midP].y;}
		
		Geometry pointWkt = GeoTranslator.point2Jts(x, y);
		return pointWkt;		
		
	}
	
	/*
	 * 根据geo返回一个中心点，用于web端定位
	 */
	public static Geometry getPointFromGeo(Geometry geo) throws Exception{
		Geometry pointGeo;
		if (geo instanceof Point){pointGeo=geo;}
		else if (geo instanceof LineString){pointGeo=getMidPointByLine(geo);}
		else{pointGeo=geo.getCentroid();}
		return pointGeo;
	}
	
	/*
	 * 判断Geometry是否是简单线
	 * "geo":线坐标
	 * "inters":交点
	 * 返回值：true--简单;false--自相交线
	 */
	public static boolean isSample(Geometry geo,List<Point> inters){
		if(geo.getCoordinates().length<3){return true;}
		
		LineString lineStr=(LineString) geo;
		if(lineStr.isSimple()){return true;}
		Point startPoint=lineStr.getStartPoint();
		Point endPoint=lineStr.getEndPoint();
		
		Geometry geo2=lineStr.union(startPoint);
		MultiLineString multiLine=(MultiLineString) geo2;
		
		List<String> intersStr=new ArrayList<String>(); 		
		
		if(multiLine==null || multiLine.isEmpty()){return true;}
		
		//获取复杂line的组成简单line的起始点，其中排除复杂line本身的起始点
		for(int i=0;i<multiLine.getNumGeometries();i++){
			LineString lineTmp=(LineString) multiLine.getGeometryN(i);
			Point startTmp=lineTmp.getStartPoint();
			Point endTmp=lineTmp.getEndPoint();
			if(!isPointEquals(startPoint,startTmp) && !isPointEquals(endPoint,startTmp)){
				String pointStr=startTmp.getX()+","+startTmp.getY();
				if(!intersStr.contains(pointStr)){
					intersStr.add(pointStr);
					inters.add(startTmp);}
			}
			if(!isPointEquals(startPoint,endTmp) && !isPointEquals(endPoint,endTmp)){
				String pointStr=endTmp.getX()+","+endTmp.getY();
				if(!intersStr.contains(pointStr)){
					intersStr.add(pointStr);
					inters.add(endTmp);}
			}
		}
		if(inters.size()>0){return false;}
		
		return true;
	}
	
	/*
	 * 计算两条路径的交点
	 * 
	 * includeSEpoint 是否包含起始、结束点（端点）
	 * 路径交点，如果不相交则返回的集合数量为空
	 */
	public static List<Point> CalculateIntersection(Geometry geoA,Geometry geoB,boolean includeSEpoint){
		List<Point> inters=new ArrayList<Point>(); 
		LineString lineA=(LineString) geoA;
		LineString lineB=(LineString) geoB;
		Point startA=lineA.getStartPoint();
		Point endA=lineA.getEndPoint();
		Geometry joinGeo=lineA.intersection(lineB);
		Coordinate[] joinPoint=joinGeo.getCoordinates();
		//排除挂接点
		for(int i=0;i<joinPoint.length;i++){
			if(!includeSEpoint && (isPointEquals(joinPoint[i].x,joinPoint[i].y,startA.getX(),startA.getY()) 
					|| isPointEquals(joinPoint[i].x,joinPoint[i].y,endA.getX(),endA.getY()))){continue;}
			inters.add((Point) getPoint(joinPoint[i]));	
		}
		return inters;
	}
	
	private static Geometry getPoint(Coordinate coordinate) {
		return getPoint(coordinate.x,coordinate.y);
	}

	public static Geometry getPoint(double x,double y){
		Geometry point=null;
		try {
			point = GeoTranslator.transform(GeoTranslator.point2Jts(x, y),100000,0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return point;
	}
	
	public static boolean isPointEquals(Point p1,Point p2)
    {
		return isPointEquals(p1.getX(),p1.getY(),p2.getX(),p2.getY());
    }
	
	public static boolean isPointEquals(double x1, double y1, double x2, double y2)
    {
        if (Math.abs(x1 - x2) >= COORDINATE_TOLERANCE)
            return false;

        if (Math.abs(y1 - y2) >= COORDINATE_TOLERANCE)
            return false;

        return true;
    }

}
