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
		List list=new ArrayList<String>();
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
			double[] loc =CompGridUtil.grid2Rect(gridId);
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
		JSONObject jsonObject = new JSONObject();
        jsonObject.put("UserName", "ZHULI");
        jsonObject.put("age", "30");
        jsonObject.put("workIn", "ALI");
        System.out.println("jsonObject1：" + jsonObject);
        
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("UserName", "ZHULI");
        hashMap.put("age", "30");
        hashMap.put("workIn", "ALI");
        System.out.println("jsonObject2：" + JSONObject.fromObject(hashMap));
        */
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("ZHULI");
        jsonArray.add("30");
        jsonArray.add("ALI");


        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("UserName", "ZHULI");
        //jsonObject2.put("Array", jsonArray);
        jsonObject2.element("xxx", new JSONArray().element(1).element(34));
        System.out.println("xxxx  ：" + jsonObject2.getJSONArray("xxx"));
        
        System.out.println("jsonObject2：" + jsonObject2);
	}
}
