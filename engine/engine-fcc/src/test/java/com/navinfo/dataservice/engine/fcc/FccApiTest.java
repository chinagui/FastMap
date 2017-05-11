package com.navinfo.dataservice.engine.fcc;

import org.junit.Before;
import org.junit.Test;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.fcc.service.FccApiImpl;

/**
 * @ClassName: FccApiTest.java
 * @author y
 * @date 2017-3-2 上午10:57:38
 * @Description: TODO
 * 
 */
public class FccApiTest extends InitApplication  {
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public  void testSearchDataBySpatial(){
		
		FccApi  apiFcc=new FccApiImpl();
		try {
			System.out.println(apiFcc.searchDataBySpatial("POLYGON ((116.375 39.91667, 116.375 39.9375, 116.375 39.95833, 116.375 39.97917, 116.375 40.0, 116.40625 40.0, 116.4375 40.0, 116.46875 40.0, 116.5 40.0, 116.5 39.97917, 116.5 39.95833, 116.5 39.9375, 116.5 39.91667, 116.46875 39.91667, 116.4375 39.91667, 116.40625 39.91667, 116.375 39.91667))", 10,1901, new JSONArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testFcc(){
	    try {
	    	FccApi  apiFcc=new FccApiImpl();
			String parameter = "{\"gdbid\":12,\"au_db_ip\":\"192.168.3.227\",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":\"orcl\",\"au_db_port\":1521,\"types\":\"\",\"phaseId\":27,\"collectTaskIds\":[21],\"taskid\":{\"manager_id\":21,\"imp_task_name\":\"北京市北京市城区天安门20170418\",\"province\":\"北京市\",\"city\":\"北京市\",\"district\":\"北京市北京市城区天安门\",\"job_nature\":\"更新\",\"job_type\":\"行人导航\"}}";
			JSONObject  obj=JSONObject.fromObject(parameter);
			apiFcc.tips2Aumark(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
