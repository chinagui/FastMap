package com.navinfo.dataservice.commons.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/** 
 * 添加一个jts Geometry 的工具类
 * 不用再每次create Geometry时都新建GeometryFactory
* @ClassName: JtsGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年5月3日 下午6:11:46 
* @Description: TODO
*/
public class JtsGeometryUtil {
	private static final GeometryFactory geometryFactory = new GeometryFactory();
	private static final WKTReader reader = new WKTReader( geometryFactory );
	public static LineString createLineString(Coordinate[] coordinates){
		return geometryFactory.createLineString(coordinates);
	}
	public static Point createPoint(Coordinate coordinate){
		return geometryFactory.createPoint(coordinate);
	}
	public static Geometry read(String wkt) throws ParseException{
		return reader.read(wkt);
	}
}
