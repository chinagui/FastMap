package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdRestrictionTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddRestriction() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"restricInfo\":\"2\",\"pid\":6937515,\"objStatus\":\"UPDATE\",\"details\":[{\"pid\":7112407,\"restricPid\":6937515,\"outLinkPid\":582745,\"flag\":1,\"restricInfo\":3,\"type\":2,\"relationshipType\":1,\"conditions\":[],\"geoLiveType\":\"RDRESTRICTIONDETAIL\",\"objStatus\":\"DELETE\"},{\"type\":\"1\",\"pid\":7112406,\"objStatus\":\"UPDATE\",\"conditions\":[{\"pid\":0,\"rowId\":\"0\",\"vehicle\":0,\"resTrailer\":0,\"resWeigh\":0,\"resAxleLoad\":0,\"resAxleCount\":0,\"resOut\":0,\"objStatus\":\"INSERT\"}]}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteRestriction() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDRESTRICTION\",\"dbId\":42,\"objId\": 100000480}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
