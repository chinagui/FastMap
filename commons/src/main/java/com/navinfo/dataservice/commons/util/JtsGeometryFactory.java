package com.navinfo.dataservice.commons.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/** 
 * 添加一个jts Geometry 的工具类
 * 不用再每次create Geometry时都新建GeometryFactory
* @ClassName: JtsGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年5月3日 下午6:11:46 
* @Description: TODO
*/
public class JtsGeometryFactory {
	private static final GeometryFactory factory = new GeometryFactory();
	public static MultiPolygon createMultiPolygon(Polygon[] polygons){
		return factory.createMultiPolygon(polygons);
	}
	public static MultiLineString createMultiLineString(LineString[] lineStrings){
		return factory.createMultiLineString(lineStrings);
	}
	public static MultiPoint createMultiPoint(Coordinate[] coordinates){
		return factory.createMultiPoint(coordinates);
	}
	public static Polygon createPolygon(LinearRing shell,
            LinearRing[] holes){
		return factory.createPolygon(shell,holes);
	}
	public static LinearRing createLinearRing(Coordinate[] coordinates){
		return factory.createLinearRing(coordinates);
	}
	public LinearRing createLinearRing(CoordinateSequence coordinates){
		return factory.createLinearRing(coordinates);
	}
	public static LineString createLineString(Coordinate[] coordinates){
	
		
		return factory.createLineString(coordinates);
	}
	public static LineString createLineString(CoordinateSequence coordinates){
		return factory.createLineString(coordinates);
	}
	public static Point createPoint(Coordinate coordinate){
		return factory.createPoint(coordinate);
	}
	public static Geometry read(String wkt) throws ParseException{
		WKTReader reader = new WKTReader( factory );
		return reader.read(wkt);
	}
    public static Polygon createPolygon(Coordinate[] coordinates){
    	return factory.createPolygon(coordinates);
    }
    public static String writeWKT(Geometry geo){
    	if(geo==null)return null;
    	WKTWriter wrt = new WKTWriter();
    	return wrt.write(geo);
    }
    
}
