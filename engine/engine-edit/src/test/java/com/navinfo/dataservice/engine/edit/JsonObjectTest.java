package com.navinfo.dataservice.engine.edit;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.joni.exception.JOniException;
import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
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
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		System.out.println(geometry2.getLength());
		System.out.println(GeometryUtils.getLinkLength(geometry2));;
		System.out.println(geometry2.getCoordinates());
		LineString lineString =geoFactory.createLineString(geometry2.getCoordinates());
		System.out.println(GeometryUtils.getLinkLength(lineString)+"----------");;
		System.out.println(lineString.getEndPoint());
		System.out.println(lineString.getLength());
		Coordinate[] coordinates = geometry2.getCoordinates();
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
	public static void main(String[] args) throws Exception {
		//testPoint();
		//testLine();
		//double c= 6356752.3142+6378137;
		//System.out.println(c/2);
		lineToMesh();
	}
	//[2] LINESTRING (116.32947 39.83333, 116.32563 39.82893)
	//[2] LINESTRING (116.33975 39.84509, 116.32947 39.83333)
	private static void lineToMesh() throws Exception{
		
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.32563,39.82893], [116.33975,39.84509]]}";
		
		String str1= "{ \"type\": \"LineString\",\"coordinates\": [ [116.29493,39.78217], [116.28745,39.77083]]}";
		//[2] LINESTRING (116.22284 39.75436, 116.22403 39.75000)
		//[3] LINESTRING (116.22403 39.75000, 116.22610 39.74242, 116.23166 39.75000)
		//[2] LINESTRING (116.23166 39.75000, 116.23442 39.75376)
		String str2 ="{ \"type\": \"LineString\",\"coordinates\": [ [116.22284,39.75436], [ 116.22610,39.74242],[ 116.23442,39.75376]]}";
		String str3 ="{ \"type\": \"LineString\",\"coordinates\": [ [116.22285,39.75436], [ 116.22610,39.74242],[ 116.23442,39.75376]]}";
		//POLYGON ((115.85776 40.00000, 115.86218 40.00000, 115.85937 40.00290, 115.85776 40.00000))
		// POLYGON ((115.86218 40.00000, 115.85776 40.00000, 115.85664 39.99797, 115.86471 39.99739, 115.86218 40.00000))
		//POINT (115.85937 40.00290)   115.85664 39.99797, 115.86471 39.99739
		String str4 = "{ \"type\": \"LineString\",\"coordinates\": [ [115.85937,40.00290], [115.85664,39.99797],[115.86471,39.99739],[115.85937,40.00290]]}";
		JSONObject geometry = JSONObject.fromObject(str2);
		JSONObject geometry11 = JSONObject.fromObject(str3);
		JSONObject geometry111 = JSONObject.fromObject(str4);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		
		Geometry geometry3=GeoTranslator.geojson2Jts(geometry11, 1, 5);
		Geometry geometry4=GeoTranslator.geojson2Jts(geometry111, 1, 5);
        System.out.println(geometry2.getCoordinates());
        for (Coordinate coordinate : geometry2.getCoordinates()){
        	System.out.println(coordinate);
        }
		Geometry geomInter = MeshUtils.linkInterMeshPolygon(geometry4,
				MeshUtils.mesh2Jts("605506"));
		System.out.println(geomInter+"--------------------------");
		geomInter.getGeometryType();
		System.out.println( geometry2.getGeometryType());
		
		 //for (Coordinate c :geomInter.getCoordinates()){
			 	//new GeometryFactory().createLineString( }
		System.out.println(geomInter.getDimension());
		System.out.println(geomInter.getCoordinates().length);
		System.out.println( geomInter.getGeometryN(0));
		System.out.println(geomInter.getBoundaryDimension());
		System.out.println(geomInter.getNumGeometries());
		System.out.println( geometry2.getCoordinates()[0]);
		
		Map<Coordinate ,Integer > maps = new HashMap<Coordinate , Integer  >();
		maps.put(geometry2.getCoordinates()[0], 1111);
		if(maps.containsKey(geometry3.getCoordinates()[0])){
			System.out.println("lllll");
		}
	}
}
