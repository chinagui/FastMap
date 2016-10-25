package com.navinfo.dataservice.control.app;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.download.DownloadOperation;
import com.navinfo.dataservice.control.app.search.Operation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class androidtest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test() {
		DownloadOperation download = new DownloadOperation();
		JSONArray gridList = new JSONArray();
		JSONObject grid = new JSONObject();
		grid.put("grid", "60560303");
		grid.put("date", "");
		gridList.add(grid);
		try {
			download.export(gridList, "f://poidownload", "poi.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
//		JSONObject test = new JSONObject();
//		test.put("test", JSONNull.getInstance());
//		test.put("test1", "");
//		test.put("123", new ArrayList<JSONObject>());
//		System.out.println(test);
	}
	

}
