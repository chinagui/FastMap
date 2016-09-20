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
		String parameter = "{\"command\":\"CREATE\",\"dbId\":1003,\"objId\":719859,\"data\":{\"longitude\":116.40170216560364,\"latitude\":40.024541793542724},\"type\":\"RDNODE\"}";
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
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":572283,\"data\":{\"longitude\":116.37209326028824,\"latitude\":40.08147207016285},\"type\":\"RDNODE\"}";
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
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"RDNODE\",\"objId\":320000025}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
