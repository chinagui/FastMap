package com.navinfo.dataservice.engine.edit.wangdongbin.poi.batch;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.batch.BatchService;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONObject;

public class batchtest extends InitApplication {
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void test() {
		try {
			long jobId=BatchService.getInstance().batchRun(7,2,1,"BATCH_POI_GUIDELINK");	
			System.out.println(jobId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void batchApi() {
		try {
			String parameter = "{\"dbId\":17,\"data\":{\"pid\":2207065,\"kindCode\":\"110200\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[116.31167,40.02831]},\"xGuide\":116.31191,\"yGuide\":40.0283,\"linkPid\":50047736,\"chain\":\"3051\",\"open24h\":2,\"meshId\":605602,\"postCode\":\"\",\"fieldState\":\"\",\"oldName\":\"必胜客上地餐厅111\",\"oldAddress\":\"农大南路１号楼华联商厦１／２层\",\"oldKind\":\"110200\",\"poiNum\":\"0010081205XL600092\",\"dataVersion\":\"260+\",\"collectTime\":\"20161028113430\",\"level\":\"B1\",\"log\":\"改名称|改RELATION\",\"sportsVenue\":\"\",\"indoor\":0,\"vipFlag\":\"\",\"rowId\":\"3AE1FB52E1A292F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\",\"truckFlag\":0,\"poiMemo\":\"\",\"names\":[{\"objStatus\":\"UPDATE\",\"pid\":201000126,\"poiPid\":2207065,\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"必胜客上地餐厅111\",\"rowId\":\"E57DBABD42FF42CF8642E582C9E201E3\"}],\"contacts\":[],\"gasstations\":[],\"parkings\":[],\"hotels\":[]},\"objId\":2207065,\"command\":\"UPDATE\",\"type\":\"IXPOIUPLOAD\"}";
			JSONObject data = JSONObject.fromObject(parameter);
			EditApiImpl edit = new EditApiImpl();
			edit.runBatch(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
