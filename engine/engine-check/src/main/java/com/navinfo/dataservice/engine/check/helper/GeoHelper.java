package com.navinfo.dataservice.engine.check.helper;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GeoHelper {

	public GeoHelper() {
		// TODO Auto-generated constructor stub
	}
	
	//功能：获取线段geo的中点坐标
	//返回值格式：String pointWkt = "Point ("+x+" "+y+")";
	public static String getMidPointByLine(Geometry geo){
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
		
		String pointWkt = "Point ("+x+" "+y+")";
		return pointWkt;		
		
	}

}
