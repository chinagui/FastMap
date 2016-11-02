package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdCrossTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddCross() {
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":22,\"objId\":403000020,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.25,40.09628],[116.2501,40.09628],[116.24987840652466,40.09616553200543],[116.25057,40.09628],[116.25047,40.0966],[116.25017,40.0967]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"LCLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
