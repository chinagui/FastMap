package com.navinfo.dataservice.engine.edit.wangdongbin.poi.download;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.obj.poi.download.DownloadOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class androidtest extends InitApplication {
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void test() {
		DownloadOperation download = new DownloadOperation();
		JSONArray gridList = new JSONArray();
		JSONObject grid = new JSONObject();
		grid.put("grid", "59567233");
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
