package com.navinfo.dataservice.engine.fcc;

import org.junit.Test;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.engine.fcc.service.FccApiImpl;

/**
 * @ClassName: FccApiTest.java
 * @author y
 * @date 2017-3-2 上午10:57:38
 * @Description: TODO
 * 
 */
public class FccApiTest {

	@Test
	public  void testSearchDataBySpatial(){
		
		FccApi  apiFcc=new FccApiImpl();
		try {
			System.out.println(apiFcc.searchDataBySpatial("POLYGON ((116.375 39.91667, 116.375 39.9375, 116.375 39.95833, 116.375 39.97917, 116.375 40.0, 116.40625 40.0, 116.4375 40.0, 116.46875 40.0, 116.5 40.0, 116.5 39.97917, 116.5 39.95833, 116.5 39.9375, 116.5 39.91667, 116.46875 39.91667, 116.4375 39.91667, 116.40625 39.91667, 116.375 39.91667))", 1901, new JSONArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
