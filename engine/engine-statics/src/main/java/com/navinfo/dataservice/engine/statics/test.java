package com.navinfo.dataservice.engine.statics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.navinfo.navicommons.geo.computation.MeshUtils;
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
			gridPolygon
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

		test1();
		//
		// ClassPathXmlApplicationContext context = new
		// ClassPathXmlApplicationContext(new String[] {
		// "dubbo-consumer-datahub-test.xml" });
		// context.start();
		// new ApplicationContextUtil().setApplicationContext(context);
		//
		// //-----------------------
		// List<Integer> list = new ArrayList<Integer>();
		// list.add(8);
		// list.add(9);
		// list.add(24);
		// for (int i : list) {
		// Connection conn = DBConnector.getInstance().getConnectionById(i);
		// System.out.println(conn + "----------------test1");
		//
		// }

	}
}
