package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

/**
 * @author zhaokk
 *
 */
public class AdLinkTest extends InitApplication {
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public  void testBreakAdLink() {
		String parameter = "{\"command\":\"BREAK\",\"projectId\":11,\"objId\":100031763,\"data\":{\"longitude\":116.4725149146884,\"latitude\":40.012285569566565},\"type\":\"ADLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
