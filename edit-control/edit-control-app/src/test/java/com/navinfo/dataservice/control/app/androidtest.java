package com.navinfo.dataservice.control.app;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.download.PoiDownloadOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class androidtest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
//	@Test
	public void test() {
		Date startTime = new Date();
		JSONArray gridDateList = new JSONArray();
		JSONObject grid = new JSONObject();
		grid.put("grid", "32407312");//60562422//60562203//60560233
		grid.put("date", "");
		gridDateList.add(grid);
		
		JSONObject grid1 = new JSONObject();
		grid1.put("grid", "47600512");//60562422//60562203//60560233
		grid1.put("date", "");
		gridDateList.add(grid1);

		
		/*
		JSONObject grid2 = new JSONObject();
		grid2.put("grid", "60562203");//60562422//60562203//60560233
		grid2.put("date", "");
		gridDateList.add(grid2);*/
		
		try {
			Map<String,String> gridDateMap = new HashMap<String,String>();
			
			for (int i=0;i<gridDateList.size();i++) {
				JSONObject gridDate = gridDateList.getJSONObject(i);
				gridDateMap.put(gridDate.getString("grid"), gridDate.getString("date"));
			}
			
			PoiDownloadOperation operation = new PoiDownloadOperation();
			//operation.export2Txt(gridDateMap, "f://poidownload", "poi222.txt");
			Date endTime = new Date();
			System.out.println("total time:"+ (endTime.getTime() - startTime.getTime()));
//			download.export(gridList, "f://poidownload", "poi.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
//		JSONObject test = new JSONObject();
//		test.put("test", JSONNull.getInstance());
//		test.put("test1", "");
//		test.put("123", new ArrayList<JSONObject>());
//		System.out.println(test);
	}
	
	@Test
	public void testbysubtask() {
		
		try {
			
			PoiDownloadOperation operation = new PoiDownloadOperation();
			operation.export2TxtBySubtaskId("f://poidownload", 26);
//			download.export(gridList, "f://poidownload", "poi.txt");
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
