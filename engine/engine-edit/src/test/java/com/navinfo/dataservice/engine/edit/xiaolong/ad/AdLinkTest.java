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
	
	@Test
	public void testAddAdLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46861791610718,40.00985856041071],[116.46900415420531,40.008929967723475]]},\"catchLinks\":[]},\"type\":\"ADLINK\"}";
		
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
