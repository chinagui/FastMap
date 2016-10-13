package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdNodeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testCreate()
	{
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":320000330,\"data\":{\"longitude\":116.30456328392027,\"latitude\":40.06810453436403},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMove() {
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":205000369,\"data\":{\"longitude\":116.34780585765839,\"latitude\":40.07702572592783},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDTRAFFICSIGNAL\",\"dbId\":19,\"objId\":201000009}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
