package com.navinfo.navicommons.geo.computation;

import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/** 
* @ClassName: MyGeometryConvertor 
* @author Xiao Xiaowen 
* @date 2016年5月3日 下午6:17:26 
* @Description: TODO
*/
public class JtsGeometryConvertor {
	
	public static Coordinate convert(DoublePoint point){
		if(point!=null){
			return new Coordinate(point.getX(),point.getY());
		}
		return null;
	}
	public static Coordinate convertWithSpecDecimal(DoublePoint point){
		if(point!=null){
			return new Coordinate(DoubleUtil.keepSpecDecimal(point.getX()),DoubleUtil.keepSpecDecimal(point.getY()));
		}
		return null;
	}
	public static DoublePoint convert(Coordinate c){
		if(c!=null){
			return new DoublePoint(c.x,c.y);
		}
		return null;
	}
	public static DoublePolyline convert(LineString ls){
		if(ls!=null){
			Coordinate[] coArr = ls.getCoordinates();
			if(coArr!=null&&coArr.length>0){
				int len = coArr.length;
				DoublePoint[] points = new DoublePoint[len];
				for(int i=0;i<len;i++){
					points[i]=convert(coArr[i]);
				}
				return new DoublePolyline(points);
			}
		}
		return null;
	}
	public static LineString convert(DoublePolyline polyline){
		if(polyline!=null&&polyline.getLineSize()>0){
			Coordinate[] coArr = new Coordinate[polyline.getLineSize()+1];
			coArr[0]=convert(polyline.getSpoint());
			for(int i=0;i<polyline.getLineSize();i++){
				coArr[i+1]=convert(polyline.getLines()[i].getEpoint());
			}
			return JtsGeometryFactory.createLineString(coArr);
		}
		return null;
	}

	public static LineString convertWithSpecDecimal(DoublePolyline polyline){
		if(polyline!=null&&polyline.getLineSize()>0){
			Coordinate[] coArr = new Coordinate[polyline.getLineSize()+1];
			coArr[0]=convertWithSpecDecimal(polyline.getSpoint());
			for(int i=0;i<polyline.getLineSize();i++){
				coArr[i+1]=convertWithSpecDecimal(polyline.getLines()[i].getEpoint());
			}
			return JtsGeometryFactory.createLineString(coArr);
		}
		return null;
	}
	/**
	 * 
	 * @param rect:[minx,miny,maxx,maxy]
	 * @return jts polygon
	 */
	public static Polygon convert(double[] rect) {
		Coordinate[] coArr = new Coordinate[5];
		Coordinate startend = new Coordinate(rect[0],rect[1]);
		coArr[0]=startend;
		coArr[1]=new Coordinate(rect[2],rect[1]);
		coArr[2]=new Coordinate(rect[2],rect[3]);
		coArr[3]=new Coordinate(rect[0],rect[3]);
		coArr[4]=startend;
		LinearRing shell = JtsGeometryFactory.createLinearRing(coArr);
		return JtsGeometryFactory.createPolygon(shell, null);
	}
}
