package com.navinfo.dataservice.engine.edit;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
	
	public static void  point() throws Exception{
		Connection conn = DBConnector.getInstance().getConnectionById(11);
		AdLink adLink =(AdLink)new AdLinkSelector(conn).loadById(100031444,true);
		JSONObject geojson = GeoTranslator.jts2Geojson(adLink
				.getGeometry());
		System.out.println(adLink.getGeometry()+"----------------");
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		System.out.println(jaLink);
		double aa = 11647260;
		double bb = 4001457;
		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);
			System.out.println(jaPS.getDouble(0)+"------------------");
			if(jaPS.getDouble(0) ==aa){
				System.out.println("kkkk");
			}if(jaPS.getDouble(1) ==bb){
				System.out.println("kkkkkkk");
			}
			System.out.println(jaPS.getDouble(1)+"------------------");
		}
		Double lon1 =116.4744126222222;
		Double lat1 =40.01449311733887;
		double lng = Math.round(lon1*100000)/100000.0;
		double lat = Math.round(lat1*100000)/100000.0;
		System.out.println(lng);
		System.out.println(lat);
		Coordinate coord = new Coordinate(lng, lat);
		Point  point =geoFactory.createPoint(coord);
		long lon2 = (long) (point.getX() * 100000);
		long lat2 = (long) (point.getY() * 100000);
		System.out.println(lon2);
		System.out.println(lat2);
	}
	public static void main(String[] args) throws Exception {
		//testPoint();
		//testLine();
		//double c= 6356752.3142+6378137;
		//System.out.println(c/2);
		//lineToMesh();
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
		String str4 = "{ \"type\": \"LineString\",\"coordinates\": [ [116.24505,40.00000], [116.24217,40.00000]]}";
		JSONObject geometry = JSONObject.fromObject(str2);
		JSONObject geometry11 = JSONObject.fromObject(str3);
		JSONObject geometry111 = JSONObject.fromObject(str4);
		Geometry geometry4=GeoTranslator.geojson2Jts(geometry111, 1, 5);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		//POINT (116.25000 40.00000)
		
		System.out.println(CompGeometryUtil.geo2MeshesWithoutBreak(geometry4)+"---------------------");
	}
	// LINESTRING (116.24505 40.00000, 116.24217 40.00000)
	private static Set<String> getLinkInterMesh(Geometry linkGeom) throws Exception {
		Set<String> set = new HashSet<String>();

		Coordinate[] cs = linkGeom.getCoordinates();

		for (Coordinate c : cs) {
			set.addAll(Arrays.asList(MeshUtils.point2Meshes(c.x, c.y)));
		}

		return set;
	}
}
