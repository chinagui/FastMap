package com.navinfo.dataservice.engine.statics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.bson.Document;

import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.vividsolutions.jts.geom.Polygon;

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

	public static void main(String[] args) throws Exception {
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
		JSONObject obj1= new JSONObject();
		obj1.put("UserName", "ZHULI");
		JSONObject obj2= new JSONObject();
		obj2.put("age", "30");
		hashMap.put("110", obj1);
		hashMap.put("111", obj2);
		System.out.println("jsonObject2：" + JSONObject.fromObject(hashMap));
	}
}
