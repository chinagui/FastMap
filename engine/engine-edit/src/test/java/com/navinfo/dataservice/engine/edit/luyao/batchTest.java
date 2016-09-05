package com.navinfo.dataservice.engine.edit.luyao;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class batchTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void run0() {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"ZONEFACE\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.3835972547531,40.04125481236323],[116.3838279247284,40.04144783633151],[116.38397276401521,40.041246598565216],[116.38397812843323,40.04075787580295],[116.38350069522858,40.04086465586522],[116.3835972547531,40.04125481236323]]}}}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void run1() {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.3835972547531,40.04125481236323],[116.38397276401521,40.041246598565216]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void run2() {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.38424098491667,40.0413841795513],[116.38397276401521,40.041246598565216]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void run3() {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.38350069522858, 40.04086465586522],[116.38367101550104,40.04096116846992]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
