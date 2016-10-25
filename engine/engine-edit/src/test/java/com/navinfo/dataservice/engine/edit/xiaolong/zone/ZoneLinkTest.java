package com.navinfo.dataservice.engine.edit.xiaolong.zone;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class ZoneLinkTest extends InitApplication{

	@Override
	public void init() {
		initContext();
	}
	
	@Test
	public void testMoveZoneNode()
	{
		String parameter = "{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100000009,\"data\":{\"longitude\":116.47951841354369,\"latitude\":40.013465982043655},\"type\":\"ZONENODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRepairZoneLink()
	{
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100033326,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47652,40.01587],[116.47678121161726,40.01572430169657],[116.47702,40.01559],[116.47653,40.01547]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"ZONELINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateZoneLink()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"ZONELINK\",\"objId\":100033326,\"data\":{\"kinds\":[{\"kind\":0,\"rowId\":\"018B4698EE3F4F6D82A70F716A491422\",\"objStatus\":\"UPDATE\",\"form\":0}],\"rowId\":\"5000FF45344D4D099DAE2D92251CEC17\",\"pid\":100033326}}";
		parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"ZONELINK\",\"objId\":210000032,\"data\":{\"kinds\":[{\"kind\":0,\"rowId\":\"03D8E2CEE3CB4166855EC4DD251CAE4D\",\"objStatus\":\"UPDATE\"}],\"rowId\":\"0785939E5E51434981F725ACA298D03C\",\"pid\":210000032}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
