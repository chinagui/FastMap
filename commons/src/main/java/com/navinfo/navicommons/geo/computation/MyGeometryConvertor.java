package com.navinfo.navicommons.geo.computation;

import com.navinfo.dataservice.commons.util.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/** 
* @ClassName: MyGeometryConvertor 
* @author Xiao Xiaowen 
* @date 2016年5月3日 下午6:17:26 
* @Description: TODO
*/
public class MyGeometryConvertor {
	
	public static Coordinate convert(DoublePoint point){
		if(point!=null){
			return new Coordinate(point.getX(),point.getY());
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
				coArr[i+1]=convert(polyline.getEpoint());
			}
			return JtsGeometryUtil.createLineString(coArr);
		}
		return null;
	}
}
