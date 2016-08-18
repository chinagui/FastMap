package com.navinfo.dataservice.engine.statics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.bson.Document;

import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class test {

	public static void test1() {
		List list = new ArrayList<String>();
		list.add("585674");
		list.add("595657");
		list.add(String.valueOf("595577"));

		List allGrids = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {

			Set<String> grids = CompGridUtil.mesh2Grid(list.get(i).toString());
			allGrids.addAll(grids);
		}

		System.out.println(allGrids);

		for (int i = 0; i < allGrids.size(); i++) {
			String gridId = allGrids.get(i).toString();
			double[] loc = CompGridUtil.grid2Rect(gridId);
			Polygon gridPolygon = JtsGeometryConvertor.convert(loc);
		}

	}

	public static void test2() {
		Document doc = new Document();
		doc.append("grid_id", 111);

		Document poi = new Document();
		poi.append("total", 5);
		poi.append("finish", 0);
		poi.append("percent", 0);
		doc.append("poi", poi);

		System.out.println(doc.toJson());
		Document d = (Document) doc.get("poi");
		d.put("total", d.getInteger("total") + 2);

		System.out.println("--------------------");
		for (Iterator iter = doc.keySet().iterator(); iter.hasNext();) {

			System.out.println(iter.next());
		}
	}

	public static void json() {
		/*
		 * //JsonObject和JsonArray区别就是JsonObject是对象形式，JsonArray是数组形式
		 * //创建JsonObject第一种方法 JSONObject jsonObject = new JSONObject();
		 * jsonObject.put("UserName", "ZHULI"); jsonObject.put("age", "30");
		 * jsonObject.put("workIn", "ALI"); System.out.println("jsonObject1：" +
		 * jsonObject);
		 * 
		 * //创建JsonObject第二种方法 HashMap<String, String> hashMap = new
		 * HashMap<String, String>(); hashMap.put("UserName", "ZHULI");
		 * hashMap.put("age", "30"); hashMap.put("workIn", "ALI");
		 * System.out.println("jsonObject2：" + JSONObject.fromObject(hashMap));
		 * 
		 * //创建一个JsonArray方法1 JSONArray jsonArray = new JSONArray();
		 * jsonArray.add(0, "ZHULI"); jsonArray.add(1, "30"); jsonArray.add(2,
		 * "ALI"); System.out.println("jsonArray1：" + jsonArray);
		 * 
		 * //创建JsonArray方法2 ArrayList<String> arrayList = new
		 * ArrayList<String>(); arrayList.add("ZHULI"); arrayList.add("30");
		 * arrayList.add("ALI"); System.out.println("jsonArray2：" +
		 * JSONArray.fromObject(arrayList));
		 * 
		 * //如果JSONArray解析一个HashMap，则会将整个对象的放进一个数组的值中
		 * System.out.println("jsonArray FROM HASHMAP：" +
		 * JSONArray.fromObject(hashMap));
		 * 
		 * //组装一个复杂的JSONArray JSONObject jsonObject2 = new JSONObject();
		 * jsonObject2.put("UserName", "ZHULI"); jsonObject2.put("age", "30");
		 * jsonObject2.put("workIn", "ALI"); jsonObject2.element("Array",
		 * arrayList); System.out.println("jsonObject2：" + jsonObject2);
		 */
		HashMap<String, JSONObject> hashMap = new HashMap<String, JSONObject>();
		JSONObject obj1 = new JSONObject();
		obj1.put("UserName", "ZHULI");
		JSONObject obj2 = new JSONObject();
		obj2.put("age", "30");
		hashMap.put("110", obj1);
		hashMap.put("111", obj2);
		System.out.println("jsonObject2：" + JSONObject.fromObject(hashMap));
	}

	public static void jtsPoint() throws Exception {
		// instance from Coordinate
		Coordinate[] cdlist = new Coordinate[] { new Coordinate(0, 0), new Coordinate(2, 2), new Coordinate(3, 3) };
		LineString p1 = new GeometryFactory().createLineString(cdlist);
		System.out.println(p1);
		System.out.println(p1.getCoordinateSequence());
		// instance from CoordinateArraySequence
		// CoordinateSequence cslist = new CoordinateArraySequence();
		// Geometry p1 = new GeometryFactory().createLineString(cslist);
		// System.out.println(p1);

	}

	public static void jtsPolygon() throws Exception {
		// touches 是 intersects 的子集 
		WKTReader wktr=new WKTReader();
		
		Geometry  p1= wktr.read("POLYGON((0 0,0 1,1 1,1 0,0 0))");
		Geometry  p2= wktr.read("POLYGON((1 0,1 1,2 1,2 0,1 0))");
		Geometry  p3= wktr.read("POLYGON((0.5 0,0.5 1,2 1,2 0,0.5 0))");
	
		
		System.out.println(p1.intersects(p2));
		System.out.println(p1.touches(p2));
		
		System.out.println(p1.intersects(p3));
		System.out.println(p1.touches(p3));
		
	}

	public static void jts1() throws Exception {
		// read a geometry from a WKT string (using the default geometry
		// factory)
		Geometry ls1 = new WKTReader().read("LINESTRING (0 0, 10 10, 20 20)");
		System.out.println("Geometry 1: " + ls1);

		// create a geometry by specifying the coordinates directly
		Coordinate[] cd = new Coordinate[] { new Coordinate(0, 0), new Coordinate(10, 10), new Coordinate(20, 20) };
		// use the default factory, which gives full double-precision
		Geometry ls2 = new GeometryFactory().createLineString(cd);
		System.out.println("Geometry 2: " + ls2);

		// compute the intersection of the two geometries
		Geometry g3 = ls1.intersection(ls2);
		System.out.println("G1 intersection G2: " + g3);
	}

	public static void main(String[] args) throws Exception {
		jtsPolygon();
	}
}
