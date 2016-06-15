package com.navinfo.dataservice.engine.edit.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.xiaolong.InitApplication;

public class RdCrossTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddCross() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDCROSS\",\"projectId\":11,\"data\":{\"nodePids\":[\"13644693\"],\"linkPids\":[]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
