package com.navinfo.dataservice.engine.check.helper;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class GeoHelper {

	public GeoHelper() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 功能：获取线段geo的中点坐标
	 * 返回值格式：String pointWkt = "Point ("+x+" "+y+")";
	 */
	public static Geometry getMidPointByLine(Geometry geo) throws Exception{
		Coordinate[] cs = geo.getCoordinates();
		
		int midP = (int)Math.round(cs.length/2);
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

}
