package com.navinfo.dataservice.engine.edit.wangdongbin.poi.upload;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;

import net.sf.json.JSONObject;

public class androidtest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void test() {
		String test = "{\"dbId\":17,\"data\":{\"pid\":302000032,\"kindCode\":\"110101\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[116.47586,40.01138]},\"xGuide\":116.47586,\"yGuide\":40.01144,\"linkPid\":730634,\"chain\":\"\",\"open24h\":0,\"meshId\":605603,\"postCode\":\"\",\"fieldState\":\"改连锁品牌\",\"oldName\":\"餐饮\",\"oldAddress\":\"\",\"oldKind\":\"110101\",\"poiNum\":\"00166420161014155714\",\"dataVersion\":\"260+\",\"collectTime\":\"20161014160240\",\"level\":\"\",\"log\":\"改地址|改POI_LEVEL|改RELATION\",\"sportsVenue\":\"\",\"indoor\":0,\"vipFlag\":\"\",\"rowId\":\"19B8257AF0C04211BB73561600F96421\",\"objStatus\":\"UPDATE\",\"truckFlag\":0,\"poiMemo\":\"\",\"contacts\":[],\"gasstations\":[],\"parkings\":[],\"hotels\":[],\"restaurants\":[]},\"objId\":302000032,\"command\":\"UPDATE\",\"type\":\"IXPOIUPLOAD\"}";
		JSONObject json = JSONObject.fromObject(test);
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command update = new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(json,null);
		try {
			com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process process = new  com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(update);
			process.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
