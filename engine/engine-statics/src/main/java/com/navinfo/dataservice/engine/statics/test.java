package com.navinfo.dataservice.engine.statics;

import java.util.Iterator;

import org.bson.Document;

public class test {

	public static void test1() {
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
