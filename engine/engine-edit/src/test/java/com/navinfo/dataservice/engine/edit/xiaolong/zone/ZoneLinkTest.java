package com.navinfo.dataservice.engine.edit.xiaolong.zone;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

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

}
