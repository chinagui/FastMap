package com.navinfo.dataservice.engine.edit;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
public class JsonObjectTest {
	private static final GeometryFactory geoFactory = new GeometryFactory();
	public static void  testPoint() throws JSONException{
		double lng = 115.98240 ;
		double lat =39.93789;
		AdNode adNode = new AdNode();
		Coordinate coord = new Coordinate(lng, lat);
		Point point = geoFactory.createPoint(coord);
		adNode.setGeometry(point);
		System.out.println(point);
	}
	public static void  testLine() throws JSONException{
		String  str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		System.out.println(geometry2.getLength());
		System.out.println(GeometryUtils.getLinkLength(geometry2));;
		System.out.println(geometry2.getCoordinates());
		LineString lineString =geoFactory.createLineString(geometry2.getCoordinates());
		System.out.println(GeometryUtils.getLinkLength(lineString)+"----------");;
		System.out.println(lineString.getEndPoint());
		System.out.println(lineString.getLength());
		Coordinate[]  coordinates = geometry2.getCoordinates();
		LinearRing ring = geoFactory.createLinearRing(geometry2.getCoordinates() );
		
		Polygon polygon = geoFactory.createPolygon( ring, null ); 
		double a =GeometryUtils.getCalculateArea(geometry2);
		//Geo
		//System.out.println(polygon.getLength());
		//System.out.println(polygon.getArea());
		System.out.println(polygon);
		
		
		
		
		
	}
	//5775044.296626
	//[4] LINESTRING (116.17659 39.97508, 116.16144 39.94844, 116.20427 39.94322, 116.17659 39.97508)
	//////LINESTRING (116.17659 39.97508, 116.16144 39.94844, 116.20427 39.94322, 116.17659 39.97508)
	public static void main(String[] args) throws JSONException {
		//testPoint();
		//testLine();
		//double c= 6356752.3142+6378137;
		//System.out.println(c/2);
		
		double [] aa = {1,2,3,4};
		System.out.println(aa[0]);
		for(int i = 0 ;i <aa.length; i++){
			aa[i] =9;
		}
		
		System.out.println(aa[0]);
	}
}
