package com.navinfo.dataservice.engine.edit.wangdongbin.poi.download;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.dao.glm.search.PoiGridSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class androidtest extends InitApplication {
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void test() {
		PoiGridSearch gridSearch = new PoiGridSearch();
		JSONArray gridList = new JSONArray();
		JSONObject grid = new JSONObject();
		grid.put("grid", "60560213");
		grid.put("date", "");
		gridList.add(grid);
		try {
			gridSearch.getPoiByGrids(gridList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
